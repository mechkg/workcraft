package advanced.core;

public interface Expression<T> {
	T evaluate(DependencyResolver resolver);
}
