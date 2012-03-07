package org.workcraft.gui.modeleditor.tools
import org.workcraft.exceptions.InvalidConnectionException
import org.workcraft.scala.effects.IO

trait ConnectionManager[-T] {
	def connect(node1 : T, node2 : T) : Either[InvalidConnectionException, IO[Unit]]
}
