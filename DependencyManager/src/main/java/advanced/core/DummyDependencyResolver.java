package advanced.core;

public class DummyDependencyResolver implements DependencyResolver {
	public <T> T resolve(Expression<T> dependency) {
		return dependency.evaluate(this);
	}
	public void changed() {
		
	}
}
