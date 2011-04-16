package org.workcraft.plugins.cpog;

public interface NodeVisitor<T> {
	public T visitArc(Arc arc);
	public T visitComponent(Component component);
}
