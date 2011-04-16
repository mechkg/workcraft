package org.workcraft.gui.graph.tools;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.*;

import java.awt.geom.Point2D;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.util.Function;
import org.workcraft.util.Maybe;

import pcollections.PCollection;

public interface HitTester<N> {
	N hitTest(Point2D point);
	PCollection<N> boxHitTest(Point2D boxStart, Point2D boxEnd);
	
	public class Util {
		public static HitTester<VisualNode> reflectiveHitTestForSelection(final VisualModel model, final Function<? super Node, ? extends Maybe<? extends Touchable>> tp) {
			return new HitTester<VisualNode>() {
				@Override
				public VisualNode hitTest(Point2D point) {
					return HitMan.hitTestForSelection(tp, point, (VisualNode)model.getRoot(), VisualNode.class);
				}

				@Override
				public PCollection<VisualNode> boxHitTest(Point2D boxStart, Point2D boxEnd) {
					return (PCollection<VisualNode>)(PCollection<?>)HitMan.boxHitTest(tp, eval(model.getRoot().children()), boxStart, boxEnd);
				}
			};
		}
	}
}
