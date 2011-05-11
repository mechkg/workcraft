package org.workcraft.plugins.cpog.optimisation;

import org.workcraft.util.Function;
import org.workcraft.util.Function0;


public class FreeVariable implements Comparable<FreeVariable> {

	private final String label;

	public FreeVariable(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	@Override
	public int compareTo(FreeVariable var) {
		return -var.getLabel().compareTo(getLabel());
	}
	
	public static Function<String, FreeVariable> constructor = new Function<String, FreeVariable>(){
		@Override
		public FreeVariable apply(String argument) {
			return new FreeVariable(argument);
		}
	};
	public static Function0<FreeVariable> emptyConstructor = new Function0<FreeVariable>(){
		@Override
		public FreeVariable apply() {
			return constructor.apply("someVar");
		}
	};
	public static Function<FreeVariable, String> labelGetter = new Function<FreeVariable, String>(){
		@Override
		public String apply(FreeVariable argument) {
			return argument.getLabel();
		}
	};	
}
