package org.workcraft.gui.modeleditor.tools
import org.workcraft.exceptions.InvalidConnectionException
import org.workcraft.scala.effects.IO
import org.workcraft.scala.Expressions._

trait ConnectionManager[-T] {
	def connect(node1 : T, node2 : T) : Expression[Either[InvalidConnectionException, IO[Unit]]]
}
