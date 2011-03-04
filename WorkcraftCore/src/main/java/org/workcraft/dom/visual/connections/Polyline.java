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

package org.workcraft.dom.visual.connections;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.core.Expressions;
import org.workcraft.dependencymanager.advanced.core.GlobalCache;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dependencymanager.advanced.user.Variable;
import org.workcraft.dom.ArbitraryInsertionGroupImpl;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.DrawHelper;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.ColorisableGraphicalContent;
import org.workcraft.dom.visual.ReflectiveTouchable;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.exceptions.NotImplementedException;
import org.workcraft.gui.Coloriser;
import org.workcraft.util.Geometry;

import pcollections.PCollection;

public class Polyline implements Node, Container, ConnectionGraphicConfiguration {
	
	private ArbitraryInsertionGroupImpl groupImpl;
	private final StorageManager storage; 
	
	
	public Polyline(VisualConnection parent, StorageManager storage) {
		this.storage = storage;
		groupImpl = new ArbitraryInsertionGroupImpl(this, parent, storage);
	}

	public void createControlPoint(int index, Point2D userLocation) {
		ControlPoint ap = new ControlPoint(storage);
		GlobalCache.setValue(ap.position(), userLocation);
		GlobalCache.setValue(ap.parent(), this);
		groupImpl.add(index, ap);
	}
	
	@Override
	public Expression<? extends Collection<Node>> children() {
		return groupImpl.children();
	}

	public ModifiableExpression<Node> parent() {
		return groupImpl.parent();
	}

	public void setParent(Node parent) {
		throw new RuntimeException ("Node does not support reparenting");
	}

	public void add(Collection<Node> nodes) {
		groupImpl.add(nodes);
	}

	public void add(Node node) {
		groupImpl.add(node);
	}

	public void remove(Collection<Node> nodes) {
		groupImpl.remove(nodes);
	}

	public void remove(Node node) {
		groupImpl.remove(node);
	}

	public void reparent(Collection<Node> nodes, Container newParent) {
		groupImpl.reparent(nodes, newParent);
	}

	public void reparent(Collection<Node> nodes) {
		groupImpl.reparent(nodes);
	}

	@Override
	public <T> T accept(ConnectionGraphicConfigurationVisitor<T> visitor) {
		return visitor.visitPolyline(new PolylineConfiguration() {
			
			@Override
			public Expression<? extends PCollection<Point2D>> controlPoints() {
				throw new NotImplementedException();
			}
		});
	}
}