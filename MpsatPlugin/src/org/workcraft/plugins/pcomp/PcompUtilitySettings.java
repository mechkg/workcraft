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

package org.workcraft.plugins.pcomp;
import static org.workcraft.dependencymanager.advanced.core.GlobalCache.eval;

import org.workcraft.Config;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.Variable;
import org.workcraft.gui.propertyeditor.EditableProperty;
import org.workcraft.gui.propertyeditor.SettingsPage;
import org.workcraft.gui.propertyeditor.string.StringProperty;

import pcollections.PVector;
import pcollections.TreePVector;

public class PcompUtilitySettings implements SettingsPage {
	public static final ModifiableExpression<String> pcompCommand = Variable.create("pcomp");
	public static final ModifiableExpression<String> pcompArgs = Variable.create("");
	
	private static final String pcompCommandKey = "Tools.pcomp.command";
	private static final String pcompArgsKey = "Tools.pcomp.args";

	public void load(Config config) {
		pcompCommand.setValue(config.getString(pcompCommandKey, "pcomp"));
		pcompArgs.setValue(config.getString(pcompArgsKey, ""));
	}

	public void save(Config config) {
		config.set(pcompCommandKey, eval(pcompCommand));
		config.set(pcompArgsKey, eval(pcompArgs));
	}
	
	public String getSection() {
		return "External tools";
	}

	@Override
	public String getName() {
		return "PComp";
	}

	@Override
	public PVector<EditableProperty> getProperties() {
		return TreePVector.<EditableProperty>empty()
				.plus(StringProperty.create("PComp command", pcompCommand))
				.plus(StringProperty.create("PPAdditional command line arguments", pcompArgs));
	}
}
