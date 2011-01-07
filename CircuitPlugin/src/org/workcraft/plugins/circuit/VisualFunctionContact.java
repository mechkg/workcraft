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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.core.GlobalCache;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpressionFilter;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.GraphicalContent;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.propertyeditor.ExpressionPropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.plugins.circuit.Contact.IoType;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaRenderingResult;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaToGraphics;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaToString;
import org.workcraft.plugins.cpog.optimisation.dnf.DnfGenerator;
import org.workcraft.plugins.cpog.optimisation.expressions.BooleanOperations;
import org.workcraft.plugins.cpog.optimisation.expressions.CleverBooleanWorker;
import org.workcraft.plugins.cpog.optimisation.expressions.DumbBooleanWorker;
import org.workcraft.plugins.cpog.optimisation.javacc.BooleanParser;
import org.workcraft.plugins.cpog.optimisation.javacc.ParseException;
import org.workcraft.util.Func;

public class VisualFunctionContact extends VisualContact {

	private final class BooleanFormulaToStringExpression extends
			ModifiableExpressionFilter<String, BooleanFormula> {
		private BooleanFormulaToStringExpression(
				ModifiableExpression<BooleanFormula> expr) {
			super(expr);
		}

		@Override
		protected BooleanFormula setFilter(String newValue) {
			return parseFormula(newValue);
		}

		@Override
		protected String getFilter(BooleanFormula value) {
			return FormulaToString.toString(value);
		}
	}

	private static Font font;
	
	static {
		try {
			font = Font.createFont(Font.TYPE1_FONT, ClassLoader.getSystemResourceAsStream("fonts/eurm10.pfb")).deriveFont(0.5f);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private final Expression<FormulaRenderingResult> renderedFormula = createRenderedFormulaExpression(VisualComponent.podgonFontRenderContext());
	
	Expression<FormulaRenderingResult> createRenderedFormulaExpression(final FontRenderContext fcon) {
		return new ExpressionBase<FormulaRenderingResult>(){
			@Override
			protected FormulaRenderingResult evaluate(EvaluationContext context) {
				return FormulaToGraphics.render(context.resolve(createCombinedFunctionExpression()), fcon, font);
			}
		};
	}
	
	private final FunctionContact function;
	
	public VisualFunctionContact(FunctionContact component) {
		this(component, Direction.WEST);
	}
	
	public VisualFunctionContact(FunctionContact component, VisualContact.Direction dir) {
		super(component, dir);
		function = component;
		
		addPropertyDeclarations();
	}
	
	private BooleanFormula parseFormula(String resetFunction) {
		try {
			return BooleanParser.parse(resetFunction,
					new Func<String, BooleanFormula>() {
						@Override
						public BooleanFormula eval(String name) {
							return ((VisualFunctionComponent)GlobalCache.eval(parent())).getOrCreateInput(name)
									.getReferencedContact();
						}
					});
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}
	
	ModifiableExpression<String> setFunction(){
		return new BooleanFormulaToStringExpression(getFunction().setFunction());
	}
	
	ModifiableExpression<String> resetFunction(){
		return new BooleanFormulaToStringExpression(getFunction().resetFunction());
	}
	
	public Expression<BooleanFormula> createCombinedFunctionExpression() {
		return new ExpressionBase<BooleanFormula>() {
			
			@Override
			protected BooleanFormula evaluate(EvaluationContext context) {
				CleverBooleanWorker worker = new CleverBooleanWorker(); 
				BooleanOperations.worker = new DumbBooleanWorker();
				FunctionContact function = getFunction();
				return DnfGenerator.generate(
						worker.or(context.resolve(function.setFunction()), worker.and(function, worker.not(context.resolve(function.resetFunction()))))
						
				);
			
			}
		};
	}

	private void addPropertyDeclarations() {
		addPropertyDeclaration(ExpressionPropertyDeclaration.create("Set function", setFunction(), String.class));
		addPropertyDeclaration(ExpressionPropertyDeclaration.create("Reset function", resetFunction(), String.class));
	}
	
	@Override
	public Expression<? extends GraphicalContent> graphicalContent() {
		return new ExpressionBase<GraphicalContent>() {

			@Override
			protected GraphicalContent evaluate(final EvaluationContext context) {
				return new GraphicalContent(){
					
					@Override
					public void draw(DrawRequest r) {
						context.resolve(VisualFunctionContact.super.graphicalContent()).draw(r);

						Graphics2D g = r.getGraphics();
						Color colorisation = r.getDecoration().getColorisation();
						
						if (context.resolve(ioType())==IoType.OUTPUT) {
							
							FormulaRenderingResult result = context.resolve(renderedFormula);
								
							Rectangle2D textBB = result.boundingBox;
							
							float textX = 0;
							float textY = (float)-textBB.getCenterY()-(float)0.5;
							
							AffineTransform transform = g.getTransform();
							AffineTransform at = new AffineTransform();
							
							switch (context.resolve(direction())) {
							case EAST:
								textX = (float)+0.5;
								break;
							case NORTH:
								at.quadrantRotate(-1);
								g.transform(at);
								textX = (float)+0.5;
								break;
							case WEST:
								textX = (float)-textBB.getWidth()-(float)0.5;
								break;
							case SOUTH:
								at.quadrantRotate(-1);
								g.transform(at);
								textX = (float)-textBB.getWidth()-(float)0.5;
								break;
							}
							
							g.translate(textX, textY);
							
							result.draw(g, Coloriser.colorise(Color.BLACK, colorisation));
									
							g.setTransform(transform);		
							
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
