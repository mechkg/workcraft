package org.workcraft.plugins.stg;

import static java.util.Arrays.asList;
import static org.workcraft.gui.graph.tools.GraphEditorToolUtil.attachPainter;
import static org.workcraft.gui.graph.tools.GraphEditorToolUtil.attachParameterisedPainter;

import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;

import javax.swing.Icon;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.Expressions;
import org.workcraft.dom.Node;
import org.workcraft.dom.references.ReferenceManager;
import org.workcraft.dom.visual.DrawMan;
import org.workcraft.dom.visual.GraphicalContent;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.TouchableProvider;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.gui.DefaultReflectiveModelPainter;
import org.workcraft.gui.graph.tools.Colorisator;
import org.workcraft.gui.graph.tools.ConnectionTool;
import org.workcraft.gui.graph.tools.CustomToolsProvider;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.graph.tools.GraphEditorTool;
import org.workcraft.gui.graph.tools.NodeGenerator;
import org.workcraft.gui.graph.tools.NodeGeneratorTool;
import org.workcraft.gui.graph.tools.NodePainter;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.tools.STGSimulationTool;
import org.workcraft.util.Func;
import org.workcraft.util.Function;
import org.workcraft.util.GUI;

public class STGToolsProvider implements CustomToolsProvider {

	private final static class PlaceGenerator implements NodeGenerator {
		Icon icon = GUI.createIconFromSVG("images/icons/svg/place.svg");

		@Override
		public Icon getIcon() {
			return icon;
		}

		@Override
		public String getLabel() {
			return "Place";
		}

		@Override
		public void generate(VisualModel model, Point2D where) throws NodeCreationException {
			VisualPlace place = ((VisualSTG)model).createPlace();
			place.position().setValue(where);
		}

		@Override
		public int getHotKeyCode() {
			return KeyEvent.VK_P;
		}
	}

	private final static class SignalTransitionGenerator implements NodeGenerator {
		Icon icon = GUI.createIconFromSVG("images/icons/svg/signal-transition.svg");

		@Override
		public Icon getIcon() {
			return icon;
		}

		@Override
		public String getLabel() {
			return "Signal Transition";
		}

		@Override
		public void generate(VisualModel model, Point2D where) throws NodeCreationException {
			VisualSignalTransition transition = ((VisualSTG)model).createSignalTransition();
			transition.position().setValue(where);
		}

		@Override
		public int getHotKeyCode() {
			return KeyEvent.VK_T;
		}
	}
	
	private final static class DummyTransitionGenerator implements NodeGenerator {
		Icon icon = GUI.createIconFromSVG("images/icons/svg/transition.svg");

		@Override
		public Icon getIcon() {
			return icon;
		}

		@Override
		public String getLabel() {
			return "Dummy Transition";
		}

		@Override
		public void generate(VisualModel model, Point2D where) throws NodeCreationException {
			VisualDummyTransition transition = ((VisualSTG)model).createDummyTransition();
			transition.position().setValue(where);
		}

		@Override
		public int getHotKeyCode() {
			return KeyEvent.VK_D;
		}
	}
	

	private Expression<? extends String> transitionName(final STG stg, final VisualSignalTransition vst) {
		return Expressions.bindFunc(stg.referenceManager(), new Function<ReferenceManager, String>() {
			@Override
			public String apply(ReferenceManager refMan) {
				return refMan.getNodeReference(vst.getReferencedTransition());
			}
		});
	}
	
	@Override
	public Iterable<GraphEditorTool> getTools(final GraphEditor editor) {

		final STG stg = ((VisualSTG)editor.getModel()).stg;
		
		TouchableProvider<Node> localTP = new TouchableProvider<Node>(){
			@Override
			public Expression<? extends Touchable> apply(Node node) {
				if(node instanceof VisualSignalTransition) {
					final VisualSignalTransition vst = (VisualSignalTransition)node;
					return vst.localSpaceTouchable(transitionName(stg, vst));
				} else 
					if(node instanceof VisualDummyTransition) {
						final VisualDummyTransition vdt = (VisualDummyTransition)node;
						return vdt.shape(stg.name(vdt.getReferencedTransition()));
					} else
				return LOCAL_REFLECTIVE.apply(node);
			}
		};
		
		final TouchableProvider<Node> tp = TouchableProvider.Util.applyTransformAndAddVisualGroups(localTP);

		final Func<Colorisator, Expression<? extends GraphicalContent>> painterProvider = new Func<Colorisator, Expression<? extends GraphicalContent>>() {
			@Override
			public Expression<? extends GraphicalContent> eval(final Colorisator colorisator) {
				
				final NodePainter myNodePainter = new NodePainter() {
					@Override
					public Expression<? extends GraphicalContent> getGraphicalContent(Node node) {
						if(node instanceof VisualSignalTransition) {
							final VisualSignalTransition vst = (VisualSignalTransition)node;
							return DefaultReflectiveModelPainter.ReflectiveNodePainter.colorise(vst.getGraphicalContent(transitionName(stg, vst)), colorisator.getColorisation(node));
						} else
							if(node instanceof VisualDummyTransition) {
								final VisualDummyTransition vdt = (VisualDummyTransition)node;
								return DefaultReflectiveModelPainter.ReflectiveNodePainter.colorise(vdt.graphicalContent(stg.name(vdt.getReferencedTransition())), colorisator.getColorisation(node));
							} else
							return new DefaultReflectiveModelPainter.ReflectiveNodePainter(tp, colorisator).getGraphicalContent(node);
					}
				};
				
				return DrawMan.graphicalContent(editor.getModel().getRoot(), myNodePainter);
			}
		};
		final Expression<? extends GraphicalContent> simpleModelPainter = painterProvider.eval(Colorisator.EMPTY);
		
		return asList(
				attachParameterisedPainter(new STGSelectionTool(editor, tp), painterProvider),
				attachParameterisedPainter(new ConnectionTool(editor, tp), painterProvider),
				attachPainter(new NodeGeneratorTool(new PlaceGenerator()), simpleModelPainter),
				attachPainter(new NodeGeneratorTool(new SignalTransitionGenerator()), simpleModelPainter),
				attachPainter(new NodeGeneratorTool(new DummyTransitionGenerator()), simpleModelPainter),
				attachParameterisedPainter(new STGSimulationTool(editor, tp), painterProvider));
	}

}
