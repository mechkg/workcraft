/*
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

package org.workcraft.dom.visual.connections;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.*;

import java.awt.Color;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpressionBase;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpressionFilter;
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.Connection;
import org.workcraft.exceptions.NotSupportedException;
import org.workcraft.gui.propertyeditor.EditableProperty;
import org.workcraft.gui.propertyeditor.choice.ChoiceProperty;
import org.workcraft.gui.propertyeditor.dubble.DoubleProperty;
import org.workcraft.util.Pair;

import pcollections.PVector;
import pcollections.TreePVector;

public class VisualConnection {
	
	public enum ConnectionType 
	{
		POLYLINE,
		BEZIER
	};
	
	public enum ScaleMode
	{
		NONE,
		LOCK_RELATIVELY,
		SCALE,
		STRETCH,
		ADAPTIVE
	}
	
	static class BoundedVariable extends ModifiableExpressionFilter<Double, Double>
	{	
		private final double min;
		private final double max;
		
		public BoundedVariable(double min, double max, ModifiableExpression<Double> backEnd) {
			super(backEnd);
			this.min = min;
			this.max = max;
		}

		@Override
		protected Double setFilter(Double newValue) {
			return newValue < min ? min : newValue > max ? max : newValue;
		}

		@Override
		protected Double getFilter(Double value) {
			return value;
		}
	}
	
	private static double defaultLineWidth = 0.02;
	private static double defaultArrowWidth = 0.15;
	private static double defaultArrowLength = 0.4;
	private static final Color defaultColor = Color.BLACK;
	
	public static double HIT_THRESHOLD = 0.2;
	/*
	private final ModifiableExpression<Color> color;
	private final ModifiableExpression<Double> lineWidth;
	private final ModifiableExpression<Double> arrowWidth;
	private final ModifiableExpression<Double> arrowLength;
	
	public final StorageManager storage;
	public final ModifiableExpression<ConnectionType> connectionType = new ModifiableExpressionBase<ConnectionType>(){
		@Override
		public void setValue(ConnectionType newValue) {
			if(eval(this) != newValue)
				graphic.setValue(newGraphicsConfiguration(newValue));
		}
		private ConnectionGraphicConfiguration newGraphicsConfiguration(ConnectionType newValue) {
			switch(newValue) {
			case POLYLINE:
				return new Polyline(VisualConnection.this, storage);
			case BEZIER:
				return new Bezier(VisualConnection.this, storage);
			default:
				throw new NotSupportedException();
			}
		}
		@Override
		protected ConnectionType evaluate(EvaluationContext context) {
			return context.resolve(graphic).accept(new ConnectionGraphicConfigurationVisitor<ConnectionType>() {

				@Override
				public ConnectionType visitPolyline(Polyline polyline) {
					return ConnectionType.POLYLINE;
				}

				@Override
				public ConnectionType visitBezier(Bezier bezier) {
					return ConnectionType.BEZIER;
				}
			});
		}
	};
	
	public PVector<EditableProperty> getProperties() {

		PVector<Pair<String,Double>> arrowLengths = TreePVector.<Pair<String, Double>>empty()
		.plus(Pair.of("short", 0.2))
		.plus(Pair.of("medium", 0.4))
		.plus(Pair.of("long", 0.8))
		;
	
		PVector<Pair<String,ConnectionType>> connectionTypes = TreePVector.<Pair<String, ConnectionType>>empty()
		.plus(Pair.of("Polyline", ConnectionType.POLYLINE))
		.plus(Pair.of("Bezier", ConnectionType.BEZIER))
		;
		
		PVector<Pair<String,ScaleMode>> scaleModes = TreePVector.<Pair<String, ScaleMode>>empty()
		.plus(Pair.of("Lock anchors", ScaleMode.NONE))
		.plus(Pair.of("Bind to components", ScaleMode.LOCK_RELATIVELY))
		.plus(Pair.of("Proportional", ScaleMode.SCALE))
		.plus(Pair.of("Stretch", ScaleMode.STRETCH))
		.plus(Pair.of("Adaptive", ScaleMode.ADAPTIVE))
		;
		
		return TreePVector.<EditableProperty>empty()
			.plus(DoubleProperty.create("Line width", lineWidth))
			.plus(DoubleProperty.create("Arrow width", arrowWidth))
			.plus(ChoiceProperty.create("Arrow length", arrowLengths, arrowLength))
			.plus(ChoiceProperty.create("Connection type", connectionTypes, connectionType ))
			.plus(ChoiceProperty.create("Scale mode", scaleModes, scaleMode))
			;
	}
	
	
	public VisualConnection(StorageManager storage, N first, N second) {
		this.storage = storage;
		color = storage.create(defaultColor);
		scaleMode = storage.create(ScaleMode.LOCK_RELATIVELY);
		lineWidth = new BoundedVariable(0.01, 0.5, storage.create(defaultLineWidth));
		arrowWidth = new BoundedVariable(0.1, 1, storage.create(defaultArrowWidth));
		arrowLength = new BoundedVariable(0.1, 1, storage.create(defaultArrowLength));

		this.first = first;
		this.second = second;
		ConnectionGraphicConfiguration polyline = new Polyline(this, storage);
		this.graphic = storage.create(polyline);
	}

	public ModifiableExpression<Color> color() {
		return color;
	}

	public ModifiableExpression<Double> lineWidth() {
		return lineWidth;
	}
	
	public ModifiableExpression<Double> arrowWidth() {
		return arrowWidth;
	}
	
	public ModifiableExpression<Double> arrowLength() {
		return arrowLength;
	}
	
	@Override
	public N getFirst() {
		return first;
	}

	@Override
	public N getSecond() {
		return second;
	}

	public Expression<ConnectionGraphicConfiguration> graphic() {
		return graphic;
	}
	
	public ModifiableExpression<ScaleMode> scaleMode() {
		return scaleMode;
	}*/
}
