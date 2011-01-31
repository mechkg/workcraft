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

package org.workcraft.plugins.circuit;

import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.AbstractModel;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathGroup;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.math.MathNode;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.util.Hierarchy;

public class Circuit extends AbstractModel implements MathModel {

	private final StorageManager storage;

	public Circuit(StorageManager storage) {
		this(new MathGroup(storage), storage);
	}
	
	public Circuit(MathGroup root, StorageManager storage) {
		super(createDefaultModelSpecification(root));
		this.storage = storage;
	}

	public void validate() throws ModelValidationException {
	}

	public MathConnection connect(Node first, Node second) throws InvalidConnectionException {
		
		MathConnection con = new MathConnection((MathNode)first, (MathNode)second, storage);
		
		add(Hierarchy.getNearestContainer(first, second), con);
		
		return con;
	}

}
