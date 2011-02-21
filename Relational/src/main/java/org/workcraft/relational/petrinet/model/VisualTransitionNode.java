package org.workcraft.relational.petrinet.model;

import static org.workcraft.dependencymanager.advanced.core.Expressions.constant;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.DrawableNew;
import org.workcraft.dom.visual.ColorisableGraphicalContent;
import org.workcraft.dom.visual.MovableNew;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.TransformHelper;
import org.workcraft.gui.Coloriser;
import org.workcraft.plugins.shared.CommonVisualSettings;
import org.workcraft.util.ExpressionUtil;

import pcollections.TreePVector;

public class VisualTransitionNode implements Node, DrawableNew, MovableNew {

	private final ModifiableExpression<AffineTransform> transform;
	private final Expression<Node> parent;
	private final Expression<? extends Touchable> localTouchable;

	public VisualTransitionNode(ModifiableExpression<AffineTransform> transform, Expression<Node> parent) {
		this.transform = transform;
		this.parent = parent;
		this.localTouchable = constant(new Touchable(){

			@Override
			public boolean hitTest(Point2D point) {
				return getBoundingBox().contains(point);
			}

			@Override
			public Rectangle2D getBoundingBox() {
				double size = CommonVisualSettings.getSize(); 
				return new Rectangle2D.Double(-size/2, -size/2, size, size);
			}

			@Override
			public Point2D getCenter() {
				return new Point2D.Double(getBoundingBox().getCenterX(), getBoundingBox().getCenterY());
			}
		});
	}
	
	@Override
	public Expression<? extends Touchable> shape() {
		return TransformHelper.transform(localTouchable, transform);
	}

	@Override
	public ModifiableExpression<Node> parent() {
		return ExpressionUtil.modificationNotSupported(parent);
	}

	@Override
	public Expression<? extends Collection<? extends Node>> children() {
		return constant(TreePVector.<Node>empty());
	}

	protected static double singleTokenSize = CommonVisualSettings.getSize() / 1.9;
	protected static double multipleTokenSeparation = CommonVisualSettings.getStrokeWidth() / 8;
	
	@Override
	public Expression<? extends ColorisableGraphicalContent> graphicalContent() {
		return new ExpressionBase<ColorisableGraphicalContent>(){
			@Override
			protected ColorisableGraphicalContent evaluate(final EvaluationContext context) {
				return new ColorisableGraphicalContent() {
					@Override
					public void draw(DrawRequest r) {
						Graphics2D g = r.getGraphics();
						
						double size = CommonVisualSettings.getSize();
						double strokeWidth = CommonVisualSettings.getStrokeWidth();
						
						Shape shape = new Rectangle2D.Double(
								-size / 2 + strokeWidth / 2,
								-size / 2 + strokeWidth / 2,
								size - strokeWidth,
								size - strokeWidth);
						g.setColor(Coloriser.colorise(Coloriser.colorise(Color.WHITE, r.getColorisation().getBackground()), r.getColorisation().getColorisation()));
						g.fill(shape);
						g.setColor(Coloriser.colorise(Coloriser.colorise(Color.BLACK, r.getColorisation().getBackground()), r.getColorisation().getColorisation()));
						g.setStroke(new BasicStroke((float)CommonVisualSettings.getStrokeWidth()));
						g.draw(shape);

					}
				};

			}
		};
	}

	@Override
	public ModifiableExpression<AffineTransform> transform() {
		return transform;
	}

}
