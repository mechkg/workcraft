package org.workcraft.plugins.stg;

import static java.util.Arrays.*;
import static org.workcraft.dependencymanager.advanced.core.Expressions.*;
import static org.workcraft.dependencymanager.advanced.core.GlobalCache.*;
import static org.workcraft.dom.visual.ColorisableGraphicalContent.Util.*;
import static org.workcraft.gui.graph.tools.GraphEditorToolUtil.*;

import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;

import javax.swing.Icon;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.Expressions;
import org.workcraft.dom.Node;
import org.workcraft.dom.references.ReferenceManager;
import org.workcraft.dom.visual.ColorisableGraphicalContent;
import org.workcraft.dom.visual.DrawMan;
import org.workcraft.dom.visual.GraphicalContent;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.TouchableProvider;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.gui.DefaultReflectiveModelPainter;
import org.workcraft.gui.graph.tools.Colorisator;
import org.workcraft.gui.graph.tools.ConnectionManager;
import org.workcraft.gui.graph.tools.ConnectionTool;
import org.workcraft.gui.graph.tools.CustomToolsProvider;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.graph.tools.GraphEditorTool;
import org.workcraft.gui.graph.tools.HitTester;
import org.workcraft.gui.graph.tools.NodeGenerator;
import org.workcraft.gui.graph.tools.NodeGeneratorTool;
import org.workcraft.gui.graph.tools.NodePainter;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.tools.STGSimulationTool;
import org.workcraft.util.Func;
import org.workcraft.util.Function;
import org.workcraft.util.GUI;
import org.workcraft.util.Maybe;

import pcollections.PCollection;

public class STGToolsProvider implements CustomToolsProvider {

	private final VisualSTG visualStg;
	private final StgEditorState editorState;

	private final class PlaceGenerator implements NodeGenerator {
		Icon icon = GUI.createIconFromSVG("images/icons/svg/place.svg");

		@Override
		public Icon getIcon() {
			return icon;
		}

		@Override
		public String getLabel() {
			return "Place";
		}

		@Override
		public void generate(Point2D where) throws NodeCreationException {
			VisualPlace place = visualStg.createPlace();
			place.position().setValue(where);
		}

		@Override
		public int getHotKeyCode() {
			return KeyEvent.VK_P;
		}
	}

	private final class SignalTransitionGenerator implements NodeGenerator {
		Icon icon = GUI.createIconFromSVG("images/icons/svg/signal-transition.svg");

		@Override
		public Icon getIcon() {
			return icon;
		}

		@Override
		public String getLabel() {
			return "Signal Transition";
		}

		@Override
		public void generate(Point2D where) throws NodeCreationException {
			VisualSignalTransition transition = visualStg.createSignalTransition();
			transition.position().setValue(where);
		}

		@Override
		public int getHotKeyCode() {
			return KeyEvent.VK_T;
		}
	}
	
	private final class DummyTransitionGenerator implements NodeGenerator {
		Icon icon = GUI.createIconFromSVG("images/icons/svg/transition.svg");

		@Override
		public Icon getIcon() {
			return icon;
		}

		@Override
		public String getLabel() {
			return "Dummy Transition";
		}

		@Override
		public void generate(Point2D where) throws NodeCreationException {
			VisualDummyTransition transition = visualStg.createDummyTransition();
			transition.position().setValue(where);
		}

		@Override
		public int getHotKeyCode() {
			return KeyEvent.VK_D;
		}
	}
	

	public STGToolsProvider(VisualSTG visualStg, StgEditorState editorState) {
		this.visualStg = visualStg;
		this.editorState = editorState;
	}

	private Expression<? extends String> transitionName(final STG stg, final VisualSignalTransition vst) {
		return Expressions.bindFunc(stg.referenceManager(), new Function<ReferenceManager, String>() {
			@Override
			public String apply(ReferenceManager refMan) {
				return refMan.getNodeReference(vst.getReferencedTransition());
			}
		});
	}
	
	@Override
	public Iterable<GraphEditorTool> getTools(final GraphEditor editor) {

		final STG stg = visualStg.stg;
		
		TouchableProvider<Node> localTP = new TouchableProvider<Node>(){
			@Override
			public Expression<? extends Maybe<? extends Touchable>> apply(Node node) {
				final Expression<Touchable> res;
				if(node instanceof VisualSignalTransition) {
					final VisualSignalTransition vst = (VisualSignalTransition)node;
					res = vst.localSpaceTouchable(transitionName(stg, vst));
				} else 
					if(node instanceof VisualDummyTransition) {
						final VisualDummyTransition vdt = (VisualDummyTransition)node;
						res = vdt.shape(stg.name(vdt.getReferencedTransition()));
					} else
						return LOCAL_REFLECTIVE.apply(node);
				return bindFunc(res, Maybe.Util.<Touchable>just());
			}
		};
		
		final TouchableProvider<Node> tp = TouchableProvider.Util.applyTransformAndAddVisualGroupsAndConnections(localTP);

		final Func<Colorisator, Expression<? extends GraphicalContent>> painterProvider = new Func<Colorisator, Expression<? extends GraphicalContent>>() {
			@Override
			public Expression<? extends GraphicalContent> eval(final Colorisator colorisator) {
				
				final NodePainter myNodePainter = new NodePainter() {
					@Override
					public Expression<? extends GraphicalContent> getGraphicalContent(Node node) {
						final Expression<? extends ColorisableGraphicalContent> colorisable;
						if(node instanceof VisualSignalTransition) {
							final VisualSignalTransition vst = (VisualSignalTransition)node;
							colorisable = vst.getGraphicalContent(transitionName(stg, vst));
						} 
						else if(node instanceof VisualDummyTransition) {
								final VisualDummyTransition vdt = (VisualDummyTransition)node;
								colorisable = vdt.graphicalContent(stg.name(vdt.getReferencedTransition()));
							}
							else if(node instanceof VisualImplicitPlaceArc) {
								colorisable = VisualImplicitPlaceArc.graphicalContent(tp, (VisualImplicitPlaceArc)node);
							}
							else
							return new DefaultReflectiveModelPainter.ReflectiveNodePainter(tp, colorisator).getGraphicalContent(node);
						return bindFunc(colorisable, colorisator.getColorisation(node), applyColourisation);
					}
				};
				
				return DrawMan.graphicalContent(visualStg.getRoot(), myNodePainter);
			}
		};
		final Expression<? extends GraphicalContent> simpleModelPainter = painterProvider.eval(Colorisator.EMPTY);
		
		Function<Node, Expression<? extends Point2D>> connectionCenterProvider = new Function<Node, Expression<? extends Point2D>>(){
			@Override
			public Expression<? extends Point2D> apply(Node argument) {
				return bindFunc(tp.apply(argument), new Function<Maybe<? extends Touchable>, Point2D>() { 
						public Point2D apply(Maybe<? extends Touchable> maybe) {
							return Maybe.Util.orElse(Maybe.Util.applyFunc(maybe, Touchable.centerGetter), new Point2D.Double(0,0));
						}
					});
			}
		};

		final Function<Node, Maybe<? extends Touchable>> unsafeWholeTp = eval(TouchableProvider.Util.asAWhole(tp));
		
		Function<Point2D, Node> connectionHitTester = new Function<Point2D, Node>(){
			@Override
			public Node apply(Point2D argument) {
				return HitMan.hitTestForConnection(unsafeWholeTp, argument, visualStg.getRoot());
			}
		};
		HitTester<VisualNode> selectionHitTester = HitTester.Util.reflectiveHitTestForSelection(visualStg, unsafeWholeTp);
		
		Function<Point2D, Point2D> snap = new Function<Point2D, Point2D>() {
			@Override
			public Point2D apply(Point2D argument) {
				return editor.snap(argument);
			}
		};	
		
		
		
		ConnectionManager<Node> connectionManager = new VisualStgConnectionManager(visualStg, visualStg.storage, tp);
		
		return asList(
				attachParameterisedPainter(new STGSelectionTool(visualStg.stg, editor, tp, selectionHitTester, editorState), painterProvider),
				attachParameterisedPainter(new ConnectionTool<Node>(connectionCenterProvider, connectionManager, connectionHitTester), painterProvider),
				attachPainter(new NodeGeneratorTool(new PlaceGenerator(), snap), simpleModelPainter),
				attachPainter(new NodeGeneratorTool(new SignalTransitionGenerator(), snap), simpleModelPainter),
				attachPainter(new NodeGeneratorTool(new DummyTransitionGenerator(), snap), simpleModelPainter),
				attachParameterisedPainter(STGSimulationTool.createSimulationTool(visualStg.getRoot(), visualStg.stg, tp), painterProvider));
	}
}
