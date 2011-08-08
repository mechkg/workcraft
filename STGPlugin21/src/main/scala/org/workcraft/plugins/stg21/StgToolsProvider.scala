package org.workcraft.plugins.stg21
import org.workcraft.gui.graph.tools.CustomToolsProvider
import org.workcraft.gui.graph.tools.NodeGenerator
import org.workcraft.util.GUI
import java.awt.geom.Point2D
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression
import org.workcraft.dependencymanager.advanced.user.StorageManager
import org.workcraft.gui.graph.tools.GraphEditor
import org.workcraft.gui.graph.tools.GraphEditorTool
import java.awt.event.KeyEvent

abstract class StgToolsProvider extends CustomToolsProvider {

/*	private final VisualSTG visualStg;
	private final StgEditorState editorState;*/

  val generators = List(
		  (StgOperations.createPlace(_ : Point2D), "images/icons/svg/place.svg", "Place", KeyEvent.VK_P),
		  (StgOperations.createSignalTransition(_ : Point2D), "images/icons/svg/signal-transition.svg", "Signal Transition", KeyEvent.VK_T),
		  (StgOperations.createDummyTransition(_ : Point2D), "images/icons/svg/transition.svg", "Dummy Transition", KeyEvent.VK_D)
  )
  
/*  
	private Expression<? extends String> transitionName(final STG stg, final VisualSignalTransition vst) {
		return Expressions.fmap(new Function<ReferenceManager, String>() {
			@Override
			public String apply(ReferenceManager refMan) {
				return refMan.getNodeReference(vst.getReferencedTransition());
			}
		}, stg.referenceManager());
	}
	

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
				return fmap(Maybe.Util.<Touchable>just(), res);
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
						return fmap(applyColourisationFunc, colorisable, colorisator.getColorisation(node));
					}
				};
				
				return DrawMan.graphicalContent(visualStg.getRoot(), myNodePainter);
			}
		};
		final Expression<? extends GraphicalContent> simpleModelPainter = painterProvider.eval(Colorisator.EMPTY);
		
		Function<Node, Expression<? extends Point2D>> connectionCenterProvider = new Function<Node, Expression<? extends Point2D>>(){
			@Override
			public Expression<? extends Point2D> apply(Node argument) {
				return fmap(new Function<Maybe<? extends Touchable>, Point2D>() { 
						public Point2D apply(Maybe<? extends Touchable> maybe) {
							return Maybe.Util.orElse(Maybe.Util.applyFunc(maybe, Touchable.centerGetter), new Point2D.Double(0,0));
						}
					}, tp.apply(argument));
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
		
		
		
		ConnectionController<Node> connectionManager = new VisualStgConnectionManager(visualStg, visualStg.storage, tp);
		
		return asList(
				attachParameterisedPainter(new STGSelectionTool(visualStg.stg, editor, tp, selectionHitTester, editorState), painterProvider),
				attachParameterisedPainter(new ConnectionTool<Node>(connectionCenterProvider, connectionManager, connectionHitTester), painterProvider),
				attachPainter(new NodeGeneratorTool(new PlaceGenerator(), snap), simpleModelPainter),
				attachPainter(new NodeGeneratorTool(new SignalTransitionGenerator(), snap), simpleModelPainter),
				attachPainter(new NodeGeneratorTool(new DummyTransitionGenerator(), snap), simpleModelPainter),
				attachParameterisedPainter(STGSimulationTool.createSimulationTool(visualStg.getRoot(), visualStg.stg, tp), painterProvider));
	}*/
}
