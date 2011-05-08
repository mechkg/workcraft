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

package org.workcraft.testing.plugins.petri;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.MovableHelper;
import org.workcraft.dom.visual.MovableNew;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.interop.ServiceHandle;
import org.workcraft.interop.ServiceProvider;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petri.PetriNetModelDescriptor;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.plugins.stg.DefaultStorageManager;
import org.workcraft.plugins.stg.HistoryPreservingStorageManager;
import org.workcraft.util.Hierarchy;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.*;

public class SaveLoadTests {

    private static final String testDataMathModel = "504b03041400080008007772263e000000000000000000000000090000006d6f64656c2e786d6cad54cd4e843010beef5334bd5b1688898765f760a2f1a0f1b03e4003b32c6e9921d382faf6b684dd88d1a0814ba77fdf4f275fbad9bdd74674c0b622cc64acd65200e654545866f2657f77752377dbd5a6a6028cc88db63693c4a57a233ee5ac0f4e35a62d2bb4aa01c7957a0ee3133829180e99941ecb44ee676841b5aab53baa473fdc33b5cd00432fb70ed830f993acd1390ce026cce380ee777d6d981a60f771667ad59d564663a91ed041092c05ea1a32e9e80468a5e8b469fdf25a461e1d9d69a2e0668ea9640953e9c2a6d2254c25f34ced59a3ad9ccfe0e0cc858db8279dc7914c708c22784b88907fa1c809fb188e0fc5a162eb2e411316fcbd62647aba0193c2f1efc283d0457808d742c2c93f85d3a584d38956a7df5a3d7e7114fe9950fbbf6abbfa04504b0708160a619e29010000db040000504b03041400080008007772263e000000000000000000000000040000006d6574615d4ebb0e823014ddfd0a72f756846220a1ba18378d837e40532ed8485b725b7cfcbdd5c1c1e50ce7dd6e9f76ccee48c1782761c573c8d069df193748b89cf7ac86ed66d13e3cdd34a93e328b5125a2c3a0c94cd153a6471582044f03ffd9f834ce8371814f18c9f0d3078f180fbec371f78bc232355915af6933d28b39655182fd9878fa0559ef29c96c9e4d27618daac8fb75c374a30513755db36655085654458fa214559937dfc6e5ffdb37504b0708fcd2ba71ad000000e6000000504b010214001400080008007772263e160a619e29010000db0400000900000000000000000000000000000000006d6f64656c2e786d6c504b010214001400080008007772263efcd2ba71ad000000e60000000400000000000000000000000000600100006d657461504b05060000000002000200690000003f0200000000";
    private static final String testDataVisualModel = "504b03041400080008007872263e000000000000000000000000090000006d6f64656c2e786d6cad54cd4e843010beef5334bd5b1688898765f760a2f1a0f1b03e4003b32c6e9921d382faf6b684dd88d1a0814ba77fdf4f275fbad9bdd74674c0b622cc64acd65200e654545866f2657f77752377dbd5a6a6028cc88db63693c4a57a233ee5ac0f4e35a62d2bb4aa01c7957a0ee3133829180e99941ecb44ee676841b5aab53baa473fdc33b5cd00432fb70ed830f993acd1390ce026cce380ee777d6d981a60f771667ad59d564663a91ed041092c05ea1a32e9e80468a5e8b469fdf25a461e1d9d69a2e0668ea9640953e9c2a6d2254c25f34ced59a3ad9ccfe0e0cc858db8279dc7914c708c22784b88907fa1c809fb188e0fc5a162eb2e411316fcbd62647aba0193c2f1efc283d0457808d742c2c93f85d3a584d38956a7df5a3d7e7114fe9950fbbf6abbfa04504b0708160a619e29010000db040000504b03041400080008007872263e0000000000000000000000000f00000076697375616c4d6f64656c2e786d6ced9a5d6f9b301486eff72b10da6da93f8080d4b4eaaa6eaad675d5da55dad5648ca1acc6b60c49d37f3f432049aba4599bac2455ae8c8f3907fbf593574038381ae5dc1a325d6452f46de800db6282ca381369dffe79fd792fb08f0e3f1ce43266dca29c1445df963a75eea5bea39a24a5a3f820cd44e12856eaccb9c98a01e197d5f1052b6d4bb3a46fdba68296b29c5f2096b933acd39aec2f5a0e54930aaadc99b0e9292d15d3e5435bed0f191287dc974eca4ca5e324c904bbd6441489d4b96de5c4cc65642a8dccea14b0c0083c69e7c66d4b909cf5ed7252697fd1b53911a9f3494ace8868d36eb33866a637247c60ba09e105ab2bec3f5eccb837992e8938bb305abf9765ce5bd878e4b955bee64a4d3d619a1770ca09650d69704a5a1d1e475575580f2d9ee9992859caf4444a79c7443199a9b744d12b331d91b6c99c448c4f7217a7ae0304e8c49862e86100b18f141a0ff93d4c3083184451a2d07fe6a3567aa2fb89cc95144c943bc11609362b5107deb1010abcb9a5ec8b55ad052db616b48ab5e00db61610c64992d0280cfc1e52b08e919081280c51ec1b6214dc79cb8629d6b9b9742fc116ba0b5eec2e781577411bec2ed8232c047e104709220ad7b1c4832ca4380061a0f6bc9db96c96609d7b4be70a6c89b5d4d3ccca4c8ac65f5cfbf196d563e3a15aa9254f479d52effa28423ea52e0ac266cf11a5907a947ad8f70d076fb1e9b562db64169dcbd6bd5b742ec1b6da85b7c42e963cf174c93d4a20c11e0a1870dd76cfa338a61ef0935e40a39d5b6ca46a9d9b45e70a6ca057ccbcd5a75208462baa0ba7ddb136d29846cf9e01be1db3924c1765f580333ec9d4318705336ddcf8ccd375bd6a0e1faf28e1ec9b594af5e7c720df5b57b1b1a4c534d0a87afefde4ebef1fa7e7c7d76737a7e7bfd6c442aa89bacde88ba4b894fc811bf01b8583c73fabf6bc6da30982e77082539cc014277787d3da7182f09df0849fe3c99df284a63ccd7df5b2e369359edc77c293ff8f3cc1294f736f9b773cadc653af5b9ef6ab8f22aab6febce2f0c35f504b07086b1d6842f70200008e210000504b03041400080008007872263e000000000000000000000000040000006d657461a590cd6ec2301084ef3c45b4779b100c4a245c2e885b2a0ee5012c6713acfa275a3b69fbf6982221c195cb1e6667bf19ed6effeb6c31234513bc84152fa140af4367fc20e1fc756435ec3f16bb9f40df9a549f98c3a4b2d061d464c614a8d056c52821d0c01f363eda69303ef21113197ebacd4f4c6de8d01e1ea7b0cc24a7d2256726fa635e3994e06e269e7b41d107ca6b364da693b0455595fdb661bad18289baae59b3aa04ab36558f622d36ebb2f927ce264eca3e31ef52fb2e79f9fa872b504b070895a11fc5c100000040010000504b010214001400080008007872263e160a619e29010000db0400000900000000000000000000000000000000006d6f64656c2e786d6c504b010214001400080008007872263e6b1d6842f70200008e2100000f000000000000000000000000006001000076697375616c4d6f64656c2e786d6c504b010214001400080008007872263e95a11fc5c1000000400100000400000000000000000000000000940400006d657461504b05060000000003000300a6000000870500000000";
	
	InputStream stringToStream(String string) throws IOException
	{
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		OutputStreamWriter writer = new OutputStreamWriter(bytes);
		writer.write(string);
		writer.close();
		
		return new ByteArrayInputStream(bytes.toByteArray());
	}
	
	@Test
	public void TestMathModelLoad() throws Exception
	{
		Framework framework = new Framework();
		framework.getPluginManager().loadManifest();
		
		ServiceProvider modelEntry = framework.load(new Base16Reader(testDataMathModel));
		PetriNet petri = (PetriNet)modelEntry.getImplementation(ServiceHandle.LegacyMathModelService);

		Assert.assertNotNull(petri);
		
		assertPetriEquals(petri, buildSamplePetri());
	}

	@Test
	public void TestVisualModelLoad() throws Exception
	{
		Framework framework = new Framework();
		framework.getPluginManager().loadManifest();
		
		ServiceProvider modelEntry = framework.load(new Base16Reader(testDataVisualModel));
		VisualPetriNet petriVisual = (VisualPetriNet)modelEntry.getImplementation(ServiceHandle.LegacyVisualModelService);
		PetriNet petri = (PetriNet)petriVisual.getMathModel();
		
		Assert.assertNotNull(petriVisual);
		Assert.assertNotNull(petri);
		
		VisualPetriNet sample = buildSampleVisualPetri();
		assertPetriEquals(petri, (PetriNet)sample.getMathModel());
		assertVisualPetriEquals(petriVisual, sample);
	}

	private void assertVisualPetriEquals(VisualPetriNet petriVisual, VisualPetriNet sample) {
		// TODO Auto-generated method stub
	}

	@Test
	public void EnsureSamplesUpToDate() throws Exception
	{
		System.out.println("If the serialisation format has changed, you can use these new serialisation samples:");
		ensureSampleUpToDate("testDataMathModel", buildSamplePetri(), testDataMathModel);
		ensureSampleUpToDate("testDataVisualModel", buildSampleVisualPetri(), testDataVisualModel);
	}
	
	private void ensureSampleUpToDate(String sampleVarName, Model model, String currentValue) throws SerialisationException, Exception 
	{
		Framework f = new Framework();
		f.getPluginManager().loadManifest();
		StringWriter writer = new StringWriter();
		f.save(new PetriNetModelDescriptor().createServiceProvider(model, new HistoryPreservingStorageManager()), new Base16Writer(writer));
		String generatedValue = writer.toString();
		if(currentValue.equals(generatedValue))
			return;
		System.out.print("    private static final String ");
		System.out.print(sampleVarName);
		System.out.print(" = \"");
		System.out.print(generatedValue);
		System.out.println("\";");
	}

	private Collection<MathNode> getComponents(PetriNet net)
	{
		ArrayList<MathNode> result = new ArrayList<MathNode>(net.getTransitions());
		result.addAll(net.getPlaces());
		return result;
	}
	
	private Collection<MathConnection> getConnections(PetriNet net)
	{
		return Hierarchy.getChildrenOfType(net.getRoot(), MathConnection.class);
	}
	
	private void assertPetriEquals(PetriNet expected, PetriNet actual) {
		Assert.assertEquals(getComponents(expected).size(), getComponents(actual).size());
		for(MathNode component : getComponents(expected))
			assertComponentEquals(component,(MathNode) eval(actual.referenceManager()).getNodeByReference(eval(expected.referenceManager()).getNodeReference(component)));

		Assert.assertEquals(getConnections(expected).size(), getConnections(actual).size());
		for(MathConnection connection : getConnections(expected))
			assertConnectionEquals(connection, (MathConnection) eval(actual.referenceManager()).getNodeByReference(eval(expected.referenceManager()).getNodeReference(connection)));
	}

	private void assertConnectionEquals(MathConnection expected, MathConnection actual) {
		assertComponentEquals(expected.getFirst(), actual.getFirst());
		assertComponentEquals(expected.getSecond(), actual.getSecond());
	}

	int toHexchar(int ch)
	{
		if(ch<10)
			return '0'+ch;
		else
			return 'a'+ch-10;
	}
	
	int fromHexchar(int ch)
	{
		if(ch <= 'f' && ch >= 'a')
			return ch-'a'+10;
		if(ch <= 'F' && ch >= 'A')
			return ch-'A'+10;
		if(ch <= '9' && ch >= '0')
			return ch-'0';
		throw new RuntimeException("Hex parse error");
	}
	
	class Base16Writer extends OutputStream
	{
		private final Writer output;

		public Base16Writer(Writer output)
		{
			this.output = output;
		}

		@Override
		public void write(int b) throws IOException {
			b &= 0xff;
			output.write(toHexchar(b/16));
			output.write(toHexchar(b%16));
		}
	}
	
	class Base16Reader extends InputStream
	{
		private final Reader stringReader;

		Base16Reader(String string)
		{
			this(new StringReader(string));
		}
		Base16Reader(Reader stringReader)
		{
			this.stringReader = stringReader;
		}
		
		@Override
		public int read() throws IOException {
			int ch1 = stringReader.read();
			if(ch1 == -1)
				return -1;
			int ch2 = stringReader.read();
			if(ch2 == -1)
				throw new RuntimeException("Length must be even");
			
			return fromHexchar(ch1)*16+fromHexchar(ch2);
		}
	}
	
	public void assertComponentEquals(MathNode node, MathNode node2)
	{
		if(node == null)
		{
			Assert.assertNull(node2);
			return;
		}
		Assert.assertNotNull(node2);
		
		Class<? extends Node> type = node.getClass();
		Assert.assertEquals(type, node2.getClass());
		
		if(type == Transition.class)
			assertTransitionEquals((Transition)node, (Transition)node2);
		if(type == Place.class)
			assertPlaceEquals((Place)node, (Place)node2);
	}

	private void assertTransitionEquals(Transition expected, Transition actual) {
	}

	private void assertPlaceEquals(Place expected, Place actual) {
		Assert.assertEquals(eval(expected.tokens()), eval(actual.tokens()));
	}

	private PetriNet buildSamplePetri() throws Exception
	{
		return (PetriNet) buildSampleVisualPetri().getMathModel();
	}
	
	private VisualPetriNet buildSampleVisualPetri() throws Exception {
		PetriNet petri = new PetriNet(new DefaultStorageManager());
		
		Place place1 = new Place(new DefaultStorageManager());
		place1.tokens().setValue(5);
		Place place2 = new Place(new DefaultStorageManager());
		place2.tokens().setValue(3);
		Place place3 = new Place(new DefaultStorageManager());
		place3.tokens().setValue(2);
		petri.add(place1);
		petri.setName(place1, "place1");
		petri.add(place2);
		petri.setName(place2, "place2");
		petri.add(place3);
		petri.setName(place3, "place3");
		
		Transition trans1 = new Transition(new DefaultStorageManager());
		petri.setName(trans1, "trans1");
		Transition trans2 = new Transition(new DefaultStorageManager());
		petri.setName(trans2, "trans2");

		petri.add(trans1);
		petri.add(trans2);
		
		petri.connect(place1, trans1);
		petri.connect(trans1, place2);
		petri.connect(trans1, place3);
		petri.connect(place3, trans2);


		VisualPetriNet visual = new VisualPetriNet(petri);
/*		VisualPlace vp1 = new VisualPlace(place1);
		VisualPlace vp2 = new VisualPlace(place2);
		VisualPlace vp3 = new VisualPlace(place3);
		
		VisualTransition vt1 = new VisualTransition(trans1);
		VisualTransition vt2 = new VisualTransition(trans2);

		VisualGroup gr1 = new VisualGroup();
		VisualGroup gr2 = new VisualGroup();
		VisualGroup gr3 = new VisualGroup();
		
		//todo: add components
		
		gr1.add(vp1);
		gr1.add(vt2);

		gr2.add(gr1);
		gr2.add(vp2);

		gr3.add(vp3);
		gr3.add(vt2);
		
		visual.getRoot().add(gr2);
		visual.getRoot().add(gr3);

		VisualConnection vc1 = new VisualConnection(con1, vp1, vt1);
		VisualConnection vc2 = new VisualConnection(con1, vt1, vp2);
		VisualConnection vc3 = new VisualConnection(con1, vt1, vp3);
		VisualConnection vc4 = new VisualConnection(con1, vp3, vt2);
		
		visual.addConnection(vc1);
		visual.addConnection(vc2);
		visual.addConnection(vc3);
		visual.addConnection(vc4);*/

		r = new Random(1);
		
		for(Node component : eval(visual.getRoot().children()))
			randomPosition(component);
		
		/*randomPosition(vp1);
		randomPosition(vp2);
		randomPosition(vp3);

		randomPosition(vt1);
		randomPosition(vt2);
		
		randomPosition(gr1);
		randomPosition(gr2);
		randomPosition(gr3);*/
		
		return visual;
	}
	Random r;

	private void randomPosition(Node node) {
		if(node instanceof MovableNew)
			MovableHelper.translate((MovableNew)node, r.nextDouble()*10, r.nextDouble()*10);
	}
}
