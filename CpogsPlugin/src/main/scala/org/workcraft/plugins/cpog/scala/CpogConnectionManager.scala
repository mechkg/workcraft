package org.workcraft.plugins.cpog.scala
import org.workcraft.gui.graph.tools.SafeConnectionManager
import nodes.Component
import org.workcraft.exceptions.InvalidConnectionException
import org.workcraft.util.Action
import nodes.Vertex
import org.workcraft.util.Maybe
import org.workcraft.plugins.cpog.ComponentVisitor
import org.workcraft.util.Maybe.Util.NothingFound

class CpogConnectionManager(cpog : CPOG) extends SafeConnectionManager[Component] {
  @throws( classOf[InvalidConnectionException])
  override def connect(first : Component, second : Component) : Action = {
		if (first == second) throw new InvalidConnectionException("Self loops are not allowed");
		
		try {
			val firstVertex : Vertex = Maybe.Util.extract(ComponentVisitor.Util.asVertex(first));
			val secondVertex : Vertex = Maybe.Util.extract(ComponentVisitor.Util.asVertex(second));
		
			return new Action {
				override def run {
					cpog.connect(firstVertex, secondVertex);
				}
			};
		}
		catch {
		  case nothing : NothingFound => throw new InvalidConnectionException("Invalid connection: only connections between vertices are allowed");
		}
	}
}
