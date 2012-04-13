package org.workcraft.plugins.fsm

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

object NewFSM extends NewModelImpl {
  def name = "Finite State Machine"
  def create = EditableFSM.create(VisualFSM.Minimal).map(new FSMModel(_))
}

object FSMServiceProvider extends GlobalServiceProvider {
  def implementations[T](service: Service[GlobalScope, T]) = service match {
    case NewModelService => List(NewFSM)

    //case ExporterService => List(DotGExporter)
    //case GuiToolService => List(LolaDeadlockTool) 
    case _ => Nil
  }
}

class FSMModule extends Module {
  def name = "Finite State Machine"
  def serviceProvider = FSMServiceProvider
}
