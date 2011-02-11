package org.workcraft.plugins.stg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.Node;
import org.workcraft.dom.references.ReferenceManager;
import org.workcraft.exceptions.FormatException;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.stg.SignalTransition.Direction;
import org.workcraft.plugins.stg.SignalTransition.Type;
import org.workcraft.plugins.stg.javacc.generated.DotGParser;
import org.workcraft.plugins.stg.javacc.generated.ParseException;
import org.workcraft.util.Pair;
import org.workcraft.util.Triple;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.*;

public class DotGParserHelper {

	private Map<String, Type> signals;
	private Map<Pair<Node, Node>, STGPlace> implicitPlaces;
	public STG stg;
	private final DotGParser parser;

	public DotGParserHelper(DotGParser parser, StorageManager storage) {
		this.parser = parser;
		signals = new HashMap<String, Type>();
		stg = new STG(storage);
		implicitPlaces = new HashMap<Pair<Node, Node>, STGPlace>();
	}

	public STGPlace getPlace(String name) throws ParseException
	{
		STGPlace result = (STGPlace) eval(stg.referenceManager()).getNodeByReference (name);

	   if(result == null)
			throw createParseException("Place '" + name + "' was not found");
	   	else
		return result; 
      
	}
	
	public Node getOrCreate (String name) throws ParseException {

		Type t = signals.get (name);
		
		Node existing = eval(stg.referenceManager()).getNodeByReference(name);
		
		if (existing == null)
			existing = eval(stg.referenceManager()).getNodeByReference(name+"/0");
			
		if (existing == null)
		{
			if (t==null)
				existing = stg.createPlace(name);
			else if (t.equals(Type.DUMMY))
				existing = stg.createDummyTransition(name);
			else
				return getOrCreate(Triple.of(name, Direction.TOGGLE, 0));
				//throw new FormatException (name + " was declared as " + t + ", but is referenced as DUMMY. Transition direction tag (+,-,~) expected.");
		}
		
		return existing;
	}
	
	public Node getOrCreate (Pair<String, Integer> ref) throws ParseException
	{
		String reference = stg.makeReference(ref);
		String name = ref.getFirst();
		Node existing = eval(stg.referenceManager()).getNodeByReference(reference);
		
		if (existing == null)
		{
			Type t = signals.get (name);
			if (t == null || !t.equals(Type.DUMMY))
				return getOrCreate(Triple.of(name, Direction.TOGGLE, ref.getSecond()));
				//throw new FormatException (name + " is referenced as DUMMY but was not declared as such.");
			else
			{
				DummyTransition dt = stg.createDummyTransition(null);
				stg.setName(dt, reference);
				dt.name().setValue(name);
				existing = dt;
			}
		}
		
		return existing;
	}
	
	public Node getOrCreate (Triple<String, Direction, Integer> ref) throws ParseException {
		String reference = stg.makeReference(ref);
		String name = ref.getFirst(); 
		Node existing = eval(stg.referenceManager()).getNodeByReference (reference);
		
		if (existing == null)
		{
		
			SignalTransition st = stg.createSignalTransition();
			stg.setName (st, reference);
			Type t = signals.get(name);
			if (t==null)
				throw createParseException("Undeclared signal encountered: " + name + 
				" ("+reference+"). Possibly malformed header.");
			st.signalType().setValue(t);
			existing = st;
		}
		
		return existing;
	}
	
	public void createArc (Node first, Node second) {

		try {
			ConnectionResult result = stg.connect(first, second);
			STGPlace implicitPlace = result.getImplicitPlace();
			
			if (implicitPlace != null)
				implicitPlaces.put (Pair.of (first, second), implicitPlace);
				
		} catch (InvalidConnectionException e)
		{
			throw new FormatException ("Cannot create arc from " + eval(stg.referenceManager()).getNodeReference(first) +
			 " to " + eval(stg.referenceManager()).getNodeReference(second) + ".", e);
		}
	}
	
	public void setSignalsType (List<String> list, Type type) throws ParseException {
		for (String signal : list) {
			if (signals.containsKey(signal))
			{
				Type prevType = signals.get(signal);
				if (prevType != null && prevType.equals(type))
					throw createParseException (type + " signal \"" + signal + "\" was already listed as " + prevType);
			}
			else
				signals.put(signal, type);
		}
	}

	private ParseException createParseException(String message) {
		return new ParseException(message +  "(at line " + parser.token.beginLine + ", column " + parser.token.beginColumn + ").");
	}

	public STGPlace getImplicitPlace(Node t1, Node t2) throws ParseException {
		STGPlace place = implicitPlaces.get(Pair.of(t1, t2));
		ReferenceManager refMan = eval(stg.referenceManager());
		if(place == null) {
			throw createParseException(String.format("Implicit place between transitions '%s' and '%s' not found", refMan.getNodeReference(t1), refMan.getNodeReference(t2)));
		}
		return place;
	}
	
}
