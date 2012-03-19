package org.workcraft.gui
import org.workcraft.services.GlobalServiceManager
import org.workcraft.scala.Expressions._
import org.workcraft.gui.services.GuiToolService
import scalaz.Scalaz._
import org.workcraft.gui.services.GuiTool
import org.workcraft.scala.effects.IO
import javax.swing.JMenu
import org.workcraft.gui.GUI._

class ToolsMenu (services: () => GlobalServiceManager, mainWindow: MainWindow) extends ReactiveMenu("Tools") {
  // Must be lazy because Scala allows to read uninitialized values
  lazy val items = mainWindow.editorInFocus.map {
    case Some(editor) => {
      
      val makeMenu = (name: String, tools: List[(GuiTool, IO[Unit])]) => {
        val result = new JMenu (name)
        tools.sortBy(_._1.description).foreach { case (tool, action) => (result.add (menuItem(tool.description, None, None, action)))}
        result
      } 
      
      val applicableTools = services().implementations(GuiToolService).flatMap( tool => tool.run(mainWindow).map ((tool, _ )))
      
      applicableTools.groupBy(_._1.classification.sectionName).toList.sortBy(_._1).map (makeMenu.tupled(_))
    }
    
    case None => List()
  }
  
  setMnemonic ('T')
}