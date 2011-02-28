package org.workcraft.interop;

import java.io.IOException;
import java.io.OutputStream;

import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.SerialisationException;

public interface ExportJob {
	/**
	 * Specifies an estimate of how well this exporter performs its task. Used to rank ExportJobs and select the best out of them.
	 * @return
	 * The goodness estimate. The more, the better.
	 */
	public int getCompatibility();
	/**
	 * Writes the data to the output stream provided. Does not close the stream.
	 * @param out
	 * The output stream to write to.
	 * @throws IOException
	 * @throws ModelValidationException
	 * @throws SerialisationException
	 */
	public void export (OutputStream out) throws IOException, ModelValidationException, SerialisationException;
}
