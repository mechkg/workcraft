package org.workcraft.dom.visual;

import static org.workcraft.dependencymanager.advanced.core.Expressions.*;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.util.Function;

public class Label {

	public static FontRenderContext podgonFontRenderContext() {
		return VisualComponent.podgonFontRenderContext();
	}
	
	public static Expression<BoundedColorisableGraphicalContent> mkLabel(final Font font, final Expression<? extends String> text) {
		
		final Expression<? extends Rectangle2D.Double> textBB = fmap(new Function<String, Rectangle2D.Double>(){
					@Override
					public Rectangle2D.Double apply(String textValue) {
						GlyphVector gv = font.createGlyphVector(podgonFontRenderContext(), textValue);
						return (Rectangle2D.Double)gv.getVisualBounds();
					}
				}, text);

		Expression<ColorisableGraphicalContent> graphics = fmap(new Function<String, ColorisableGraphicalContent>() {
			@Override
			public ColorisableGraphicalContent apply(final String text) {
				return new ColorisableGraphicalContent() {
					@Override
					public void draw(DrawRequest request) {
						Graphics2D g = request.getGraphics();
						g.setFont(font);
						g.drawString(text, 0, 0);
					}
				};
			}
		}, text);
		
		Expression<BoundedColorisableGraphicalContent> simpleLabel = fmap(BoundedColorisableGraphicalContent.constructor, graphics, textBB);
		return fmap(BoundedColorisableGraphicalContent.centerToZero, simpleLabel);
	}
}
