package org.workcraft.plugins.cpog;

import static java.util.Arrays.*;
import static org.workcraft.dependencymanager.advanced.core.Expressions.*;
import static org.workcraft.dependencymanager.advanced.core.GlobalCache.*;
import static org.workcraft.gui.graph.tools.GraphEditorToolUtil.*;
import static org.workcraft.util.Maybe.Util.*;

import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;

import javax.swing.Icon;

import org.workcraft.dependencymanager.advanced.core.Combinator;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.Expressions;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dom.visual.DrawMan;
import org.workcraft.dom.visual.GraphicalContent;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.exceptions.NotImplementedException;
import org.workcraft.gui.graph.Viewport;
import org.workcraft.gui.graph.tools.AbstractTool;
import org.workcraft.gui.graph.tools.ConnectionManager;
import org.workcraft.gui.graph.tools.ConnectionTool;
import org.workcraft.gui.graph.tools.DragHandler;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.graph.tools.GraphEditorMouseListener;
import org.workcraft.gui.graph.tools.GraphEditorTool;
import org.workcraft.gui.graph.tools.GraphEditorTool.Identification;
import org.workcraft.gui.graph.tools.HitTester;
import org.workcraft.gui.graph.tools.MovableController;
import org.workcraft.gui.graph.tools.NodeGenerator;
import org.workcraft.gui.graph.tools.NodeGeneratorTool;
import org.workcraft.gui.graph.tools.selection.GenericSelectionTool;
import org.workcraft.gui.graph.tools.selection.MoveDragHandler;
import org.workcraft.util.Function;
import org.workcraft.util.Function0;
import org.workcraft.util.GUI;
import org.workcraft.util.Maybe;

import pcollections.HashTreePSet;
import pcollections.PSet;

public class CustomToolsProvider {
	
	public CustomToolsProvider(CPOG cpog) {
		this.cpog = cpog;
	}
	
	Identification createIdentification(final String label, final String svgIconPath, final int hotKeyCode) {
		return new Identification() {
			@Override
			public String getLabel() {
				return label;
			}

			@Override
			public Icon getIcon() {
				return GUI.createIconFromSVG(svgIconPath);
			}

			@Override
			public int getHotKeyCode() {
				return hotKeyCode;
			}
		};
	}
	
	public final CPOG cpog;

	private final class VertexGenerator implements NodeGenerator {
		Identification identification = createIdentification("Vertex", "images/icons/svg/vertex.svg", KeyEvent.VK_V);

		@Override
		public Identification getIdentification() {
			return identification;
		}

		@Override
		public void generate(Point2D where) throws NodeCreationException {
			Vertex vertex = cpog.createVertex();
			vertex.visualInfo.position.setValue(where);
		}
	}

	private final class VariableGenerator implements NodeGenerator {
		Identification identification = createIdentification("Variable", "images/icons/svg/variable.svg", KeyEvent.VK_X);

		@Override
		public Identification getIdentification() {
			return identification;
		}

		@Override
		public void generate(Point2D where) throws NodeCreationException {
			Variable variable = cpog.createVariable();
			variable.visualVar.position.setValue(where);
		}
	}

	private final class RhoClauseGenerator implements NodeGenerator {
		Identification identification = createIdentification("RhoClause", "images/icons/svg/rho.svg", KeyEvent.VK_R);

		@Override
		public Identification getIdentification() {
			return identification;
		}

		@Override
		public void generate(Point2D where) throws NodeCreationException {
			RhoClause rhoClause = cpog.createRhoClause();
			rhoClause.visualInfo.position.setValue(where);
		}
	}

	public Iterable<GraphEditorTool> getTools(final GraphEditor editor)
	{
//		final Func<Colorisator, Expression<? extends GraphicalContent>> colorisablePainter = reflectivePainterProvider(tp , cpog);
//		final Expression<? extends GraphicalContent> simplePainter = colorisablePainter.eval(Colorisator.EMPTY);

		final ModifiableExpression<PSet<Node>> selection = cpog.storage.<PSet<Node>>create(HashTreePSet.<Node>empty());
		Function0<? extends Iterable<? extends Node>> nodesExtractor = asFunction(cpog.nodes());
		final Function<Point2D, Point2D> snap = new Function<Point2D, Point2D>() {
			@Override
			public Point2D apply(Point2D argument) {
				return editor.snap(argument);
			}
		};

		//		componentMovableController :: Component -> ModifiableExpression Point2D
		//		componentMovableController component = case component of
		//				(RhoClause rho) -> position (visualInfo rho)
		//				(Variable v) -> position (visualVar v)
		//				(Vertex v) -> position (visualInfo v)
		final Function<Component, ModifiableExpression<Point2D>> componentMovableController = new Function<Component, ModifiableExpression<Point2D>>(){
			@Override
			public ModifiableExpression<Point2D> apply(Component component) {
				return component.accept(new ComponentVisitor<ModifiableExpression<Point2D>>() {
					@Override
					public ModifiableExpression<Point2D> visitRho(RhoClause rho) {
						return rho.visualInfo.position;
					}

					@Override
					public ModifiableExpression<Point2D> visitVariable(Variable variable) {
						return variable.visualVar.position;
					}

					@Override
					public ModifiableExpression<Point2D> visitVertex(Vertex vertex) {
						return vertex.visualInfo.position;
					}
				});
			}
		};
		
		//		movableController :: Node -> Maybe (ModifiableExpression Point2D)
		//		movableController node = case node of
		//			Arc _ -> Nothing
		//			Component c -> Just $ componentMovableController c
		final MovableController<Node> movableController = new MovableController<Node>(){
			@Override
			public Maybe<? extends ModifiableExpression<Point2D>> apply(Node node) {
				return node.accept(new NodeVisitor<Maybe<? extends ModifiableExpression<Point2D>>>() {
					@Override
					public Maybe<? extends ModifiableExpression<Point2D>> visitArc(Arc arc) {
						return nothing();
					}

					@Override
					public Maybe<? extends ModifiableExpression<Point2D>> visitComponent(Component component) {
						return just(componentMovableController.apply(component));
					}
				});
			}
		};

		Function<Node, Expression<Touchable>> touchableProvider = AsTouchable.instance(movableController);
		final HitTester<? extends Node> selectionHitTester = new HitMan.Flat<Node>(nodesExtractor, eval(joinFunction(touchableProvider))).getHitTester();
		final Function<? super Point2D, ? extends Component> connectionHitTester = new Function<Point2D, Component>(){
			@Override
			public Component apply(Point2D point) {
				Node node = selectionHitTester.hitTest(point);
				if(node == null)
					return null;
				return node.accept(new NodeVisitor<Component>() {

					@Override
					public Component visitArc(Arc arc) {
						return null;
					}

					@Override
					public Component visitComponent(Component component) {
						return component;
					}
				});
			}
		};
		
		final DragHandler<Node> dragHandler = new MoveDragHandler<Node>(selection, movableController, snap);
		final Function<Node, ? extends Expression<? extends GraphicalContent>> nodePainter = new Function<Node, Expression<? extends GraphicalContent>>(){
			@Override
			public Expression<? extends GraphicalContent> apply(Node node) {
				return node.accept(new NodeVisitor<Expression<? extends GraphicalContent>>() {

					@Override
					public Expression<? extends GraphicalContent> visitArc(Arc arc) {
						NotImplementedException.warn("need to draw the arc!");
						return constant(GraphicalContent.EMPTY);
					}

					@Override
					public Expression<? extends GraphicalContent> visitComponent(Component component) {
						NotImplementedException.warn("need to draw the component!");
						return constant(GraphicalContent.EMPTY);
					}
				});
			}
		};
		
		final Expression<? extends GraphicalContent> painter = Expressions.<Iterable<? extends Node>, GraphicalContent>bind(cpog.nodes(), new Combinator<Iterable<? extends Node>, GraphicalContent>() {
			@Override
			public Expression<? extends GraphicalContent> apply(Iterable<? extends Node> nodes) {
				return DrawMan.drawCollection(nodes, nodePainter);
			}
		});
		final ConnectionManager<? super Component> connectionManager = ConnectionManager.Util.fromSafe(new CpogConnectionManager(cpog));
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
			public Identification getIdentification() {
				return org.workcraft.gui.graph.tools.selection.SelectionTool.identification;
			}
		};
		
		return asList(
				attachPainter(selectionTool, painter),
				attachPainter(new ConnectionTool<Component>(centerProvider, connectionManager, connectionHitTester), painter),
				attachPainter(new NodeGeneratorTool(new VertexGenerator(), snap), painter),
				attachPainter(new NodeGeneratorTool(new VariableGenerator(), snap), painter),
				attachPainter(new NodeGeneratorTool(new RhoClauseGenerator(), snap), painter));
	}
}
