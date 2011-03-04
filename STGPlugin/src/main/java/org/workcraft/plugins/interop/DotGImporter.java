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

package org.workcraft.plugins.interop;

import java.io.File;
import java.io.InputStream;

import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.FormatException;
import org.workcraft.interop.Importer;
import org.workcraft.interop.ServiceProvider;
import org.workcraft.plugins.stg.HistoryPreservingStorageManager;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.STGModelDescriptor;
import org.workcraft.plugins.stg.javacc.generated.DotGParser;
import org.workcraft.plugins.stg.javacc.generated.ParseException;

public class DotGImporter implements Importer {
	@Override
	public boolean accept(File file) {
		return file.getName().endsWith(".g");
	}

	@Override
	public String getDescription() {
		return "Signal Transition Graph (.g)";
	}

	@Override
	public ServiceProvider importFrom(InputStream in) throws DeserialisationException {
		HistoryPreservingStorageManager storage = new HistoryPreservingStorageManager();
		return STGModelDescriptor.getServices(importSTG(in, storage), storage);
	}

	public STG importSTG(InputStream in, StorageManager storage) throws DeserialisationException {
		try {
			STG result = new DotGParser(in).parse(storage);
			return result;
		} catch (FormatException e) {
			throw new DeserialisationException(e);
		} catch (ParseException e) {
			throw new DeserialisationException(e);
		}
	}
}
