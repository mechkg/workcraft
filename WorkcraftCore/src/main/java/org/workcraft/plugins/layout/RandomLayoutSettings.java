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

package org.workcraft.plugins.layout;

import org.workcraft.Config;
import org.workcraft.dependencymanager.advanced.user.Variable;
import org.workcraft.gui.propertyeditor.EditableProperty;
import org.workcraft.gui.propertyeditor.SettingsPage;
import org.workcraft.gui.propertyeditor.dubble.DoubleProperty;

import pcollections.PVector;
import pcollections.TreePVector;

public class RandomLayoutSettings implements SettingsPage {
	public static final Variable<Double> startX = Variable.create(0.0);
	public static final Variable<Double>  startY = Variable.create(0.0);
	public static final Variable<Double>  rangeX = Variable.create(30.0);
	public static final Variable<Double>  rangeY = Variable.create(30.0);
	
	@Override
	public PVector<EditableProperty> getProperties() {
		return TreePVector.<EditableProperty>empty()
		.plus(DoubleProperty.create("Start X", startX))
		.plus(DoubleProperty.create("Start Y", startY))
		.plus(DoubleProperty.create("Range X", rangeX))
		.plus(DoubleProperty.create("Range Y", rangeY))
		;
	}
	public void load(Config config) {
		startX.setValue(config.getDouble("RandomLayout.startX", 0));
		startY.setValue(config.getDouble("RandomLayout.startY", 0));
		rangeX.setValue(config.getDouble("RandomLayout.rangeX", 30));
		rangeY.setValue(config.getDouble("RandomLayout.rangeY", 30));
	}

	public void save(Config config) {
		config.setDouble("RandomLayout.startX", startX.getValue());
		config.setDouble("RandomLayout.startY", startY.getValue());
		config.setDouble("RandomLayout.rangeX", rangeX.getValue());
		config.setDouble("RandomLayout.rangeY", rangeY.getValue());
	}
	
	public String getSection() {
		return "Layout";
	}

	@Override
	public String getName() {
		return "Random";
	}
}
