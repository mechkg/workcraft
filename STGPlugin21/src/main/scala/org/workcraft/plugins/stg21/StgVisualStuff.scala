package org.workcraft.plugins.stg21
import org.workcraft.graphics.stg.RichGraphicalContent
import org.workcraft.graphics.stg.NotSoRichGraphicalContent._
import java.awt.geom.Point2D
import org.workcraft.plugins.stg21.types._
import scalaz.Lens
import org.workcraft.gui.CommonVisualSettings
import java.awt.BasicStroke
import java.awt.Color
import org.workcraft.dom.visual.connections.Polyline
import scalaz.Scalaz
import Scalaz.ma
import Scalaz.mab
import org.workcraft.graphics.GraphicalContent
import org.workcraft.graphics.ColorisableGraphicalContent
import org.workcraft.graphics.Colorisation

object StgVisualStuff {

  implicit def apply(visualStg: VisualStg): StgVisualStuff = new StgVisualStuff(visualStg)

  def position(n: VisualNode): Lens[VisualStg, Point2D.Double] = n match {
    case StgVisualNode(n) => {
      VisualInfo.position compose
        EditableProperties.mapLookWithDefault(n, VisualInfo(position = new Point2D.Double(1, 0), parent = None)) compose
        VisualModel.nodesInfo[StgNode, Id[Arc]] compose
        VisualStg.visual
    }
    case GroupVisualNode(g) => {
      VisualInfo.position compose
        Group.info compose
        Col.uncheckedLook(g) compose
        VisualModel.groups[StgNode, Id[Arc]] compose
        VisualStg.visual
    }
  }

  def entityPosition(e: VisualEntity): Option[Lens[VisualStg, Point2D.Double]] = {
    e match {
      case ArcVisualEntity(_) => None
      case NodeVisualEntity(n) => Some(position(n))
    }
  }

  
  def applyLens[A, B](l: Lens[A, B], a: A): Store[B, A] = Store(l(a), x => l.set(a, x))
}

class StgVisualStuff(visualStg: VisualStg) {
  import StgVisualStuff._

  val stgNodes =
    visualStg.math.places.keys.map(k => ExplicitPlaceNode(k)) ::: visualStg.math.transitions.keys.map(t => TransitionNode(t))

  val visualNodes =
    (visualStg.math.places.keys.map(k => ExplicitPlaceNode(k)) :::
      visualStg.math.transitions.keys.map(t => TransitionNode(t))).map(n => StgVisualNode(n)) :::
      visualStg.visual.groups.keys.map(g => GroupVisualNode(g))

  val visualEntities =
    visualNodes.map(NodeVisualEntity(_: VisualNode)) ::: visualStg.math.arcs.keys.map(ArcVisualEntity(_: Id[Arc]))

  val stgConnectables =
    stgNodes.map(n => NodeConnectable(n)) ::: visualStg.math.arcs.keys.map(aid => ArcConnectable(aid))

  def visual(e: VisualEntity): RichGraphicalContent = {
    val position = entityPosition(e).map(_.get(visualStg)).getOrElse(new Point2D.Double(0, 0));
    ((e match {
      case NodeVisualEntity(n) => {
        n match {
          case StgVisualNode(ExplicitPlaceNode(p)) => Visual.place(visualStg.math.places(p))
          case StgVisualNode(TransitionNode(t)) => Visual.transition(t)(visualStg).get
          case GroupVisualNode(g) => {
            (s : CommonVisualSettings) => rectangle(1, 1, Some((new BasicStroke(0.1.toFloat), Color.BLACK)), Some(Color.WHITE))
          } // todo: recursively draw all? 
        }
      }
      case ArcVisualEntity(arcId) => ({
        {
          val arc = visualStg.math.arcs(arcId)
          val visual = visualStg.visual.arcs.get(arcId).getOrElse(Polyline(Nil))
          for (
            first <- touchableC(NodeVisualEntity(StgVisualNode(arc.first)));
            second <- touchableC(NodeVisualEntity(StgVisualNode(arc.second)));
            res <- VisualConnectionG.getConnectionGui(first, second, visual)
          ) yield res
        }
      }: RichGraphicalContent)
    }): RichGraphicalContent).map(_.translate(position))
  }

  def touchableC(n: VisualEntity) = visual(n).map(_.touchable)

  def touchable(n: VisualEntity) = touchableC(n).map(_.touchable)
  
  def image : CommonVisualSettings => GraphicalContent = s => {
      visualStg.visualEntities.map(visualStg.visual(_)(s).bcgc.cgc).
      foldLeft(ColorisableGraphicalContent.Empty)(_.compose(_)).applyColorisation(Colorisation.Empty)
    }
}
