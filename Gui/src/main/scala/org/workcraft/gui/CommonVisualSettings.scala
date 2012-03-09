package org.workcraft.gui;
import java.awt.Color;
import java.awt.Font;

import org.workcraft.dependencymanager.advanced.user.Variable

import pcollections.PVector
import pcollections.TreePVector

import org.workcraft.scala.Expressions._

case class CommonVisualSettings (
	size:Double = 1.0,
	strokeWidth:Double = 0.1,
	iconSize:Int = 16,
	backgroundColor:Color = Color.WHITE,
	foregroundColor:Color = Color.BLACK,
	fillColor:Color = Color.WHITE,
	serifFont:Font = new Font("Serif", Font.PLAIN, 1),
	labelFont:Font = new Font("Serif", Font.PLAIN, 1),
	labelFontSize: Double = 0.5,
	sansSerifFont:Font = new Font("SansSerif", Font.PLAIN, 1),
	editorMessageFont:Font = new Font("SansSerif", Font.PLAIN, 12)
	) {
  def effectiveLabelFont = labelFont.deriveFont(labelFontSize.toFloat)
}

object CommonVisualSettings /*implements SettingsPage*/ {
  val settings = Variable.create(CommonVisualSettings())

	
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
