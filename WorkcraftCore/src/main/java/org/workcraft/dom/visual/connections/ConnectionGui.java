package org.workcraft.dom.visual.connections;

import org.workcraft.dom.visual.ColorisableGraphicalContent;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.util.Function;

public interface ConnectionGui {
	
	public static Function<ConnectionGui, ColorisableGraphicalContent> getGraphicalContent = new Function<ConnectionGui, ColorisableGraphicalContent>(){
		@Override
		public ColorisableGraphicalContent apply(ConnectionGui argument) {
			return argument.graphicalContent();
		}
	};
	
	public static Function<ConnectionGui, Touchable> getTouchable = new Function<ConnectionGui, Touchable>(){
		@Override
		public Touchable apply(ConnectionGui argument) {
			return argument.shape();
		}
	};
	
	Touchable shape();
	ColorisableGraphicalContent graphicalContent();
	ParametricCurve parametricCurve();
}
