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
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.visual.ColorisableGraphicalContent;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.DrawableNew;
import org.workcraft.dom.visual.Label;
import org.workcraft.dom.visual.ReflectiveTouchable;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.exceptions.NotSupportedException;
import org.workcraft.gui.Coloriser;
import org.workcraft.serialisation.xml.NoAutoSerialisation;

public class VisualDummyTransition extends VisualStgTransition implements ReflectiveTouchable, DrawableNew {
	private static Font font = new Font("Sans-serif", Font.PLAIN, 1).deriveFont(0.75f);
	
	public VisualDummyTransition(DummyTransition transition, StorageManager storage) {
		super(transition, storage);
	}
	
	public Label createLabel(Expression<String> text) {
		return new Label(font, text);
	}

	public Expression<? extends ColorisableGraphicalContent> graphicalContent() {
		throw new NotSupportedException("use parameterised graphicalContent(Expression<String> text) instead");
	}
	
	public Expression<? extends ColorisableGraphicalContent> graphicalContent(Expression<String> text) {
		final Label label = createLabel(text);
		return new ExpressionBase<ColorisableGraphicalContent>() {

			@Override
			protected ColorisableGraphicalContent evaluate(final EvaluationContext context) {
				final ColorisableGraphicalContent labelGraphics = context.resolve(label.graphics);
				return new ColorisableGraphicalContent() {

					@Override
					public void draw(DrawRequest r) {
						
						context.resolve(labelGraphics()).draw(r);

						Graphics2D g = r.getGraphics();
						
						Color background = r.getColorisation().getBackground();
						if(background!=null)
						{
							g.setColor(background);
							g.fill(context.resolve(shape()).getBoundingBox());
						}

						g.setColor(Coloriser.colorise(getColor(), r.getColorisation().getColorisation()));
						
						labelGraphics.draw(r);
					}
				};
			}
		};
	}
	
	public Expression<Touchable> shape() {
		throw new NotSupportedException("use parameterised shape(Expression<String> text) instead");
	}
	
	public Expression<Touchable> shape(Expression<String> text) {
		final Label label = new Label(font, text);
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
						return  context.resolve(label.centeredBB);
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
}
