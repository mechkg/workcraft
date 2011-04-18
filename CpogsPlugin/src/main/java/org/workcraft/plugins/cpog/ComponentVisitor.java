package org.workcraft.plugins.cpog;

public interface ComponentVisitor<T> {
	public T visitRho(RhoClause rho);
	public T visitVariable(Variable variable);
	public T visitVertex(Vertex vertex);
}
