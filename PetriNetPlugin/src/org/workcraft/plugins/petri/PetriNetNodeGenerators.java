package org.workcraft.plugins.petri;

import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;

import javax.swing.Icon;

import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.gui.graph.tools.NodeGenerator;
import org.workcraft.util.GUI;

public class PetriNetNodeGenerators {
	public PetriNetNodeGenerators(VisualPetriNet petriNet) {
		this.petriNet = petriNet;
	}
	
	VisualPetriNet petriNet;

	public NodeGenerator placeGenerator = new NodeGenerator() {
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
		public void generate(Point2D where) throws NodeCreationException {
			VisualPlace place = petriNet.createPlace();
			place.position().setValue(where);
		}

		@Override
		public int getHotKeyCode() {
			return KeyEvent.VK_P;
		}
	};

	public NodeGenerator transitionGenerator = new NodeGenerator() {
		Icon icon = GUI.createIconFromSVG("images/icons/svg/transition.svg");

		@Override
		public Icon getIcon() {
			return icon;
		}

		@Override
		public String getLabel() {
			return "Transition";
		}

		@Override
		public void generate(Point2D where) throws NodeCreationException {
			VisualTransition transition = petriNet.createTransition();
			transition.position().setValue(where);
		}

		@Override
		public int getHotKeyCode() {
			return KeyEvent.VK_T;
		}
	};
}
