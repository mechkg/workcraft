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

package org.workcraft.dom.visual;

import java.util.Collection;
import java.util.Collections;

import javax.swing.JPopupMenu;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.Expressions;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.PopupMenuBuilder.PopupMenuSegment;
import org.workcraft.gui.actions.ScriptedActionListener;
import org.workcraft.gui.propertyeditor.EditableProperty;
import org.workcraft.gui.propertyeditor.Properties;

import pcollections.PVector;
import pcollections.TreePVector;


public abstract class VisualNode implements Properties, Node {

	public VisualNode(StorageManager storage) {
		parent = storage.create(null);
	}
	
	public Expression<? extends Collection<? extends Node>> children() {
		return Expressions.constant(Collections.<Node>emptyList());
	}
	
	private final ModifiableExpression<Node> parent;
	
	private PopupMenuBuilder popupMenuBuilder = new PopupMenuBuilder();

	public ModifiableExpression<Node> parent() {
		return parent;
	}
	
	protected final void addPopupMenuSegment (PopupMenuSegment segment) {
		popupMenuBuilder.addSegment(segment);
	}
	
	public final JPopupMenu createPopupMenu(ScriptedActionListener actionListener) {
		return popupMenuBuilder.build(actionListener);
	}
	
	public PVector<EditableProperty> getProperties() {
		return TreePVector.empty();
	}

	public Expression<Boolean> hidden() {
		return Expressions.constant(false);
	}
}
