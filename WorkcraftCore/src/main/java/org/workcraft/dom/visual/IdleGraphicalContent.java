package org.workcraft.dom.visual;

import java.awt.Graphics2D;

import org.workcraft.gui.graph.tools.Decorator;

public class IdleGraphicalContent implements PartialHierarchicalGraphicalContent {
	
	public final static IdleGraphicalContent INSTANCE = new IdleGraphicalContent();
	
	@Override
	public void draw(Graphics2D graphics, Decorator decorator, org.workcraft.gui.graph.tools.Decoration decoration) {
	}
}
