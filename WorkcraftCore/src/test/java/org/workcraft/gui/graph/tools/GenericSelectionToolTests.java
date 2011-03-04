package org.workcraft.gui.graph.tools;

import java.awt.geom.Point2D;

import org.junit.Test;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.Variable;
import org.workcraft.gui.graph.tools.GenericSelectionToolTests.Dummy;
import org.workcraft.gui.graph.tools.selection.GenericSelectionTool;
import org.workcraft.util.Function;

import pcollections.HashTreePSet;
import pcollections.PCollection;
import pcollections.PSet;

public class GenericSelectionToolTests {
	
	@Test
	public void test1(){
		Function<Point2D, Point2D> snap = new Function<Point2D, Point2D>(){
			@Override
			public Point2D apply(Point2D argument) {
				return argument;
			}
		};
		MovableController<Dummy> movableController = new MovableController<Dummy>(){
			@Override
			public ModifiableExpression<Point2D> position(Dummy node) {
				return node.coordinate;
			}
		};
		final Dummy obj = new Dummy();
		ModifiableExpression<PSet<Dummy>> selection = Variable.<PSet<Dummy>>create(HashTreePSet.<Dummy>empty());
		HitTester<? extends Dummy> hitTester = new HitTester<Dummy>() {

			@Override
			public Dummy hitTest(Point2D point) {
				return obj;
			}

			@Override
			public PCollection<Dummy> boxHitTest(Point2D boxStart, Point2D boxEnd) {
				return null;
			}
		};
		new GenericSelectionTool<Dummy>(selection, hitTester, movableController , snap);
	}
}
