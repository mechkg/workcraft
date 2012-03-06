package org.workcraft.plugins.stg21

import org.workcraft.gui.graph.tools.SafeConnectionManager
import org.workcraft.plugins.stg21.types._
import org.workcraft.exceptions.InvalidConnectionException
import org.workcraft.util.Action
import org.workcraft.plugins.stg21.modifiable._
import org.workcraft.scala.Expressions._
import org.workcraft.gui.modeleditor.tools.ConnectionManager
import org.workcraft.scala.effects.IO

class StgConnectionManager(val visualStg : ModifiableExpression[VisualStg]) extends ConnectionManager[StgConnectable] {
      def connect(node1 : StgConnectable, node2 : StgConnectable) : Either[InvalidConnectionException, IO[Unit]]= {
        (node1, node2) match {
          case (NodeConnectable(n1), NodeConnectable(n2)) => (n1, n2) match {
            case (ExplicitPlaceNode(p), TransitionNode(t)) => Right(addConnection(ConsumingArc(p, t)))
            case (TransitionNode(t), ExplicitPlaceNode(p)) => Right(addConnection(ProducingArc(t, p)))
            case (TransitionNode(t1), TransitionNode(t2)) => Right(addConnection(ImplicitPlaceArc(t1, t2, 0)))
            case (ExplicitPlaceNode(_), ExplicitPlaceNode(_)) => Left(new InvalidConnectionException("Arcs between places are not allowed"))
          }
          case _ => throw new InvalidConnectionException("Connecting connections is not supported yet")
          // TODO: "Arcs between places and implicit places are not allowed"
          // "Only connections with arcs having implicit places are allowed"
          // "Arc already exists"
        }
      }
      
      def addConnection(a : Arc) : IO[Unit] = IO.ioPure.pure{
          org.workcraft.plugins.stg21.modifiable.decorateModifiableExpression(visualStg.math.arcs).runState(Col.add(a))
        }
}
