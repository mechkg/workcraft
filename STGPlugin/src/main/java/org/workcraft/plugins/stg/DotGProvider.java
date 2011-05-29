package org.workcraft.plugins.stg;

import java.io.File;
import java.io.IOException;

import org.workcraft.Framework;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.NotImplementedException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.gui.workspace.Path;
import org.workcraft.interop.ModelServices;
import org.workcraft.interop.ServiceNotAvailableException;
import org.workcraft.serialisation.Format;
import org.workcraft.util.Export;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

public class DotGProvider {
	private final Framework framework;
	private final Workspace workspace;

	public DotGProvider(final Framework framework) {
		this.framework = framework;
		this.workspace = framework.getWorkspace();
	}

	public File getDotG(final Path<String> source) throws ServiceNotAvailableException {
		final WorkspaceEntry we = workspace.getOpenFile(source);

		if (we != null) {
			final ModelServices modelEntry = we.getModelEntry();

			try {
				final String prefix = "workcraft-stg";
				final File file = File.createTempFile(prefix, ".g");
				Export.exportToFile(modelEntry, file, Format.STG, framework.getPluginManager());
				return file;
			} catch (final IOException e) {
				throw new RuntimeException(e);
			} catch (final ModelValidationException e) {
				throw new RuntimeException(e);
			} catch (final SerialisationException e) {
				throw new RuntimeException(e);
			}
		} else if (source.getNode().endsWith(".g")) {
			return workspace.getFile(source);
		} else if (source.getNode().endsWith(".work")) {
			throw new NotImplementedException();
		} else {
			throw new RuntimeException("Don't know how to create a .g file from " + source);
		}
	}
}
