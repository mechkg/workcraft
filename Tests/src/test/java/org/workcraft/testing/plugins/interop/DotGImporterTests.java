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

package org.workcraft.testing.plugins.interop;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.Collection;

import junit.framework.Assert;

import org.junit.Test;
import org.workcraft.dom.Connection;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.interop.DotGImporter;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.stg.DefaultStorageManager;
import org.workcraft.plugins.stg.DummyTransition;
import org.workcraft.plugins.stg.HistoryPreservingStorageManager;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.STGModel;
import org.workcraft.plugins.stg.STGPlace;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.SignalTransition.Direction;
import org.workcraft.plugins.stg.javacc.generated.DotGParser;
import org.workcraft.plugins.stg.javacc.generated.ParseException;
import org.workcraft.util.Hierarchy;
import org.workcraft.util.Import;
import org.workcraft.workspace.ModelEntry;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.*;

import static org.junit.Assert.*;

public class DotGImporterTests {
	@Test
	public void Test1() throws IOException, DeserialisationException
	{
		File tempFile = File.createTempFile("test", ".g");
		
		FileOutputStream fileStream = new FileOutputStream(tempFile);
		
		OutputStreamWriter writer = new OutputStreamWriter(fileStream);
		
		writer.write("\n");
		writer.write("   #test \n");
		writer.write("   # for DotGImporter\n");
		writer.write("\n");
		writer.write(".outputs  x\t y   z\n");
		writer.write("\n");
		writer.write(".inputs  a\tb \tc\n");
		writer.write("\n");
		writer.write(" \t.graph\n");
		writer.write("a+ p1 p2\n");
		writer.write("b+ p1 p2\n");
		writer.write(" c+  p1 \t p2\n");
		writer.write("\n");
		writer.write("p1 z+ y+ x+\n");
		writer.write("p2 z+ y+ x+\n");
		writer.write("\n");
		writer.write(".marking { }\n");
		writer.write(".end\n");
		
		writer.close();
		fileStream.close();
		
		ModelEntry importedEntry =  Import.importFromFile(new DotGImporter(), tempFile);
		STG imported = (STG)importedEntry.getModel();
		
		Assert.assertEquals(6, Hierarchy.getChildrenOfType(imported.getRoot(), Transition.class).size());
		Assert.assertEquals(2, Hierarchy.getChildrenOfType(imported.getRoot(), Place.class).size());
		Assert.assertEquals(12, Hierarchy.getChildrenOfType(imported.getRoot(), Connection.class).size());
	}
	
	@Test
	public void Test2() throws Throwable
	{
		final InputStream test = ClassLoader.getSystemClassLoader().getResourceAsStream("test2.g");
		STGModel imported = new DotGImporter().importSTG(test, new HistoryPreservingStorageManager());//DotGImporterTests.class.getClassLoader().getResourceAsStream("test2.g"));
		Assert.assertEquals(17, imported.getTransitions().size());
		Assert.assertEquals(0, imported.getDummies().size());
		
		int explicitPlaces = 0;
		for(Place p : imported.getPlaces())
		{
			if(!eval(((STGPlace)p).implicit())) explicitPlaces ++;
		}
		
		Assert.assertEquals(2, explicitPlaces);
		
		Assert.assertEquals(18, imported.getPlaces().size());
		
		for(Transition t : imported.getTransitions())
		{
			Assert.assertTrue(eval(imported.nodeContext()).getPreset(t).size()>0);
			Assert.assertTrue(eval(imported.nodeContext()).getPostset(t).size()>0);
		}
	}
	
	static STG parse(String resourceName) throws ParseException {
		InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream(resourceName);
		try{
			return new DotGParser(inputStream).parse(new DefaultStorageManager());
		}
		finally{
			try {
				inputStream.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	private void testForParseError(String resourceName, int line, int column) {
		final InputStream test = ClassLoader.getSystemClassLoader().getResourceAsStream(resourceName);
		DotGParser parser = new DotGParser(test);
		try { parser.parse(new DefaultStorageManager()); }
		catch(ParseException ex) { 
			Assert.assertTrue("line number information must be contained in: '" + ex.getMessage() + "'", ex.getMessage().contains("line "+line));
			Assert.assertTrue("column number information must be contained in: '" + ex.getMessage() + "'", ex.getMessage().contains("column "+column));
			return; }
		Assert.fail("should have thrown a ParseException");
	}
	
	@Test
	public void testGoodPlaceReference() throws ParseException {
		STG stg = parse("goodPlaceReference.g");
		Collection<STGPlace> places = stg.getPlaces();
		Collection<SignalTransition> transitions = stg.getSignalTransitions();
		Collection<DummyTransition> dummies = stg.getDummies();
		assertEquals(0, dummies.size());
		assertEquals(1, places.size());
		assertEquals(1, transitions.size());
		STGPlace place = places.iterator().next();
		SignalTransition transition = transitions.iterator().next();
		assertEquals("cleverPlace", eval(stg.name(place)));
		assertEquals("a", eval(stg.signalName(transition)));
		assertEquals(0, eval(stg.instanceNumber(transition)).intValue());
		assertEquals(Direction.PLUS, eval(stg.direction(transition)));
	}
	
	@Test
	public void testBadPlaceReference() {
		testForParseError("badPlaceReference.g", 4, 12);
	}
	
	@Test
	public void testBadImplicitPlaceReference() {
		testForParseError("badImplicitPlaceReference.g", 4, 13);
	}	
	
	@Test
	public void testBadImplicitPlaceReference2() {
		testForParseError("badImplicitPlaceReference2.g", 6, 22); // column information needs fixing
	}
	
	@Test
	public void testBadImplicitPlaceReference3() {
		testForParseError("badImplicitPlaceReference3.g", 7, 18); // column information needs fixing
	}
}
