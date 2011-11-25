package org.workcraft.plugins.balsa.io;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.eval;

import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.NotImplementedException;
import org.workcraft.exceptions.NotSupportedException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.interop.ExportJob;
import org.workcraft.interop.Exporter;
import org.workcraft.interop.ModelServices;
import org.workcraft.interop.ServiceNotAvailableException;
import org.workcraft.interop.ServiceProvider;
import org.workcraft.plugins.balsa.VisualBreezeComponent;
import org.workcraft.plugins.balsa.VisualHandshake;
import org.workcraft.plugins.interop.DotExportNode;
import org.workcraft.plugins.interop.DotExportable;
import org.workcraft.serialisation.Format;

public class DotExporter implements Exporter {


	@Override
	public ExportJob getExportJob(ModelServices modelServices) throws ServiceNotAvailableException {
		final LayoutableBalsaCircuit layoutable = modelServices.getImplementation(LayoutableBalsaCircuit.SERVICE_HANDLE);
		final Model model = layoutable.getModel();
		
		return new ExportJob() {
			
			@Override
			public int getCompatibility() {
				return Exporter.BEST_COMPATIBILITY;
			}
			
			@Override
			public void export(OutputStream outStream) throws IOException, ModelValidationException, SerialisationException {
				
				org.workcraft.plugins.interop.DotExporter.export(new DotExportable()
				{
					@Override
					public DotExportNode[] getNodes() {
						ArrayList<DotExportNode> result = new ArrayList<DotExportNode>();
						for (Node n : eval(model.getRoot().children())) {
							if (n instanceof VisualBreezeComponent) {
								VisualBreezeComponent comp = (VisualBreezeComponent) n;
								final String id = eval(model.referenceManager()).getNodeReference(comp);
								if(id!=null) {
									
									final Rectangle2D bb; if(true)throw new NotImplementedException(); // = eval(layoutable.getTouchableProvider().apply(comp)).getBoundingBox();
									if(bb!=null) {
										
										final ArrayList<String> destinations = new ArrayList<String>(); 
										for (VisualHandshake hs : comp.getHandshakes())
										{
											if(hs.getHandshake().isActive())
											{
												Set<Node> nodes = new HashSet<Node>();
												nodes.addAll(eval(model.nodeContext()).getPostset(hs));
												nodes.addAll(eval(model.nodeContext()).getPreset(hs));
												for(Node target : nodes) {
													if (target instanceof VisualHandshake) {
														VisualBreezeComponent targetComp = (VisualBreezeComponent)eval(((VisualHandshake)target).parent());
														String targetId = eval(model.referenceManager()).getNodeReference(targetComp);
														if(targetId!=null) {
															destinations.add(targetId);
														}
													}
												}
											}
										}
										
										result.add(new DotExportNode()
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
													public double getWidth() {
														return bb.getWidth();
													}

													@Override
													public double getHeight() {
														return bb.getHeight();
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
								}
							}
						}
						return result.toArray(new DotExportNode[0]);
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
