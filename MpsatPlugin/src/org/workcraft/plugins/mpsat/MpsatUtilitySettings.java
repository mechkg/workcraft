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

package org.workcraft.plugins.mpsat;
import static org.workcraft.dependencymanager.advanced.core.GlobalCache.eval;

import org.workcraft.Config;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.Variable;
import org.workcraft.gui.propertyeditor.EditableProperty;
import org.workcraft.gui.propertyeditor.SettingsPage;
import org.workcraft.gui.propertyeditor.string.StringProperty;

import pcollections.PVector;
import pcollections.TreePVector;

public class MpsatUtilitySettings implements SettingsPage {
	public static final ModifiableExpression<String> mpsatCommand = Variable.create("mpsat");
	public static final ModifiableExpression<String> mpsatArgs = Variable.create("");
	
	private static final String mpsatCommandKey = "Tools.mpsat.command";
	private static final String mpsatArgsKey = "Tools.mpsat.args";

	public PVector<EditableProperty> getProperties() {
		return TreePVector.<EditableProperty>empty()
				.plus(StringProperty.create("MPSat command", mpsatCommand))
	            .plus(StringProperty.create("MPSat additional arguments", mpsatArgs));
	}

	public void load(Config config) {
		mpsatCommand.setValue(config.getString(mpsatCommandKey, "mpsat"));
		mpsatArgs.setValue(config.getString(mpsatArgsKey, ""));
	}

	public void save(Config config) {
		config.set(mpsatCommandKey, eval(mpsatCommand));
		config.set(mpsatArgsKey, eval(mpsatArgs));
	}
	
	public String getSection() {
		return "External tools";
	}

	@Override
	public String getName() {
		return "MPSat";
	}
}
