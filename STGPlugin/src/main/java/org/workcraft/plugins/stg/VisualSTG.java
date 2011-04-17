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

package org.workcraft.plugins.stg;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.eval;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;

import org.workcraft.annotations.CustomTools;
import org.workcraft.annotations.DefaultCreateButtons;
import org.workcraft.annotations.DisplayName;
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.dom.visual.CustomToolButtons;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.gui.graph.tools.ConnectionController;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.plugins.stg.SignalTransition.Direction;
import org.workcraft.plugins.stg.tools.STGSimulationTool;
import org.workcraft.util.Hierarchy;

public class VisualSTG extends AbstractVisualModel {
	public final STG stg;
	public StorageManager storage;

	public VisualSTG(STG model, StorageManager storage) {
		this (model, null, storage);
	}

	public VisualSTG(STG model, VisualGroup root, StorageManager storage) {
		super(model, root, storage);
		
		this.storage = storage;

		if (root == null)
			try {
				createDefaultFlatStructure();
			} catch (NodeCreationException e) {
				throw new RuntimeException(e);
			}

			this.stg = model;

/*			Collection<VisualPlace> places = new ArrayList<VisualPlace>(Hierarchy.getDescendantsOfType(getRoot(), VisualPlace.class));
			for(VisualPlace place : places)
				maybeMakeImplicit(place);*/
	}

	public VisualPlace createPlace() {
		return createPlace(null);
	}
	
	public VisualPlace createPlace(String name) {
		VisualPlace place = new VisualPlace(stg.createPlace(name), storage);
		add(place);
		return place;
	}

	public VisualSignalTransition createSignalTransition(String signalName, SignalTransition.Type type, Direction direction) {
		SignalTransition transition = stg.createSignalTransition(signalName, direction);
		stg.signalType(transition).setValue(type);
		VisualSignalTransition visualTransition = new VisualSignalTransition(transition, storage);
		add(visualTransition);
		return visualTransition;
	}

	public VisualSignalTransition createSignalTransition() {
		SignalTransition transition = stg.createSignalTransition();
		VisualSignalTransition visualTransition = new VisualSignalTransition(transition, storage);
		add(visualTransition);
		return visualTransition;
	}

	public VisualDummyTransition createDummyTransition() {
		DummyTransition transition = stg.createDummyTransition();
		VisualDummyTransition visualTransition = new VisualDummyTransition(transition, storage);
		add(visualTransition);
		return visualTransition;
	}
	
	public final VisualGroup getRoot() {
		return (VisualGroup) super.getRoot();
	}
}
