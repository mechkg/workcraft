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

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.*;

import java.awt.geom.Point2D;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.DummyMouseListener;
import org.workcraft.gui.graph.tools.HitTester;
import org.workcraft.util.Function;
import org.workcraft.util.Nothing;

public class DefaultAnchorGenerator extends DummyMouseListener {
	
	Function<? super Node, ? extends Expression<? extends Touchable>>tp;
	HitTester<Node> hitTester;

	public DefaultAnchorGenerator(HitTester<Node> hitTester, Function<? super Node, ? extends Expression<? extends Touchable>> tp) {
		this.hitTester = hitTester;
		this.tp = tp;
	}
	
	@Override
	public void mouseClicked(GraphEditorMouseEvent e) {
		if (e.getClickCount()==2) {
			final Point2D location = e.getPosition();
			Node hitNode = hitTester.hitTest(location);
			if (hitNode instanceof VisualConnection) {
				VisualConnection con = (VisualConnection)hitNode;
				final VisualConnectionProperties connectionProps = eval(VisualConnectionGui.getConnectionProperties(tp, con));
				ConnectionGraphicConfiguration g = eval(con.graphic());
				g.accept(new ConnectionGraphicConfigurationVisitor<Nothing>() {

					@Override
					public Nothing visitPolyline(Polyline polyline) {
						PolylineGui.createPolylineControlPoint(connectionProps, polyline, location);
						return Nothing.VALUE;
					}

					@Override
					public Nothing visitBezier(Bezier bezier) {
						return Nothing.VALUE;
					}
				});
			}
		}
	}
}
