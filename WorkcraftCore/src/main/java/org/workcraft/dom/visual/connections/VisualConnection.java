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
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.user.CachedHashSet;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpressionBase;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpressionFilter;
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.DependentNode;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.exceptions.NotSupportedException;
import org.workcraft.gui.propertyeditor.EditableProperty;
import org.workcraft.gui.propertyeditor.choice.ChoiceProperty;
import org.workcraft.gui.propertyeditor.dubble.DoubleProperty;
import org.workcraft.serialisation.xml.NoAutoSerialisation;
import org.workcraft.util.Pair;

import pcollections.PVector;
import pcollections.TreePVector;

public class VisualConnection extends VisualNode implements
		Node, Connection,
		DependentNode {
	
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
	
	private MathConnection refConnection;
	private VisualComponent first;
	private VisualComponent second;

	private final ModifiableExpression<ScaleMode> scaleMode;
	
	private final ModifiableExpression<ConnectionGraphicConfiguration> graphic = new ModifiableExpressionBase<ConnectionGraphicConfiguration>(){
		@Override
		protected ConnectionGraphicConfiguration evaluate(EvaluationContext context) {
			return context.resolve(children).iterator().next();
		};
		@Override
		public void setValue(ConnectionGraphicConfiguration newValue) {
			children.clear();
			children.add(newValue);
		};
	};

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
	public static double HIT_THRESHOLD = 0.2;
	private static final Color defaultColor = Color.BLACK;

	private final ModifiableExpression<Color> color;
	private final ModifiableExpression<Double> lineWidth;
	private final ModifiableExpression<Double> arrowWidth;
	private final ModifiableExpression<Double> arrowLength;
	
	private CachedHashSet<ConnectionGraphicConfiguration> children;
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
	
	@Override
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
		
		return super.getProperties()
			.plus(DoubleProperty.create("Line width", lineWidth))
			.plus(DoubleProperty.create("Arrow width", arrowWidth))
			.plus(ChoiceProperty.create("Arrow length", arrowLengths, arrowLength))
			.plus(ChoiceProperty.create("Connection type", connectionTypes, connectionType ))
			.plus(ChoiceProperty.create("Scale mode", scaleModes, scaleMode))
			;
	}

	public VisualConnection(StorageManager storage) {
		super(storage);
		this.storage = storage;
		color = storage.create(defaultColor);
		scaleMode = storage.create(ScaleMode.LOCK_RELATIVELY);
		lineWidth = new BoundedVariable(0.01, 0.5, storage.create(defaultLineWidth));
		arrowWidth = new BoundedVariable(0.1, 1, storage.create(defaultArrowWidth));
		arrowLength = new BoundedVariable(0.1, 1, storage.create(defaultArrowLength));
	}
	
	public void setVisualConnectionDependencies(VisualComponent first, VisualComponent second, ConnectionGraphicConfiguration graphic, MathConnection refConnection) {
		if(first == null)
			throw new NullPointerException("first");
		if(second == null)
			throw new NullPointerException("second");
		if(graphic == null)
			throw new NullPointerException("graphic");
	
		this.first = first;
		this.second = second;
		this.refConnection = refConnection;
		this.children = new CachedHashSet<ConnectionGraphicConfiguration>(storage);
		this.children.add(graphic);
	}
	
	public VisualConnection(MathConnection refConnection, VisualComponent first, VisualComponent second, StorageManager storage) {
		this(storage);
		this.refConnection = refConnection;
		this.first = first;
		this.second = second;
		this.children = new CachedHashSet<ConnectionGraphicConfiguration>(storage);
		this.children.add(new Polyline(this, storage));
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
	
	public MathConnection getReferencedConnection() {
		return refConnection;
	}
	
	@Override
	public VisualComponent getFirst() {
		return first;
	}

	@Override
	public VisualComponent getSecond() {
		return second;
	}

	@Override
	public Set<MathNode> getMathReferences() {
		Set<MathNode> ret = new HashSet<MathNode>();
		MathConnection refCon = getReferencedConnection();
		if(refCon!=null)
			ret.add(refCon);
		return ret;
	}

	@NoAutoSerialisation
	public Expression<ConnectionGraphicConfiguration> graphic() {
		return graphic;
	}
	
	@Override
	public Expression<? extends Collection<? extends Node>> children() {
		return children;
	}
	
	@NoAutoSerialisation
	public ModifiableExpression<ScaleMode> scaleMode() {
		return scaleMode;
	}
}
