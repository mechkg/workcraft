package org.workcraft.gui;
import java.awt.Color;
import java.awt.Font;

import org.workcraft.dependencymanager.advanced.user.Variable

import pcollections.PVector
import pcollections.TreePVector

class CommonVisualSettings /*implements SettingsPage*/ {
	val size = Variable.create(1.0)
	val strokeWidth = Variable.create(0.1)
	val iconSize = Variable.create(16)
	val backgroundColor = Variable.create(Color.WHITE)
	val foregroundColor = Variable.create(Color.BLACK)
	val fillColor = Variable.create(Color.WHITE)
	val serifFont = Variable.create(new Font("Serif", Font.PLAIN, 1))
	val sansSerifFont = Variable.create(new Font("SansSerif", Font.PLAIN, 1))
	/*
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
	}*/
}
