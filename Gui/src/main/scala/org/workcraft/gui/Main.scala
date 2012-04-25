package org.workcraft.gui
import java.awt.Window
import org.workcraft.logging.StandardStreamLogger
import java.util.UUID
import org.workcraft.services.GlobalServiceManager
import org.workcraft.services.Module
import java.io.File
import org.streum.configrity.Configuration
import javax.swing.JDialog
import javax.swing.UIManager
import org.pushingpixels.substance.api.SubstanceLookAndFeel
import org.pushingpixels.substance.api.SubstanceConstants.TabContentPaneBorderKind
import org.workcraft.logging.Logger._
import org.workcraft.logging.Logger
import org.workcraft.scala.effects.IO
import org.workcraft.scala.effects.IO._
import javax.swing.WindowConstants
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.SwingUtilities

object Main {
  var currentLogger: Logger[IO] = new StandardStreamLogger()

  implicit val logger = () => currentLogger

  val configDirPath = System.getProperty("user.home") + File.separator + ".workcraft"

  def configFilePath(fileName: String) = configDirPath + File.separator + fileName

  def checkConfig = {
    unsafeInfo("Checking Workcraft configuration directory")

    val configDir = new File(configDirPath)

    if (!configDir.exists()) {
      unsafeInfo("Configuration directory does not exist, will create \"" + configDirPath + "\" now")
      configDir.mkdirs()
      configDir.mkdir()
    }

    unsafeInfo("Configuration directory OK")
  }

  def main(configDescription: String, globalServices: GlobalServiceManager, args: Array[String]) = {
    unsafeInfo("Welcome to Workcraft 2.2: Return of The Deadlock")
    unsafeInfo("This build is using " + configDescription)

    checkConfig

    unsafeInfo("Starting GUI")
    val guiConfiguration = GuiConfiguration.load(configFilePath("gui.conf"))

    guiConfiguration.foreach(c => LafManager.setLaf(c.lookandfeel))

    // Apply LaF tweaks
    JDialog.setDefaultLookAndFeelDecorated(true)
    UIManager.put(SubstanceLookAndFeel.TABBED_PANE_CONTENT_BORDER_KIND, TabContentPaneBorderKind.SINGLE_FULL)

    var mainWindow:MainWindow = null

    SwingUtilities.invokeAndWait(new Runnable {
      def run = {
        mainWindow = new MainWindow(globalServices, guiConfiguration)

        currentLogger = mainWindow.loggerWindow

        unsafeInfo("Now in GUI mode. Welcome to Workcraft 2.2: Return of The Deadlock!")

        mainWindow.setVisible(true)
      }})

    mainWindow.synchronized {
      mainWindow.wait()
    }

    currentLogger = new StandardStreamLogger
    
    unsafeInfo("Shutting down")

    GuiConfiguration.save(configFilePath("gui.conf"), mainWindow.guiConfiguration)
	
    unsafeInfo("Have a nice day!")

    System.exit(0)
  }
}
