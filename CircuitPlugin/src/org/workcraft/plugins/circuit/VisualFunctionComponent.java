package org.workcraft.plugins.circuit;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.eval;

import java.awt.event.KeyEvent;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.plugins.circuit.Contact.IoType;
import org.workcraft.util.Hierarchy;

@DisplayName("Function")
@Hotkey(KeyEvent.VK_F)
@SVGIcon("images/icons/svg/circuit-formula.svg")

public class VisualFunctionComponent extends VisualCircuitComponent {
	
	public VisualFunctionComponent(CircuitComponent component) {
		super(component);
	}
	
	public VisualFunctionContact addFunction(String name, IoType ioType, boolean allowShort) {
		name = Contact.getNewName(this.getReferencedComponent(), name, null, allowShort);
		
		VisualContact.Direction dir=null;
		if (ioType==null) ioType = IoType.OUTPUT;
		
		dir=VisualContact.Direction.WEST;
		if (ioType==IoType.OUTPUT)
			dir=VisualContact.Direction.EAST;
		
		FunctionContact c = new FunctionContact(ioType, name);
		
		VisualFunctionContact vc = new VisualFunctionContact(c, dir);
		addContact(vc);
		
		return vc;
	}
	
	public VisualFunctionContact getOrCreateInput(String arg) {

		for(VisualFunctionContact c : Hierarchy.filterNodesByType(eval(children()), VisualFunctionContact.class)) {
			if(eval(c.name()).equals(arg)) return c;
		}
		
		return addFunction(arg, IoType.INPUT, true); 
	}


}
