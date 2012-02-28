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
import scalaz.effects.IO

object Main {
  var currentLogger: Logger[IO] = new StandardStreamLogger()
  
  def switchLogger (logger: Logger[IO]) = currentLogger = logger
  
  implicit val logger = () => currentLogger
    
  val version = UUID.fromString("dd10f600-4769-11e1-b86c-0800200c9a66")
  val manifestPath = "config/manifest"
  val pluginPackages = List("org.workcraft.plugins")

  def checkConfig = {
    unsafeInfo("Checking configuration directory")

    val configDir = new File("config")

    if (!configDir.exists()) {
      unsafeInfo("Configuration directory does not exist, will create now")
      configDir.mkdirs()
      configDir.mkdir()
    }

    unsafeInfo("Configuration directory OK")
  }

  def loadPlugins(reconfigure: Boolean) = new PluginManager(version, pluginPackages, manifestPath, reconfigure) 
  
  var pluginManager = loadPlugins(false) 
  
  def serviceManager() = new GlobalServiceManager (pluginManager)
  
  def reconfigure() = pluginManager = loadPlugins(true)

  def main(args: Array[String]) = {
    unsafeInfo("Welcome to Workcraft 2.2: Return of The Deadlock")
    unsafeInfo("This build's version ID is " + version.toString())
    
    checkConfig
    
    unsafeInfo("Starting GUI")

    val mainWindow = MainWindow.startGui(serviceManager, reconfigure, switchLogger).unsafePerformIO
  }
}