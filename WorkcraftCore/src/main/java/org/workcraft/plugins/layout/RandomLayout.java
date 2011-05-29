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

import java.util.Random;

import org.workcraft.Tool;
import org.workcraft.ToolJob;
import org.workcraft.dependencymanager.advanced.core.GlobalCache;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualTransformableNode;
import org.workcraft.interop.ModelService;
import org.workcraft.interop.ModelServices;
import org.workcraft.interop.ServiceNotAvailableException;
import org.workcraft.workspace.WorkspaceEntry;

public class RandomLayout implements Tool {
	Random r = new Random();

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
				for (Node n : GlobalCache.eval(model.getRoot().children())) {
					if (n instanceof VisualTransformableNode) {
						GlobalCache.setValue(((VisualTransformableNode)n).x(),(eval(RandomLayoutSettings.startX) + r.nextDouble()*eval(RandomLayoutSettings.rangeX)));
						GlobalCache.setValue(((VisualTransformableNode)n).y(),(eval(RandomLayoutSettings.startY) + r.nextDouble()*eval(RandomLayoutSettings.rangeY)));
					}
				}	
			}
		};
	}
	
	@Override
	public String getDisplayName() {
		return "Randomize layout";
	}
}
