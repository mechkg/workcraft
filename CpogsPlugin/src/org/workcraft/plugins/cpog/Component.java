package org.workcraft.plugins.cpog;

import org.workcraft.util.Maybe;
import static org.workcraft.util.Maybe.Util.*;

public interface Component extends Node {
	class ComponentVisitorWithDefaultValue<T> implements ComponentVisitor<T> {
		private final T value;

		public ComponentVisitorWithDefaultValue(T value) {
			this.value = value;
		}
		
		@Override
		public T visitRho(RhoClause rho) {
			return value;
		}
		
		@Override
		public T visitVariable(Variable variable) {
			return value;
		};
		
		@Override
		public T visitVertex(Vertex vertex) {
			return value;
		};
	}
	
	class Util {
		/** <pre>
		 * asVertex (Vertex v) -> Just v
		 * asVertex _ -> Nothing
		 * </pre>
		 */
		public static Maybe<Vertex> asVertex(Component component) {
			Maybe<Vertex> noVertex = nothing();
			return component.accept(new ComponentVisitorWithDefaultValue<Maybe<Vertex>>(noVertex){
				@Override
				public Maybe<Vertex> visitVertex(Vertex vertex) {
					return just(vertex);
				}
			});
		}
		
		public static boolean isVertex(Component component) {
			return component.accept(new ComponentVisitorWithDefaultValue<Boolean>(false) {
				@Override
				public Boolean visitVertex(Vertex vertex) {
					return true;
				}
			});
		}
		
	}

	<T> T accept(ComponentVisitor<T> visitor);
}
