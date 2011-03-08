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
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.visual.ColorisableGraphicalContent;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Label;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.gui.Coloriser;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.stg.SignalTransition.Type;
import org.workcraft.serialisation.xml.NoAutoSerialisation;

public class VisualSignalTransition extends VisualStgTransition{
	private static Color inputsColor = Color.RED.darker();
	private static Color outputsColor = Color.BLUE.darker();
	private static Color internalsColor = Color.GREEN.darker();

	private static Font font = new Font("Sans-serif", Font.PLAIN, 1).deriveFont(0.75f);
	
	public VisualSignalTransition(Transition transition, StorageManager storage) {
		super(transition, storage);
	}
	
	public Expression<? extends ColorisableGraphicalContent> getGraphicalContent(final Expression<? extends String> text) {
		final Label label = label(text);
		final Expression<Touchable> shapeExpr = localSpaceTouchable(text);
		final Expression<Color> colorExpr = color();
		return new ExpressionBase<ColorisableGraphicalContent>() {
			@Override
			protected ColorisableGraphicalContent evaluate(final EvaluationContext context) {
				return new ColorisableGraphicalContent() {
					@Override
					public void draw(DrawRequest r) {
						
						final ColorisableGraphicalContent labelGraphics = context.resolve(labelGraphics());
						final ColorisableGraphicalContent nameLabelGraphics = context.resolve(label.graphics);
						final Color color = context.resolve(colorExpr);
						final Touchable shape = context.resolve(shapeExpr);
						
						labelGraphics.draw(r);
						
						Graphics2D g = r.getGraphics();
						
						Color background = r.getColorisation().getBackground();
						if(background!=null)
						{
							g.setColor(background);
							g.fill(shape.getBoundingBox());
						}
						
						g.setColor(Coloriser.colorise(color, r.getColorisation().getColorisation()));
						
						nameLabelGraphics.draw(r);
					}
				};
			}
		};
	}

	private Label label(final Expression<? extends String> text) {
		return new Label(font, text);
	}
	
	public Expression<Touchable> localSpaceTouchable(final Expression<? extends String> text) {
		final Label label = label(text);
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
						return new Point2D.Double(0, 0);
					}
					
					@Override
					public boolean hitTest(Point2D point) {
						return getBoundingBox().contains(point);
					}
				};
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
}
