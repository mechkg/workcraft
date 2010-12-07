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

package org.workcraft.plugins.stg;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedHashMap;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.GraphicalContent;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.plugins.stg.SignalTransition.Direction;
import org.workcraft.plugins.stg.SignalTransition.Type;
import org.workcraft.serialisation.xml.NoAutoSerialisation;

@Hotkey(KeyEvent.VK_T)
@DisplayName("Signal Transition")
@SVGIcon("images/icons/svg/signal-transition.svg")
public class VisualSignalTransition extends VisualTransition {
	private static Color inputsColor = Color.RED.darker();
	private static Color outputsColor = Color.BLUE.darker();
	private static Color internalsColor = Color.GREEN.darker();

	private static Font font = new Font("Sans-serif", Font.PLAIN, 1).deriveFont(0.75f);
	
	private Label label = new Label(font, text());
	
	public VisualSignalTransition(Transition transition) {
		super(transition);
		addPropertyDeclarations();
	}

	private void addPropertyDeclarations() {
		LinkedHashMap<String, Object> types = new LinkedHashMap<String, Object>();
		types.put("Input", SignalTransition.Type.INPUT);
		types.put("Output", SignalTransition.Type.OUTPUT);
		types.put("Internal", SignalTransition.Type.INTERNAL);
		
		LinkedHashMap<String, Object> directions = new LinkedHashMap<String, Object>();
		directions.put("+", SignalTransition.Direction.PLUS);
		directions.put("-", SignalTransition.Direction.MINUS);
		directions.put("", SignalTransition.Direction.TOGGLE);
		
		//addPropertyDeclaration(new PropertyDeclaration(this, "Signal name", "getSignalName", "setSignalName", String.class));
		addPropertyDeclaration(new PropertyDeclaration(this, "Transition", "getDirection", "setDirection", SignalTransition.Direction.class, directions));
		addPropertyDeclaration(new PropertyDeclaration(this, "Signal type", "getType", "setType", SignalTransition.Type.class, types));
	}
	
	@Override
	public Expression<? extends GraphicalContent> graphicalContent() {
		return new ExpressionBase<GraphicalContent>() {
			@Override
			protected GraphicalContent evaluate(final EvaluationContext context) {
				return new GraphicalContent() {
					@Override
					public void draw(DrawRequest r) {
						drawLabelInLocalSpace(r);
						Graphics2D g = r.getGraphics();
						
						Color background = r.getDecoration().getBackground();
						if(background!=null)
						{
							g.setColor(background);
							g.fill(context.resolve(localSpaceTouchable()).getBoundingBox());
						}
						
						g.setColor(Coloriser.colorise(context.resolve(color()), r.getDecoration().getColorisation()));
						
						context.resolve(label.graphics).draw(r);
					}
				};
			}
		};
	}
	
	@Override
	public Expression<Touchable> localSpaceTouchable() {
		return new ExpressionBase<Touchable>() {
			@Override
			protected Touchable evaluate(final EvaluationContext context) {
				return new Touchable() {
					@Override
					public Rectangle2D getBoundingBox() {
						return context.resolve(label.centeredBB);
					}
					
					@Override
					public Point2D getCenter() {
						return new Point2D.Double(getBoundingBox().getCenterX(), getBoundingBox().getCenterY());
					}
					
					@Override
					public boolean hitTest(Point2D point) {
						return getBoundingBox().contains(point);
					}
				};
			}
		};
	}
	
	private Expression<String> text() {
		return new ExpressionBase<String>() {
			@Override
			protected String evaluate(EvaluationContext context) {
				SignalTransition t = getReferencedTransition();
				final StringBuffer result = new StringBuffer(context.resolve(t.signalName()));
				switch (context.resolve(t.direction())) {
				case PLUS:
					result.append("+"); break;
				case MINUS:
					result.append("-"); break;
				case TOGGLE:
					result.append("~"); break;
				}
				return result.toString();
			}
		};
	}
	
	private Expression<Color> color() {
		return new ExpressionBase<Color>() {
			@Override
			protected Color evaluate(EvaluationContext context) {
				Type type = context.resolve(signalType());
				if (type == SignalTransition.Type.INTERNAL)
					return internalsColor;
				if (type == SignalTransition.Type.INPUT)
					return inputsColor;
				if (type == SignalTransition.Type.OUTPUT)
					return outputsColor;
				return Color.BLACK;
			}
		};
	}

	@NoAutoSerialisation
	public SignalTransition getReferencedTransition() {
		return (SignalTransition)getReferencedComponent();
	}
	
	@NoAutoSerialisation
	public ModifiableExpression<Type> signalType() {
		return getReferencedTransition().signalType();
	}
	
	@NoAutoSerialisation
	public ModifiableExpression<Direction> direction() {
		return getReferencedTransition().direction();
	}
	
	@NoAutoSerialisation
	public ModifiableExpression<String> signalName() {
		return getReferencedTransition().signalName();
	}
}
