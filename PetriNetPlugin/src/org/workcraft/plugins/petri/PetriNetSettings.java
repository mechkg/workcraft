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

package org.workcraft.plugins.petri;

import java.awt.Color;

import org.workcraft.Config;
import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpressionBase;
import org.workcraft.dependencymanager.advanced.user.Variable;
import org.workcraft.gui.propertyeditor.EditableProperty;
import org.workcraft.gui.propertyeditor.SettingsPage;
import org.workcraft.gui.propertyeditor.bool.BooleanProperty;
import org.workcraft.gui.propertyeditor.colour.ColorProperty;

import pcollections.PVector;
import pcollections.TreePVector;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.eval;

public class PetriNetSettings implements SettingsPage {
	private final static Variable<Color> rawEnabledBackgroundColor = Variable.create(new Color(0.0f, 0.0f, 0.0f));
	private final static Variable<Color> rawEnabledForegroundColor = Variable.create(new Color(1.0f, 0.5f, 0.0f));
	
	private static ModifiableExpression<Color> nullIfNotUsed (final ModifiableExpression<Color> color, final ModifiableExpression<Boolean> use) {
		return new ModifiableExpressionBase<Color>() {
			@Override
			public void setValue(Color newValue) {
				color.setValue(newValue);
			}

			@Override
			protected Color evaluate(EvaluationContext context) {
				return context.resolve(use) ? context.resolve(color) : null;
			}
		};
	}

	public final static Variable<Boolean> useEnabledBackgroundColor = Variable.create(false);
	public final static Variable<Boolean> useEnabledForegroundColor = Variable.create(true);
	
	public final static ModifiableExpression<Color> enabledBackgroundColor = nullIfNotUsed(rawEnabledBackgroundColor, useEnabledBackgroundColor);
	public final static ModifiableExpression<Color> enabledForegroundColor = nullIfNotUsed(rawEnabledForegroundColor, useEnabledForegroundColor);
	
	public PVector<EditableProperty> getProperties() {
		return TreePVector.<EditableProperty>empty()
			.plus(BooleanProperty.create("Use enabled transition foreground", useEnabledForegroundColor))
			.plus(ColorProperty.create("Enabled transition foreground", enabledForegroundColor))
			.plus(BooleanProperty.create("Use enabled transition background", useEnabledBackgroundColor))
			.plus(ColorProperty.create("Enabled transition background", enabledBackgroundColor));
	}

	public void load(Config config) {
		useEnabledForegroundColor.setValue(config.getBoolean("PetriNetSettings.useEnabledForegroundColor", true));
		enabledForegroundColor.setValue(config.getColor("PetriNetSettings.enabledForegroundColor", new Color(1.0f, 0.5f, 0.0f)));

		useEnabledBackgroundColor.setValue(config.getBoolean("PetriNetSettings.useEnabledBackgroundColor", false));
		enabledBackgroundColor.setValue(config.getColor("PetriNetSettings.enabledBackgroundColor", new Color(1.0f, 0.5f, 0.0f)));
	}

	public void save(Config config) {
		config.setBoolean("PetriNetSettings.useEnabledForegroundColor", eval(useEnabledForegroundColor));
		config.setColor("PetriNetSettings.enabledBackgroundColor", eval(enabledBackgroundColor));
		
		config.setBoolean("PetriNetSettings.useEnabledBackgroundColor", eval(useEnabledBackgroundColor));
		config.setColor("PetriNetSettings.enabledForegroundColor", eval(enabledForegroundColor));
	}

	public String getSection() {
		return "Visual";
	}

	@Override
	public String getName() {
		return "Petri Net";
	}
}
