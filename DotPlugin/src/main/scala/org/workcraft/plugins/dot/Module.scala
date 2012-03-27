package org.workcraft.plugins.dot

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
import org.workcraft.gui.tasks.ModalTaskDialog
import scalaz.Scalaz._

object DotServiceProvider extends GlobalServiceProvider {
  def implementations[T](service: Service[GlobalScope, T]) = service match {
    //case ExporterService => List(DotExporter)
    case GuiToolService => List(DotLayoutTool)
    case _ => Nil
  }
}

class DotModule extends Module {
  def name = "Workcraft dot integration"
  def serviceProvider = DotServiceProvider
}
