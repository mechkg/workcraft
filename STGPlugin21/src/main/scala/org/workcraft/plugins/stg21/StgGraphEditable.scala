package org.workcraft.plugins.stg21
import org.workcraft.gui.graph.tools.GraphEditorTool
import org.workcraft.gui.graph.GraphEditable
import org.workcraft.gui.graph.tools.GraphEditor
import org.workcraft.dependencymanager.advanced.core.Expression
import org.workcraft.dependencymanager.advanced.core.Expressions
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
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression
import org.workcraft.exceptions.NotImplementedException
import org.workcraft.scala.Scalaz._
import org.workcraft.scala.Expressions._
import org.workcraft.dom.visual.GraphicalContent

class StgGraphEditable(visualStg : ModifiableExpression[VisualStg]) extends GraphEditable {
  def createTools (editor : GraphEditor) : java.lang.Iterable[_ <: GraphEditorTool] = {
    val selection = Variable.create[PSet[VisualNode]](HashTreePSet.empty[VisualNode])
    val hitTester = new HitTester[VisualNode] {
      def hitTest(point : Point2D) : Maybe[VisualNode] = {
            throw new NotImplementedException()
      }

      def boxHitTest(boxStart : Point2D, boxEnd : Point2D) : PCollection[VisualNode] = {
            throw new NotImplementedException()
      }
    }
    
    val dh : DragHandler[VisualNode] = new DragHandler[VisualNode] {
      def startDrag(hitNode : VisualNode) : DragHandle = {
        new DragHandle {
          def cancel {
            throw new NotImplementedException()
          }
      	  def commit {
            throw new NotImplementedException()
      	  }
      	  def setOffset(offset : Point2D) {
            throw new NotImplementedException()
          }
        }
      }
    }

    def movableController(n : VisualNode) : Maybe[ModifiableExpression[Point2D]] = {
      import org.workcraft.plugins.stg21.modifiable._
      n match {
        case StgVisualNode(node) =>
          Maybe.Util.just(visualStg.visual.nodesInfo.lookup(node).orElse(VisualInfo(position = new Point2D.Double(0, 0), parent = None)).position)
        case GroupVisualNode(g) => {
          Maybe.Util.just(visualStg.visual.groups.lookup(g).modifiableField((x : Option[Group]) => x match {
            case None => new Point2D.Double(0,0)
            case Some(g) => g.info.position
          }
          ) ((x : Point2D) => (o : Option[Group]) => o match {
            case None => None
            case Some(g) => Some(g.copy(info=g.info.copy(position=x)))
          }))
        }
      }
    }
    
    val visualNodes = for(v <- (visualStg : Expression[VisualStg])) yield {
      ((v.math.places.keys.map(k => PlaceNode(k)) ::: v.math.transitions.keys.map(t => TransitionNode(t))).map(n => StgVisualNode(n)) ::: v.visual.groups.keys.map(g => GroupVisualNode(g))
      ) : Iterable[VisualNode]
    }
    
    val selectionJ = Variable.create[PSet[VisualNode]](HashTreePSet.empty())
    
    def touchable(n : VisualNode) = visual(n)
    
    val selectionTool = SelectionTool.create[VisualNode](visualNodes, selectionJ, movableController, (x => /*snap */x), touchable)
    
    val nodeGeneratorTools = org.workcraft.plugins.stg21.StgToolsProvider(visualStg).nodeGeneratorTools
    
    scala.collection.JavaConversions.asJavaIterable(
      selectionTool.asGraphEditorTool((_, _) => Expressions.constant(GraphicalContent.EMPTY)) ::
      nodeGeneratorTools
    )
  }
  def properties : Expression[_ <: PVector[EditableProperty]] = Expressions.constant(TreePVector.empty())
}
