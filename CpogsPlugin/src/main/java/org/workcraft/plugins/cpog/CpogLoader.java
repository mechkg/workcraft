package org.workcraft.plugins.cpog;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.workcraft.Loader;
import org.workcraft.interop.ModelServices;
import org.workcraft.plugins.stg.HistoryPreservingStorageManager;
import org.workcraft.util.Maybe;

import org.workcraft.plugins.cpog.scala.CPOG;

public class CpogLoader implements Loader {

	@Override
	public Maybe<ModelServices> load(byte[] input) {
		Object readObject;
		try {
			readObject = new ObjectInputStream(new ByteArrayInputStream(input)).readObject();
		} catch (IOException e) {
			e.printStackTrace();
			return Maybe.Util.nothing();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return Maybe.Util.nothing();
		}
		
		if(readObject instanceof org.workcraft.plugins.cpog.scala.nodes.snapshot.CPOG)
			return Maybe.Util.just(reconstructModel((org.workcraft.plugins.cpog.scala.nodes.snapshot.CPOG)readObject));
		else
			return Maybe.Util.nothing();
	}

	private ModelServices reconstructModel(org.workcraft.plugins.cpog.scala.nodes.snapshot.CPOG readObject) {
		HistoryPreservingStorageManager storage = new HistoryPreservingStorageManager();
		CPOG cpog = org.workcraft.plugins.cpog.scala.serialisation.SnapshotLoader.load(readObject, new org.workcraft.scala.StorageManager(storage));
		return new CpogModelDescriptor().createCpogServices(cpog, storage);
	}
}
