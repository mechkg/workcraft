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
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.ColorisableGraphicalContent;
import org.workcraft.dom.visual.Label;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.propertyeditor.ExpressionPropertyDeclaration;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.BooleanVariable;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.BooleanReplacer;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaRenderingResult;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaToGraphics;
import org.workcraft.plugins.cpog.optimisation.expressions.One;
import org.workcraft.plugins.cpog.optimisation.expressions.Zero;
import org.workcraft.plugins.shared.CommonVisualSettings;
import org.workcraft.util.Func;

public class VisualVertex extends VisualComponent
{
	private final static double size = 1;
	private final static float strokeWidth = 0.1f;
	public final ModifiableExpression<LabelPositioning> labelPositioning;
	
	final FormulaLabel label;
	
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
	

	public VisualVertex(Vertex vertex, StorageManager storage)
	{
		super(vertex, storage);
		
		labelPositioning = storage.create(LabelPositioning.TOP);
		
		LinkedHashMap<String, Object> positions = new LinkedHashMap<String, Object>();
		
		for(LabelPositioning lp : LabelPositioning.values())
			positions.put(lp.name, lp);
		
		addPropertyDeclaration(ExpressionPropertyDeclaration.create("Label positioning", labelPositioning(), labelPositioning(), LabelPositioning.class, positions));
		
		label = makeLabel();
	}
	
	private FormulaLabel makeLabel() {
		Expression<FormulaRenderingResult> rendered = new ExpressionBase<FormulaRenderingResult>() {

			@Override
			protected FormulaRenderingResult evaluate(EvaluationContext context) {
				String text = context.resolve(label());
				BooleanFormula condition = context.resolve(condition());
				if (condition != One.instance()) text += ": ";
				
				FormulaRenderingResult result = FormulaToGraphics.print(text, labelFont, Label.podgonFontRenderContext());
				
				if (condition != One.instance()) result.add(FormulaToGraphics.render(condition, Label.podgonFontRenderContext(), labelFont));
				
				return result;
			}
		};
		Expression<Func<Rectangle2D, AffineTransform>> aligner = new ExpressionBase<Func<Rectangle2D,AffineTransform>>(){
			@Override
			protected Func<Rectangle2D, AffineTransform> evaluate(EvaluationContext context) {
				final LabelPositioning lp = context.resolve(labelPositioning());
				return new Func<Rectangle2D, AffineTransform>() {
					@Override
					public AffineTransform eval(Rectangle2D labelBB) {
						Point2D labelPosition = new Point2D.Double(
								-labelBB.getCenterX() + 0.5 * lp.dx * (1.0 + labelBB.getWidth() + 0.2),
								-labelBB.getCenterY() + 0.5 * lp.dy * (1.0 + labelBB.getHeight() + 0.2));
						
						return AffineTransform.getTranslateInstance(labelPosition.getX(), labelPosition.getY());
					}
				};
			}
		};
		return new FormulaLabel(rendered, aligner);
	}

	@Override
	public Expression<? extends ColorisableGraphicalContent> graphicalContent() {
		return new ExpressionBase<ColorisableGraphicalContent>(){
			@Override
			protected ColorisableGraphicalContent evaluate(final EvaluationContext context) {
				return new ColorisableGraphicalContent() {
					
					@Override
					public void draw(DrawRequest r) {
						Graphics2D g = r.getGraphics();
						Color colorisation = r.getColorisation().getColorisation();
						
						Shape shape = new Ellipse2D.Double(-size / 2 + strokeWidth / 2, -size / 2 + strokeWidth / 2,
								size - strokeWidth, size - strokeWidth);

						BooleanFormula value = context.resolve(value());
						
						g.setColor(Coloriser.colorise(context.resolve(fillColor()), colorisation));
						g.fill(shape);
						
						g.setColor(Coloriser.colorise(context.resolve(foregroundColor()), colorisation));
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
						context.resolve(label.graphicalContent).draw(r);
					}
				};
			}
		};
	}

	public Vertex getMathVertex()
	{
		return (Vertex) getReferencedComponent();
	}

	public ModifiableExpression<BooleanFormula> condition()
	{
		return getMathVertex().condition();
	}
	
	public ModifiableExpression<LabelPositioning> labelPositioning()
	{
		return labelPositioning;
	}

	public Expression<BooleanFormula> value() {
		return new ExpressionBase<BooleanFormula>() {

			@Override
			protected BooleanFormula evaluate(final EvaluationContext context) {
				return context.resolve(condition()).accept(
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

	@Override
	public Expression<? extends Touchable> localSpaceTouchable() {
		return new ExpressionBase<Touchable>() {

			@Override
			protected Touchable evaluate(EvaluationContext context) {
				final Rectangle2D labelBB = context.resolve(label.boundingBox);
				
				return new Touchable(){
					@Override
					public Rectangle2D getBoundingBox()
					{
						return BoundingBoxHelper.union(labelBB, new Rectangle2D.Double(-size / 2, -size / 2, size, size));
					}	
					
					@Override
					public boolean hitTest(Point2D pointInLocalSpace)
					{
						if (labelBB != null && labelBB.contains(pointInLocalSpace)) return true;

						double size = CommonVisualSettings.getSize();
						
						return pointInLocalSpace.distanceSq(0, 0) < size * size / 4;
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
