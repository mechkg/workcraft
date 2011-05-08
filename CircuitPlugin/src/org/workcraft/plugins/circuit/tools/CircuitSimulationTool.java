package org.workcraft.plugins.circuit.tools;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.eval;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.TouchableProvider;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.plugins.circuit.Contact;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualContact;
import org.workcraft.plugins.circuit.stg.CircuitPetriNetGenerator;
import org.workcraft.plugins.stg.DefaultStorageManager;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.STGModel;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.SignalTransition.Direction;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.plugins.stg.tools.STGSimulationTool;
import org.workcraft.util.Func;
import org.workcraft.util.Hierarchy;

public class CircuitSimulationTool extends STGSimulationTool {

	VisualCircuit circuit;
	GraphEditor editor;
	
	JButton copyInitButton;
	
	@Override
	public String getLabel() {
		return "Simulation";
	}
	
	STG stg;
	
	@Override
	public void activated() {
		this.circuit = (VisualCircuit)editor.getModel();
		
		VisualSTG visualSTG = CircuitPetriNetGenerator.generate(circuit, new DefaultStorageManager());
		visualNet = visualSTG;
		stg = visualSTG.stg;
		net = stg;
		
		initialMarking = readMarking();
		traceStep = 0;
		branchTrace = null;
		branchStep = 0;
		
		update();
		
	}
	
	@Override
	public void update() {
		super.update();
		editor.repaint();
	}

	// return first enabled transition
	public static SignalTransition isContactExcited(VisualContact c, STGModel net) {
		boolean up=false;
		boolean down=false;
		
		SignalTransition st=null;
		if (c==null) return null;
		
		for (SignalTransition tr: c.getReferencedTransitions()) {
			if (net.isEnabled(tr)) {
				if (st==null) st = tr;
				if (eval(net.direction(tr))==Direction.MINUS)
					down = true;
				if (eval(net.direction(tr))==Direction.PLUS)
					up=true;
				if (up&&down) break;
			}
		}
		
		if (up&&down) return null;
		return st;
	}
	
	public CircuitSimulationTool(GraphEditor editor) {
		super(editor, TouchableProvider.DEFAULT);
		this.editor = editor;
		createInterface();
		
	}
	private void createInterface() {
		
		copyInitButton = new JButton ("Copy init");
		copyInitButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				copyInit();
			}
		});
		
		interfacePanel.add(copyInitButton);
		
	}
	
	private void copyInit() {
		
		for (VisualContact vc: Hierarchy.getDescendantsOfType(circuit.getRoot(), VisualContact.class)) {
			Contact c = (Contact)vc.getReferencedComponent();
			if (!vc.getReferencedTransitions().isEmpty()) {
				c.initOne().setValue(eval(vc.getReferencedOnePlace().tokens())==1);
			}
		}
	}
	
	@Override
	public void mousePressed(GraphEditorMouseEvent e) {
		Node node = HitMan.hitDeepest(TouchableProvider.DEFAULT, e.getPosition(), e.getModel().getRoot(), new Func<Node, Boolean>()
				{
					@Override
					public Boolean eval(Node node) {
						return node instanceof VisualContact;
					}
				});
		
		if (node==null) return;
		SignalTransition st = isContactExcited((VisualContact)node, stg);
		if (st!=null) {
			executeTransition(st);
			update();
		}
	}
	
//	@Override
//	public Expression<? extends NodeGraphicalContentProvider> getDecorator() { // TODO: make it dependent on the enabledness
//		return new ExpressionBase<NodeGraphicalContentProvider>(){
//
//			@Override
//			protected NodeGraphicalContentProvider evaluate(EvaluationContext context) {
//				return new NodeGraphicalContentProvider() {
//
//					@Override
//					public Decoration getDecoration(Node node) {
//						if(node instanceof VisualContact) {
//							
//							VisualContact contact = (VisualContact)node;
//							
//							String transitionId = null;
//							Node transition2 = null;
//							
//							if (branchTrace!=null&&branchStep<branchTrace.size()) {
//								transitionId = branchTrace.get(branchStep);
//								transition2 = eval(net.referenceManager()).getNodeByReference(transitionId);
//							} else if (branchTrace==null&&trace!=null&&traceStep<trace.size()) {
//								transitionId = trace.get(traceStep);
//								transition2 = eval(net.referenceManager()).getNodeByReference(transitionId);
//							}
//							
//							if (contact.getReferencedTransitions().contains(transition2)) {
//								return new Decoration(){
//
//									@Override
//									public Color getColorisation() {
//										return PetriNetSettings.getEnabledBackgroundColor();
//									}
//
//									@Override
//									public Color getBackground() {
//										return PetriNetSettings.getEnabledForegroundColor();
//									}
//								};
//								
//							}
//							
//								
//							if (isContactExcited((VisualContact)node, stg)!=null)
//								return new Decoration(){
//									@Override
//									public Color getColorisation() {
//										return PetriNetSettings.getEnabledForegroundColor();
//									}
//
//									@Override
//									public Color getBackground() {
//										return PetriNetSettings.getEnabledBackgroundColor();
//									}
//								};
//							
//							if (!contact.getReferencedTransitions().isEmpty()) return null;
//								
//							if (contact.getReferencedOnePlace()==null||contact.getReferencedZeroPlace()==null) return null;
//							
//							boolean isOne = eval(contact.getReferencedOnePlace().tokens())==1;
//							boolean isZero = eval(contact.getReferencedZeroPlace().tokens())==1;
//							
//							
//							if (isOne&&!isZero)
//								return new Decoration(){
//									@Override
//									public Color getColorisation() {
//										return null;
//									}
//									
//									@Override
//									public Color getBackground() {
//										return CircuitSettings.getActiveWireColor();
//									}
//								};
//							
//							if (!isOne&&isZero)
//								return new Decoration(){
//									@Override
//									public Color getColorisation() {
//										return null;
//									}
//									@Override
//									public Color getBackground() {
//										return CircuitSettings.getInactiveWireColor();
//									}
//								};
//							
//						} else if (node instanceof VisualJoint) {
//							VisualJoint vj = (VisualJoint)node;
//							
//							if (vj.getReferencedOnePlace()==null||vj.getReferencedZeroPlace()==null) return null;
//							
//							boolean isOne = eval(vj.getReferencedOnePlace().tokens())==1;
//							boolean isZero = eval(vj.getReferencedZeroPlace().tokens())==1;
//							
//							if (isOne&&!isZero)
//								return new Decoration(){
//									@Override
//									public Color getColorisation() {
//										return CircuitSettings.getActiveWireColor();
//									}
//									@Override
//									public Color getBackground() {
//										return null;
//									}
//								};
//							if (!isOne&&isZero)
//								return new Decoration(){
//									@Override
//									public Color getColorisation() {
//										return CircuitSettings.getInactiveWireColor();
//									}
//									@Override
//									public Color getBackground() {
//										return null;
//									}
//								};
//						} else if (node instanceof VisualCircuitConnection) {
//							VisualCircuitConnection vc = (VisualCircuitConnection)node;
//							
//							if (vc.getReferencedOnePlace()==null||vc.getReferencedZeroPlace()==null) return null;
//							
//							boolean isOne = eval(vc.getReferencedOnePlace().tokens())==1;
//							boolean isZero = eval(vc.getReferencedZeroPlace().tokens())==1;
//							
//							if (isOne&&!isZero)
//								return new Decoration(){
//									@Override
//									public Color getColorisation() {
//										return CircuitSettings.getActiveWireColor();
//									}
//									@Override
//									public Color getBackground() {
//										return null;
//									}
//								};
//							if (!isOne&&isZero)
//								return new Decoration(){
//									@Override
//									public Color getColorisation() {
//										return CircuitSettings.getInactiveWireColor();
//									}
//									@Override
//									public Color getBackground() {
//										return null;
//									}
//								};
//							
//						}
//						return null;
//					} 
//				};
//			}
//		};
//	}
}
