package org.workcraft.plugins.cpog;

import static org.workcraft.dependencymanager.advanced.core.Expressions.asFunction;
import static org.workcraft.dependencymanager.advanced.core.Expressions.bind;
import static org.workcraft.dependencymanager.advanced.core.Expressions.fmap;
import static org.workcraft.dependencymanager.advanced.core.GlobalCache.eval;
import static org.workcraft.dom.visual.ColorisableGraphicalContent.Util.applyColourisationFunc;

import java.util.Set;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dom.visual.ColorisableGraphicalContent;
import org.workcraft.dom.visual.DrawMan;
import org.workcraft.dom.visual.GraphicalContent;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.gui.graph.tools.Colorisation;
import org.workcraft.gui.graph.tools.HitTester;
import org.workcraft.util.Function;

public class CustomToolsProvider {
	
	public static <N> HitTester<N> createHitTester
		( Expression<? extends Iterable<? extends N>> nodesGetter
		, Function<? super N, ? extends Expression<? extends Touchable>> touchableProvider) 
			{ final Function<N, Touchable> transformedTouchableProvider = eval(touchableProvider)
			; return new HitMan.Flat<N>(asFunction(nodesGetter), transformedTouchableProvider).getHitTester()
			; }


	public static <N> Expression<GraphicalContent> drawWithHighlight(final Colorisation highlightedColorisation, final Expression<? extends Set<? extends N>> highlighted, final Function<N, ? extends Expression<? extends ColorisableGraphicalContent>> painter, Expression<? extends Iterable<? extends N>> nodes) {
		final Function<N, Expression<? extends GraphicalContent>> colorisedPainter = new Function<N, Expression<? extends GraphicalContent>>(){
			@Override
			public Expression<? extends GraphicalContent> apply(final N node) {
				Expression<Colorisation> colorisation = fmap(new Function<Set<? extends N>, Colorisation>(){

					@Override
					public Colorisation apply(Set<? extends N> highlighted) {
						return highlighted.contains(node) ? highlightedColorisation : Colorisation.EMPTY;
					}
				}, highlighted);
				return fmap(applyColourisationFunc, painter.apply(node), colorisation);
			}
		};
		
		return bind(
				nodes
				, new Function<Iterable<? extends N>, Expression<? extends GraphicalContent>>() {
					@Override
					public Expression<? extends GraphicalContent> apply(Iterable<? extends N> nodes) {
						return DrawMan.drawCollection(nodes, colorisedPainter);
					}
				}
			);
	}
}
	
		
	/*	
	public CustomToolsProvider(CPOG cpog) {
		this.cpog = cpog;
	}
	
	public final CPOG cpog;
	
	
	static <V> Function<Node, V> withDefault(final V def, final Function<? super Node, ? extends Maybe<? extends V>> f) {
		return new Function<Node, V>(){
			@Override
			public V apply(Node argument) {
				return orElse(f.apply(argument), def);
			}
		};
	}

	static final Function<Arc, Expression<ConnectionGui>> arcToGui(final Function<Component, ? extends Expression<? extends Touchable>> transformedComponents) {
		return new Function<Arc, Expression<ConnectionGui>>(){
			@Override
			public Expression<ConnectionGui> apply(Arc arc) {
				return arcToGui(transformedComponents, arc);
			}
		};
	}
	
	public static Expression<ConnectionGui> arcToGui(final Function<? super Component, ? extends Expression<? extends Touchable>> transformedComponents, final Arc arc) {
		return bind(arc.visual, new Function<VisualConnectionData, Expression<ConnectionGui>>(){
			@Override
			public Expression<ConnectionGui> apply(final VisualConnectionData data) {
				
				final VisualConnectionProperties properties = new VisualConnectionProperties(){

					@Override
					public Color getDrawColor() {
						return Color.green;
					}

					@Override
					public double getArrowWidth() {
						return 0.1;
					}

					@Override
					public double getArrowLength() {
						return 0.2;
					}

					@Override
					public boolean hasArrow() {
						return true;
					}

					@Override
					public Stroke getStroke() {
						return new BasicStroke(0.05f);
					}
				};
				
				return bind(VisualConnectionData.Util.getStatic(data), new Function<StaticVisualConnectionData, Expression<ConnectionGui>>(){
					public Expression<ConnectionGui> apply(final StaticVisualConnectionData data) {
						
						return fmap(new Function2<Touchable, Touchable, ConnectionGui>(){
							@Override
							public ConnectionGui apply(final Touchable component1, final Touchable component2) {
								final VisualConnectionContext context = new VisualConnectionContext(){
									@Override
									public Touchable component1() {
										return component1;
									}
		
									@Override
									public Touchable component2() {
										return component2;
									}
								};
								ConnectionGui connectionGui = VisualConnectionGui.getConnectionGui(properties, context, data);
								return connectionGui;
							}
						}, transformedComponents.apply(arc.first()), transformedComponents.apply(arc.second()));
					}
				});
			}
		});
	}

	public Iterable<GraphEditorTool> getTools(final GraphEditor editor)
	{
		
		final ModifiableExpression<PSet<Node>> selection = cpog.storage.<PSet<Node>>create(HashTreePSet.<Node>empty());
		final Generators generators = Generators.createFor(cpog);
		
		final Function<? super Node, ? extends Expression<? extends Point2D>> transformer = withDefault(constant(new Point2D.Double(0, 0)), movableController);

		final Function<Component, Expression<Touchable>> transformedComponentTouchableProvider = transform(componentLocalTouchable, transformer);

		final Function<Arc, Expression<ConnectionGui>> arcToGui = arcToGui(transformedComponentTouchableProvider);
		Function<Node, Expression<Touchable>> nodeTouchableProvider 
			= NodeVisitor.create
				( composition(arcToGui, fmap(getTouchable))
				, transformedComponentTouchableProvider);
		
		final HitTester<Node> selectionHitTester = createHitTester(cpog.nodes(), nodeTouchableProvider);
		final HitTester<Component> connectionHitTester = createHitTester(cpog.components(), transformedComponentTouchableProvider);
		
		
		final DragHandler<Node> dragHandler = new MoveDragHandler<Node>(selection, movableController, editor.snapFunction());
		
		final Function<Node, ? extends Expression<? extends ColorisableGraphicalContent>> nodePainter = new Function<Node, Expression<? extends ColorisableGraphicalContent>>(){
			@Override
			public Expression<? extends ColorisableGraphicalContent> apply(Node node) {
				return node.accept(new NodeVisitor<Expression<? extends ColorisableGraphicalContent>>() {

					@Override
					public Expression<? extends ColorisableGraphicalContent> visitArc(Arc arc) {
						return fmap(getGraphicalContent, arcToGui.apply(arc));
					}

					@Override
					public Expression<? extends ColorisableGraphicalContent> visitComponent(Component component) {
						Expression<? extends BoundedColorisableGraphicalContent> bcgc = component.accept(new ComponentVisitor<Expression<? extends BoundedColorisableGraphicalContent>>() {

							@Override
							public Expression<? extends BoundedColorisableGraphicalContent> visitRho(RhoClause rho) {
								return VisualRhoClause.getVisualRhoClause(rho);
							}

							@Override
							public Expression<? extends BoundedColorisableGraphicalContent> visitVariable(Variable variable) {
								return VisualVariableGui.getImage(variable);
							}

							@Override
							public Expression<? extends BoundedColorisableGraphicalContent> visitVertex(Vertex vertex) {
								return VisualVertex.getImage(vertex);
							}
						});
						
						return  fmap 
								( BoundedColorisableGraphicalContent.getGraphics
								, fmap
										( BoundedColorisableGraphicalContent.translateQweqwe
										, bcgc
										, componentMovableController.apply(component)
										)
								);
					}
				});
			}
		};
		
		final Colorisation highlightedColorisation = new Colorisation() {
			@Override
			public Color getColorisation() {
				return new Color(99, 130, 191).brighter();
			}
			
			@Override
			public Color getBackground() {
				return null;
			}
		};

		Function<Expression<? extends Set<? extends Node>>, Expression<GraphicalContent>> makePainter = new Function<Expression<? extends Set<? extends Node>>, Expression<GraphicalContent>>(){
			@Override
			public Expression<GraphicalContent> apply(final Expression<? extends Set<? extends Node>> highlighted) {
				return drawWithHighlight(highlightedColorisation, highlighted, nodePainter, cpog.nodes());
			}
		};
		
		final ConnectionController<? super Component> connectionManager = ConnectionController.Util.fromSafe(new CpogConnectionManager(cpog));

		final GenericSelectionTool<Node> genericSelectionTool = new GenericSelectionTool<Node>(selection, selectionHitTester, dragHandler);
		GraphEditorTool selectionTool = new AbstractTool() {
			@Override
			public GraphEditorMouseListener mouseListener() {
				return genericSelectionTool.getMouseListener();
			}
			
			@Override
			public Expression<? extends GraphicalContent> userSpaceContent(Viewport viewport, Expression<Boolean> hasFocus) {
				return genericSelectionTool.userSpaceContent(viewport);
			}
			
			@Override
			public Expression<? extends GraphicalContent> screenSpaceContent(Viewport viewport, Expression<Boolean> hasFocus) {
				return constant(GraphicalContent.EMPTY);
			}		

			@Override
			public Button getButton() {
				return org.workcraft.gui.graph.tools.selection.SelectionTool.identification;
			}
		};
		
		ConnectionTool<Component> connectionTool = new ConnectionTool<Component>(componentMovableController, connectionManager, HitTester.Util.asPointHitTester(connectionHitTester));
		
		Set<Node> emptyNodesSet = java.util.Collections.emptySet();
		Expression<? extends GraphicalContent> painter = makePainter.apply(constant(emptyNodesSet));
		
		PSet<ControlPoint> emptyControlPointSet = HashTreePSet.empty();
		ModifiableExpression<PSet<ControlPoint>> selectedControlPoints = org.workcraft.dependencymanager.advanced.user.Variable.create(emptyControlPointSet);
		Expression<? extends Iterable<? extends ControlPoint>> visibleControlPoints = bind(selection, new Function<Set<Node>, Expression<PSet<ControlPoint>>>(){

			@Override
			public Expression<PSet<ControlPoint>> apply(Set<Node> selectedNodes) {
				final PSet<ControlPoint> emptySet = HashTreePSet.empty();
				Expression<PSet<ControlPoint>> result = constant(emptySet);
				ArrayList<Expression<PSet<ControlPoint>>> controlPointLists = new ArrayList<Expression<PSet<ControlPoint>>>(); 
				for(Node node : selectedNodes) {
					controlPointLists.add(node.accept(new NodeVisitor<Expression<PSet<ControlPoint>>>(){

						@Override
						public Expression<PSet<ControlPoint>> visitArc(Arc arc) {
							return fmap(new Function<VisualConnectionData, PSet<ControlPoint>>(){
								@Override
								public PSet<ControlPoint> apply(VisualConnectionData argument) {
									return argument.accept(new ConnectionDataVisitor<PSet<ControlPoint>>() {

										@Override
										public PSet<ControlPoint> visitPolyline(PolylineData data) {
											PSet<ControlPoint> result = HashTreePSet.empty();
											for (ModifiableExpression<Point2D> pos : data.controlPoints()) {
												result = result.plus(new ControlPoint(pos));
											}
											return result;
										}

										@Override
										public PSet<ControlPoint> visitBezier(BezierData data) {
											final PSet<ControlPoint> empty = HashTreePSet.empty();
											return empty
												.plus(new ControlPoint(data.cp1()))
												.plus(new ControlPoint(data.cp2()));
										}
									});
								}
							}, arc.visual);
						}

						@Override
						public Expression<PSet<ControlPoint>> visitComponent(Component component) {
							return constant(emptySet);
						}
					}));
				}
				Function2<PSet<ControlPoint>, Set<ControlPoint>, PSet<ControlPoint>> union = new Function2<PSet<ControlPoint>, Set<ControlPoint>, PSet<ControlPoint>>() {
					@Override
					public PSet<ControlPoint> apply(PSet<ControlPoint> argument1, Set<ControlPoint> argument2) {
						return argument1.plusAll(argument2);
					}
				};
				for(Expression<PSet<ControlPoint>> cps : controlPointLists) {
					result = fmap(union, result, cps);
				}
				return result;
			}
		});
		
		MovableController<ControlPoint> controlPointMovableController = new MovableController<ControlPoint>(){
			@Override
			public Maybe<? extends ModifiableExpression<Point2D>> apply(ControlPoint argument) {
				return just(argument.position);
			}
		};
		DragHandler<ControlPoint> cpDragHandler = new MoveDragHandler<ControlPoint>(selectedControlPoints, controlPointMovableController, editor.snapFunction());

		final Function<ControlPoint, Expression<BoundedColorisableGraphicalContent>> cpGc = new Function<ControlPoint, Expression<BoundedColorisableGraphicalContent>>(){
			@Override
			public Expression<BoundedColorisableGraphicalContent> apply(ControlPoint argument) {
				
				Function<? super Point2D, ? extends BoundedColorisableGraphicalContent> drawCircle = new Function<Point2D, BoundedColorisableGraphicalContent>(){
					public BoundedColorisableGraphicalContent apply(Point2D offset) {
						 return BoundedColorisableGraphicalContent.translate(org.workcraft.plugins.cpog.scala.Graphics.boundedCircle(1, new BasicStroke(0), Color.BLUE, Color.RED), offset);						
					};
				};
				return fmap(drawCircle, argument.position);
			}
		};
		
		HitTester<? extends ControlPoint> cpHitTester = createHitTester(visibleControlPoints, new Function<ControlPoint, Expression<Touchable>>(){
			@Override
			public Expression<Touchable> apply(ControlPoint argument) {
					Expression<BoundedColorisableGraphicalContent> bgc = cpGc.apply(argument);
					Expression<Rectangle2D> bb = fmap(BoundedColorisableGraphicalContent.getBoundingBox, bgc);
					return fmap(bbToTouchable, bb);
			}
		});
		
		final GenericSelectionTool<ControlPoint> gcpet = new GenericSelectionTool<ControlPoint>(selectedControlPoints, cpHitTester, cpDragHandler);
		
		// cpPainter = fmap (fmap BoundedColorisableGraphicalContent.getGraphics) cpGc
		Function<ControlPoint, Expression<ColorisableGraphicalContent>> cpPainter = new Function<ControlPoint, Expression<ColorisableGraphicalContent>>(){
			@Override
			public Expression<ColorisableGraphicalContent> apply(ControlPoint argument) {
				return fmap(BoundedColorisableGraphicalContent.getGraphics, cpGc.apply(argument));
			}
		};
		
		Expression<GraphicalContent> controlPointGC = CustomToolsProvider.<ControlPoint>drawWithHighlight(highlightedColorisation, selectedControlPoints, cpPainter, visibleControlPoints);
		
		GraphEditorTool controlPointEditorTool = new AbstractTool() {
			
			@Override
			public GraphEditorMouseListener mouseListener() {
				return gcpet.getMouseListener();
			}
			
			@Override
			public Expression<? extends GraphicalContent> userSpaceContent(Viewport viewport, Expression<Boolean> hasFocus) {
				return gcpet.userSpaceContent(viewport);
			}
			
			@Override
			public Expression<? extends GraphicalContent> screenSpaceContent(Viewport viewport, Expression<Boolean> hasFocus) {
				return constant(GraphicalContent.EMPTY);
			}
			
			@Override
			public Button getButton() {
				return new Button() {
					
					@Override
					public String getLabel() {
						return "Control point editor";
					}
					
					@Override
					public Icon getIcon() {
						return null;
					}
					
					@Override
					public int getHotKeyCode() {
						return java.awt.event.KeyEvent.VK_Q;
					}
				};
			}
		};
		
		return asList(
				attachPainter(selectionTool, makePainter.apply(genericSelectionTool.effectiveSelection())),
				attachPainter(controlPointEditorTool,  fmap(Graphics.composeFunc, painter, controlPointGC)),
				attachPainter(connectionTool, makePainter.apply(fmap(Collections.<Node>singleton(), connectionTool.mouseOverNode()))),
				attachPainter(new NodeGeneratorTool(generators.vertexGenerator, editor.snapFunction()), painter),
				attachPainter(new NodeGeneratorTool(generators.variableGenerator, editor.snapFunction()), painter),
				attachPainter(new NodeGeneratorTool(generators.rhoClauseGenerator, editor.snapFunction()), painter));
	}
}
*/