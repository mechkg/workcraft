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

package org.workcraft.plugins.layout;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.eval;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.ToolJob;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.MovableHelper;
import org.workcraft.dom.visual.MovableNew;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.connections.Polyline;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.dom.visual.connections.VisualConnection.ConnectionType;
import org.workcraft.dom.visual.connections.VisualConnection.ScaleMode;
import org.workcraft.exceptions.LayoutException;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.interop.ExportJob;
import org.workcraft.interop.ServiceHandle;
import org.workcraft.interop.ServiceNotAvailableException;
import org.workcraft.interop.ServiceProvider;
import org.workcraft.plugins.layout.generated.DotParser;
import org.workcraft.plugins.layout.generated.ParseException;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.shared.tasks.ExternalProcessTask;
import org.workcraft.serialisation.Format;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.Task;
import org.workcraft.util.Export;
import org.workcraft.util.FileUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class DotLayout implements Tool {
	
	private final Framework framework;

	public DotLayout(Framework framework) {
		this.framework = framework;
	}
	
	private void saveGraph(ExportJob dotExporter, File file) throws IOException, ModelValidationException, SerialisationException {
		FileOutputStream out = new FileOutputStream(file);
		try{
			dotExporter.export(out);
		}
		finally{
			out.close();
		}
	}
	
	List<Point2D> parseConnectionSpline(String pos) throws ParseException
	{
		try
		{
			ArrayList<Point2D> result = new ArrayList<Point2D>();
			Point2D end = null;
			String [] split = pos.split(" ");
			
			for(String s : split)
			{
				String [] ss = s.split(",");
				if(ss.length <2 || ss.length>3)
					throw new ParseException("bad connection position format");
				double pointsToInches = 1.0/72;
				if(ss.length == 3)
				{
					double x = Double.parseDouble(ss[1])*pointsToInches;
					double y = -Double.parseDouble(ss[2])*pointsToInches;
					Point2D p = new Point2D.Double(x,y);
					if(ss[0].equals("s"))
						result.add(0,p);
					else
						if(ss[0].equals("e"))
							end = p;
						else
							throw new ParseException("bad connection position format");
				}
				else
				{
					double x = Double.parseDouble(ss[0])*pointsToInches;
					double y = -Double.parseDouble(ss[1])*pointsToInches;
					result.add(0,new Point2D.Double(x,y));
				}
			}
			
			if(end!=null)
				result.add(0,end);
			return result;
		}
		catch(NumberFormatException ex)
		{
			throw new ParseException(ex.getMessage());
		}
	}
	
	private void applyLayout(String in, final VisualModel model) {
		
		DotParser parser = new DotParser(new StringReader(in.replace("\\\n", "")));
		
		try {
			parser.graph(new DotListener()
			{
				@Override
				public void node(String id, Map<String, String> properties) {
					Node comp = eval(model.referenceManager()).getNodeByReference(id);
					
					if(comp!=null && comp instanceof MovableNew)
					{
						MovableNew m = (MovableNew)comp;
						String posStr = properties.get("pos");
						if(posStr!=null)
						{
							String [] posParts = posStr.split(",");
							if(posParts.length==2)
							{
								MovableHelper.resetTransform(m);
								MovableHelper.translate(m, 
									Double.parseDouble(posParts[0])*1.0/72,
									-Double.parseDouble(posParts[1])*1.0/72);
							}
							else
							{
								System.err.println("Dot graph parse error: node 'pos' attribute has value '"+posStr+"', which is not a comma-separated pair of integers");
							}
						}
					}
				}
				
				@Override
				public void arc(String from, String to, Map<String, String> properties) {
					
					if(eval(DotLayoutSettings.importConnectionsShape))
					{
						Node comp1 = eval(model.referenceManager()).getNodeByReference(from);
						Node comp2 = eval(model.referenceManager()).getNodeByReference(to);
						Set<Connection> connections = eval(model.nodeContext()).getConnections(comp1);
						Connection con = null;
						for(Connection c : connections)
						{
							if(c.getSecond() == comp2)
								con = c;
						}
						if(con!=null)
						{
							VisualConnection vc = (VisualConnection)con;
							vc.connectionType.setValue(ConnectionType.POLYLINE);
							vc.scaleMode().setValue(ScaleMode.ADAPTIVE);
							
							Polyline poly = (Polyline)eval(vc.graphic());
							poly.remove(eval(poly.children()));
							List<Point2D> points;
							try {
								points = parseConnectionSpline(properties.get("pos"));
								
								for(int i=points.size()-1;i>=0;i--)
								{
									Point2D p = points.get(i);
									poly.createControlPoint(i, p);
								}
								
							} catch (ParseException e) {
								e.printStackTrace();
							}
						}
						else
						{
							System.err.println(String.format("Unable to find a connection from %s to %s", from, to));
						}
					}
				}
			});
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public ToolJob applyTo(WorkspaceEntry entry) throws ServiceNotAvailableException {
		final ServiceProvider services = entry.getModelEntry();
		final VisualModel model = services.getImplementation(ServiceHandle.LegacyVisualModelService);
		final ExportJob dotExporter = Export.chooseBestExporter(framework.getPluginManager(), services, Format.DOT);
		
		return new ToolJob(){

			@Override
			public void run() {
				File original = null, layout = null;
				try {
					original = File.createTempFile("work", ".dot");
					layout = File.createTempFile("worklayout", ".dot");
					
					saveGraph(dotExporter, original);
					
					List<String> args = new ArrayList<String>();
					args.add(eval(DotLayoutSettings.dotCommand));
					args.add("-Tdot");
					args.add("-o");
					args.add(layout.getAbsolutePath());
					args.add(original.getAbsolutePath());
					
					Task<ExternalProcessResult> task = new ExternalProcessTask(args, new File("."));
					Result<? extends ExternalProcessResult> res = framework.getTaskManager().execute(task, "Laying out the graph...");
					
					if(res.getOutcome() == Outcome.CANCELLED)
						return;
					if(res.getOutcome() == Outcome.FAILED)
						throw new LayoutException("Failed to execute external process:\n" + res.getCause());
					if(res.getReturnValue().getReturnCode() == 0) {
						String in = FileUtils.readAllText(layout);
						applyLayout(in, model);
					}
					else
						throw new LayoutException("External process (dot) failed (code " + res.getReturnValue().getReturnCode() +")\n\n"+new String(res.getReturnValue().getOutput())+"\n\n"+new String(res.getReturnValue().getErrors()));
				} catch(IOException e) {
					throw new RuntimeException(e);
				} catch (ModelValidationException e) {
					throw new RuntimeException(e);
				} catch (SerialisationException e) {
					throw new RuntimeException(e);
				} finally {
					if(original!=null)
						original.delete();
					if(layout!=null)
						layout.delete();
				}
			}
		};
	}
	
	@Override
	public String getSection() {
		return "Layout";
	}

	@Override
	public String getDisplayName() {
		return "Layout using dot";
	}
}