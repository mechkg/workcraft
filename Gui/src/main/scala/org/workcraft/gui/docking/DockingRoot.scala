package org.workcraft.gui.docking
import javax.swing.JPanel
import org.flexdock.perspective.PerspectiveManager
import org.flexdock.docking.DockingManager
import org.flexdock.perspective.Perspective
import org.flexdock.docking.defaults.DefaultDockingPort
import java.awt.BorderLayout
import javax.swing.JComponent
import org.flexdock.docking.DockingConstants
import org.flexdock.docking.defaults.StandardBorderManager
import org.flexdock.plaf.common.border.ShadowBorder
import org.flexdock.docking.props.PropertyManager
import javax.swing.JTabbedPane
import javax.swing.SwingConstants
import org.workcraft.scala.Expressions._

class DockingRoot(val id: String) extends JPanel {
  val pm = DockingManager.getLayoutManager().asInstanceOf[PerspectiveManager]
  pm.add(new Perspective(id, id))
  pm.setCurrentPerspective(id, true)

  val rootDockingPort = new DefaultDockingPort("root")

  val props = PropertyManager.getDockingPortPropertySet(rootDockingPort)
  props.setTabPlacement(SwingConstants.TOP)

  rootDockingPort.setBorderManager(new StandardBorderManager(new ShadowBorder()))

  setLayout(new BorderLayout)
  add(rootDockingPort, BorderLayout.CENTER)

  def createWindowWithSetSplit[A <: JComponent](title: Expression[String], persistentId: String, content: A, configuration: DockableWindowConfiguration[A],
    neighbour: DockableWindow[_], relativeRegion: String = DockingConstants.CENTER_REGION, split: Double = 0.5) = {
    val window = new DockableWindow[A](title, persistentId, content, configuration)
    DockingManager.registerDockable(window)
    DockingManager.dock(window, neighbour, relativeRegion)
    DockingManager.setSplitProportion(window, split.toFloat)

    window
  }

  def createWindow[A <: JComponent](title: Expression[String], persistentId: String, content: A, configuration: DockableWindowConfiguration[A],
    neighbour: DockableWindow[_], relativeRegion: String = DockingConstants.CENTER_REGION) = {
    val window = new DockableWindow[A](title, persistentId, content, configuration)
    DockingManager.registerDockable(window)
    DockingManager.dock(window, neighbour, relativeRegion)

    window
  }

  def createRootWindow[A <: JComponent](title: Expression[String], persistentId: String, content: A, configuration: DockableWindowConfiguration[A]) = {
    val window = new DockableWindow[A](title, persistentId, content, configuration)
    DockingManager.registerDockable(window)
    DockingManager.dock(window, rootDockingPort, DockingConstants.CENTER_REGION)

    window
  }
}
