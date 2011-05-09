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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;

import org.workcraft.dependencymanager.advanced.core.Combinator;
import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dom.visual.BoundedColorisableGraphicalContent;
import org.workcraft.dom.visual.ColorisableGraphicalContent;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.gui.Coloriser;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.BooleanVariable;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.BooleanReplacer;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaRenderingResult;
import org.workcraft.plugins.cpog.optimisation.expressions.One;
import org.workcraft.plugins.cpog.optimisation.expressions.Zero;
import org.workcraft.plugins.shared.CommonVisualSettings;
import org.workcraft.util.Function;
import org.workcraft.util.Function2;

import org.workcraft.plugins.cpog.scala.nodes.*;

public class VisualRhoClause
{
	private static float strokeWidth = 0.038f;
	
	interface FormulaRenderInfo {
		BooleanFormula formula();
		BooleanFormula value();
		Color foreColor();
		Color backColor();
	}
	
	
	static Function<FormulaRenderInfo, BoundedColorisableGraphicalContent> formulaToImage = new Function<FormulaRenderInfo, BoundedColorisableGraphicalContent>(){
		@Override
		public BoundedColorisableGraphicalContent apply(FormulaRenderInfo renderInfo) {
			
			final BooleanFormula formula = renderInfo.formula();
			final BooleanFormula value = renderInfo.value();
			final Color fillColor = renderInfo.backColor();
			final Color foreColor = renderInfo.foreColor();
			
			final FormulaRenderingResult result = FormulaRenderer.render(formula);
			Rectangle2D textBB = result.boundingBox;

			final float textX = (float)-textBB.getCenterX();
			final float textY = (float)-textBB.getCenterY();
				
			float width = (float)textBB.getWidth() + 0.4f;
			float height = (float)textBB.getHeight() + 0.2f;		

			final Rectangle2D.Float bb = new Rectangle2D.Float(-width / 2, -height / 2, width, height);
			
			ColorisableGraphicalContent gc = new ColorisableGraphicalContent(){
				public void draw(DrawRequest r)
				{
					Graphics2D g = r.getGraphics();
					Color colorisation = r.getColorisation().getColorisation();
					
					g.setStroke(new BasicStroke(strokeWidth));

					g.setColor(Coloriser.colorise(fillColor, colorisation));
					g.fill(bb);
					g.setColor(Coloriser.colorise(foreColor, colorisation));
					g.draw(bb);
					
					AffineTransform transform = g.getTransform();
					g.translate(textX, textY);
					
					result.draw(g, Coloriser.colorise(valueToColor.apply(value, foreColor), colorisation));
							
					g.setTransform(transform);		
				}
			};
			return new BoundedColorisableGraphicalContent(gc, bb);
		}
	};
	
	public static Expression<BoundedColorisableGraphicalContent> getVisualRhoClause(RhoClause rhoClause)
	{
		return fmap(formulaToImage, getRenderInfo(rhoClause));
	}

/**
 * This function would be cleaner if implemented with inheritance from ExpressionBase, 
 * but monadic interface is more general and should be preferred when possible.
 * 
 * getRenderInfo :: RhoClause -> Expression FormulaRenderInfo
 * getRenderInfo rhoClause = do
 *   formula <- formula rhoClause
 *   value <- value formula
 *   foreColor <- CommonVisualSettings.foregroundColor
 *   fillColor <- CommonVisualSettings.fillColor
 *   return $ FormulaRenderInfo
 *       { formula
 *       , value
 *       , foreColor
 *       , fillColor }
 */
	private static Expression<FormulaRenderInfo> getRenderInfo(RhoClause rhoClause) {
		return bind(rhoClause.formula(), new Combinator<BooleanFormula, FormulaRenderInfo>() {
			@Override
			public Expression<? extends FormulaRenderInfo> apply(final BooleanFormula formula) {
				return bind(value(formula), new Combinator<BooleanFormula, FormulaRenderInfo>(){

					@Override
					public Expression<? extends FormulaRenderInfo> apply(final BooleanFormula value) {
						return fmap(new Function2<Color, Color, FormulaRenderInfo>(){

							@Override
							public FormulaRenderInfo apply(final Color fore, final Color fill) {
								return new FormulaRenderInfo(){

									@Override
									public BooleanFormula formula() {
										return formula;
									}

									@Override
									public BooleanFormula value() {
										return value;
									}

									@Override
									public Color foreColor() {
										return fore;
									}

									@Override
									public Color backColor() {
										return fill;
									}
								};
							}
						}, CommonVisualSettings.foregroundColor, CommonVisualSettings.fillColor);
					}
				});
			}
		});
	}

	
	private static Function2<BooleanFormula, Color, Color> valueToColor = new Function2<BooleanFormula, Color, Color>() {
		@Override
		public Color apply(BooleanFormula val, Color defaultColor) {
			if(val == One.instance())
				return new Color(0x00cc00);
			else
				if(val == Zero.instance())
					return Color.RED;
				else
					return defaultColor;
		}
	};

	private static Expression<BooleanFormula> value(final BooleanFormula formula) {
		return new ExpressionBase<BooleanFormula>() {
			@Override
			protected BooleanFormula evaluate(final EvaluationContext context) {
				return formula.accept(
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
						}
					);
			}
		};
	}
}
