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

package org.workcraft.serialisation;

import java.util.UUID;

public class Format {
	public Format(UUID uuid) {
		this.uuid = uuid;
	}
	public final UUID uuid;
	
	public static Format create(UUID uuid) {
		return new Format(uuid);
	}
	
	public static Format create(String uuidStr) {
		return create(UUID.fromString(uuidStr));
	}
	
	public static final Format STG = create("000199d9-4ac1-4423-b8ea-9017d838e45b");
	public static final Format SVG = create("99439c3c-753b-46e3-a5d5-6a0993305a2c");
	public static final Format PS  = create("9b5bd9f0-b5cf-11df-8d81-0800200c9a66");
	public static final Format workcraftXML = create("6ea20f69-c9c4-4888-9124-252fe4345309"); 
	public static final Format defaultVisualXML = create("2fa9669c-a1bf-4be4-8622-007635d672e5");
	public static final Format DOT = create("f1596b60-e294-11de-8a39-0800200c9a66");
	public static final Format EQN = create("58b3c8d0-e297-11de-8a39-0800200c9a66");
	
	public static String getDescription (Format format)
	{
		if (format.equals(STG))
			return ".g (Signal Transition Graph)";
		else if (format.equals(SVG))
			return ".svg (Scalable Vector Graphics)";
		else if (format.equals(PS))
			return ".ps (PostScript)";
		else if (format.equals(workcraftXML))
			return ".xml (Workcraft math model)";
		else if (format.equals(defaultVisualXML))
			return ".xml (Workcraft visual model)";
		else if (format.equals(DOT))
			return ".dot (GraphViz dot)";
		else if (format.equals(EQN))
			return ".eqn (Signal equations)";
		else
			return "Unknown format";
	}
}