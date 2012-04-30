package org.workcraft.plugins.fsm
import org.workcraft.services.LayoutNode
import java.awt.geom.Point2D
import org.workcraft.scala.Expressions._
import scalaz.Scalaz._
import org.workcraft.services.Layout
import org.workcraft.services.LayoutSpec
import org.workcraft.services.LayoutOrientation

object FSMLayout {
  class FSMLayoutNode extends LayoutNode
  def apply(efsm: EditableFSM) = efsm.saveState.eval.map(vfsm => {
    val nodeToComponent: Map[LayoutNode, State] = vfsm.fsm.states.list.map((new FSMLayoutNode, _)).toMap
    val componentToNode = nodeToComponent.map(_.swap).toMap

    val size = (n: LayoutNode) => (1.5, 1.5) // FIXME: should be CommonVisualSettings.size

    val outgoingArcs = (n: LayoutNode) => vfsm.fsm.postset(nodeToComponent(n)).map( p => componentToNode(p._1))

    val apply = (l: List[(LayoutNode, Point2D.Double)]) => l.map { case (n, p) => efsm.layout.update(_ + (nodeToComponent(n) -> p)) }.sequence >| {}

    Layout(LayoutSpec(nodeToComponent.map(_._1).toList, size, outgoingArcs, 2, 2, LayoutOrientation.Down), apply)
  })
}
