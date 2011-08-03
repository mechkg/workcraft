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

class StgGraphEditable extends GraphEditable {
  def createTools (editor : GraphEditor) : java.lang.Iterable[_ <: GraphEditorTool] = {
    val selection = Variable.create[PSet[Id[VisualNode]]](HashTreePSet.empty[Id[VisualNode]])
    val hitTester = new HitTester[Id[VisualNode]] {
      def hitTest(point : Point2D) : Maybe[VisualNode] = {
        null
      }

      def boxHitTest(boxStart : Point2D, boxEnd : Point2D) : PCollection[VisualNode] = {
        null
      }
    }
    
    val dh = new DragHandler[Id[VisualNode]] {
      def startDrag(hitNode : Id[VisualNode]) : DragHandle = {
        new DragHandle {
          def cancel {
          }
      	  def commit {
      	  
      	  }
      	  def setOffset(offset : Point2D) {
      	  
          }
        }
      }
    }

    
    val gst = new GenericSelectionTool[Id[VisualNode]](selection, hitTester, dragHandler);
    scala.collection.JavaConversions.asJavaIterable(
      List(gst)
    )
  }
  def properties : Expression[_ <: PVector[EditableProperty]] = Expressions.constant(TreePVector.empty())
}
