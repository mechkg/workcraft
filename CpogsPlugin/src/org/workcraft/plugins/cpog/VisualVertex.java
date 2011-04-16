/*
 *
 * Copyright 2008,2009 Newcastle University
 *
 * This file is part of Workcraft.
 * 
 * Workcraft is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Workcraft is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.workcraft.plugins.cpog;

import static org.workcraft.dependencymanager.advanced.core.Expressions.*;
import static org.workcraft.dom.visual.BoundedColorisableImage.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dom.visual.BoundedColorisableImage;
import org.workcraft.dom.visual.ColorisableGraphicalContent;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Label;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.propertyeditor.EditableProperty;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.BooleanVariable;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.BooleanReplacer;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaRenderingResult;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaToGraphics;
import org.workcraft.plugins.cpog.optimisation.expressions.One;
import org.workcraft.plugins.cpog.optimisation.expressions.Zero;
import org.workcraft.plugins.shared.CommonVisualSettings;

import pcollections.PVector;

public class VisualVertex
{
	
	public static Expression<BoundedColorisableImage> getImage(final Vertex vertex)  {
		final ExpressionBase<BoundedColorisableImage> circle = new ExpressionBase<BoundedColorisableImage>(){

			@Override
			protected BoundedColorisableImage evaluate(final EvaluationContext context) {
				ColorisableGraphicalContent gc = new ColorisableGraphicalContent() {
					
					@Override
					public void draw(DrawRequest r) {
						Graphics2D g = r.getGraphics();
						Color colorisation = r.getColorisation().getColorisation();
						
						Shape shape = new Ellipse2D.Double(-size / 2 + strokeWidth / 2, -size / 2 + strokeWidth / 2,
								size - strokeWidth, size - strokeWidth);

						BooleanFormula value = context.resolve(value(vertex));
						
						g.setColor(Coloriser.colorise(context.resolve(CommonVisualSettings.fillColor), colorisation));
						g.fill(shape);
						
						g.setColor(Coloriser.colorise(context.resolve(CommonVisualSettings.foregroundColor), colorisation));
						if (value == Zero.instance())
						{
							g.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_BUTT,
							        BasicStroke.JOIN_MITER, 1.0f, new float[] {0.18f, 0.18f}, 0.00f));
						}
						else
						{
							g.setStroke(new BasicStroke(strokeWidth));
							if (value != One.instance())
								g.setColor(Coloriser.colorise(Color.LIGHT_GRAY, colorisation));
						}
						
						g.draw(shape);
					}
				};
				return new BoundedColorisableImage(gc, new Rectangle2D.Double(-size/2, -size/2, size, size));
			}
		};
		
		Expression<BoundedColorisableImage> nameLabel = new ExpressionBase<BoundedColorisableImage>() {
			@Override
			protected BoundedColorisableImage evaluate(EvaluationContext context) {
				String text = context.resolve(vertex.visualInfo.label);
				BooleanFormula condition = context.resolve(vertex.condition);
				if (condition != One.instance()) text += ": ";
				
				FormulaRenderingResult result = FormulaToGraphics.print(text, FormulaRenderer.fancyFont, Label.podgonFontRenderContext());
				
				if (condition != One.instance()) result.add(FormulaToGraphics.render(condition, Label.podgonFontRenderContext(), FormulaRenderer.fancyFont));
				
				return LabelPositioning.positionRelative(context.resolve(circle).boundingBox
						, context.resolve(vertex.visualInfo.labelPosition)
						, result.asBoundedColorisableImage());
			}
		};
		
		return bindFunc(circle, nameLabel, compose);
	}
	
	private final static double size = 1;
	private final static float strokeWidth = 0.1f;

	public static PVector<EditableProperty> getProperties(Vertex v) {
		return VisualComponent.getProperties(v.visualInfo);
	}

	public static Expression<BooleanFormula> value(final Vertex vert) {
		return new ExpressionBase<BooleanFormula>() {

			@Override
			protected BooleanFormula evaluate(final EvaluationContext context) {
				return context.resolve(vert.condition).accept(
						new BooleanReplacer(new HashMap<BooleanVariable, BooleanFormula>())
						{
							@Override
							public BooleanFormula visit(BooleanVariable node) {
								switch(context.resolve(((Variable)node).state()))
								{
								case TRUE:
									return One.instance();
								case FALSE:
									return Zero.instance();
								default:
									return node;
								}
							}
						});
			}
		};
	}
}
