package org.workcraft.plugins.stg;

import org.workcraft.Config;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.Variable;
import org.workcraft.gui.propertyeditor.EditableProperty;
import org.workcraft.gui.propertyeditor.SettingsPage;
import org.workcraft.gui.propertyeditor.bool.BooleanProperty;

import pcollections.PVector;
import pcollections.TreePVector;

public class STGSettings implements SettingsPage {
	private static Variable<Boolean> showInstanceNumbers = Variable.create(false);

	@Override
	public String getName() {
		return "Signal Transition Graph";
	}

	@Override
	public String getSection() {
		return "Visual";
	}

	@Override
	public void load(Config config) {
		showInstanceNumbers.setValue(config.getBoolean("STG.showInstanceNumbers", false));
	}

	@Override
	public void save(Config config) {
		config.setBoolean("STG.showInstanceNumbers", showInstanceNumbers.getValue());
	}

	public static ModifiableExpression<Boolean> showInstanceNumbers() {
		return showInstanceNumbers;
	}

	@Override
	public PVector<EditableProperty> getProperties() {
		return TreePVector.<EditableProperty>empty()
			.plus(BooleanProperty.create("Show instance numbers", showInstanceNumbers));
	}
}
