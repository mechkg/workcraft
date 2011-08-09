package org.workcraft.plugins.cpog.gui;

import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;

import org.workcraft.gui.graph.tools.GraphEditorTool.Button;
import org.workcraft.gui.graph.tools.NodeGenerator;
import org.workcraft.plugins.cpog.CPOG;
import org.workcraft.plugins.cpog.scala.nodes.RhoClause;
import org.workcraft.plugins.cpog.scala.nodes.Variable;
import org.workcraft.plugins.cpog.scala.nodes.Vertex;

import static org.workcraft.gui.graph.tools.GraphEditorToolUtil.*;

public class Generators {
	private final CPOG cpog;
	
	private Generators(CPOG cpog) {
		this.cpog = cpog;
	}
	
	public static Generators createFor (CPOG cpog) {
		return  new Generators(cpog);
	}

	public final NodeGenerator vertexGenerator = new NodeGenerator() {
		Button identification = createButton("Vertex", "images/icons/svg/vertex.svg", KeyEvent.VK_V);

		@Override
		public Button getIdentification() {
			return identification;
		}

		@Override
		public void generate(Point2D where) {
			Vertex vertex = cpog.createVertex();
			vertex.visualProperties().position().setValue(where);
		}
	};

	public final NodeGenerator variableGenerator = new NodeGenerator() {
		Button identification = createButton("Variable", "images/icons/svg/variable.svg", KeyEvent.VK_X);

		@Override
		public Button getIdentification() {
			return identification;
		}

		@Override
		public void generate(Point2D where) {
			Variable variable = cpog.createVariable();
			variable.visualProperties().position().setValue(where);
		}
	};

	public final NodeGenerator rhoClauseGenerator = new NodeGenerator() {
		Button identification = createButton("RhoClause", "images/icons/svg/rho.svg", KeyEvent.VK_R);

		@Override
		public Button getIdentification() {
			return identification;
		}

		@Override
		public void generate(Point2D where) {
			RhoClause rhoClause = cpog.createRhoClause();
			rhoClause.visualProperties().position().setValue(where);
		}
	};

}
