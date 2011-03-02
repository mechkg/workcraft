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

import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dom.Container;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathModel;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.interop.ServiceHandle;


public interface VisualModel extends Model {
	ServiceHandle<VisualModel> SERVICE_HANDLE = ServiceHandle.createNewService(VisualModel.class, "A visual model");
	public ModifiableExpression<Container> currentLevel();
	public MathModel getMathModel();
	
	public void connect(Node first, Node second) throws InvalidConnectionException;
	public void validateConnection(Node first, Node second) throws InvalidConnectionException;
	
	public Node group(Collection<Node> nodes);
	public Collection<Node> ungroup(Collection<Node> nodes);
}
