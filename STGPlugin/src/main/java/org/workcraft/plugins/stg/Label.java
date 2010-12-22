package org.workcraft.plugins.stg;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.GraphicalContent;
import org.workcraft.dom.visual.VisualComponent;

public class Label {

	public static FontRenderContext podgonFontRenderContext() {
		return VisualComponent.podgonFontRenderContext();
	}
	
	public final Expression<GraphicalContent> graphics;
	public final Expression<Rectangle2D> centeredBB;
	
	public Label(final Font font, final Expression<String> text) {
		
		final Expression<GlyphVector> glyphVector = new ExpressionBase<GlyphVector>() {
			@Override
			protected GlyphVector evaluate(EvaluationContext context) {
				return font.createGlyphVector(podgonFontRenderContext(), context.resolve(text));
			}
		};
		
		final Expression<Rectangle2D> textBB = new ExpressionBase<Rectangle2D>() {
			@Override
			protected Rectangle2D evaluate(EvaluationContext context) {
				return context.resolve(glyphVector).getVisualBounds();
			}
		};
		
		final Expression<? extends Point2D> textCoords = new ExpressionBase<Point2D>() {
			@Override
			protected Point2D evaluate(EvaluationContext context) {
				Rectangle2D bb = context.resolve(textBB);
				return new Point2D.Double(-bb.getCenterX(), -bb.getCenterY());
			}
		};
		
		centeredBB = new ExpressionBase<Rectangle2D>() {
			@Override
			protected Rectangle2D evaluate(EvaluationContext context) {
				double margin = 0.15;
				Rectangle2D result = BoundingBoxHelper.expand(context.resolve(textBB), margin, margin);
				result.setRect(result.getX() - result.getCenterX(), result.getY() - result.getCenterY(), result.getWidth(), result.getHeight());
				return result;
			}
		};
		
		graphics = new ExpressionBase<GraphicalContent>() {
			@Override
			protected GraphicalContent evaluate(final EvaluationContext context) {
			
				Point2D textXY = context.resolve(textCoords);
				final float textX = (float)textXY.getX();
				final float textY = (float)textXY.getY();
			
				return new GraphicalContent() {
					@Override
					public void draw(DrawRequest request) {
						Graphics2D g = request.getGraphics();
						g.setFont(font);
						g.drawGlyphVector(context.resolve(glyphVector), textX, textY);
					}
				};
			}
		};
	}
}