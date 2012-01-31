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
import scalaz.effects.IO
import scalaz.effects.IO._
import scalaz.Scalaz._

class MainWindow private (val globalServices: GlobalServiceManager) extends JFrame {
  SwingUtilities.updateComponentTreeUI(this)

  val content = new JPanel(new BorderLayout(0, 0))
  setContentPane(content)
}

object MainWindow {
  def startGui (implicit globalServices: GlobalServiceManager, logger: Logger[IO]) : IO[MainWindow] = {
    unsafeInfo("Loading GUI configuration")
    
    implicit val config = try {
      Configuration.loadResource("config/gui.conf") 
    } catch {
      case e: Throwable => { unsafeWarning("Failed to load config file \"config/gui.conf\""); warning(e).unsafePerformIO; Configuration() }
    }

    // LaF tweaks
    JDialog.setDefaultLookAndFeelDecorated(true)
    UIManager.put(SubstanceLookAndFeel.TABBED_PANE_CONTENT_BORDER_KIND, TabContentPaneBorderKind.SINGLE_FULL)
    
    val mainWindow = new MainWindow(globalServices)
    
    mainWindow.setTitle("Workcraft")
    mainWindow.setSize(800, 600)
    mainWindow.setVisible(true)
    
    MainWindowIconManager.apply(mainWindow)
    
    LafManager.setLaf(config[String]("gui.lookandfeel", UIManager.getCrossPlatformLookAndFeelClassName()))    
    
    mainWindow
  }.pure
}