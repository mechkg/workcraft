package org.workcraft.plugins.interop;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.eval;

import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.TouchableProvider;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.NotSupportedException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.interop.ExportJob;
import org.workcraft.interop.Exporter;
import org.workcraft.interop.ModelServices;
import org.workcraft.interop.ServiceNotAvailableException;
import org.workcraft.serialisation.Format;
import org.workcraft.util.Action1;
import org.workcraft.util.Maybe;

public class DotExporter implements Exporter {
	
	private final TouchableProvider<Node> tp;

	public DotExporter(TouchableProvider<Node> tp) {
		this.tp = tp;
	}

	public static void export(DotExportable model, OutputStream outStream) throws IOException
	{
		PrintStream out = new PrintStream(outStream);
		
		out.println("digraph work {");
		out.println("graph [nodesep=\"0.5\", overlap=\"false\", splines=\"true\"];");
		out.println("node [shape=box];");
		
		for (DotExportNode node : model.getNodes()) {
			String id = node.getName();
			Dimension2D dimensions = node.getDimensions();
			double width = dimensions.getWidth();
			double height = dimensions.getHeight();
			out.println("\""+id+"\" [width=\""+width+"\", height=\""+height+"\", fixedsize=\"true\"];");
			
			for(String target : node.getOutgoingArcs()) {
				out.println("\""+id+"\" -> \""+target+"\";");
			}
		}
		out.println("}");
	}
	
	@Override
	public ExportJob getExportJob(ModelServices modelServices) throws ServiceNotAvailableException {
		
		DotExportableService exportableService = modelServices.getImplementation(DotExportableService.SERVICE_HANDLE);
		final Model model = exportableService.getModel();
		
		return new ExportJob() {
			
			@Override
			public int getCompatibility() {
				return Exporter.GENERAL_COMPATIBILITY;
			}
			
			
			@Override
			public void export(OutputStream outStream) throws IOException, ModelValidationException, SerialisationException {

				final List<DotExportNode> export = new ArrayList<DotExportNode>();
				for (Node n : eval(model.getRoot().children())) {
					if (n instanceof VisualComponent) {
						final VisualComponent comp = (VisualComponent) n;
						final String id = eval(model.referenceManager()).getNodeReference(comp);
						
						if(id!=null) {
							final Maybe<? extends Touchable> t = eval(tp.apply(comp));
							
							Maybe.Util.doIfJust(t, new Action1<Touchable>(){

								@Override
								public void run(Touchable argument) {
									final Rectangle2D bb = argument.getBoundingBox();
									final List<String> destinations = new ArrayList<String>();
									
									
									Set<Node> postset = eval(model.nodeContext()).getPostset(comp);
									
									for(Node target : postset) {
										String targetId = eval(model.referenceManager()).getNodeReference(target);
										if(targetId!=null)
											destinations.add(targetId);
									}
									
									export.add(new DotExportNode()
									{
										@Override
										public String getName() {
											return id;
										}
				
										@Override
										public Dimension2D getDimensions() {
											return new Dimension2D()
											{
												@Override
												public double getHeight() {
													return bb.getHeight();
												}
												@Override
												public double getWidth() {
													return bb.getWidth();
												}
												@Override
												public void setSize(double width, double height) {
													throw new NotSupportedException();
												}
											};
										}
				
										@Override
										public String[] getOutgoingArcs() {
											return destinations.toArray(new String[0]);
										}
									});
								}
							});
					}
				}
				}
				
				DotExporter.export(new DotExportable() {
					@Override
					public DotExportNode[] getNodes() {
						return export.toArray(new DotExportNode[0]);
					}
				}, outStream);
			}
		};
	}

	@Override
	public String getDescription() {
		return ".dot (GraphViz dot graph format)";
	}

	@Override
	public String getExtenstion() {
		return ".dot";
	}

	@Override
	public Format getTargetFormat() {
		return Format.DOT;
	}
}
