package org.workcraft;

import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.Model;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.exceptions.NotImplementedException;
import org.workcraft.interop.ModelServices;
import org.workcraft.interop.ModelServicesImpl;
import org.workcraft.plugins.balsa.BalsaCircuit;
import org.workcraft.plugins.stg.DefaultStorageManager;

public class BalsaModelDescriptor implements ModelDescriptor {
	@Override
	public String getDisplayName() {
		return "Breeze circuit";
	}

	@Override
	public ModelServices createServiceProvider(Model model, StorageManager storage) {
		throw new NotImplementedException("What do you want, giving me an abstract 'model'?!");
	}
	@Override
	public ModelServices newDocument() {
		return ModelServicesImpl.EMPTY;
	}

	public static ModelServices createServiceProvider(
			BalsaCircuit importFromBreeze, DefaultStorageManager storage) {
		throw new NotImplementedException("What do you want, giving me an abstract 'model'?!");
	}
}
