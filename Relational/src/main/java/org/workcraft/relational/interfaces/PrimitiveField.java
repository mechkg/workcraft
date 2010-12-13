package org.workcraft.relational.interfaces;

public interface PrimitiveField<T> {
	public class Instance<T> implements Field, PrimitiveField<T> {

		private final Class<T> type;

		public static <T> Instance<T> create(Class<T> type) {
			return new Instance<T>(type);
		}
		
		public Instance(Class<T> type) {
			this.type = type;
		}
		
		@Override
		public <T2> T2 accept(FieldVisitor<T2> visitor) {
			return visitor.visit(this);
		}
		
		@Override
		public Class<T> getType() {
			return type;
		}

	}

	Class<T> getType();
}