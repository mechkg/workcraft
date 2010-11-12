package org.workcraft.dependencymanager.advanced.core;


public class GlobalCache {
	public static CacheManager resolver = new CacheManager();
	public static <T> T eval(Expression<T> expression) {
		return resolver.eval(expression);
	}
	
	public static void changed(Expression<?> expression) {
		resolver.changed(expression);
	}
}
