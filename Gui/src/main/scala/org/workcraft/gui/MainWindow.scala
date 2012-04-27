package org.workcraft.gui

import java.awt.Font
import javax.swing.JFrame
import org.workcraft.services.GlobalServiceManager
import org.workcraft.logging.Logger
import org.workcraft.logging.Logger._
import org.workcraft.logging.StandardStreamLogger
import javax.swing.JDialog
import javax.swing.UIManager
import org.pushingpixels.substance.api.SubstanceLookAndFeel
import org.pushingpixels.substance.api.SubstanceConstants.TabContentPaneBorderKind
import org.streum.configrity.Configuration
import javax.swing.SwingUtilities
import javax.swing.JPanel
import java.awt.BorderLayout
import org.workcraft.scala.effects.IO
import org.workcraft.scala.effects.IO._
import scalaz.Scalaz._
import java.awt.Frame
import javax.swing.WindowConstants
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JButton
import javax.swing.JComponent
import org.workcraft.gui.docking.DockingRoot
import org.workcraft.gui.docking.DockableWindowConfiguration
import org.workcraft.gui.docking.DockableWindow
import org.workcraft.gui.logger.LoggerWindow
import org.workcraft.gui.modeleditor.ModelEditorPanel
import org.workcraft.services.NewModelImpl
import org.flexdock.docking.DockingManager
import org.flexdock.docking.DockingConstants
import org.workcraft.gui.modeleditor.EditorService
import javax.swing.JOptionPane
import org.workcraft.services.ModelServiceProvider
import org.workcraft.gui.modeleditor.EditorState
import org.workcraft.gui.modeleditor.tools.ToolboxPanel
import org.workcraft.gui.propertyeditor.PropertyEditorWindow
import org.workcraft.scala.Expressions._
import org.workcraft.dependencymanager.advanced.user.Variable
import org.workcraft.gui.modeleditor.PropertyService
import java.awt.event.FocusListener
import java.awt.event.FocusEvent
import java.io.File
import java.awt.Color

class MainWindow(
  val globalServices: GlobalServiceManager,
  configuration: Option[GuiConfiguration]) extends JFrame {
  val loggerWindow = new LoggerWindow
  implicit val implicitLogger: () => Logger[IO] = () => loggerWindow


  setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE)

  addWindowListener(new WindowAdapter() {
    override def windowClosing(e: WindowEvent) = exit.unsafePerformIO
  })


  applyIconManager

  applyGuiConfiguration(configuration)

  setTitle("Workcraft")

  val dockingRoot = new DockingRoot("workcraft")
  setContentPane(dockingRoot)

  val toolboxWindow = new JPanel(new BorderLayout)
  toolboxWindow.add(new NotAvailablePanel(), BorderLayout.CENTER)

  var editorInFocus: ModifiableExpression[Option[DockableWindow[ModelEditorPanel]]] = Variable.create[Option[DockableWindow[ModelEditorPanel]]](None)
  val interfacePanel = editorInFocus.expr >>= (_.traverse(_.content.toolbox.selectedToolInstance.map(_.interfacePanel)).map(_.join))

  val toolControlWindow = new ToolInterfaceWindow(interfacePanel)

  val propEdWindow = new PropertyEditorWindow

  val placeholderDockable = dockingRoot.createRootWindow(constant(""), "DocumentPlaceholder", new DocumentPlaceholder(new Color(255,255,255)), DockableWindowConfiguration(false, false, false))
  val loggerDockable = createUtilityWindow(constant("Log"), "Log", loggerWindow, placeholderDockable, DockingConstants.SOUTH_REGION, 0.8)
  val toolboxDockable = createUtilityWindow(constant("Toolbox"), "Toolbox", toolboxWindow, placeholderDockable, DockingConstants.EAST_REGION, 0.8)
  val toolControlDockable = createUtilityWindow(constant("Tool controls"), "ToolControls", toolControlWindow, toolboxDockable, DockingConstants.NORTH_REGION, 0.8)
  val propEdDockable = createUtilityWindow(constant("Properties"), "PropEd", propEdWindow, toolControlDockable, DockingConstants.CENTER_REGION, 0.8)

  val tabSwitcher = swingAutoRefresh(interfacePanel, (i: Option[JPanel]) => ioPure.pure {
    if (i.isDefined) toolControlDockable.ensureTabSelected
    else propEdDockable.ensureTabSelected
  })

  val fileMapping = new FileMappingManager

  var openEditors = List[DockableWindow[ModelEditorPanel]]()

  val menu = new MainMenu(this, List(loggerDockable, toolboxDockable, propEdDockable), globalServices, { case (m, b) => newModel(m, b) })
  this.setJMenuBar(menu)


  def createUtilityWindow(title: Expression[String], persistentId: String, content: JComponent, relativeTo: DockableWindow[_], relativeRegion: String, split: Double): DockableWindow[_ <: JComponent] =
    dockingRoot.createWindowWithSetSplit(title, persistentId, content, DockableWindowConfiguration(maximiseButton = false, onCloseClicked = closeUtilityWindow), relativeTo, relativeRegion, split)

  def closeUtilityWindow(window: DockableWindow[_ <: JComponent]) = {
    window.close
    menu.windowsMenu.update(window)
  }

  private def applyGuiConfiguration(configOption: Option[GuiConfiguration]) = SwingUtilities.invokeLater(new Runnable {
    def run = {
      configOption match {
        case Some(config) => {
          setSize(config.xSize, config.ySize)
          setLocation(config.xPos, config.yPos)

          if (config.maximised)
            setExtendedState(Frame.MAXIMIZED_BOTH)
        }
        case None =>
          {
            setSize(800, 600)
          }
          SwingUtilities.updateComponentTreeUI(MainWindow.this)
      }
    }
  })

  def guiConfiguration = {
    val size = getSize()
    val maximised = (getExtendedState() & Frame.MAXIMIZED_BOTH) != 0

    GuiConfiguration(xPos = getX(), yPos = getY(),
      xSize = size.getWidth().toInt, ySize = size.getHeight().toInt, maximised = maximised,
      lookandfeel = LafManager.getCurrentLaf)
  }

  private def applyIconManager(implicit logger: () => Logger[IO]) = MainWindowIconManager.apply(this, logger)

  def newModel(newModelImpl: NewModelImpl, editorRequested: Boolean) =
    newModelImpl.create >>= (model => (fileMapping.update(model, None) >>=| (if (editorRequested) openEditor(model, None) else IO.Empty)))
  
  def setFocus(editorDockable: Option[DockableWindow[ModelEditorPanel]]): IO[Unit] = ioPure.pure {
    toolboxWindow.removeAll()
    editorDockable match {
      case Some(editorWindow) => {
        toolboxWindow.add(new ToolboxPanel(editorWindow.content.toolbox), BorderLayout.CENTER)
        toolboxWindow.revalidate()

        editorWindow.content.editor.implementation(PropertyService) match {
          case Some(props) => propEdWindow.propertyObject.setValue(props.map(Some(_)))
          case None => propEdWindow.propertyObject.setValue(constant(None))
        }
        
        propEdWindow.revalidate()
      }
      case None => {
        toolboxWindow.add(new NotAvailablePanel)
        propEdWindow.propertyObject.setValue(constant(None))
      }
    }

    editorInFocus.set(editorDockable).unsafePerformIO
  }

  def openEditor(model: ModelServiceProvider, source: Option[File]): IO[Unit] = {
    model.implementation(EditorService) match {
      case None => ioPure.pure { JOptionPane.showMessageDialog(this, "The model type that you have chosen does not support visual editing :(", "Warning", JOptionPane.WARNING_MESSAGE) }
      case Some(editor) => fileMapping.update (model, source) >>=| ioPure.pure {
        val editorPanel = new ModelEditorPanel(model, editor)(implicitLogger)
        
        val editorDockable = dockingRoot.createWindow(fileMapping.lastSavedAs(model).map(_.map(_.getName).getOrElse("New model")), "unused", editorPanel, DockableWindowConfiguration(onCloseClicked = closeEditor), if (openEditors.isEmpty) placeholderDockable else (openEditors.head), DockingConstants.CENTER_REGION)
        
        editorPanel.addFocusListener(new FocusListener {
          override def focusGained(e: FocusEvent) = setFocus(Some(editorDockable)).unsafePerformIO
          override def focusLost(e: FocusEvent) = {}
        })
        

        if (openEditors.isEmpty) {
          openEditors = List(editorDockable)
          DockingManager.undock(placeholderDockable)
        } else
          openEditors ::= editorDockable

	(editorDockable, editorPanel)

      } >>= { case (dockable, panel) => setFocus(Some(dockable)) }// >>=| panel.fitView }
    }
  }

  def closeEditor(editorDockable: DockableWindow[ModelEditorPanel]) {
    // TODO: Ask to save etc.
    fileMapping.remove(editorDockable.content.model)

    openEditors = openEditors.filterNot (_ == editorDockable)    

    if (openEditors.isEmpty)
      DockingManager.dock(placeholderDockable, editorDockable, DockingConstants.CENTER_REGION)

    editorDockable.close
    DockingManager.undock(editorDockable)
  }

  def exit = ioPure.pure { 
    setVisible(false) 
    dispose()
    this.synchronized {
      notify()
    }
  }
}
