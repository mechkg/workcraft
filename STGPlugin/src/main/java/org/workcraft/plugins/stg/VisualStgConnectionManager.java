package org.workcraft.plugins.stg;

import java.awt.geom.Point2D;

import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.Container;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.dom.visual.connections.VisualConnectionGui;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.util.Hierarchy;

public class VisualStgConnectionManager {

	final VisualSTG model;
	final StorageManager storage;
	
	private VisualPlace makeExplicit(VisualImplicitPlaceArc con) {
		Container group = Hierarchy.getNearestAncestor(con, Container.class);

		STGPlace implicitPlace = con.getImplicitPlace();
		
		model.stg.makeExplicit(implicitPlace);
		
		VisualPlace place = new VisualPlace(implicitPlace, storage);
		VisualConnectionGui.
		Point2D p = con.getPointOnConnection(0.5);
		place.position().setValue(p);

		VisualConnection con1 = new VisualConnection(con.getRefCon1(), con.getFirst(), place, storage);
		VisualConnection con2 = new VisualConnection(con.getRefCon2(), place, con.getSecond(), storage);

		group.add(place);
		group.add(con1);
		group.add(con2);

		model.remove(con);
		return place;
	}

}
