package org.workcraft.relational.interfaces;

public interface ObjectDeclaration {

	String name();
	
	public class Instance implements ObjectDeclaration {

		private final String name;

		public Instance(String name) {
			this.name = name;
		}

		@Override
		public String name() {
			return name;
		}
	}
}
