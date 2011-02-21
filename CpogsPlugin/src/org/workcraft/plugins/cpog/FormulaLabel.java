package org.workcraft.plugins.cpog;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.ColorisableGraphicalContent;
import org.workcraft.gui.Coloriser;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaRenderingResult;
import org.workcraft.util.Func;

public class FormulaLabel {
	public FormulaLabel(
			final Expression<? extends FormulaRenderingResult> renderedLabel, 
			final Expression<? extends Func<? super Rectangle2D, ? extends AffineTransform>> aligner) {
		
		final Expression<AffineTransform> labelTransform = new ExpressionBase<AffineTransform>() {
			@Override
			protected AffineTransform evaluate(EvaluationContext context) {
				FormulaRenderingResult rendered = context.resolve(renderedLabel);
				if(rendered == null)
					return null;
				
				return context.resolve(aligner).eval(rendered.boundingBox);
			}
		};
		
		boundingBox = new ExpressionBase<Rectangle2D>() {

			@Override
			protected Rectangle2D evaluate(EvaluationContext context) {
				FormulaRenderingResult rendered = context.resolve(renderedLabel);
				if(rendered == null)
					return null;
				return BoundingBoxHelper.transform(rendered.boundingBox, context.resolve(labelTransform));
			}
		};
		
		graphicalContent = new ExpressionBase<ColorisableGraphicalContent>(){

			@Override
			protected ColorisableGraphicalContent evaluate(final EvaluationContext context) {
				return new ColorisableGraphicalContent(){

					@Override
					public void draw(DrawRequest r)
					{
						FormulaRenderingResult result = context.resolve(renderedLabel);
						if(result == null)
							return;

						Graphics2D g = r.getGraphics();
						
						AffineTransform oldTransform = g.getTransform();

						g.transform(context.resolve(labelTransform));		
						result.draw(g, Coloriser.colorise(Color.BLACK, r.getColorisation().getColorisation()));
						g.setTransform(oldTransform);
					}
				};
			}
		};
		
	}
	
	public final Expression<Rectangle2D> boundingBox;
	public final Expression<ColorisableGraphicalContent> graphicalContent;
	
}
