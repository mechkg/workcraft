package org.workcraft.plugins.petri2

import org.workcraft.services.LayoutNode
import org.workcraft.scala.Expressions._
import scalaz.Scalaz._
import java.awt.geom.Rectangle2D
import java.awt.geom.Point2D
import org.workcraft.services.LayoutSpec
import org.workcraft.services.Layout
import org.workcraft.services.LayoutOrientation

object PetriNetLayout {
  class PetriNetLayoutNode extends LayoutNode

  def apply(net: EditablePetriNet) = net.saveState.eval.map(vpn => {
    val nodeToComponent: Map[LayoutNode, Component] = (vpn.net.places ++ vpn.net.transitions).map((new PetriNetLayoutNode, _)).toMap
    
    val componentToNode = nodeToComponent.map(_.swap).toMap

    val size = (n: LayoutNode) => (1.0, 1.0) // FIXME: should be CommonVisualSettings.size
    
    val outgoingArcs = (n: LayoutNode) => vpn.net.postset(nodeToComponent(n)).map(componentToNode(_)) 
    
    val apply = (l : List[(LayoutNode, Point2D.Double)]) => l.map { case (n, p) => net.layout.update ( _ + (nodeToComponent(n) -> p) ) }.sequence >| {}
    
    Layout(LayoutSpec(nodeToComponent.map(_._1).toList, size, outgoingArcs, 2, 3, LayoutOrientation.LeftToRight), apply)
  })
}