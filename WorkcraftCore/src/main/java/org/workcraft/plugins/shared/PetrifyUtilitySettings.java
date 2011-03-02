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
import org.workcraft.Config;
import org.workcraft.dependencymanager.advanced.user.Variable;
import org.workcraft.gui.propertyeditor.EditableProperty;
import org.workcraft.gui.propertyeditor.SettingsPage;
import org.workcraft.gui.propertyeditor.string.StringProperty;

import pcollections.PVector;
import pcollections.TreePVector;

public class PetrifyUtilitySettings implements SettingsPage {

	public static final Variable<String> petrifyCommand = Variable.create("petrify");
	public static final Variable<String> petrifyArgs = Variable.create("");
	public static final Variable<String> draw_astgCommand = Variable.create("draw_astg");
	public static final Variable<String> draw_astgArgs = Variable.create("");
	public static final Variable<String> write_sgCommand = Variable.create("write_sg");
	public static final Variable<String> write_sgArgs = Variable.create("");
	
	private static final String petrifyCommandKey = "Tools.petrify.command";
	private static final String petrifyArgsKey = "Tools.petrify.args";
	
	private static final String draw_astgCommandKey = "Tools.draw_astg.command";
	private static final String draw_astgArgsKey = "Tools.draw_astg.args";
	
	private static final String write_sgCommandKey = "Tools.write_sg.command";
	private static final String write_sgArgsKey = "Tools.write_sg.args";
	
	@Override
	public PVector<EditableProperty> getProperties() {
		return TreePVector.<EditableProperty>empty()
			.plus(StringProperty.create("petrify command", petrifyCommand))
			.plus(StringProperty.create("Additional petrify command line arguments", petrifyArgs))
			.plus(StringProperty.create("write_sg command", write_sgCommand))
			.plus(StringProperty.create("Additional write_sg command line arguments", write_sgArgs))
			.plus(StringProperty.create("draw_astg command", draw_astgCommand))
			.plus(StringProperty.create("Additional draw_astg command line arguments", draw_astgArgs));
	}

	public void load(Config config) {
		petrifyCommand.setValue(config.getString(petrifyCommandKey, "petrify"));
		petrifyArgs.setValue(config.getString(petrifyArgsKey, ""));
		draw_astgCommand.setValue(config.getString(draw_astgCommandKey, "draw_astg"));
		draw_astgArgs.setValue(config.getString(draw_astgArgsKey, ""));
		write_sgCommand.setValue(config.getString(write_sgCommandKey, "write_sg"));
		write_sgArgs.setValue(config.getString(write_sgArgsKey, ""));
	}

	public void save(Config config) {
		config.set(petrifyCommandKey, petrifyCommand.getValue());
		config.set(petrifyArgsKey, petrifyArgs.getValue());
		config.set(draw_astgCommandKey, draw_astgCommand.getValue());
		config.set(draw_astgArgsKey, draw_astgArgs.getValue());
		config.set(write_sgCommandKey, write_sgCommand.getValue());
		config.set(write_sgArgsKey, write_sgArgs.getValue());
	}
	
	public String getSection() {
		return "External tools";
	}

	@Override
	public String getName() {
		return "Petrify";
	}
}