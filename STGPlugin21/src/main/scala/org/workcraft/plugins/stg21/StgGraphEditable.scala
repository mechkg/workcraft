package org.workcraft.plugins.stg21
import org.workcraft.gui.graph.tools.GraphEditorTool
import org.workcraft.gui.graph.GraphEditable
import org.workcraft.gui.graph.tools.GraphEditor
import pcollections.PVector
import org.workcraft.gui.propertyeditor.EditableProperty
import pcollections.TreePVector
import org.workcraft.gui.graph.tools.selection.GenericSelectionTool
import org.workcraft.dependencymanager.advanced.user.Variable
import pcollections.HashTreePSet
import org.workcraft.gui.graph.tools.HitTester
import java.awt.geom.Point2D
import org.workcraft.util.Maybe
import pcollections.PCollection
import pcollections.PSet
import org.workcraft.gui.graph.tools.DragHandle
import org.workcraft.gui.graph.tools.DragHandler
import org.workcraft.plugins.stg21.types._
import org.workcraft.scala.grapheditor.tools.SelectionTool
import org.workcraft.exceptions.NotImplementedException
import org.workcraft.scala.Scalaz._
import org.workcraft.scala.Expressions._
import org.workcraft.dom.visual.GraphicalContent
import org.workcraft.graphics.Graphics
import org.workcraft.graphics.RichGraphicalContent
import java.awt.BasicStroke
import java.awt.Color
import org.workcraft.dom.visual.ColorisableGraphicalContent
import org.workcraft.gui.graph.tools.Colorisation
import org.workcraft.scala.grapheditor.tools.ConnectionTool
import org.workcraft.gui.graph.tools.ConnectionController
import org.workcraft.gui.graph.tools.SafeConnectionManager
import org.workcraft.exceptions.InvalidConnectionException
import org.workcraft.util.Action
import org.workcraft.dom.visual.Touchable

class StgGraphEditable(visualStg : ModifiableExpression[VisualStg]) extends GraphEditable {
  def createTools (editor : GraphEditor) : java.lang.Iterable[_ <: GraphEditorTool] = {
    val selection = Variable.create[PSet[VisualNode]](HashTreePSet.empty[VisualNode])

    def movableController(n : VisualNode) : ModifiableExpression[Point2D] = {
      import org.workcraft.plugins.stg21.modifiable._
      n match {
        case StgVisualNode(node) =>
          {
            val n = visualStg.visual.nodesInfo.lookup(node)
            n.orElse(VisualInfo(position = new Point2D.Double(1, 0), parent = None)).position
          }
        case GroupVisualNode(g) => {
          visualStg.visual.groups.lookup(g).modifiableField((x : Option[Group]) => x match {
            case None => new Point2D.Double(0,0)
            case Some(g) => g.info.position
          }
          ) ((x : Point2D) => (o : Option[Group]) => o match {
            case None => None
            case Some(g) => Some(g.copy(info=g.info.copy(position=x)))
          })
        }
      }
    }
    
    def entityMovableController(e : VisualEntity) : Maybe[ModifiableExpression[Point2D]] = {
      e match {
        case ArcVisualEntity(_) => Maybe.Util.nothing[ModifiableExpression[Point2D]]
        case NodeVisualEntity(n) => Maybe.Util.just(movableController(n))
      }
    }
    
    val stgNodes = for(v <- (visualStg : Expression[VisualStg])) yield {
      v.math.places.keys.map(k => PlaceNode(k)) ::: v.math.transitions.keys.map(t => TransitionNode(t))
    }
    
    val visualNodes = for(v <- (visualStg : Expression[VisualStg])) yield {
      ((v.math.places.keys.map(k => PlaceNode(k)) ::: v.math.transitions.keys.map(t => TransitionNode(t))).map(n => StgVisualNode(n)) ::: v.visual.groups.keys.map(g => GroupVisualNode(g))
      )
    }
    val visualEntities = for(v <- (visualStg : Expression[VisualStg]); n <- visualNodes)
      yield {
      n.map(NodeVisualEntity(_ : VisualNode)) ::: v.math.arcs.keys.map(ArcVisualEntity(_ : Id[Arc]))
    }
    
    val selectionJ = Variable.create[PSet[VisualEntity]](HashTreePSet.empty())
    
    implicit def decorateMaybe[T](m : Maybe[T]) = new {
      def toOption = m.accept[Option[T]](new org.workcraft.util.MaybeVisitor[T, Option[T]] {
        override val visitNothing = None
        override def visitJust(t : T) = Some(t)
      })
    }
    
    def visual(e : VisualEntity) = for (vstg <- visualStg : Expression[VisualStg];
    		position <- entityMovableController(e).toOption.map(m => m.expr).getOrElse(constant(new Point2D.Double(0,0)))
    ) yield {
      (e match {
        case NodeVisualEntity(n) => {
          n match {
            case StgVisualNode(PlaceNode(p)) => Visual.place(vstg.math.places.lookup(p).get)
            case StgVisualNode(TransitionNode(t)) => Visual.transition(t)(vstg).getOrElse(RichGraphicalContent.empty)
            case GroupVisualNode(g) => Graphics.rectangle(1, 1, Some((new BasicStroke(0.1.toFloat), Color.BLACK)), Some(Color.WHITE)) : RichGraphicalContent // todo: recursively draw all? 
          }
        }
        case ArcVisualEntity(a) => {
          RichGraphicalContent.empty
        }
      }
          ).translate(position : Point2D)
    }
    
    def touchable(n : VisualEntity) = for(v <- visual(n)) yield v.touchable
    
    def deepCenters(n : StgConnectable) : Expression[Point2D] = n match {
      case NodeConnectable(n) => movableController(StgVisualNode(n))
      case ArcConnectable(a) => constant(new Point2D.Double(0,0)) // TODO
    }
    
    val selectionTool = SelectionTool.create[VisualEntity](visualEntities, selectionJ, entityMovableController, (x => /*snap */x), touchable)
    
    val connectionController = ConnectionController.Util.fromSafe(new ConnectionManager(visualStg))
    
    val stgConnectables = for(v <- visualStg; n <- stgNodes) yield {
      n.map(n => NodeConnectable(n)) ::: v.math.arcs.keys.map(aid => ArcConnectable(aid))
    }
    
    def connectableTouchable(c : StgConnectable) : Expression[Touchable] = c match { // TODO: deep!
      case NodeConnectable(n) => touchable(NodeVisualEntity(StgVisualNode(n)))
    }
    
    val connectionTool = ConnectionTool.create[StgConnectable](stgConnectables, connectableTouchable, deepCenters, connectionController)
    
    val nodeGeneratorTools = org.workcraft.plugins.stg21.StgToolsProvider(visualStg).nodeGeneratorTools
    
    implicit def decorateColorisable(c : ColorisableGraphicalContent) = new {
      val noColorisation = ColorisableGraphicalContent.Util.applyColourisation(c, Colorisation.EMPTY)
    }

    def paint : Expression[GraphicalContent] = {
      for(entities <- visualEntities : Expression[_ <: List[VisualEntity]]; visuals <- entities.map(visual).toList.sequence) 
        yield (visuals.foldl(RichGraphicalContent.empty)((a: RichGraphicalContent,b: RichGraphicalContent) => b over a).colorisableGraphicalContent.noColorisation)
    }
    
    scala.collection.JavaConversions.asJavaIterable(
      selectionTool.asGraphEditorTool((colorisation, selection) => paint) ::
      connectionTool.asGraphEditorTool((colorisation,highlighted) => paint) ::
      nodeGeneratorTools
    )
  }
  def properties : org.workcraft.dependencymanager.advanced.core.Expression[_ <: PVector[EditableProperty]] = constant(TreePVector.empty[EditableProperty]).jexpr
}
