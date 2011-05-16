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

package org.workcraft.plugins.circuit;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.Node;
import org.workcraft.dom.NodeContext;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.ColorisableGraphicalContent;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.Coloriser;
import org.workcraft.plugins.circuit.Contact.IoType;
import org.workcraft.plugins.circuit.renderers.ComponentRenderingResult.RenderType;
import org.workcraft.plugins.cpog.formularendering.FormulaRenderingResult;
import org.workcraft.plugins.cpog.formularendering.FancyPrinter;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;

public class VisualFunctionContact extends VisualContact {

	private static Font font;
	
	static {
		try {
			font = Font.createFont(Font.TYPE1_FONT, ClassLoader.getSystemResourceAsStream("fonts/eurm10.pfb")).deriveFont(0.5f);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private final Expression<FormulaRenderingResult> renderedSetFormula;
	private final Expression<FormulaRenderingResult> renderedResetFormula;
		
	Expression<FormulaRenderingResult> createRenderedFormulaExpression(final Expression<BooleanFormula> formula) {
		return new ExpressionBase<FormulaRenderingResult>(){
			@Override
			protected FormulaRenderingResult evaluate(EvaluationContext context) {
				BooleanFormula formulaValue = context.resolve(formula);
				return formulaValue == null ? null : FancyPrinter.render(formulaValue, VisualComponent.podgonFontRenderContext(), font);
			}
		};
	}
	
	private final FunctionContact function;
	
	public VisualFunctionContact(FunctionContact component, StorageManager storage) {
		this(component, Direction.WEST, storage);
	}
	
	public VisualFunctionContact(FunctionContact component, VisualContact.Direction dir, StorageManager storage) {
		super(component, dir, storage);
		function = component;
		renderedSetFormula = createRenderedFormulaExpression(component.setFunction());
		renderedResetFormula = createRenderedFormulaExpression(component.resetFunction());
	}
	
	public static Expression<? extends ColorisableGraphicalContent> graphicalContent(Expression<? extends NodeContext> nodeContext, final VisualFunctionContact contact) {
		final Expression<? extends ColorisableGraphicalContent> superGraphicalContent = createGraphicalContent(nodeContext, contact);
		return new ExpressionBase<ColorisableGraphicalContent>() {

			@Override
			protected ColorisableGraphicalContent evaluate(final EvaluationContext context) {
				return new ColorisableGraphicalContent(){
					private void drawFormula(Graphics2D g, int arrowType, float xOffset, float yOffset, Color foreground, Color background, FormulaRenderingResult result) {
						
						Rectangle2D textBB = result.boundingBox;
						
						float textX = 0;
						float textY = (float)-textBB.getCenterY()-(float)0.5-yOffset;
						
						float arrX = 0;
						float arrY = (float)-textBB.getCenterY()-(float)0.5-yOffset;
						
						
						AffineTransform transform = g.getTransform();
						AffineTransform at = new AffineTransform();
						Direction dir = context.resolve(contact.direction());
						
						if (!(context.resolve(contact.parent()) instanceof VisualFunctionComponent)) {
							dir = flipDirection(dir);
						}
						
						switch (dir) {
						case EAST:
							textX = (float)+xOffset;
							arrX = (float)+(xOffset-0.15);
							break;
						case NORTH:
							at.quadrantRotate(-1);
							g.transform(at);
							textX = (float)+xOffset;
							arrX = (float)+(xOffset-0.15);
							break;
						case WEST:
							textX = (float)-textBB.getWidth()-xOffset;
							arrX = (float)-(xOffset-0.15);
							break;
						case SOUTH:
							at.quadrantRotate(-1);
							g.transform(at);
							textX = (float)-textBB.getWidth()-xOffset;
							arrX = (float)-(xOffset-0.15);
							break;
						}
						
						
						if (arrowType==2) {
							Line2D line = new Line2D.Double(arrX, arrY, arrX, arrY-0.225);
							
							Path2D path = new Path2D.Double();
							path.moveTo(arrX-0.05, arrY-0.225);
							path.lineTo(arrX+0.05, arrY-0.225);
							path.lineTo(arrX, arrY-0.375);
							path.closePath();
							
							g.setStroke(new BasicStroke((float)0.02));
							
							g.setColor(foreground);
							g.fill(path);
//							g.draw(path);
							g.draw(line);
						} else if (arrowType==1) {
							
							Line2D line = new Line2D.Double(arrX, arrY-0.15, arrX, arrY-0.375);
							
							Path2D path = new Path2D.Double();
							
							path.moveTo(arrX-0.05, arrY-0.15);
							path.lineTo(arrX+0.05, arrY-0.15);
							path.lineTo(arrX, arrY);
							path.closePath();
							
							g.setStroke(new BasicStroke((float)0.02));
							
							g.setColor(foreground);
							g.fill(path);
//							g.draw(path);
							g.draw(line);
						}
						
						g.translate(textX, textY);
						
						
						result.draw(g, foreground);
						
						g.setTransform(transform);
					}
					
					
					@Override
					public void draw(DrawRequest r) {
						context.resolve(superGraphicalContent).draw(r);
						
						Graphics2D g = r.getGraphics();
						
						Color colorisation = r.getColorisation().getColorisation();
						Node p = context.resolve(contact.parent()); 
						if (p!=null) {
							if ((context.resolve(contact.ioType())==IoType.INPUT)^(p instanceof VisualComponent)) {
								if (!(p instanceof VisualCircuitComponent)||
										context.resolve(((VisualCircuitComponent)p).renderType())==RenderType.BOX) {
									
									FormulaRenderingResult setResult = context.resolve(contact.renderedSetFormula);
									FormulaRenderingResult resetResult = context.resolve(contact.renderedResetFormula);
									float xOfs = (float)0.5;
									
									if (!CircuitSettings.getShowContacts()&&(p instanceof VisualComponent)) xOfs = (float)-0.5;
									
									if (resetResult!=null) {
										drawFormula(g, 1, xOfs, (float)-0.2, Coloriser.colorise(Color.BLACK, colorisation), Coloriser.colorise(Color.WHITE, colorisation), resetResult);
										drawFormula(g, 2, xOfs, (float)0.5, Coloriser.colorise(Color.BLACK, colorisation), Coloriser.colorise(Color.WHITE, colorisation), setResult);
										
									} else {
										drawFormula(g, 0, xOfs, (resetResult==null?(float)0:(float)0.5), Coloriser.colorise(Color.BLACK, colorisation), Coloriser.colorise(Color.WHITE, colorisation), setResult);
									}
								}
							}
						}
					}
					
				};
			}
			
		};
	}
	
	public FunctionContact getFunction() {
		return function;
	}
}
