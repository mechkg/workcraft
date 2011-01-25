package org.workcraft.testing.serialisation.xml;

import static org.junit.Assert.assertFalse;
import static org.workcraft.dependencymanager.advanced.core.GlobalCache.eval;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;

import junit.framework.Assert;

import org.apache.batik.svggen.SVGGraphics2D;
import org.junit.Test;
import org.w3c.dom.Document;
import org.workcraft.Framework;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.Expressions;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.DrawMan;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.DrawableNew;
import org.workcraft.dom.visual.GraphicalContent;
import org.workcraft.dom.visual.Hidable;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.connections.Bezier;
import org.workcraft.dom.visual.connections.BezierControlPoint;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.dom.visual.connections.VisualConnection.ConnectionType;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.gui.graph.tools.Decorator;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.DefaultStorageManager;
import org.workcraft.plugins.stg.DummyTransition;
import org.workcraft.plugins.stg.HistoryPreservingStorageManager;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.STGPlace;
import org.workcraft.plugins.stg.VisualDummyTransition;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.util.XmlUtil;

public class RegressionTests_2011_01_06 {
	@Test
	public void bezierDeserialisationInitTest() throws DeserialisationException {
		
		VisualConnection conn = new VisualConnection(new DefaultStorageManager());
		new Bezier(conn, new DefaultStorageManager());
	}
	
	class HiddenNode implements Node, DrawableNew, Hidable {

		@Override
		public Expression<Boolean> hidden() {
			return Expressions.constant(true);
		}

		@Override
		public Expression<? extends GraphicalContent> graphicalContent() {
			return Expressions.constant(new GraphicalContent(){
				@Override
				public void draw(DrawRequest request) {
					hasBeenDrawn = true;
				}
			});
		}
		
		public boolean hasBeenDrawn = false;

		@Override
		public Expression<? extends Touchable> shape() {
			return null;
		}

		@Override
		public ModifiableExpression<Node> parent() {
			return null;
		}

		@Override
		public Expression<? extends Collection<? extends Node>> children() {
			return Expressions.constant(Collections.<Node>emptyList());
		}
		
	}
	
	@Test
	public void hiddenNotDrawnTest() throws Exception {
		HiddenNode node = new HiddenNode();
		Document doc = XmlUtil.createDocument();
		SVGGraphics2D g2d = new SVGGraphics2D(doc);
		eval(new DrawMan(null).graphicalContent(node)).draw(g2d, Decorator.Empty.INSTANCE);
		assertFalse(node.hasBeenDrawn);
	}
	
	@Test
	public void bezierHideUnhideControlPointsTest() throws Exception {
		STG mat = new STG(new HistoryPreservingStorageManager());
		VisualSTG visual = new VisualSTG(mat);
		DummyTransition t1 = new DummyTransition(new DefaultStorageManager());
		STGPlace p1 = new STGPlace(new DefaultStorageManager());
		mat.add(t1);
		mat.add(p1);
		VisualDummyTransition vt1 = new VisualDummyTransition(t1, new DefaultStorageManager());
		VisualPlace vp1 = new VisualPlace(p1, new DefaultStorageManager());
		visual.add(vt1);
		visual.add(vp1);
		VisualConnection vConn = visual.createConnection(vt1, vp1);
		vConn.setConnectionType(ConnectionType.BEZIER);
		Bezier graphic = (Bezier)vConn.getGraphic();
		visual.ensureConsistency();
		BezierControlPoint[] cpoints = eval(graphic.getControlPoints());
		Assert.assertEquals(2, cpoints.length);
		Assert.assertTrue(eval(cpoints[0].hidden()));
		Assert.assertTrue(eval(cpoints[1].hidden()));
		visual.select(cpoints[0]);
		Assert.assertFalse(eval(cpoints[0].hidden()));
		Assert.assertFalse(eval(cpoints[1].hidden()));
		visual.selectNone();
		Assert.assertTrue(eval(cpoints[0].hidden()));
		Assert.assertTrue(eval(cpoints[1].hidden()));
		visual.select(vConn);
		Assert.assertFalse(eval(cpoints[0].hidden()));
		Assert.assertFalse(eval(cpoints[1].hidden()));
	}
	
	@Test
	public void toggleStgLoadTest() throws Exception {
		Framework fw = new Framework();
		fw.initPlugins();
		InputStream workStream = ClassLoader.getSystemResourceAsStream("toggle.work");
		workStream.available();
		fw.load(workStream);
		workStream.close();
	}
}

