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

package org.workcraft.plugins.layout;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.eval;
import static org.workcraft.dependencymanager.advanced.core.GlobalCache.setValue;

import org.workcraft.Tool;
import org.workcraft.ToolJob;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualTransformableNode;
import org.workcraft.interop.ModelService;
import org.workcraft.interop.ModelServices;
import org.workcraft.interop.ServiceNotAvailableException;
import org.workcraft.workspace.WorkspaceEntry;

public class NullLayout implements Tool {

	@Override
	public String getSection() {
		return "Layout";
	}

	@Override
	public ToolJob applyTo(WorkspaceEntry entry) throws ServiceNotAvailableException {
		final ModelServices services = entry.getModelEntry();
		final VisualModel model = services.getImplementation(ModelService.LegacyVisualModelService);
		return new ToolJob() {
			
			@Override
			public void run() {
				for (Node n : eval(model.getRoot().children())) {
					if (n instanceof VisualTransformableNode) {
						setValue(((VisualTransformableNode)n).x(),(Double)0.0);
						setValue(((VisualTransformableNode)n).y(),(Double)0.0);
					}
				}
			}
		};
	}

	@Override
	public String getDisplayName() {
		return "Reset layout";
	}
}