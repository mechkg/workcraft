package org.workcraft.relational.petrinet.model;

import static org.workcraft.dependencymanager.advanced.core.Expressions.constant;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
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
import org.workcraft.dom.visual.DeprecatedGraphicalContent;
import org.workcraft.dom.visual.MovableNew;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.TransformHelper;
import org.workcraft.gui.Coloriser;
import org.workcraft.plugins.shared.CommonVisualSettings;
import org.workcraft.util.ExpressionUtil;

import pcollections.TreePVector;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.*;

public class VisualPlaceNode implements Node, DrawableNew, MovableNew {

	private final ModifiableExpression<AffineTransform> transform;
	private final Expression<Integer> tokenCount;
	private final Expression<Node> parent;
	private final Expression<? extends Touchable> localTouchable;
	private final ModifiableExpression<Color> tokenColor;

	public VisualPlaceNode(ModifiableExpression<AffineTransform> transform, Expression<Integer> tokenCount, Expression<Node> parent, ModifiableExpression<Color> tokenColor) {
		this.transform = transform;
		this.tokenCount = tokenCount;
		this.parent = parent;
		this.tokenColor = tokenColor;
		this.localTouchable = constant(new Touchable(){

			@Override
			public boolean hitTest(Point2D point) {
				double size = CommonVisualSettings.getSize();
				return point.distanceSq(0, 0) < size*size/4;
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
	
	Integer getTokens() {
		return eval(tokenCount);
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
	
	public static void drawTokens(int tokens, double singleTokenSize, double multipleTokenSeparation, 
			double diameter, double borderWidth, Color tokenColor,	Graphics2D g) {
		Shape shape;
		if (tokens == 1)
		{
			shape = new Ellipse2D.Double(
					-singleTokenSize / 2,
					-singleTokenSize / 2,
					singleTokenSize,
					singleTokenSize);

			g.setColor(tokenColor);
			g.fill(shape);
		}
		else
			if (tokens > 1 && tokens < 8)
			{
				double al = Math.PI / tokens;
				if (tokens == 7) al = Math.PI / 6;

				double r = (diameter / 2 - borderWidth - multipleTokenSeparation) / (1 + 1 / Math.sin(al));
				double R = r / Math.sin(al);

				r -= multipleTokenSeparation;

				for(int i = 0; i < tokens; i++)
				{
					if (i == 6)
						shape = new Ellipse2D.Double( -r, -r, r * 2, r * 2);
					else
						shape = new Ellipse2D.Double(
								-R * Math.sin(i * al * 2) - r,
								-R * Math.cos(i * al * 2) - r,
								r * 2,
								r * 2);

					g.setColor(tokenColor);
					g.fill(shape);
				}
			}
			else if (tokens > 7)
			{
				String out = Integer.toString(tokens);
				Font superFont = g.getFont().deriveFont((float)CommonVisualSettings.getSize()/2);

				Rectangle2D rect = superFont.getStringBounds(out, g.getFontRenderContext());
				g.setFont(superFont);
				g.setColor(tokenColor);
				g.drawString(Integer.toString(tokens), (float)(-rect.getCenterX()), (float)(-rect.getCenterY()));
			}
	}
	
	@Override
	public Expression<? extends DeprecatedGraphicalContent> graphicalContent() {
		return new ExpressionBase<DeprecatedGraphicalContent>(){
			@Override
			protected DeprecatedGraphicalContent evaluate(final EvaluationContext context) {
				return new DeprecatedGraphicalContent() {
					public void draw(DrawRequest r) {
						Graphics2D g = r.getGraphics();
						
						double size = CommonVisualSettings.getSize();
						double strokeWidth = CommonVisualSettings.getStrokeWidth();
						
						Shape shape = new Ellipse2D.Double(
								-size / 2 + strokeWidth / 2,
								-size / 2 + strokeWidth / 2,
								size - strokeWidth,
								size - strokeWidth);
	
						g.setColor(Coloriser.colorise(Color.WHITE, r.getDecoration().getColorisation()));
						g.fill(shape);
						g.setColor(Coloriser.colorise(Color.BLACK, r.getDecoration().getColorisation()));
						g.setStroke(new BasicStroke((float)strokeWidth));
						g.draw(shape);
	
						drawTokens(context.resolve(tokenCount), singleTokenSize, multipleTokenSeparation, size, strokeWidth, Coloriser.colorise(context.resolve(tokenColor), r.getDecoration().getColorisation()), g);
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
