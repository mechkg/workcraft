package advanced.core;

import util.listeners.Listener;

public interface DependencyResolver extends Listener {
	<T> T resolve(Expression<T> dependency);
}
