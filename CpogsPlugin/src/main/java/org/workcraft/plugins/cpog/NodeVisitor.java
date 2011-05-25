package org.workcraft.plugins.cpog;

import org.workcraft.util.Function;

import org.workcraft.plugins.cpog.scala.nodes.*;

public abstract class NodeVisitor<T> implements Function<Node, T> {
	public abstract T visitArc(Arc arc);
	public abstract T visitComponent(Component component);
	
	public final Function<Arc, T> arcVisitor() {
		return new Function<Arc, T>(){
			@Override
			public T apply(Arc arc) {
				return visitArc(arc);
			}
		};
	}
	
	public final Function<Component, T> componentVisitor() {
		return new Function<Component, T>(){
			@Override
			public T apply(Component component) {
				return visitComponent(component);
			}
		};
	}
	
	public final T apply(Node node) {
		return node.<T>accept(this);
	}
	
	public static <T> NodeVisitor<T> create(final Function<Arc, T> arcVisitor, final Function<Component, T> componentVisitor) {
		return new NodeVisitor<T>() {

			@Override
			public T visitArc(Arc arc) {
				return arcVisitor.apply(arc);
			}

			@Override
			public T visitComponent(Component component) {
				return componentVisitor.apply(component);
			}
		};
	}
}
