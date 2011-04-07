package org.workcraft.plugins.stg.tools;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.*;
import static org.workcraft.plugins.petri.tools.PetriNetSpecific.*;
import static org.workcraft.util.Maybe.Util.*;

import java.awt.Color;
import java.awt.Component;
import java.awt.geom.Point2D;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.TouchableProvider;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.gui.SimpleFlowLayout;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.petri.PetriNetSettings;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.tools.PetriNetSpecific;
import org.workcraft.plugins.petri.tools.SimControl;
import org.workcraft.plugins.petri.tools.SimStateControl;
import org.workcraft.plugins.petri.tools.SimulationControlPanel;
import org.workcraft.plugins.petri.tools.SimulationState;
import org.workcraft.plugins.petri.tools.SimulationTool;
import org.workcraft.plugins.petri.tools.SimulationTraceTable;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.util.Func;
import org.workcraft.util.Function;
import org.workcraft.util.Function2;
import org.workcraft.util.Maybe;

public class STGSimulationTool {
	
	private static Color inputsColor = Color.RED.darker();
	private static Color outputsColor = Color.BLUE.darker();
	private static Color internalsColor = Color.GREEN.darker();
	
	public static Function<Point2D, Maybe<String>> getHitTester(final PetriNetModel net, TouchableProvider<Node> touchableProvider, final Node visualRoot) {
		final Function<Node, Maybe<? extends Touchable>> tp = eval(TouchableProvider.Util.asAWhole(touchableProvider));

		return new Function<Point2D, Maybe<String>>(){
			@Override
			public Maybe<String> apply(Point2D position) {
				Node node = HitMan.hitDeepest(tp, position, visualRoot, new Func<Node, Boolean>() {
					@Override
					public Boolean eval(Node node) {
						return node instanceof VisualSignalTransition && net.isEnabled(((VisualSignalTransition) node).getReferencedTransition());
					}
				});
				if(node == null)
					return nothing();
				else
					return just(eval(net.referenceManager()).getNodeReference(((VisualSignalTransition) node).getReferencedTransition()));
			}
		};
	}
	
	public static SimulationTool<String> createSimulationTool(VisualGroup root, STG stg, TouchableProvider<Node> touchableProvider) {

		SimulationTraceTable<Map<Place,Integer>> traceTable = new SimulationTraceTable<Map<Place,Integer>>(petriNetAsSimulationModel(stg), getTableCellRenderer(stg));
		SimControl<String> simControl = traceTable.asSimControl();
		SimulationControlPanel<SimulationState<Map<Place,Integer>>> controlPanel = new SimulationControlPanel<SimulationState<Map<Place,Integer>>>(traceTable.getSimControl());
		SimStateControl simStateControl = controlPanel.asStateControl();
		
		JPanel interfacePanel = new JPanel(new SimpleFlowLayout(5, 5));

		for(Component controlComponent : controlPanel.components())
			interfacePanel.add(controlComponent);
		
		for(Component controlComponent : traceTable.components())
			interfacePanel.add(controlComponent);
		
		Function<Point2D, Maybe<String>> hitTester = getHitTester(stg, touchableProvider, root);
		Function<Node, Maybe<String>> nodeEventExtractor = PetriNetSpecific.nodeEventExtractor(stg);
		
		return new SimulationTool<String>(simControl, simStateControl, hitTester, nodeEventExtractor, interfacePanel);
	}
	
	static Function2<String, Boolean, Component> getTableCellRenderer(final PetriNetModel net) { 
		return new Function2<String, Boolean, Component>(){
	
			@Override
			public Component apply(String transitionId, Boolean isActive) {
				JLabel label = new JLabel();
				label.setOpaque(true);
				label.setForeground(Color.BLACK);
				
				label.setText(transitionId);
				
				Color fore = eval(PetriNetSettings.enabledForegroundColor);
				Color back = eval(PetriNetSettings.enabledBackgroundColor);
	
				if (isActive) {
					if (fore!=null&&back!=null) { 
						label.setBackground(fore);
						label.setForeground(back);
					} else {
						label.setBackground(Color.YELLOW);
					}
				} else {
					label.setBackground(Color.WHITE);
				}
	
				Node n = eval(net.referenceManager()).getNodeByReference(transitionId);
				if (n instanceof SignalTransition) {
					SignalTransition st = (SignalTransition)n;
					switch (eval(st.signalType())) {
						case INPUT:    label.setForeground(inputsColor); break;
						case OUTPUT:   label.setForeground(outputsColor); break;
						case INTERNAL: label.setForeground(internalsColor); break;
					}
				}
				
				return label;
			}
		};
	}
}
