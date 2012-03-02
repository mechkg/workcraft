package org.workcraft.gui
import org.workcraft.logging.StandardStreamLogger
import org.workcraft.pluginmanager.PluginManager
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
import javax.swing.WindowConstants
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.SwingUtilities

object Main {
  var currentLogger: Logger[IO] = new StandardStreamLogger()

  implicit val logger = () => currentLogger

  val version = UUID.fromString("dd10f600-4769-11e1-b86c-0800200c9a66")
  val configDirPath = System.getProperty("user.home") + File.separator + ".workcraft"
  val pluginPackages = List("org.workcraft.plugins")

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

  private def shutdown(mainWindow: MainWindow)(implicit logger: () => Logger[IO]) = {
    currentLogger = new StandardStreamLogger
    unsafeInfo("Shutting down")

    GuiConfiguration.save(configFilePath("gui.conf"), mainWindow.guiConfiguration)

    mainWindow.setVisible(false)

    unsafeInfo("Have a nice day!")
    System.exit(0)
  }

  def loadPlugins(reconfigure: Boolean) = new PluginManager(version, pluginPackages, configFilePath("manifest"), reconfigure)

  def main(args: Array[String]) = {
    unsafeInfo("Welcome to Workcraft 2.2: Return of The Deadlock")
    unsafeInfo("This build's version ID is " + version.toString())

    checkConfig

    var pluginManager = loadPlugins(false)
    var globalServices = new GlobalServiceManager(pluginManager)

    def reconfigure() = {
      pluginManager = loadPlugins(true)
      globalServices = new GlobalServiceManager(pluginManager)
    }

    unsafeInfo("Starting GUI")
    val guiConfiguration = GuiConfiguration.load(configFilePath("gui.conf"))

    guiConfiguration.foreach(c => LafManager.setLaf(c.lookandfeel))

    // Apply LaF tweaks
    JDialog.setDefaultLookAndFeelDecorated(true)
    UIManager.put(SubstanceLookAndFeel.TABBED_PANE_CONTENT_BORDER_KIND, TabContentPaneBorderKind.SINGLE_FULL)

    SwingUtilities.invokeLater(new Runnable {
      def run = {

        val mainWindow = new MainWindow(() => globalServices, reconfigure, shutdown, guiConfiguration)

        mainWindow.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE)
        mainWindow.addWindowListener(new WindowAdapter() {
          override def windowClosing(e: WindowEvent) = shutdown(mainWindow)
        })

        currentLogger = mainWindow.loggerWindow

        unsafeInfo("Now in GUI mode. Welcome to Workcraft 2.2: Return of The Deadlock!")

        mainWindow.setVisible(true)
      }
    })
  }
}