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

import static org.workcraft.dependencymanager.advanced.core.Expressions.*;
import static org.workcraft.dependencymanager.advanced.core.GlobalCache.*;
import static org.workcraft.dom.visual.BoundedColorisableGraphicalContent.*;
import static org.workcraft.plugins.cpog.LabelPositioning.*;
import static org.workcraft.util.Function.Util.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dom.visual.BoundedColorisableGraphicalContent;
import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.ColorisableGraphicalContent;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Label;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.propertyeditor.EditableProperty;
import org.workcraft.gui.propertyeditor.choice.ChoiceProperty;
import org.workcraft.plugins.cpog.formularendering.FancyPrinter;
import org.workcraft.plugins.shared.CommonVisualSettings;
import org.workcraft.util.Function;
import org.workcraft.util.Function2;
import org.workcraft.util.Pair;

import pcollections.PVector;
import pcollections.TreePVector;

import org.workcraft.plugins.cpog.scala.nodes.*;

public class VisualVariableGui
{
	private static double size = 1;
	private static float strokeWidth = 0.08f;
	
	private static Font nameFont = FormulaRenderer.fancyFont;
	private static Font valueFont = nameFont.deriveFont(0.75f);
	
	public static PVector<EditableProperty> getProperties(Variable var) {
		PVector<Pair<String, VariableState>> states = TreePVector.<Pair<String, VariableState>>empty()
		.plus(Pair.of("[1] true", VariableState.TRUE))
		.plus(Pair.of("[0] false", VariableState.FALSE))
		.plus(Pair.of("[?] undefined", VariableState.UNDEFINED));
		
		return VisualComponent.getProperties(var.visualProperties())
			.plus(ChoiceProperty.create("State", states, var.state()));
	}
	
	private static BoundedColorisableGraphicalContent makeLabel(final String formula) {
		return FancyPrinter.print(formula, valueFont, Label.podgonFontRenderContext()).asBoundedColorisableImage();
	}
	
	private static Function<String, BoundedColorisableGraphicalContent> makeLabel =
		new Function<String, BoundedColorisableGraphicalContent>() {
			@Override
			public BoundedColorisableGraphicalContent apply(String argument) {
				return makeLabel(argument);
			}
		};
	
	private static Function<Object, String> toString = new Function<Object, String>(){
		@Override
		public String apply(Object argument) {
			return argument.toString();
		}
	};
	
	public static void toggle(Variable var)
	{
		var.state().setValue(eval(var.state()).toggle());
	}	
	
	public static Expression<BoundedColorisableGraphicalContent> getImage(Variable var)  {
		Function2<BoundedColorisableGraphicalContent, LabelPositioning, BoundedColorisableGraphicalContent> labelNamePositioner = new Function2<BoundedColorisableGraphicalContent, LabelPositioning, BoundedColorisableGraphicalContent> (){
			@Override
			public BoundedColorisableGraphicalContent apply(BoundedColorisableGraphicalContent image, LabelPositioning positioning) {
				return positionRelative(visualBox, positioning, image);
			}
		};
		
		final Expression<BoundedColorisableGraphicalContent> valueLabel = fmap(composition(toString, makeLabel, centerToZero), var.state());
		final Expression<BoundedColorisableGraphicalContent> nameLabel = fmap(labelNamePositioner, fmap(makeLabel, var.label()), var.visualProperties().labelPositioning());
		final Expression<BoundedColorisableGraphicalContent> box = simpleColorisableRectangle(visualBox);
		return fmap(composeFunc, fmap(composeFunc, box, nameLabel), valueLabel);
	}
	
	public static Expression<BoundedColorisableGraphicalContent> simpleColorisableRectangle(final Rectangle2D rect) {
		return fmap(new Function2<Color, Color, BoundedColorisableGraphicalContent>(){

			@Override
			public BoundedColorisableGraphicalContent apply(final Color fillColor, final Color foreColor) {
				return new BoundedColorisableGraphicalContent(new ColorisableGraphicalContent(){

					@Override
					public void draw(DrawRequest r) {
						Graphics2D g = r.getGraphics();
						Color colorisation = r.getColorisation().getColorisation();
						
						Shape shape = BoundingBoxHelper.expand(rect, -strokeWidth, -strokeWidth);

						g.setStroke(new BasicStroke(strokeWidth));

						g.setColor(Coloriser.colorise(fillColor, colorisation));
						g.fill(shape);
						g.setColor(Coloriser.colorise(foreColor, colorisation));
						g.draw(shape);
					}
				}, rect);
			}
		}, CommonVisualSettings.fillColor, CommonVisualSettings.foregroundColor);
	}

	public static Rectangle2D visualBox = new Rectangle2D.Double(-size / 2, -size / 2, size, size);
}
