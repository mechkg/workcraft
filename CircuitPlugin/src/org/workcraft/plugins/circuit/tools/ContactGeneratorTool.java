package org.workcraft.plugins.circuit.tools;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import javax.swing.Icon;

import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.NodeGenerator;
import org.workcraft.gui.graph.tools.NodeGeneratorTool;
import org.workcraft.plugins.circuit.Contact.IoType;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.util.GUI;

public class ContactGeneratorTool extends NodeGeneratorTool {
	static boolean shiftPressed;
	
	public ContactGeneratorTool() {
		super(new NodeGenerator()
		{
			Icon icon = GUI.createIconFromSVG("images/icons/svg/circuit-port.svg");
			
			@Override
			public Icon getIcon() {
				return icon;
			}

			@Override
			public String getLabel() {
				return "Input/output port";
			}

			@Override
			public void generate(VisualModel model, Point2D where)
					throws NodeCreationException {
				((VisualCircuit)model).createFunctionContact(shiftPressed ? IoType.INPUT : IoType.OUTPUT, where);
			}

			@Override
			public int getHotKeyCode() {
				return KeyEvent.VK_P;
			}
		});
	}

	@Override
	public void mousePressed(GraphEditorMouseEvent e) {
		
//		if (((e.getModifiers() & MouseEvent.SHIFT_DOWN_MASK) != 0)!=shiftPressed) {
//			System.out.print("shift!");
//		}
		shiftPressed = ((e.getModifiers() & MouseEvent.SHIFT_DOWN_MASK) != 0);
		
		super.mousePressed(e);
	}

}

