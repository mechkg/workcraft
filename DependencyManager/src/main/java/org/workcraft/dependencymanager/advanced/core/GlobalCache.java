package org.workcraft.dependencymanager.advanced.core;

import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.util.listeners.Listener;


public class GlobalCache {
	public static CacheManager resolver = new CacheManager();
	public static <T> T eval(Expression<T> expression) {
		return resolver.eval(expression);
	}
	
	public static void changed(Expression<?> expression) {
		resolver.changed(expression);
	}
	
	public static <T> void setValue(ModifiableExpression<? super T> expr, T value) {
		expr.setValue(new DependencyResolver() {
			@Override
			public <T2> T2 resolve(Expression<T2> dependency) {
				return eval(dependency);
			}
		}, value);
	}
	
	public static void onInvalidate(Expression<?> expr, Listener listener) {
		resolver.onInvalidate(expr, listener);
	}
	public static void autoRefresh(final Expression<?> expr) {
		resolver.onInvalidate(expr, new Listener(){
			@Override
			public void changed() {
				eval(expr);
			}
		});
		eval(expr);
	}
}
