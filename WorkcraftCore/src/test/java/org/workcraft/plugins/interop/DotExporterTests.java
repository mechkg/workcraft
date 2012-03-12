package org.workcraft.plugins.interop;

import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import junit.framework.Assert;

import org.junit.Test;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.Expressions;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dom.Container;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.NodeContext;
import org.workcraft.dom.references.ReferenceManager;
import org.workcraft.dom.visual.TouchableProvider;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.NotSupportedException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.interop.ModelServices;
import org.workcraft.interop.ModelServicesImpl;
import org.workcraft.interop.ServiceNotAvailableException;

import pcollections.PVector;
import pcollections.TreePVector;

public class DotExporterTests {
	private static final class MockModel implements Model {
		
		private MockNode root;

		public MockModel(MockNode root) {
			this.root = root;
		}
		
		@Override
		public void setTitle(String title) {
			throw new NotSupportedException();
		}

		@Override
		public String getTitle() {
			return null;
		}

		@Override
		public Expression<? extends ReferenceManager> referenceManager() {
			return null;
		}

		@Override
		public Node getRoot() {
			return root;
		}

		@Override
		public void add(Node node) {
		}

		@Override
		public void add(Container parent, Node node) {
		}

		@Override
		public void remove(Node node) {
		}

		@Override
		public void remove(Collection<? extends Node> nodes) {
		}

		@Override
		public PVector<Object> getProperties(Node node) {
			return null;
		}

		@Override
		public Expression<? extends NodeContext> nodeContext() {
			return null;
		}
	}
	
	private static final class MockNode implements Node {
		
		public MockNode(Rectangle2D shape, PVector<MockNode> children) {
			super();
			this.children = children;
		}
		public PVector<MockNode> children;
		
		@Override
		public ModifiableExpression<Node> parent() {
			return null;
		}
		@Override
		public Expression<? extends Collection<? extends Node>> children() {
			return Expressions.constant(children);
		}
	}

	@Test
	public void testEmpty() throws IOException, ModelValidationException, SerialisationException, ServiceNotAvailableException{
		DotExporter exporter = new DotExporter(TouchableProvider.DEFAULT);
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		final MockModel model = new MockModel(new MockNode(null, TreePVector.<MockNode>empty()));
		ModelServices serviceProviderImpl = ModelServicesImpl.EMPTY.plus(DotExportableService.SERVICE_HANDLE, new DotExportableService(){
			@Override
			public Model getModel() {
				return model;
			}
		});
		exporter.getExportJob(serviceProviderImpl).export(outStream);
		Assert.assertEquals("digraph work {\ngraph [nodesep=\"0.5\", overlap=\"false\", splines=\"true\"];\nnode [shape=box];\n}\n", outStream.toString("UTF-8"));
	}

	@Test
	public void testSingleNode() throws IOException, ModelValidationException, SerialisationException{
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		DotExporter.export(new DotExportable() {
			
			@Override
			public DotExportNode[] getNodes() {
				ArrayList<DotExportNode> nodes = new ArrayList<DotExportNode>();
				nodes.add(new DotExportNode() {
					
					@Override
					public String[] getOutgoingArcs() {
						return new String[]{"b"};
					}
					
					@Override
					public String getName() {
						return "a";
					}
					
					@Override
					public Dimension2D getDimensions() {
						return new Dimension2D(){

							@Override
							public double getWidth() {
								return 1;
							}

							@Override
							public double getHeight() {
								return 1;
							}

							@Override
							public void setSize(double width, double height) {
								throw new NotSupportedException();
							}
						};
					}
				});
				nodes.add(new DotExportNode() {
					
					@Override
					public String[] getOutgoingArcs() {
						return new String[]{};
					}
					
					@Override
					public String getName() {
						return "b";
					}
					
					@Override
					public Dimension2D getDimensions() {
						return new Dimension2D(){

							@Override
							public double getWidth() {
								return 2;
							}

							@Override
							public double getHeight() {
								return 1;
							}

							@Override
							public void setSize(double width, double height) {
								throw new NotSupportedException();
							}
						};
					}
				});
				return nodes.toArray(new DotExportNode[0]);
			}
		}, outStream);
		String expected = "digraph work {\ngraph [nodesep=\"0.5\", overlap=\"false\", splines=\"true\"];\nnode [shape=box];\n"+
				"\"a\" [width=\"1.0\", height=\"1.0\", fixedsize=\"true\"];\n" +
				"\"a\" -> \"b\";\n" +
				"\"b\" [width=\"2.0\", height=\"1.0\", fixedsize=\"true\"];\n" +
				"}\n";
		String actual = outStream.toString("UTF-8");
		Assert.assertEquals(expected, actual);
	}
}
