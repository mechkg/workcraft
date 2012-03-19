package org.workcraft.plugins.lola

import org.workcraft.services.Module
import org.workcraft.services.Service
import org.workcraft.services.GlobalServiceProvider
import org.workcraft.services.NewModelImpl
import org.workcraft.services.GlobalScope
import org.workcraft.services.NewModelService
import org.workcraft.services.ModelServiceProvider
import org.workcraft.services.Exporter
import org.workcraft.services.ExporterService
import org.workcraft.services.FileOpenService
import org.workcraft.gui.services.GuiToolService
import org.workcraft.gui.services.GuiTool
import org.workcraft.gui.services.ToolClass
import org.workcraft.scala.effects.IO._
import org.workcraft.gui.MainWindow
import javax.swing.JOptionPane

object TestTool extends GuiTool {
  val description = "Do something"
  val classification = ToolClass.Verification
  def run (mainWindow: MainWindow) = Some(ioPure.pure { println ("hehe");  JOptionPane.showMessageDialog (mainWindow, "Hey", "Hey", JOptionPane.INFORMATION_MESSAGE)})
}

object LolaServiceProvider extends GlobalServiceProvider {
  def implementations[T](service: Service[GlobalScope, T]) = service match {
    case ExporterService => List(LolaExporter)
    case GuiToolService => List(TestTool) 
    case _ => Nil
  }
}

class LolaModule extends Module {
  def name = "Workcraft LoLA integration"
  def serviceProvider = LolaServiceProvider
}
