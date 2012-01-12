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

public class PunfUtilitySettings implements SettingsPage {
	public static final ModifiableExpression<String> punfCommand = Variable.create("punf");
	public static final ModifiableExpression<String> punfArgs = Variable.create("");
	
	public static final ModifiableExpression<Boolean> punfRAComplexityReduction = Variable.create(false);
	
	private static final String punfCommandKey = "Tools.punf.command";
	private static final String punfArgsKey = "Tools.punf.args";
	
	private static final String punfRAComplexityReductionKey = "Tools.punf.RAComplexityReduction";

	public PVector<EditableProperty> getProperties() {
		return TreePVector.<EditableProperty>empty()
				.plus(StringProperty.create("Punf command", punfCommand))
				.plus(StringProperty.create("Additional command line arguments", punfArgs));
	}

	public void load(Config config) {
		punfCommand.setValue(config.getString(punfCommandKey, "punf"));
		punfArgs.setValue(config.getString(punfArgsKey, ""));
		punfRAComplexityReduction.setValue(config.getBoolean(punfRAComplexityReductionKey, false));
	}

	public void save(Config config) {
		config.set(punfCommandKey, eval(punfCommand));
		config.set(punfArgsKey, eval(punfArgs));
		config.setBoolean(punfRAComplexityReductionKey, eval(punfRAComplexityReduction));
	}
	
	public String getSection() {
		return "External tools";
	}

	@Override
	public String getName() {
		return "punf";
	}
}
