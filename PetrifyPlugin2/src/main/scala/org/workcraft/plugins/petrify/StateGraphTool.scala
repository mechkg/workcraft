package org.workcraft.plugins.petrify

import org.workcraft.gui.services.GuiTool
import org.workcraft.gui.services.ToolClass
import org.workcraft.gui.MainWindow
import org.workcraft.scala.Expressions
import org.workcraft.scala.effects.IO
import org.workcraft.scala.effects.IO._
import org.workcraft.scala.Expressions._
import scalaz.Scalaz._
import java.io.File
import org.workcraft.services.ExportError
import org.workcraft.gui.tasks.ModalTaskDialog
import org.workcraft.tasks.Task._
import javax.swing.JOptionPane
import org.workcraft.plugins.petri2.PetriNetService
import org.workcraft.plugins.fsm.FSMModel
import org.workcraft.plugins.fsm.EditableFSM
import org.workcraft.plugins.fsm.VisualFSM
import org.workcraft.plugins.fsm.State
import java.awt.geom.Point2D
import org.workcraft.plugins.fsm.Arc
import org.workcraft.dom.visual.connections.StaticVisualConnectionData
import org.workcraft.dom.visual.connections.Polyline

object StateGraphTool extends GuiTool {
  val description = "Show state graph (generate using petrify)"
  val classification = ToolClass.Custom("State graph")

  import PetrifyError._

  def run(mainWindow: MainWindow) = mainWindow.editorInFocus.expr.map(editorWindow => editorWindow.flatMap(_.content.model.implementation(PetriNetService)) match {
    case Some(pn) => Some({
      val input = File.createTempFile("workcraft", ".g")
      val output = File.createTempFile("workcraft", ".g")

      val export = new PnToDotGExportJob(pn).asTask(input).mapError2(DotGExportError(_))
      val parse = DotGParser.parseTask(output).mapError2(DotGParseError(_))
      val writeSgTask = new WriteSgTask("./tools/write_sg", input, output)

      val chain = export >>=| writeSgTask >>=| parse

      ModalTaskDialog.runTask(mainWindow, "Generating state graph using petrify", chain) >>= {
        case Left(None) => ioPure.pure { JOptionPane.showMessageDialog(mainWindow, "Cancelled") }
        case Left(Some(error)) => ioPure.pure { JOptionPane.showMessageDialog(mainWindow, error, "Error", JOptionPane.ERROR_MESSAGE) }
        case Right(dotg) => EditableFSM.create(VisualFSM(FsmBuilder.buildFSM(dotg), Map[State, Point2D.Double]().withDefaultValue(new Point2D.Double(0, 0)), Map[Arc, StaticVisualConnectionData]().withDefaultValue(new Polyline(List())))) >>= (editable => mainWindow.openEditor(new FSMModel(editable), None))
      }
    })
    case None => None
  })
}
