package org.workcraft.plugins.cpog;

import static org.workcraft.util.Maybe.Util.just;
import static org.workcraft.util.Maybe.Util.nothing;

import org.workcraft.util.Maybe;

public interface ComponentVisitor<T> {
	public T visitRho(org.workcraft.plugins.cpog.scala.nodes.RhoClause rho);
	public T visitVariable(org.workcraft.plugins.cpog.scala.nodes.Variable variable);
	public T visitVertex(org.workcraft.plugins.cpog.scala.nodes.Vertex vertex);
	
	class ComponentVisitorWithDefaultValue<T> implements ComponentVisitor<T> {
		private final T value;

		public ComponentVisitorWithDefaultValue(T value) {
			this.value = value;
		}
		
		@Override
		public T visitRho(org.workcraft.plugins.cpog.scala.nodes.RhoClause rho) {
			return value;
		}
		
		@Override
		public T visitVariable(org.workcraft.plugins.cpog.scala.nodes.Variable variable) {
			return value;
		};
		
		@Override
		public T visitVertex(org.workcraft.plugins.cpog.scala.nodes.Vertex vertex) {
			return value;
		};
	}
	
	class Util {
		/** <pre>
		 * asVertex (Vertex v) -> Just v
		 * asVertex _ -> Nothing
		 * </pre>
		 */
		public static Maybe<org.workcraft.plugins.cpog.scala.nodes.Vertex> asVertex(org.workcraft.plugins.cpog.scala.nodes.Component component) {
			Maybe<org.workcraft.plugins.cpog.scala.nodes.Vertex> noVertex = nothing();
			return component.<Maybe<org.workcraft.plugins.cpog.scala.nodes.Vertex>>accept(new ComponentVisitorWithDefaultValue<Maybe<org.workcraft.plugins.cpog.scala.nodes.Vertex>>(noVertex){
				@Override
				public Maybe<org.workcraft.plugins.cpog.scala.nodes.Vertex> visitVertex(org.workcraft.plugins.cpog.scala.nodes.Vertex vertex) {
					return just(vertex);
				}
			});
		}
		
		public static boolean isVertex(org.workcraft.plugins.cpog.scala.nodes.Component component) {
			return component.accept(new ComponentVisitorWithDefaultValue<Boolean>(false) {
				@Override
				public Boolean visitVertex(org.workcraft.plugins.cpog.scala.nodes.Vertex vertex) {
					return true;
				}
			});
		}
	}
}
