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

package org.workcraft.plugins.shared;
import java.awt.Color;

import org.workcraft.Config;
import org.workcraft.dependencymanager.advanced.user.Variable;
import org.workcraft.gui.propertyeditor.EditableProperty;
import org.workcraft.gui.propertyeditor.SettingsPage;
import org.workcraft.gui.propertyeditor.colour.ColorProperty;
import org.workcraft.gui.propertyeditor.dubble.DoubleProperty;
import org.workcraft.gui.propertyeditor.integer.IntegerProperty;

import pcollections.PVector;
import pcollections.TreePVector;

public class CommonVisualSettings implements SettingsPage {
	
	public static final Variable<Double> size = Variable.create(1.0);
	public static final Variable<Double> strokeWidth = Variable.create(0.1);
	public static final Variable<Integer> iconSize = Variable.create(16);
	public static final Variable<Color> backgroundColor = Variable.create(Color.WHITE);
	public static final Variable<Color> foregroundColor = Variable.create(Color.BLACK);
	public static final Variable<Color> fillColor = Variable.create(Color.WHITE);
	
	@Override
	public PVector<EditableProperty> getProperties() {
		return TreePVector.<EditableProperty>empty()
		.plus(IntegerProperty.create("Base icon width (pixels, 8-256)", iconSize))
		.plus(DoubleProperty.create("Base component size (cm)", size))
		.plus(DoubleProperty.create("Default stroke width (cm)", strokeWidth))
		.plus(ColorProperty.create("Editor background color", backgroundColor))
		.plus(ColorProperty.create("Default foreground color", foregroundColor))
		.plus(ColorProperty.create("Default fill color", fillColor));
	}

	public void load(Config config) {
		size.setValue(config.getDouble("CommonVisualSettings.size", 1.0));
		iconSize.setValue(config.getInt("CommonVisualSettings.iconSize", 16));
		strokeWidth.setValue(config.getDouble("CommonVisualSettings.strokeWidth", 0.1));
		backgroundColor.setValue(config.getColor("CommonVisualSettings.backgroundColor", Color.WHITE));
		foregroundColor.setValue(config.getColor("CommonVisualSettings.foregroundColor", Color.BLACK));
		fillColor.setValue(config.getColor("CommonVisualSettings.fillColor", Color.WHITE));
	}

	public void save(Config config) {
		config.setInt("CommonVisualSettings.iconSize", iconSize.getValue());
		config.setDouble("CommonVisualSettings.size", size.getValue());
		config.setDouble("CommonVisualSettings.strokeWidth", strokeWidth.getValue());
		config.setColor("CommonVisualSettings.backgroundColor", backgroundColor.getValue());
		config.setColor("CommonVisualSettings.foregroundColor", foregroundColor.getValue());
		config.setColor("CommonVisualSettings.fillColor", fillColor.getValue());
	}

	@Override
	public String getName() {
		return "Common visual settings";
	}

	@Override
	public String getSection() {
		return "Visual";
	}
}
