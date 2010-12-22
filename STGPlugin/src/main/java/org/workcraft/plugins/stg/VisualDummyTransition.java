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
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.serialisation.xml.NoAutoSerialisation;

@Hotkey(KeyEvent.VK_D)
@DisplayName("Dummy Transition")
@SVGIcon("images/icons/svg/transition.svg")
public class VisualDummyTransition extends VisualTransition {
	private static Font font = new Font("Sans-serif", Font.PLAIN, 1).deriveFont(0.75f);
	
	private final Label label;

	public VisualDummyTransition(DummyTransition transition) {
		super(transition);
		
		label = new Label(font, transition.name());
	}

	
	@Override
	public Expression<? extends GraphicalContent> graphicalContent() {
		return new ExpressionBase<GraphicalContent>() {

			@Override
			protected GraphicalContent evaluate(final EvaluationContext context) {
				final GraphicalContent labelGraphics = context.resolve(label.graphics);
				return new GraphicalContent() {

					@Override
					public void draw(DrawRequest r) {
						
						context.resolve(labelGraphics()).draw(r);

						Graphics2D g = r.getGraphics();
						
						Color background = r.getDecoration().getBackground();
						if(background!=null)
						{
							g.setColor(background);
							g.fill(context.resolve(shape()).getBoundingBox());
						}

						g.setColor(Coloriser.colorise(getColor(), r.getDecoration().getColorisation()));
						
						labelGraphics.draw(r);
					}
				};
			}
		};
	}
	
	@Override
	public Expression<Touchable> localSpaceTouchable() {
		return new  ExpressionBase<Touchable>() {

			@Override
			protected Touchable evaluate(final EvaluationContext context) {
				return new Touchable() {

					@Override
					public boolean hitTest(Point2D point) {
						return getBoundingBox().contains(point);
					}

					@Override
					public Rectangle2D getBoundingBox() {
						return context.resolve(label.centeredBB);
					}

					@Override
					public Point2D getCenter() {
						return new Point2D.Double(0, 0);
					}
					
				};
			}
		};
	}
	
	private Color getColor() {
		return Color.BLACK;
	}
	
	@NoAutoSerialisation
	public DummyTransition getReferencedTransition() {
		return (DummyTransition)getReferencedComponent();
	}
	
	public ModifiableExpression<String> name() {
		return getReferencedTransition().name();
	}
}
