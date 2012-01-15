package org.workcraft.plugins.cpog;

import static org.workcraft.dependencymanager.advanced.core.Expressions.*;

import java.awt.geom.Point2D;

import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.graphics.LabelPositioning;
import org.workcraft.gui.propertyeditor.EditableProperty;
import org.workcraft.gui.propertyeditor.choice.ChoiceProperty;
import org.workcraft.gui.propertyeditor.dubble.DoubleProperty;
import org.workcraft.gui.propertyeditor.string.StringProperty;
import org.workcraft.util.FieldAccessor;

import pcollections.PVector;
import pcollections.TreePVector;

public class VisualComponent {
	
	public static PVector<EditableProperty> getProperties(org.workcraft.plugins.cpog.scala.nodes.VisualProperties component) {
		FieldAccessor<Point2D.Double, Double> xView = new FieldAccessor<Point2D.Double, Double>(){
			@Override
			public Double apply(Point2D.Double argument) {
				return argument.getX();
			}

			@Override
			public Point2D.Double assign(Point2D.Double old, Double x) {
				return new Point2D.Double(x, old.getY());
			}
		};
		FieldAccessor<Point2D.Double, Double> yView = new FieldAccessor<Point2D.Double, Double>(){
			@Override
			public Double apply(Point2D.Double argument) {
				return argument.getY();
			}
			
			@Override
			public Point2D.Double assign(Point2D.Double old, Double y) {
				return new Point2D.Double(old.getX(), y);
			}
		};
		return TreePVector.<EditableProperty>empty()
			.plus(StringProperty.create("Label", component.label().jexpr()))
			.plus(DoubleProperty.create("X", bindFunc(component.position().jexpr(), xView)))
			.plus(DoubleProperty.create("Y", bindFunc(component.position().jexpr(), yView)))
			.plus(ChoiceProperty.create("Label positioning", LabelPositioning.getChoice(), component.labelPositioning().jexpr()));
	}
}
