package org.workcraft.plugins.cpog.scala
import org.workcraft.gui.graph.tools.NodeGenerator
import org.workcraft.gui.graph.tools.GraphEditorToolUtil._
import java.awt.event.KeyEvent
import java.awt.geom.Point2D

case class GeneratorTools(cpog : CPOG) {
  val vertexGenerator : NodeGenerator = new NodeGenerator {
		override lazy val getIdentification = createButton("Vertex", "images/icons/svg/vertex.svg", KeyEvent.VK_V)
		override def generate(where : Point2D.Double) {
			val vertex = cpog.createVertex
			vertex.visualProperties.position.setValue(where)
		}
	}

  val variableGenerator : NodeGenerator = new NodeGenerator {
		override lazy val getIdentification = createButton("Variable", "images/icons/svg/variable.svg", KeyEvent.VK_X)
		override def generate(where : Point2D.Double) {
			val variable = cpog.createVariable
			variable.visualProperties.position.setValue(where)
		}
	}

  val rhoClauseGenerator : NodeGenerator = new NodeGenerator {
		override lazy val getIdentification = createButton("RhoClause", "images/icons/svg/rho.svg", KeyEvent.VK_R)
		override def generate(where : Point2D.Double) {
			val rhoClause = cpog.createRhoClause
			rhoClause .visualProperties.position.setValue(where)
		}
	}
}
