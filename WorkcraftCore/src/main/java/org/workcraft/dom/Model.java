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

package org.workcraft.dom;

import java.util.Collection;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dom.references.ReferenceManager;
import org.workcraft.gui.propertyeditor.Properties;


public interface Model {
	public void setTitle(String title);
	public String getTitle();
	
	public Expression<? extends ReferenceManager> referenceManager();
	
	public Node getRoot();
	
	public void add (Node node);
	public void add (Container parent, Node node);
	public void remove (Node node);
	public void remove (Collection<? extends Node> nodes);
	
	public Properties getProperties(Node node);
	
	public Expression<? extends NodeContext> nodeContext();
}