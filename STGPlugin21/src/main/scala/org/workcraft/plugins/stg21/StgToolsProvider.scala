package org.workcraft.plugins.stg21
import org.workcraft.gui.graph.tools.CustomToolsProvider
import org.workcraft.gui.graph.tools.NodeGenerator
import org.workcraft.util.GUI
import java.awt.geom.Point2D
import org.workcraft.exceptions.NodeCreationException
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression
import org.workcraft.dependencymanager.advanced.user.StorageManager

class StgToolsProvider extends CustomToolsProvider {

/*	private final VisualSTG visualStg;
	private final StgEditorState editorState;*/
  
  val sm : StorageManager

  def createPlace(where : Point2D) : State[VisualStg, Id[Place]] = {
    val p = Place[ModifiableExpression](sm.create(0))
    val pr = sm.create(p)
    visualStg.stg.places.modify(places => p :: places)
    visualStg.visual += ()
  }
  
	private final class PlaceGenerator extends NodeGenerator {
		override lazy val getIcon = GUI.createIconFromSVG("images/icons/svg/place.svg");
		override val getLabel = "Place"
		
		@throws(classOf[NodeCreationException])
		override def generate(where : Point2D) {
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
		return Expressions.fmap(new Function<ReferenceManager, String>() {
			@Override
			public String apply(ReferenceManager refMan) {
				return refMan.getNodeReference(vst.getReferencedTransition());
			}
		}, stg.referenceManager());
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
	}
}
