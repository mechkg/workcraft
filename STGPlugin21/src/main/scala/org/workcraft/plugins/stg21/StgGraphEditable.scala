package org.workcraft.plugins.stg21
import org.workcraft.gui.graph.tools.GraphEditorTool
import org.workcraft.gui.graph.GraphEditable
import org.workcraft.gui.graph.tools.GraphEditor
import pcollections.PVector
import org.workcraft.gui.propertyeditor.EditableProperty
import pcollections.TreePVector
import org.workcraft.gui.modeleditor.tools.selection.GenericSelectionToolMouseListener
import org.workcraft.dependencymanager.advanced.user.Variable
import pcollections.HashTreePSet
import org.workcraft.gui.graph.tools.HitTester
import java.awt.geom.Point2D
import org.workcraft.gui.graph.tools.DragHandle
import org.workcraft.gui.graph.tools.DragHandler
import org.workcraft.plugins.stg21.types._
import org.workcraft.scala.grapheditor.tools.GenericSelectionTool
import org.workcraft.exceptions.NotImplementedException
import org.workcraft.scala.Scalaz._
import org.workcraft.scala.Expressions._
import org.workcraft.graphics.RichGraphicalContent
import java.awt.BasicStroke
import java.awt.Color
import org.workcraft.scala.grapheditor.tools.GenericConnectionTool
import org.workcraft.exceptions.InvalidConnectionException
import org.workcraft.util.Action
import org.workcraft.dom.visual.connections.VisualConnectionProperties
import scalaz.Lens
import org.workcraft.graphics.BoundedColorisableGraphicalContent
import org.workcraft.graphics.Graphics
import org.workcraft.dom.visual.connections.Polyline
import org.workcraft.graphics.TouchableC
import org.workcraft.graphics.GraphicalContent
import org.workcraft.gui.modeleditor.tools.ConnectionManager
import org.workcraft.graphics.ColorisableGraphicalContent
import org.workcraft.graphics.Colorisation
import RichGraphicalContent._
import org.workcraft.gui.modeleditor.ModelEditor
import scalaz.NonEmptyList
import org.workcraft.gui.modeleditor.tools.ModelEditorTool
import org.workcraft.scala.grapheditor.tools.GenericConnectionTool
import org.workcraft.gui.modeleditor.tools._
import org.workcraft.gui.modeleditor.tools.ModelEditorTool.ModelEditorToolConstructor

class StgGraphEditable(visualStg : ModifiableExpression[VisualStg]) extends ModelEditor {
  val selection = Variable.create[Set[VisualEntity]](Set.empty)
  
  def tools: NonEmptyList[ModelEditorToolConstructor] = {

    def movableController(n : VisualNode) : ModifiableExpression[Point2D.Double] = {
      import org.workcraft.plugins.stg21.modifiable._
      n match {
        case StgVisualNode(node) =>
          {
            val n = visualStg.visual.nodesInfo.lookup(node)
            n.orElse(VisualInfo(position = new Point2D.Double(1, 0), parent = None)).position
          }
        case GroupVisualNode(g) => {
          visualStg.visual.groups.lookup(g).refract(Lens((x : Option[Group]) => x match {
            case None => new Point2D.Double(0,0)
            case Some(g) => g.info.position
          },
          (o : Option[Group], x : Point2D.Double) => o match {
            case None => None
            case Some(g) => Some(g.copy(info=g.info.copy(position=x)))
          }))
        }
      }
    }
    
    def entityMovableController(e : VisualEntity) : Option[ModifiableExpression[Point2D.Double]] = {
      e match {
        case ArcVisualEntity(_) => None
        case NodeVisualEntity(n) => Some(movableController(n))
      }
    }
    
    val stgNodes = for(v <- (visualStg : Expression[VisualStg])) yield {
      v.math.places.keys.map(k => ExplicitPlaceNode(k)) ::: v.math.transitions.keys.map(t => TransitionNode(t))
    }
    
    val visualNodes = for(v <- (visualStg : Expression[VisualStg])) yield {
      ((v.math.places.keys.map(k => ExplicitPlaceNode(k)) ::: v.math.transitions.keys.map(t => TransitionNode(t))).map(n => StgVisualNode(n)) ::: v.visual.groups.keys.map(g => GroupVisualNode(g))
      )
    }
    val visualEntities = for(v <- (visualStg : Expression[VisualStg]); n <- visualNodes)
      yield {
      n.map(NodeVisualEntity(_ : VisualNode)) ::: v.math.arcs.keys.map(ArcVisualEntity(_ : Id[Arc]))
    }

    def touchableC(n : VisualEntity) = visual(n).map(_.touchable)
    
    def touchable(n : VisualEntity) = touchableC(n).map(_.touchable)
    
    def visual(e : VisualEntity) : Expression[RichGraphicalContent] = for (vstg <- visualStg : Expression[VisualStg];
    		position <- entityMovableController(e).map(m => m.expr).getOrElse(constant(new Point2D.Double(0,0)));
    		result <- (e match {
        case NodeVisualEntity(n) => {
          n match {
            case StgVisualNode(ExplicitPlaceNode(p)) => Visual.place(vstg.math.places.lookup(p).get)
            case StgVisualNode(TransitionNode(t)) => constant(Visual.transition(t)(vstg).get)
            case GroupVisualNode(g) => constant({
              rectangle(1, 1, Some((new BasicStroke(0.1.toFloat), Color.BLACK)), Some(Color.WHITE))
            }) // todo: recursively draw all? 
          }
        }
        case ArcVisualEntity(arcId) => ({
          (for(arc <- vstg.math.arcs.lookup(arcId)) yield{
            val visual = vstg.visual.arcs.get(arcId).getOrElse(Polyline(Nil))          
            for (first  <- touchableC(NodeVisualEntity(StgVisualNode(arc.first)));
            second <- touchableC(NodeVisualEntity(StgVisualNode(arc.second)))
           ) yield (VisualConnectionG.getConnectionGui(first, second, visual) : RichGraphicalContent) 
          }).get
        } : Expression[RichGraphicalContent])
      })) yield result.translate(position)
    
    
    def deepCenters(n : StgConnectable) : Expression[Point2D.Double] = n match {
      case NodeConnectable(n) => movableController(StgVisualNode(n))
      case ArcConnectable(a) => constant(new Point2D.Double(0,0)) // TODO
    }
    
    val connectionController = new StgConnectionManager(visualStg)
    
    val stgConnectables = for(v <- visualStg; n <- stgNodes) yield {
      n.map(n => NodeConnectable(n)) ::: v.math.arcs.keys.map(aid => ArcConnectable(aid))
    }
    
    def connectableTouchable(c : StgConnectable) : Expression[TouchableC] = c match { // TODO: deep!
      case NodeConnectable(n) => touchableC(NodeVisualEntity(StgVisualNode(n)))
      case ArcConnectable(a) => touchableC(ArcVisualEntity(a))
    }
    
    def paint : Expression[GraphicalContent] = {
      for(entities <- visualEntities : Expression[_ <: List[VisualEntity]]; visuals <- entities.map(visual).toList.sequence) 
        yield (visuals.map(r => r.bcgc.cgc).foldl(ColorisableGraphicalContent.Empty)(_.compose(_)).applyColorisation(Colorisation.Empty))
    }

    val connectionTool = GenericConnectionTool.apply[StgConnectable](stgConnectables, (connectableTouchable(_)).map(_.map(_.touchable)), deepCenters, connectionController, _ => paint)
    
    val nodeGeneratorTools = org.workcraft.plugins.stg21.StgToolsProvider(visualStg).nodeGeneratorTools(paint)
    
    val selectionTool = GenericSelectionTool.apply[VisualEntity](
        visualEntities, 
        selection, 
        entityMovableController, 
        (x => /*snap */x), 
        touchable,
        (_ => paint),
        Nil)
    


    val simulationTool = null

    NonEmptyList.nel(
      selectionTool,
      connectionTool ::
      nodeGeneratorTools)
  }
import scala.collection.JavaConversions._
  def properties : org.workcraft.dependencymanager.advanced.core.Expression[_ <: PVector[EditableProperty]] = {
    for(s <- (selection : Expression[Set[VisualEntity]]);
      props <- sequenceExpressions(s.toList.map(e => EditableProperties.objProperties(e)(visualStg)))
    ) yield (TreePVector.from(scala.collection.JavaConversions.asJavaCollection(props.flatten)))
  }
}
