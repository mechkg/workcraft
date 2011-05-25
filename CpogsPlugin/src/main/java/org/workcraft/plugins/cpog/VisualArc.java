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
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.HashMap;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.ColorisableGraphicalContent;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.connections.ParametricCurve;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.dom.visual.connections.VisualConnectionProperties;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.BooleanReplacer;
import org.workcraft.plugins.cpog.optimisation.expressions.BooleanOperations;
import org.workcraft.plugins.cpog.optimisation.expressions.One;
import org.workcraft.plugins.cpog.optimisation.expressions.Zero;
import org.workcraft.dom.visual.Label;
import org.workcraft.exceptions.NotImplementedException;
import org.workcraft.util.Func;
import org.workcraft.util.Geometry;

import org.workcraft.plugins.cpog.scala.nodes.*;

/*public class VisualArc
{
	private static Font labelFont;
	
	static {
		try {
			labelFont = Font.createFont(Font.TYPE1_FONT, ClassLoader.getSystemResourceAsStream("fonts/eurm10.pfb")).deriveFont(0.5f);
		} catch (FontFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Expression<BooleanFormula> condition() {
		throw new NotImplementedException();
	}
	
	FormulaLabel makeFormulaLabel() {
		final Expression<FormulaRenderingResult> renderedLabel = new ExpressionBase<FormulaRenderingResult>(){
			@Override
			protected FormulaRenderingResult evaluate(EvaluationContext context) {
				BooleanFormula condition = context.resolve(condition());
				if (condition == One.instance()) return null;
				
				return FancyPrinter.render(condition, Label.podgonFontRenderContext(), labelFont);
			}

		};

		return new FormulaLabel(renderedLabel, new ExpressionBase<Func<Rectangle2D, AffineTransform>>(){
			@Override
			protected Func<Rectangle2D, AffineTransform> evaluate(final EvaluationContext context) {
				return new Func<Rectangle2D, AffineTransform>(){

					@Override
					public AffineTransform eval(Rectangle2D labelBB) {
						ParametricCurve graphic = context.resolve(curve());
						
						Point2D p = graphic.getPointOnCurve(0.5);
						Point2D d = graphic.getDerivativeAt(0.5);
						Point2D dd = graphic.getSecondDerivativeAt(0.5);
						
						if (d.getX() < 0)
						{
							d = Geometry.multiply(d, -1);
							//dd = Geometry.multiply(dd, -1);
						}
						Point2D labelPosition = new Point2D.Double(labelBB.getCenterX(), labelBB.getMaxY());
						if (Geometry.crossProduct(d, dd) < 0) labelPosition.setLocation(labelPosition.getX(), labelBB.getMinY()); 

						AffineTransform transform = AffineTransform.getTranslateInstance(p.getX() - labelPosition.getX(), p.getY() - labelPosition.getY()); 
						transform.concatenate(AffineTransform.getRotateInstance(d.getX(), d.getY(), labelPosition.getX(), labelPosition.getY()));
						
						return transform;
					}

					private Expression<ParametricCurve> curve() {
						throw new NotImplementedException();
					}
					
				};
			}
			
		});
	}
	
	public ExpressionBase<VisualConnectionProperties> properties() {
		return new ExpressionBase<VisualConnectionProperties>() {

			@Override
			protected VisualConnectionProperties evaluate(final EvaluationContext context) {
				final VisualConnectionProperties superProperties = null;// TODO: superProperties
				
				return new VisualConnectionProperties.Inheriting(superProperties) {

					@Override
					public Color getDrawColor() {
						BooleanFormula value = context.resolve(value());
						
						if (value == Zero.instance() || value == One.instance()) return superProperties.getDrawColor();
						
						return Color.LIGHT_GRAY;
					}

					@Override
					public Stroke getStroke() {
						BooleanFormula value = context.resolve(value());
						
						Expression<Double> lineWidth = null; // TODO: lineWidth 
						if (value == Zero.instance()) 
							return new BasicStroke((float)context.resolve(lineWidth).doubleValue(), BasicStroke.CAP_BUTT,
						        BasicStroke.JOIN_MITER, 1.0f, new float[] {0.18f, 0.18f}, 0.00f);
						
						return superProperties.getStroke();
					}
				};
			}
		};
	}

	Expression<BooleanFormula> value(Vertex v) {
		throw new NotImplementedException();
	}
	
	private Expression<BooleanFormula> value()
	{
		return new ExpressionBase<BooleanFormula>() {
			@Override
			protected BooleanFormula evaluate(final EvaluationContext context) {
				BooleanFormula condition = context.resolve(condition());
				
				Vertex first = null; // TODO: get from somewhere
				Vertex second = null;
				condition = BooleanOperations.and(condition, context.resolve(value(first)));
				condition = BooleanOperations.and(condition, context.resolve(value(second)));
				
				return condition.accept(
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
	
	public Expression<? extends Touchable> shape() {
		return new ExpressionBase<Touchable>() {

			@Override
			protected Touchable evaluate(final EvaluationContext context) {
				
				final Touchable superShape = null; //TODO: context.resolve(VisualArc.super.shape());
				
				return new Touchable() {
					
					@Override
					public boolean hitTest(Point2D point) {
						Rectangle2D lbb = context.resolve(null // TODO: label.boundingBox  );
						if (lbb!=null && lbb.contains(point)) return true;
						return superShape.hitTest(point);
					}
					
					@Override
					public Point2D getCenter() {
						return null; //TODO: VisualArc.this.getPointOnConnection(0.5);
					}
					
					@Override
					public Rectangle2D getBoundingBox() {
						return null;//TODO: BoundingBoxHelper.union(superShape.getBoundingBox(), context.resolve(null // TODO: label.boundingBox ));
					}
				};
			}
			
		};
	}
}
*/
