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

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.eval;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.io.IOException;
import java.util.LinkedHashMap;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.DeprecatedGraphicalContent;
import org.workcraft.dom.visual.Label;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.propertyeditor.ExpressionPropertyDeclaration;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaRenderingResult;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaToGraphics;
import org.workcraft.serialisation.xml.NoAutoSerialisation;
import org.workcraft.util.Func;

public class VisualVariable extends VisualComponent
{
	private static double size = 1;
	private static float strokeWidth = 0.08f;
	
	private static Font valueFont;
	private static Font nameFont;
	
	final FormulaLabel nameLabel;
	final FormulaLabel valueLabel;
	
	static {
		try {
			nameFont = Font.createFont(Font.TYPE1_FONT, ClassLoader.getSystemResourceAsStream("fonts/eurm10.pfb")).deriveFont(0.5f);
			valueFont = nameFont.deriveFont(0.75f);
		} catch (FontFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ModifiableExpression<LabelPositioning> labelPositioning;
	
	public VisualVariable(Variable variable, StorageManager storage)
	{
		super(variable, storage);
		
		labelPositioning = storage.create(LabelPositioning.TOP);
		
		LinkedHashMap<String, Object> states = new LinkedHashMap<String, Object>();
		states.put("[1] true", VariableState.TRUE);
		states.put("[0] false", VariableState.FALSE);
		states.put("[?] undefined", VariableState.UNDEFINED);
		
		addPropertyDeclaration(ExpressionPropertyDeclaration.create("State", state(), state(), VariableState.class, states));

		LinkedHashMap<String, Object> positions = new LinkedHashMap<String, Object>();
		
		for(LabelPositioning lp : LabelPositioning.values())
			positions.put(lp.name, lp);
		
		addPropertyDeclaration(ExpressionPropertyDeclaration.create("Label positioning", labelPositioning(), labelPositioning(), LabelPositioning.class, positions));
		
		nameLabel = makeNameLabel();
		valueLabel = makeValueLabel();
	}

	private FormulaLabel makeNameLabel() {
		Expression<FormulaRenderingResult> rendered = new ExpressionBase<FormulaRenderingResult>(){
			@Override
			protected FormulaRenderingResult evaluate(EvaluationContext context) {
				return FormulaToGraphics.print(context.resolve(label()), nameFont, Label.podgonFontRenderContext());
			}
		};
		
		Expression<Func<Rectangle2D, AffineTransform>> aligner = new ExpressionBase<Func<Rectangle2D,AffineTransform>>(){
			@Override
			protected Func<Rectangle2D, AffineTransform> evaluate(final EvaluationContext context) {
				return new Func<Rectangle2D, AffineTransform>() {

					@Override
					public AffineTransform eval(Rectangle2D labelBB) {
						Rectangle2D bb = getVisualBox();
						LabelPositioning labelPositioning = context.resolve(labelPositioning());
						Point2D labelPosition = new Point2D.Double(
								bb.getCenterX() - labelBB.getCenterX() + 0.5 * labelPositioning.dx * (bb.getWidth() + labelBB.getWidth() + 0.2),
								bb.getCenterY() - labelBB.getCenterY() + 0.5 * labelPositioning.dy * (bb.getHeight() + labelBB.getHeight() + 0.2));
					
						return AffineTransform.getTranslateInstance(labelPosition.getX(), labelPosition.getY());
					}
				};
			}
		};
		
		return new FormulaLabel(rendered, aligner);
	}

	private FormulaLabel makeValueLabel() {
		Expression<FormulaRenderingResult> rendered = new ExpressionBase<FormulaRenderingResult>(){
			@Override
			protected FormulaRenderingResult evaluate(EvaluationContext context) {
				return FormulaToGraphics.print(context.resolve(state()).toString(), valueFont, Label.podgonFontRenderContext());
			}
		};
		
		Expression<Func<Rectangle2D, AffineTransform>> aligner = new ExpressionBase<Func<Rectangle2D,AffineTransform>>(){
			@Override
			protected Func<Rectangle2D, AffineTransform> evaluate(final EvaluationContext context) {
				return new Func<Rectangle2D, AffineTransform>() {

					@Override
					public AffineTransform eval(Rectangle2D textBB) {
						float textX = (float)-textBB.getCenterX();
						float textY = (float)-textBB.getCenterY() + 0.08f;
							
						return AffineTransform.getTranslateInstance(textX, textY);
					}
				};
			}
		};
		
		return new FormulaLabel(rendered, aligner);
	}

	
	
	public void draw(DrawRequest r)
	{
	}
	
	@NoAutoSerialisation	
	public ModifiableExpression<String> label()
	{
		return getMathVariable().label();
	}

	public Variable getMathVariable()
	{
		return (Variable)getReferencedComponent();
	}
	
	public ModifiableExpression<VariableState> state()
	{
		return getMathVariable().state();
	}
	
	public void toggle()
	{
		state().setValue(eval(state()).toggle());
	}	
	
	public ModifiableExpression<LabelPositioning> labelPositioning()
	{
		return labelPositioning;
	}

	@Override
	public Expression<DeprecatedGraphicalContent> graphicalContent() {
		return new ExpressionBase<DeprecatedGraphicalContent>() {
			@Override
			protected DeprecatedGraphicalContent evaluate(final EvaluationContext context) {
				return new DeprecatedGraphicalContent() {
					@Override
					public void draw(DrawRequest r) {
						Graphics2D g = r.getGraphics();
						Color colorisation = r.getDecoration().getColorisation();
						
						Shape shape = new Rectangle2D.Double(-size / 2 + strokeWidth / 2, -size / 2 + strokeWidth / 2,
								size - strokeWidth, size - strokeWidth);

						g.setStroke(new BasicStroke(strokeWidth));

						g.setColor(Coloriser.colorise(context.resolve(fillColor()), colorisation));
						g.fill(shape);
						g.setColor(Coloriser.colorise(context.resolve(foregroundColor()), colorisation));
						g.draw(shape);
						
						context.resolve(nameLabel.graphicalContent).draw(r);
						context.resolve(valueLabel.graphicalContent).draw(r);
					}
				};
			}
		};
	}

	@Override
	public Expression<? extends Touchable> localSpaceTouchable() {
		return new ExpressionBase<Touchable>(){

			@Override
			protected Touchable evaluate(EvaluationContext context) {
				final Rectangle2D label = context.resolve(nameLabel.boundingBox);
				
				return new Touchable(){
					public Rectangle2D getBoundingBox()
					{
						return BoundingBoxHelper.union(label, getVisualBox());
					}

					public boolean hitTest(Point2D point)
					{
						if (label != null && label.contains(point)) return true;
						return Math.abs(point.getX()) <= size / 2 && Math.abs(point.getY()) <= size / 2;		
					}

					@Override
					public Point2D getCenter() {
						return new Point2D.Double(0, 0);
					}
				};
			}
		};
	}

	private Double getVisualBox() {
		return new Rectangle2D.Double(-size / 2, -size / 2, size, size);
	}
}
