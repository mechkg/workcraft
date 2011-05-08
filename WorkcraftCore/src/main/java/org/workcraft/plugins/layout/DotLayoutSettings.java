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
import org.workcraft.gui.propertyeditor.bool.BooleanProperty;
import org.workcraft.gui.propertyeditor.string.StringProperty;

import pcollections.PVector;
import pcollections.TreePVector;

public class DotLayoutSettings implements SettingsPage {
	public final static Variable<Boolean> importConnectionsShape = Variable.create(false);
	public final static Variable<String> dotCommand = Variable.create("dot");
	
	@Override
	public PVector<EditableProperty> getProperties() {
		return TreePVector.<EditableProperty>empty()
			.plus(StringProperty.create("Dot command", dotCommand))
			.plus(BooleanProperty.create("Import connections shape from Dot graph (experimental)", importConnectionsShape))
			;
	}

	public void load(Config config) {
		dotCommand.setValue(config.getString("DotLayout.dotCommand", "dot"));
		importConnectionsShape.setValue(config.getBoolean("DotLayout.importConnectionsShape", false));
	}

	public void save(Config config) {
		config.set("DotLayout.dotCommand", dotCommand.getValue());
		config.setBoolean("DotLayout.importConnectionsShape", importConnectionsShape.getValue());
	}
	
	public String getSection() {
		return "Layout";
	}
	@Override
	public String getName() {
		return "Dot";
	}
}
