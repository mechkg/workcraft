package org.workcraft.plugins.stg21
import org.workcraft.gui.graph.tools.CustomToolsProvider
import org.workcraft.gui.graph.tools.NodeGenerator
import org.workcraft.util.GUI
import java.awt.geom.Point2D
import org.workcraft.gui.graph.tools.GraphEditor
import org.workcraft.gui.graph.tools.GraphEditorTool
import java.awt.event.KeyEvent
import org.workcraft.plugins.stg21.types.VisualStg
import scalaz.State
import org.workcraft.gui.modeleditor.tools.NodeGeneratorTool
import org.workcraft.scala.Util._
import org.workcraft.scala.Expressions._
import org.workcraft.gui.graph.tools.GraphEditorToolUtil
import org.workcraft.graphics.GraphicalContent

case class StgToolsProvider(visualStg : ModifiableExpression[VisualStg]) {

/*	private final VisualSTG visualStg;
	private final StgEditorState editorState;*/

  val generators = List(
		  (StgOperations.createPlace(_ : Point2D.Double, "p"), "images/icons/svg/place.svg", "Place", KeyEvent.VK_P),
		  (StgOperations.createSignalTransition(_ : Point2D.Double), "images/icons/svg/signal-transition.svg", "Signal Transition", KeyEvent.VK_T),
		  (StgOperations.createDummyTransition(_ : Point2D.Double, "d"), "images/icons/svg/transition.svg", "Dummy Transition", KeyEvent.VK_D)
  )
  import org.workcraft.gui.modeleditor.tools._
  def mkNodeGeneratorTool(generator : (Point2D.Double => State[VisualStg, Any], java.lang.String, java.lang.String, Int), graphicalContent : Expression[GraphicalContent])
     : ModelEditorTool = {
    val (g, iconPath, name, hotkey) = generator
    NodeGeneratorTool(Button(name, iconPath, Some(hotkey)).unsafePerformIO, graphicalContent,
	  (where : Point2D.Double) => visualStg.update(g(where)~>_))
  }
  
  def nodeGeneratorTools(graphicalContent : Expression[GraphicalContent]) : List[ModelEditorTool] = generators.map(mkNodeGeneratorTool(_,graphicalContent))
  
/*  
	private Expression<? extends String> transitionName(final STG stg, final VisualSignalTransition vst) {
		return Expressions.fmap(new Function<ReferenceManager, String>() {
			@Override
			public String apply(ReferenceManager refMan) {
				return refMan.getNodeReference(vst.getReferencedTransition());
			}
		}, stg.referenceManager());
	}

		
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
