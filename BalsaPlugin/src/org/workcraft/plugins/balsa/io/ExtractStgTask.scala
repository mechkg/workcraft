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

package org.workcraft.plugins.balsa.io;

import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.util.ArrayList
import java.util.HashMap
import java.util.List
import java.util.Map
import java.util.NoSuchElementException
import org.workcraft.Framework
import org.workcraft.dependencymanager.advanced.user.StorageManager
import org.workcraft.exceptions.DeserialisationException
import org.workcraft.parsers.breeze.Netlist
import org.workcraft.plugins.balsa.BreezeComponent
import org.workcraft.plugins.balsa.BreezeConnection
import org.workcraft.plugins.balsa.BreezeHandshake
import org.workcraft.plugins.balsa.components.DynamicComponent
import org.workcraft.plugins.balsa.handshakebuilder.Handshake
import org.workcraft.plugins.balsa.handshakeevents.TwoWayStg
import org.workcraft.plugins.balsa.handshakestgbuilder.ActivenessSelector
import org.workcraft.plugins.balsa.handshakestgbuilder.HandshakeProtocol
import org.workcraft.plugins.balsa.handshakestgbuilder.InternalHandshakeStgBuilder
import org.workcraft.plugins.balsa.handshakestgbuilder.TwoSideStg
import org.workcraft.plugins.balsa.io.BalsaExportConfig.CompositionMode
import org.workcraft.plugins.balsa.protocols.FourPhaseProtocol_NoDataPath
import org.workcraft.plugins.balsa.protocols.TwoPhaseProtocol
import org.workcraft.plugins.balsa.stg.MainStgBuilder
import org.workcraft.plugins.balsa.stgbuilder.SignalId
import org.workcraft.plugins.balsa.stgbuilder.StgBuilder
import org.workcraft.plugins.balsa.stgbuilder.StgPlace
import org.workcraft.plugins.balsa.stgbuilder.StgSignal
import org.workcraft.plugins.balsa.stgmodelstgbuilder.NameProvider
import org.workcraft.plugins.balsa.stgmodelstgbuilder.StgModelStgBuilder
import org.workcraft.plugins.interop.DotGImporter
import org.workcraft.plugins.pcomp.PCompOutputMode
import org.workcraft.plugins.pcomp.tasks.PcompTask
import org.workcraft.plugins.shared.tasks.ExternalProcessResult
import org.workcraft.plugins.stg.DefaultStorageManager
import org.workcraft.plugins.stg.STG
import org.workcraft.plugins.stg.STGModel
import org.workcraft.plugins.stg.STGModelDescriptor
import org.workcraft.serialisation.Format
import org.workcraft.tasks.ProgressMonitor
import org.workcraft.tasks.Result
import org.workcraft.tasks.Result.Outcome
import org.workcraft.tasks.Task
import org.workcraft.util.Export
import org.workcraft.util.Export.ExportTask
import org.workcraft.plugins.stg21.types.VisualStg
import ExtractStgTask._
import org.workcraft.plugins.stg21.StgModelDescriptor


object ExtractStgTask {
  type BalsaCircuit = CachedCircuit[BreezeHandshake, BreezeComponent, BreezeConnection]

	private def buildComponentStg(injectiveLabelling  : Boolean, circuit : BalsaCircuit, breezeComponent : BreezeComponent, protocol : HandshakeProtocol, stgBuilder : StgBuilder) = {
		val fullHandshakes = new HashMap[String, Handshake](breezeComponent.getHandshakes)
		
		MainStgBuilder.addDataPathHandshakes(fullHandshakes, breezeComponent.getUnderlyingComponent)
		
		val component = breezeComponent.getUnderlyingComponent();
		
		if(!injectiveLabelling && component.declaration().getName().equals("Call"))
		{
			val inp0 = fullHandshakes.get("inp0");
			val inp1 = fullHandshakes.get("inp1");
			val out = fullHandshakes.get("out");
			val place = stgBuilder.buildPlace(1);
			val r1 = stgBuilder.buildSignal(new SignalId(inp0, "rq"), false);
			val r2 = stgBuilder.buildSignal(new SignalId(inp1, "rq"), false);
			val a1 = stgBuilder.buildSignal(new SignalId(inp0, "ac"), true);
			val a2 = stgBuilder.buildSignal(new SignalId(inp1, "ac"), true);
			val r_1 = stgBuilder.buildSignal(new SignalId(out, "rq"), true);
			val a_1 = stgBuilder.buildSignal(new SignalId(out, "ac"), false);
			val r_2 = stgBuilder.buildSignal(new SignalId(out, "rq"), true);
			val a_2 = stgBuilder.buildSignal(new SignalId(out, "ac"), false);
			
			stgBuilder.connect(place, r1.getPlus());
			stgBuilder.connect(r1.getPlus(), r_1.getPlus());
			stgBuilder.connect(r_1.getPlus(), a_1.getPlus());
			stgBuilder.connect(a_1.getPlus(), a1.getPlus());
			stgBuilder.connect(a1.getPlus(), r1.getMinus());
			stgBuilder.connect(r1.getMinus(), r_1.getMinus());
			stgBuilder.connect(r_1.getMinus(), a_1.getMinus());
			stgBuilder.connect(a_1.getMinus(), a1.getMinus());
			stgBuilder.connect(a1.getMinus(), place);
			
			stgBuilder.connect(place, r2.getPlus());
			stgBuilder.connect(r2.getPlus(), r_2.getPlus());
			stgBuilder.connect(r_2.getPlus(), a_2.getPlus());
			stgBuilder.connect(a_2.getPlus(), a2.getPlus());
			stgBuilder.connect(a2.getPlus(), r2.getMinus());
			stgBuilder.connect(r2.getMinus(), r_2.getMinus());
			stgBuilder.connect(r_2.getMinus(), a_2.getMinus());
			stgBuilder.connect(a_2.getMinus(), a2.getMinus());
			stgBuilder.connect(a2.getMinus(), place);
		}
		else
		{
			val handshakeStgs = MainStgBuilder.buildHandshakes(fullHandshakes, protocol, stgBuilder);
			MainStgBuilder.buildStg(component, handshakeStgs, stgBuilder);
		}
	}
}

	class CountingNameProvider[T](prefix : String) extends NameProvider[T]
	{
		val names = new HashMap[T, String]
		var nextId = 1

		override def getName(key : T) : String = {
			val oldName = names.get(key)
			val name = if(oldName == null)
			{
				val newName = "" + nextId
				nextId = nextId + 1
				names.put(key, newName)
				newName
			} else oldName
			prefix + name
		}
	}
	

class ExtractStgTask (
	val protocol : HandshakeProtocol,
	val balsa : BalsaCircuit,
	val settings : BalsaExportConfig,
	val framework : Framework,
	val storage : StorageManager,
    val injectiveLabelling : Boolean
) extends Task[StgExtractionResult] 
	{	

/*	public ExtractControlSTGTask(Framework framework, org.workcraft.plugins.balsa.BalsaCircuit balsa, BalsaExportConfig settings, StorageManager storage)
	{
		this(framework, balsa.asNetlist(), settings, storage);
	}
	
	public ExtractControlSTGTask(Framework framework, Netlist<BreezeHandshake, BreezeComponent, BreezeConnection> circuit, BalsaExportConfig settings, StorageManager storage) {
		this.storage = storage;
		this.balsa = new BalsaCircuit(circuit);
		this.settings = settings;
		this.framework = framework;
		
		if (settings.getProtocol() == BalsaExportConfig.Protocol.TWO_PHASE)
			protocol = new TwoPhaseProtocol();
		else
			protocol = new FourPhaseProtocol_NoDataPath();
	} */

	def getComponentsToSave(balsa : BalsaCircuit) : Iterable[BreezeComponent] = balsa.getBlocks


	/**
	 * Works in StgBuilder monad. Blame Java for bad types. :)
	 */
	def buildStgFull(balsa : BalsaCircuit, stg : StgBuilder) : Unit = {
		val components = getComponentsToSave(balsa)
		
		val internalHandshakes = new HashMap[BreezeConnection, TwoWayStg];
		
		for(component <- components)
		{
			val fullHandshakes = new HashMap[String, Handshake](component.getHandshakes);
			
			MainStgBuilder.addDataPathHandshakes(fullHandshakes, component.getUnderlyingComponent());
			
			val external = new HashMap[String, Handshake]
			val internal = new HashMap[String, TwoWayStg]
			
			for(name <- fullHandshakes.keySet)
			{
				BreezeConnection connection = getInternalConnection(balsa, components, component, fullHandshakes.get(name));
				if(connection == null)
					external.put(name, fullHandshakes.get(name));
				else
				{
					if(!internalHandshakes.containsKey(connection))
					{
						TwoWayStg internalStg = buildInternalStg(fullHandshakes.get(name), stg);
						internalHandshakes.put(connection, internalStg);
					}
	
					internal.put(name, internalHandshakes.get(connection));
				}
			}
			
			val handshakeStgs = MainStgBuilder.buildHandshakes(external, protocol, stg);
			
			for(name <- internal.keySet)
				handshakeStgs.put(name, ActivenessSelector.direct(internal.get(name), fullHandshakes.get(name).isActive()));
			
			MainStgBuilder.buildStg(component.getUnderlyingComponent(), handshakeStgs, stg);		
		}
	}

	private def getInternalConnection(balsa : BalsaCircuit, components : Iterable[BreezeComponent], component : BreezeComponent , handshake : Handshake) : BreezeConnection = {
		val hs = component.getHandshakeComponents().get(handshake);
		if(hs==null)
			return null;
		val connection = balsa.getConnection(hs);
		if(connection == null)
			return null;
		
		if(!contains(components, balsa.getConnectedHandshake(hs).getOwner()))
			return null;
		return connection;
	}

	private def buildInternalStg(handshake : Handshake, stg : StgBuilder) : TwoWayStg = {
		return handshake.accept(new InternalHandshakeStgBuilder(stg))
	}

	private def contains(components : Iterable[BreezeComponent], component : BreezeComponent) : Boolean = 
	{
		for(c <- components)
			if(component == c)
				return true;
		return false;
	}



	private def getNamesProvider(circuit : BalsaCircuit) : NameProvider[Handshake] = 
	{
		val componentNames = new CountingNameProvider[BreezeComponent]("c")
		
		val names = new HashMap[Handshake, String]
		
		val externalPorts = circuit.getPorts()
		
		for(hs <- externalPorts)
			names.put(hs.getHandshake, "port_" + hs.getHandshakeName)
		for(comp <- circuit.getBlocks)
			for(hs <- comp.getPorts)
				names.put(hs.getHandshake, componentNames.getName(comp) + "_" + hs.getHandshakeName())
		
		for(con <- circuit.getConnections)
		{
			Handshake first = con.getFirst.getHandshake
			Handshake second = con.getSecond.getHandshake
			String fullName = names.get(first) + "__" + names.get(second)
			names.put(first, fullName)
			names.put(second, fullName)
		}

		return new NameProvider[Handshake]()
		{
			def getName(handshake : Handshake) : String =
			{
				val result = names.get(handshake);
				if(result == null)
					throw new NoSuchElementException("No name found for the given handshake");
				return result;
			}
		};
	}

	override def run(monitor : ProgressMonitor[_ >: StgExtractionResult]) : Result[_ <: StgExtractionResult]  = {
		
		val useSimpleInternalHandshakes = settings.getCompositionMode() == CompositionMode.INTERNAL;
		
		val names = getNamesProvider(balsa);

		if(useSimpleInternalHandshakes)
		{
			val stgFull = buildStgFull(balsa, new StgModelStgBuilder(names));
			return Result.finished(new StgExtractionResult(stgFull, null));
		}
		else
		{
			val tempFiles = new ArrayList[File];
			for(component <- getComponentsToSave(balsa))
			{
				val stg = buildComponentStg(balsa, component, protocol, new StgModelStgBuilder(names));
	
				var tempFile : File = null;
				try {
					tempFile = File.createTempFile("brz_", ".g");
				} catch {
				  case (e : IOException) => return Result.exception(e);
				}
				
				try {
					val exportTask = Export.createExportTask(StgModelDescriptor.newDocument(stg), tempFile, Format.STG, framework.getPluginManager());
					val exportResult = framework.getTaskManager().execute(exportTask, "Writing .g");
				
					if (exportResult.getOutcome() != Outcome.FINISHED)
					{
						if (exportResult.getOutcome() == Outcome.CANCELLED)
							return Result.cancelled();
						else
							return Result.exception(exportResult.getCause());
					}
				} catch {
				  case (e : Exception) => return Result.exception(e);
				}
				
				
				tempFiles.add(tempFile);
			}
			
			if(tempFiles.size() > 0)
			{
			
				val task = new PcompTask(tempFiles.toArray(new File[0]), PCompOutputMode.DUMMY, settings.getCompositionMode() == CompositionMode.IMPROVED_PCOMP);
		
				try
				{
					final Result<? extends ExternalProcessResult> result = framework.getTaskManager().execute(task, "Parallel composition");
					
					if (result.getOutcome() != Outcome.FINISHED)
					{
						if (result.getOutcome() == Outcome.CANCELLED)
							return Result.cancelled();
						else
							if (result.getCause() != null)
								return Result.exception(result.getCause());
							else
								return Result.failed(new StgExtractionResult(null, result.getReturnValue()));
					}

					try {
						final STGModel stg = new DotGImporter().importSTG(new ByteArrayInputStream(result.getReturnValue().getOutput()), storage);
						return Result.finished(new StgExtractionResult(stg, null));
					} catch (DeserialisationException e) {
						return Result.exception(e);
					}
				}
				finally
				{
					for(File f : tempFiles)
						f.delete();
				}
			}
			else
				return Result.finished(new StgExtractionResult(new STG(storage), null));
		}
		
	}

	public STGModel getSTG() {
		return framework.getTaskManager().execute(this, "extraction").getReturnValue().getResult();
	}
}

