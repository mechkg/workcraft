package org.workcraft.plugins.cpog;

import static org.workcraft.dependencymanager.advanced.core.Expressions.*;

import java.awt.geom.Point2D;

import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.gui.propertyeditor.EditableProperty;
import org.workcraft.gui.propertyeditor.choice.ChoiceProperty;
import org.workcraft.gui.propertyeditor.dubble.DoubleProperty;
import org.workcraft.gui.propertyeditor.string.StringProperty;
import org.workcraft.util.FieldAccessor;

import pcollections.PVector;
import pcollections.TreePVector;

public class VisualComponent {
	public final ModifiableExpression<String> label;
	public final ModifiableExpression<LabelPositioning> labelPosition;
	public final ModifiableExpression<Point2D> position;


	public static VisualComponent make(StorageManager storage) {
		return new VisualComponent(storage.<Point2D>create(new Point2D.Double(0, 0)), storage.create(""), storage.create(LabelPositioning.BOTTOM));
	}
	
	public VisualComponent(ModifiableExpression<Point2D> position, ModifiableExpression<String> label, ModifiableExpression<LabelPositioning> labelPosition) {
		this.position = position;
		this.label = label;
		this.labelPosition = labelPosition;
	}
	
	public static PVector<EditableProperty> getProperties(org.workcraft.plugins.cpog.scala.nodes.VisualProperties component) {
		FieldAccessor<Point2D, Double> xView = new FieldAccessor<Point2D, Double>(){
			@Override
			public Double apply(Point2D argument) {
				return argument.getX();
			}

			@Override
			public Point2D assign(Point2D old, Double x) {
				return new Point2D.Double(x, old.getY());
			}
		};
		FieldAccessor<Point2D, Double> yView = new FieldAccessor<Point2D, Double>(){
			@Override
			public Double apply(Point2D argument) {
				return argument.getY();
			}
			
			@Override
			public Point2D assign(Point2D old, Double y) {
				return new Point2D.Double(old.getX(), y);
			}
		};
		return TreePVector.<EditableProperty>empty()
			.plus(StringProperty.create("Label", component.label()))
			.plus(DoubleProperty.create("X", bindFunc(component.position(), xView)))
			.plus(DoubleProperty.create("Y", bindFunc(component.position(), yView)))
			.plus(ChoiceProperty.create("Label positioning", LabelPositioning.getChoice(), component.labelPositioning()));
	}
}
