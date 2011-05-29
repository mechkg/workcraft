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

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.Document;
import org.workcraft.dom.Model;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.interop.ExportJob;
import org.workcraft.interop.Exporter;
import org.workcraft.interop.ModelServices;
import org.workcraft.interop.ServiceNotAvailableException;
import org.workcraft.serialisation.Format;
import org.workcraft.util.XmlUtil;

public abstract class AbstractSVGExporter implements Exporter {

	public abstract void draw(Model model, Graphics2D g);

	@Override
	public ExportJob getExportJob(ModelServices modelServices) throws ServiceNotAvailableException {
		final SvgExportable exportable = modelServices.getImplementation(SvgExportable.SERVICE_HANDLE);

		return new ExportJob() {
			@Override
			public void export(OutputStream out) throws IOException, ModelValidationException, SerialisationException {

				try {
					Document doc = XmlUtil.createDocument();

					SVGGraphics2D g2d = new SVGGraphics2D(doc);

					g2d.scale(50, 50);

					Rectangle2D bounds = exportable.getBoundingBox();

					g2d.translate(-bounds.getMinX(), -bounds.getMinY());
					g2d.setSVGCanvasSize(new Dimension((int) (bounds.getWidth() * 50), (int) (bounds.getHeight() * 50)));

					exportable.draw(g2d);

					g2d.stream(new OutputStreamWriter(out));

				} catch (ParserConfigurationException e) {
					throw new SerialisationException(e);
				}
			}

			@Override
			public int getCompatibility() {
				return Exporter.GENERAL_COMPATIBILITY;
			}

		};
	}

	public final String getExtenstion() {
		return ".svg";
	}

	@Override
	public final Format getTargetFormat() {
		return Format.SVG;
	}
}
