package org.workcraft.util;

public interface Action {
	public void run();
	public static final Action EMPTY = new Action(){
		@Override
		public void run() {
		}		
	};
	public static class Util {
		public static Action combine(final Action first, final Action second) {
			return new Action(){
				@Override
				public void run() {
					first.run();
					second.run();
				}
			};
		}
	}
}
