package org.workcraft.plugins.stg;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.*;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;

import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.visual.TouchableProvider;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.dom.visual.connections.VisualConnectionGui;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.gui.graph.tools.ConnectionController;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.util.Hierarchy;

public class VisualStgConnectionManager implements ConnectionController<Node> {

	final VisualSTG model;
	final StorageManager storage;
	final TouchableProvider<Node> tp;
	
	public VisualStgConnectionManager(VisualSTG model, StorageManager storage, TouchableProvider<Node> tp) {
		this.model = model;
		this.storage = storage;
		this.tp = tp;
	}

	@Override
	public void validateConnection(Node first, Node second)	throws InvalidConnectionException {
		if (first==second) {
			throw new InvalidConnectionException ("Connections are only valid between different objects");
		}

		if (first instanceof VisualTransition) {
			if (second instanceof VisualConnection)
				if (! (second  instanceof VisualImplicitPlaceArc))
					throw new InvalidConnectionException ();
		}

		if (first instanceof VisualConnection) {
			if (!(first instanceof VisualImplicitPlaceArc))
				throw new InvalidConnectionException ("Only connections with arcs having implicit places are allowed");
			if (second instanceof VisualConnection)
				throw new InvalidConnectionException ("Arcs between places are not allowed");
			if (second instanceof VisualPlace)
				throw new InvalidConnectionException ("Arcs between places are not allowed");

			VisualImplicitPlaceArc con = (VisualImplicitPlaceArc) first;
			if (con.getFirst() == second || con.getSecond() == second)
				throw new InvalidConnectionException ();
		}
	}

	@Override
	public void connect(Node first,	Node second)  throws InvalidConnectionException {
		createConnection(first, second);
	}
	
	public VisualConnection createConnection(Node first,	Node second) throws InvalidConnectionException {
		validateConnection(first, second);

		if (first instanceof VisualStgTransition) {
			if (second instanceof VisualStgTransition) {
				return createImplicitPlaceConnection((VisualStgTransition) first, (VisualStgTransition) second);
			} else if (second instanceof VisualImplicitPlaceArc) {
				VisualImplicitPlaceArc con = (VisualImplicitPlaceArc)second;
				VisualPlace place = makeExplicit(con);
				return createConnection (first, place);
			} else if (second instanceof VisualPlace) {
				return createSimpleConnection((VisualComponent) first, (VisualComponent) second);
			}
			else throw new InvalidConnectionException("invalid connection");
		} else if (first instanceof VisualImplicitPlaceArc) {
			if (second instanceof VisualStgTransition) {
				VisualImplicitPlaceArc con = (VisualImplicitPlaceArc)first;
				VisualPlace place = makeExplicit(con);
				return createConnection(place, second);
			}
			else throw new InvalidConnectionException("invalid connection");
		} else {
			return createSimpleConnection((VisualComponent) first, (VisualComponent) second);
		}
	}

	private VisualImplicitPlaceArc createImplicitPlaceConnection(VisualStgTransition t1,
			VisualStgTransition t2) throws InvalidConnectionException {
		final ConnectionResult connectResult = model.stg.connect(t1.getReferencedTransition().getTransition(), t2.getReferencedTransition().getTransition());

		STGPlace implicitPlace = connectResult.getImplicitPlace();
		MathConnection con1 = connectResult.getCon1();
		MathConnection con2 = connectResult.getCon2();

		if (implicitPlace == null || con1 == null || con2 == null)
			throw new NullPointerException();

		VisualImplicitPlaceArc result = new VisualImplicitPlaceArc(t1, t2, con1, con2, implicitPlace, storage);
		model.add(Hierarchy.getNearestContainer(t1, t2), result);
		return result;
	}

	private VisualConnection createSimpleConnection(final VisualComponent firstComponent,
			final VisualComponent secondComponent)
	throws InvalidConnectionException {
		ConnectionResult mathConnection = model.stg.connect(
				firstComponent.getReferencedComponent(), 
				secondComponent.getReferencedComponent());

		MathConnection con = mathConnection.getSimpleResult();

		if (con == null)
			throw new NullPointerException();

		VisualConnection result = new VisualConnection(con, firstComponent, secondComponent, storage);
		model.add(Hierarchy.getNearestContainer(firstComponent, secondComponent), result);
		return result;
	}

	private void maybeMakeImplicit (VisualPlace place) {
		final STGPlace stgPlace = (STGPlace)place.getReferencedPlace();
		if ( eval(stgPlace.implicit()) ) {

			MathConnection refCon1 = null, refCon2 = null;

			VisualComponent first = (VisualComponent) eval(model.nodeContext()).getPreset(place).iterator().next();
			VisualComponent second = (VisualComponent) eval(model.nodeContext()).getPostset(place).iterator().next();

			Collection<Connection> connections = new ArrayList<Connection> (eval(model.nodeContext()).getConnections(place));
			for (Connection con: connections)
				if (con.getFirst() == place)
					refCon2 = ((VisualConnection)con).getReferencedConnection();
				else if (con.getSecond() == place)
					refCon1 = ((VisualConnection)con).getReferencedConnection();


			VisualImplicitPlaceArc con = new VisualImplicitPlaceArc(first, second, refCon1, refCon2, (STGPlace)place.getReferencedPlace(), storage);

			Hierarchy.getNearestAncestor(
					Hierarchy.getCommonParent(first, second), Container.class)
					.add(con);

			model.remove(place);
			// connections will get removed automatically by the hanging connection remover
		}
	} 

	private VisualPlace makeExplicit(VisualImplicitPlaceArc con) {
		Container group = Hierarchy.getNearestAncestor(con, Container.class);

		STGPlace implicitPlace = con.getImplicitPlace();
		
		model.stg.makeExplicit(implicitPlace);
		
		VisualPlace place = new VisualPlace(implicitPlace, storage);
		Point2D p = eval(VisualConnectionGui.getConnectionGui(TouchableProvider.Util.podgonHideMaybe(tp), con).parametricCurve()).getPointOnCurve(0.5);
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
