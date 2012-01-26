package org.workcraft.gui

import org.workcraft.logging.StandardStreamLogger
//import org.workcraft.pluginmanager.PluginManager
import java.util.UUID
//import org.workcraft.services.GlobalServiceManager
//import org.workcraft.services.Module
import java.io.File
import scalaz.Scalaz._
import scalaz.effects.IO._

object Main {
  def main (args: Array[String]) = {
    val version = UUID.fromString("dd10f600-4769-11e1-b86c-0800200c9a66")
    
    val configDir = new File("config")
    
    if (!configDir.exists()) {
    	configDir.mkdirs()
    	configDir.mkdir()
    }
    
    
    val manifestPath = "config/manifest"
    
    val logger = new StandardStreamLogger()
    
    /*   val pluginManager = new PluginManager(version, List("org.workcraft.plugins"), manifestPath, logger)
    
    val serviceManager = new GlobalServiceManager(pluginManager)
    
    
    val mainWindow = new MainWindow(serviceManager)
    
    mainWindow.setVisible(true)    */ 
  }
}