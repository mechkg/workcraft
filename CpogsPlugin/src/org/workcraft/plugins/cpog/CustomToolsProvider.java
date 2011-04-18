package org.workcraft.plugins.cpog;

import static java.util.Arrays.*;
import static org.workcraft.dependencymanager.advanced.core.Expressions.*;
import static org.workcraft.dependencymanager.advanced.core.GlobalCache.*;
import static org.workcraft.dom.visual.ColorisableGraphicalContent.Util.*;
import static org.workcraft.dom.visual.connections.ConnectionGui.*;
import static org.workcraft.gui.graph.tools.GraphEditorToolUtil.*;
import static org.workcraft.plugins.cpog.gui.MovableController.*;
import static org.workcraft.plugins.cpog.gui.TouchableProvider.*;
import static org.workcraft.util.Maybe.Util.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.util.Set;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dom.visual.BoundedColorisableGraphicalContent;
import org.workcraft.dom.visual.ColorisableGraphicalContent;
import org.workcraft.dom.visual.DrawMan;
import org.workcraft.dom.visual.GraphicalContent;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.connections.ConnectionGui;
import org.workcraft.dom.visual.connections.VisualConnectionContext;
import org.workcraft.dom.visual.connections.VisualConnectionData;
import org.workcraft.dom.visual.connections.VisualConnectionGui;
import org.workcraft.dom.visual.connections.VisualConnectionProperties;
import org.workcraft.gui.graph.Viewport;
import org.workcraft.gui.graph.tools.AbstractTool;
import org.workcraft.gui.graph.tools.Colorisation;
import org.workcraft.gui.graph.tools.ConnectionController;
import org.workcraft.gui.graph.tools.ConnectionTool;
import org.workcraft.gui.graph.tools.DragHandler;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.graph.tools.GraphEditorMouseListener;
import org.workcraft.gui.graph.tools.GraphEditorTool;
import org.workcraft.gui.graph.tools.HitTester;
import org.workcraft.gui.graph.tools.NodeGeneratorTool;
import org.workcraft.gui.graph.tools.selection.GenericSelectionTool;
import org.workcraft.gui.graph.tools.selection.MoveDragHandler;
import org.workcraft.plugins.cpog.gui.Generators;
import org.workcraft.util.Collections;
import org.workcraft.util.Function;
import org.workcraft.util.Function2;
import org.workcraft.util.Maybe;

import pcollections.HashTreePSet;
import pcollections.PSet;

import static org.workcraft.util.Function.Util.composition;

public class CustomToolsProvider {
	
	public CustomToolsProvider(CPOG cpog) {
		this.cpog = cpog;
	}
	
	public final CPOG cpog;
	
	
	static <N> HitTester<N> createHitTester
		( Expression<? extends Iterable<? extends N>> nodesGetter
		, Function<? super N, ? extends Expression<? extends Touchable>> touchableProvider) {

		final Function<N, Touchable> transformedTouchableProvider = eval(touchableProvider);
		return new HitMan.Flat<N>(asFunction(nodesGetter), transformedTouchableProvider).getHitTester();
	}
	
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
				}, transformedComponents.apply(arc.first), transformedComponents.apply(arc.second));
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
										( BoundedColorisableGraphicalContent.translate
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
				final Function<Node, Expression<? extends GraphicalContent>> colorisedPainter = new Function<Node, Expression<? extends GraphicalContent>>(){

					@Override
					public Expression<? extends GraphicalContent> apply(final Node node) {
						Expression<Colorisation> colorisation = fmap(new Function<Set<? extends Node>, Colorisation>(){

							@Override
							public Colorisation apply(Set<? extends Node> highlighted) {
								return highlighted.contains(node) ? highlightedColorisation : Colorisation.EMPTY;
							}
						}, highlighted);
						return fmap(applyColourisation, nodePainter.apply(node), colorisation);
					}
				};
				
				return bind(
						cpog.nodes()
						, new Function<Iterable<? extends Node>, Expression<? extends GraphicalContent>>() {
							@Override
							public Expression<? extends GraphicalContent> apply(Iterable<? extends Node> nodes) {
								return DrawMan.drawCollection(nodes, colorisedPainter);
							}
						}
					);
			}
		};
		
		final ConnectionController<? super Component> connectionManager = ConnectionController.Util.fromSafe(new CpogConnectionManager(cpog));
		final Function<? super Component, ? extends Expression<? extends Point2D>> centerProvider = new Function<Component, Expression<? extends Point2D>>(){
			@Override
			public Expression<? extends Point2D> apply(Component component) {
				return componentMovableController.apply(component);
			}
		};

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

		ConnectionTool<Component> connectionTool = new ConnectionTool<Component>(centerProvider, connectionManager, HitTester.Util.asPointHitTester(connectionHitTester));
		
		Set<Node> emptyNodesSet = java.util.Collections.emptySet();
		Expression<? extends GraphicalContent> painter = makePainter.apply(constant(emptyNodesSet));
		
		return asList(
				attachPainter(selectionTool, makePainter.apply(genericSelectionTool.effectiveSelection())),
				attachPainter(connectionTool, makePainter.apply(fmap(Collections.<Node>singleton(), connectionTool.mouseOverNode()))),
				attachPainter(new NodeGeneratorTool(generators.vertexGenerator, editor.snapFunction()), painter),
				attachPainter(new NodeGeneratorTool(generators.variableGenerator, editor.snapFunction()), painter),
				attachPainter(new NodeGeneratorTool(generators.rhoClauseGenerator, editor.snapFunction()), painter));
	}
}
