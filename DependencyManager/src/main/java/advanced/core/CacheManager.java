package advanced.core;

import java.util.Map;
import java.util.WeakHashMap;

import collections.WeakDirectedGraph;

public class CacheManager {
	
	Map<Expression<?>, Object> cache = new WeakHashMap<Expression<?>, Object>();
	WeakDirectedGraph<Expression<?>> dependencies = new WeakDirectedGraph<Expression<?>>();  
	
	public void changed(Expression<?> ex) {
		cache.remove(ex);
		dependencies.removeOutgoingArcs(ex);
		for(Expression<?> dependant : dependencies.getIncoming(ex)) {
			changed(dependant);
		}
	}
	
	public void registerDependency(Expression<?> expr, Expression<?> dep) {
		dependencies.add(expr, dep);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T eval(final Expression<T> expression)
	{
		T result = (T) cache.get(expression);
		if(result == null) {
			result = expression.evaluate(new DependencyResolver()
			{

				@Override
				public <TT> TT resolve(Expression<TT> dependency) {
					registerDependency(expression, dependency);
					return eval(dependency);
				}

				@Override
				public void changed() {
					CacheManager.this.changed(expression);
				}
			}
			);
			cache.put(expression, result);
		}
		return result;
	}
}
