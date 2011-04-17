package org.workcraft.plugins.cpog;

import static java.util.Arrays.*;
import static org.workcraft.dependencymanager.advanced.core.Expressions.*;
import static org.workcraft.dependencymanager.advanced.core.GlobalCache.*;
import static org.workcraft.gui.graph.tools.GraphEditorToolUtil.*;
import static org.workcraft.plugins.cpog.gui.MovableController.*;
import static org.workcraft.plugins.cpog.gui.TouchableProvider.*;
import static org.workcraft.util.Maybe.Util.*;

import java.awt.Color;
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
import org.workcraft.exceptions.NotImplementedException;
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
import org.workcraft.util.Maybe;

import pcollections.HashTreePSet;
import pcollections.PSet;

import static org.workcraft.dom.visual.ColorisableGraphicalContent.Util.*;

public class CustomToolsProvider {
	
	public CustomToolsProvider(CPOG cpog) {
		this.cpog = cpog;
	}
	
	public final CPOG cpog;
	
	static <N> HitTester<N> createHitTester
		( Expression<? extends Iterable<? extends N>> nodesGetter
		, Function<? super N, ? extends Expression<? extends Point2D>> transform
		, Function<? super N, ? extends Expression<? extends Touchable>> touchableProvider) {

		final Function<N, Touchable> transformedTouchableProvider = eval(transform(touchableProvider, transform));
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
	
	public Iterable<GraphEditorTool> getTools(final GraphEditor editor)
	{
		final ModifiableExpression<PSet<Node>> selection = cpog.storage.<PSet<Node>>create(HashTreePSet.<Node>empty());
		final Generators generators = Generators.createFor(cpog);
		
		final Function<? super Node, ? extends Expression<? extends Point2D>> transformer = withDefault(constant(new Point2D.Double(0, 0)), movableController);

		final HitTester<Node> selectionHitTester = createHitTester(cpog.nodes(), transformer, nodeLocalTouchable);
		final HitTester<Component> connectionHitTester = createHitTester(cpog.components(), componentMovableController, componentLocalTouchable);
		
		final DragHandler<Node> dragHandler = new MoveDragHandler<Node>(selection, movableController, editor.snapFunction());

		
		final Function<Node, ? extends Expression<? extends ColorisableGraphicalContent>> nodePainter = new Function<Node, Expression<? extends ColorisableGraphicalContent>>(){
			@Override
			public Expression<? extends ColorisableGraphicalContent> apply(Node node) {
				return node.accept(new NodeVisitor<Expression<? extends ColorisableGraphicalContent>>() {

					@Override
					public Expression<? extends ColorisableGraphicalContent> visitArc(Arc arc) {
						NotImplementedException.warn("need to draw the arc!");
						return constant(ColorisableGraphicalContent.EMPTY);
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
		
		/** 
		* compose :: [GraphicalContent] -> GraphicalContent
		* 
		* composeGraphics :: Expression [ GraphicalContent ] -> Expression GraphicalContent
		* composeGraphics = fmap compose
		*   
		* nodes :: Expression [Node]
		* nodePainter :: Node -> Expression ColorisableGraphicalContent
		* 
		* drawCollection :: [Node] -> Expression [ColorisableGraphicalContent]
		* drawCollection = sequence map nodePainter
		* 
		* colorise :: ColorisableGraphicalContent -> GraphicalContent
		* 
		*  
		*  drawCollection :: [Node] -> NodePainter -> Expression ColorisableGraphicalContent
		*/

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
		
		Set<Node> emptySet = java.util.Collections.emptySet();
		Expression<? extends GraphicalContent> painter = makePainter.apply(constant(emptySet));
		
		return asList(
				attachPainter(selectionTool, makePainter.apply(genericSelectionTool.effectiveSelection())),
				attachPainter(connectionTool, makePainter.apply(fmap(Collections.<Node>singleton(), connectionTool.mouseOverNode()))),
				attachPainter(new NodeGeneratorTool(generators.vertexGenerator, editor.snapFunction()), painter),
				attachPainter(new NodeGeneratorTool(generators.variableGenerator, editor.snapFunction()), painter),
				attachPainter(new NodeGeneratorTool(generators.rhoClauseGenerator, editor.snapFunction()), painter));
	}
}