package org.workcraft.relational.petrinet.model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.DrawableNew;
import org.workcraft.dom.visual.ColorisableGraphicalContent;
import org.workcraft.dom.visual.MovableNew;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.TransformHelper;
import org.workcraft.exceptions.NotSupportedException;
import org.workcraft.gui.Coloriser;
import org.workcraft.relational.engine.Id;

public class VisualGroupNode implements DrawableNew, MovableNew, Container{
	private final ModifiableExpression<AffineTransform> transform;
	private final Expression<? extends Collection<Node>> children;
	private final ModifiableExpression<Node> parent;
	private final Id visualGroupId;

	public Id getVisualGroupId() {
		return visualGroupId;
	}
	
	public VisualGroupNode(ModifiableExpression<AffineTransform> transform, Expression<? extends Collection<Node>> children, ModifiableExpression<Node> parent, Id visualGroupId) {
		this.transform = transform;
		this.children = children;
		this.parent = parent;
		this.visualGroupId = visualGroupId;
	}

	@Override
	public ModifiableExpression<Node> parent() {
		return parent;
	}

	@Override
	public Expression<? extends Collection<? extends Node>> children() {
		return children;
	}

	@Override
	public void add(Node node) {
		throw new NotSupportedException("re-parenting should be performed with a parent() field");
	}

	@Override
	public void add(Collection<Node> nodes) {
		throw new NotSupportedException("re-parenting should be performed with a parent() field");
	}

	@Override
	public void remove(Node node) {
		throw new NotSupportedException("re-parenting should be performed with a parent() field");
	}

	@Override
	public void remove(Collection<Node> node) {
		throw new NotSupportedException("re-parenting should be performed with a parent() field");
	}

	@Override
	public void reparent(Collection<Node> nodes) {
		throw new NotSupportedException("re-parenting should be performed with a parent() field");
	}

	@Override
	public void reparent(Collection<Node> nodes, Container newParent) {
		throw new NotSupportedException("re-parenting should be performed with a parent() field");
	}

	@Override
	public ModifiableExpression<AffineTransform> transform() {
		return transform;
	}

	@Override
	public ExpressionBase<ColorisableGraphicalContent> graphicalContent() {
		return new ExpressionBase<ColorisableGraphicalContent>() {

			@Override
			public ColorisableGraphicalContent evaluate(EvaluationContext resolver) {
				final Rectangle2D bb = null;
				final Node parent = resolver.resolve(parent());
				
				return new ColorisableGraphicalContent() {
					
					@Override
					public void draw(DrawRequest r) {
						if (bb != null && parent != null) {
							bb.setRect(bb.getX() - 0.1, bb.getY() - 0.1, bb.getWidth() + 0.2, bb.getHeight() + 0.2);
							r.getGraphics().setColor(Coloriser.colorise(Color.GRAY, r.getColorisation().getColorisation()));
							r.getGraphics().setStroke(new BasicStroke(0.02f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1.0f, new float[]{0.2f, 0.2f}, 0.0f));
							r.getGraphics().draw(bb);
						}
					}
				};
			}
		};
	}
}
