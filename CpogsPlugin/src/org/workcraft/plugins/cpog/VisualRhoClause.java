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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.HashMap;

import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.GraphicalContent;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.Coloriser;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.BooleanVariable;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.BooleanReplacer;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaRenderingResult;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaToGraphics;
import org.workcraft.plugins.cpog.optimisation.expressions.One;
import org.workcraft.plugins.cpog.optimisation.expressions.Zero;

@Hotkey(KeyEvent.VK_R)
@SVGIcon("images/icons/svg/rho.svg")
public class VisualRhoClause extends VisualComponent
{
	private static float strokeWidth = 0.038f;

	// Ideally should be done with a normal expression instead of variable. 
	// This is a lazy approach to conversion to Expressions, which does not require big modifications to draw() method. 
	private ModifiableExpression<Rectangle2D> boudingBox = new org.workcraft.dependencymanager.advanced.user.Variable<Rectangle2D>(new Rectangle2D.Float(0, 0, 0, 0));
	
	private static Font font;
	
	static {
		try {
			font = Font.createFont(Font.TYPE1_FONT, ClassLoader.getSystemResourceAsStream("fonts/eurm10.pfb")).deriveFont(0.5f);
		} catch (FontFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public VisualRhoClause(RhoClause rhoClause)
	{
		super(rhoClause);
	}
	
	@Override
	public Expression<? extends GraphicalContent> graphicalContent() {
		return new ExpressionBase<GraphicalContent>(){

			@Override
			protected GraphicalContent evaluate(final EvaluationContext context) {
				return new GraphicalContent(){
					public void draw(DrawRequest r)
					{
						Graphics2D g = r.getGraphics();
						Color colorisation = r.getDecoration().getColorisation();
						
						FormulaRenderingResult result = FormulaToGraphics.render(context.resolve(formula()), g.getFontRenderContext(), font);
						
						Rectangle2D textBB = result.boundingBox;
							
						float textX = (float)-textBB.getCenterX();
						float textY = (float)-textBB.getCenterY();
							
						float width = (float)textBB.getWidth() + 0.4f;
						float height = (float)textBB.getHeight() + 0.2f;		
						
						Rectangle2D.Float bb = new Rectangle2D.Float(-width / 2, -height / 2, width, height);
						boudingBox.setValue(bb);
						// careful not to use boundingBox afterwards, or infinite re-evaluation would happen

						g.setStroke(new BasicStroke(strokeWidth));

						g.setColor(Coloriser.colorise(context.resolve(fillColor()), colorisation));
						g.fill(bb);
						g.setColor(Coloriser.colorise(context.resolve(foregroundColor()), colorisation));
						g.draw(bb);
						
						AffineTransform transform = g.getTransform();
						g.translate(textX, textY);
						
						result.draw(g, Coloriser.colorise(getColor(context), colorisation));
								
						g.setTransform(transform);		
					}
				};
			}
		};
	}
	
	private Color getColor(EvaluationContext context) {
		BooleanFormula value = context.resolve(value());
		if(value == One.instance())
			return new Color(0x00cc00);
		else
			if(value == Zero.instance())
				return Color.RED;
			else
				return context.resolve(foregroundColor());
	}

	private Expression<BooleanFormula> value() {
		return new ExpressionBase<BooleanFormula>() {

			@Override
			protected BooleanFormula evaluate(final EvaluationContext context) {
				return context.resolve(formula()).accept(
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

	public RhoClause getMathRhoClause()
	{
		return (RhoClause)getReferencedComponent();
	}
	
	public ModifiableExpression<BooleanFormula> formula()
	{
		return getMathRhoClause().formula();
	}

	@Override
	public Expression<? extends Touchable> localSpaceTouchable() {
		return new ExpressionBase<Touchable>() {

			@Override
			protected Touchable evaluate(final EvaluationContext context) {
				return new Touchable(){

					@Override
					public boolean hitTest(Point2D point) {
						return getBoundingBox().contains(point);
					}

					@Override
					public Rectangle2D getBoundingBox() {
						return context.resolve(boudingBox);
					}

					@Override
					public Point2D getCenter() {
						return new Point2D.Double(0, 0);
					}
					
				};
			}
		};
	}	
}
