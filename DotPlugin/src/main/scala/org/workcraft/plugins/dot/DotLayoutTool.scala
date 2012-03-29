package org.workcraft.plugins.dot
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
import org.workcraft.plugins.dot.parser.Dot
import org.workcraft.plugins.dot.parser.Node
import org.workcraft.gui.tasks.ModalTaskDialog
import org.workcraft.tasks.Task._
import javax.swing.JOptionPane
import org.workcraft.services.LayoutService
import org.workcraft.plugins.dot.parser.Attr
import java.awt.geom.Point2D
import java.awt.geom.AffineTransform

sealed trait DotError

object DotError {
  case class CouldNotStart(cause: Throwable) extends DotError
  case class RuntimeError(text: String) extends DotError
  case class DotExportError(cause: ExportError) extends DotError
  case class DotParseError(cause: String) extends DotError
}

object DotLayoutTool extends GuiTool {
  import DotError._

  val description = "Layout using dot"
  val classification = ToolClass.Layout
  
  def parsePos (pos: Option[String], transform: AffineTransform) = {
    val Array(x, y) = pos.map(_.value).getOrElse("0,0").split(",").map(_.toDouble)
    val res = new Point2D.Double (x / 72.0, y / 72.0)
    transform.transform(res, res)
    res
  }

  def run(mainWindow: MainWindow) = mainWindow.editorInFocus.expr.map(editorWindow => editorWindow.flatMap(_.content.model.implementation(LayoutService)) match {
    case Some(layout) => Some(layout >>= (layout => {
      val input = File.createTempFile("workcraft", ".dot")
      val output = File.createTempFile("workcraft", ".dot")

      val export = new DotExportJob(layout.spec).asTask(input).mapError2(DotExportError(_))
      val parse = Dot.parseTask(output).mapError2(DotParseError(_))
      val layoutTask = new DotLayoutTask("./tools/dot/dot", input, output)

      ModalTaskDialog.runTask(mainWindow, "Generating layout using dot", export flatMap (_ => layoutTask) flatMap (_ => parse)) >>= {
        case Left(None) => ioPure.pure { JOptionPane.showMessageDialog(mainWindow, "Cancelled") }
        case Left(Some(error)) => ioPure.pure { JOptionPane.showMessageDialog(mainWindow, error, "Error", JOptionPane.ERROR_MESSAGE) }
        case Right(graph) => {
          val nodeToId = layout.spec.nodes.zipWithIndex.toMap
          val idToNode = nodeToId.map(_.swap).toMap

          layout.apply(graph.statements.map { case Node(id, _, attrs @ _ *) => Some((idToNode(id.toInt), parsePos(attrs.find(_.name == "pos").map(_.value.get), layout.spec.orientation.transform))); case _ => None }.flatten.toList)
        }

      }

    }))
    case None => None
  })
}