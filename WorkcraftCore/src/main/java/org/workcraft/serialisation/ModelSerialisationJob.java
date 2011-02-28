package org.workcraft.serialisation;

import java.io.OutputStream;

import org.workcraft.exceptions.SerialisationException;

public interface ModelSerialisationJob {
	public ReferenceProducer serialise (OutputStream out, ReferenceProducer externalReferences) throws SerialisationException;
}
