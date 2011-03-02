package org.workcraft.dependencymanager.advanced.core;

public interface Maybe<T> {
	class Util {
		public static <T> Maybe<T> just(final T value) {
			return new Maybe<T>(){
				@Override
				public <R> R accept(MaybeVisitor<T, R> visitor) {
					return visitor.visitJust(value);
				}
			};
		}
		
		public static <T> Maybe<T> nothing() {
			return new Maybe<T>(){
				@Override
				public <R> R accept(MaybeVisitor<T, R> visitor) {
					return visitor.visitNothing();
				}
			};
		}
	}

	public <R> R accept(MaybeVisitor<T,R> visitor);
}
