	package org.workcraft.dependencymanager.traditional.user;
	
	import org.workcraft.dependencymanager.traditional.core.Expression;
	import org.workcraft.dependencymanager.util.listeners.Listener;
import org.workcraft.dependencymanager.util.listeners.ListenerCollection;
	
	public class SumExpression implements Expression<Integer> {
		private final Expression<Integer> a;
		private final Expression<Integer> b;
	
		Integer value;
		private Listener invalidator = new Listener() {
			@Override
			public void changed() {
				value = null;
			}
		};
		
		public SumExpression(SimpleVariable<Integer> a, SimpleVariable<Integer> b) {
			this.a = a;
			this.b = b;
			a.listeners().addListener(invalidator);// don't forget to call it!
			b.listeners().addListener(invalidator);
		}
		
		public Integer getValue()
		{
			validate();
			return value;
		}
		
		private void validate() {
			if(value == null)
				value = evaluate();
		}
	
		private Integer evaluate() {
			return a.getValue() + b.getValue();
		}

		public void dispose() { // USER, DON'T FORGET TO CALL IT!!!
			a.removeListener(invalidator);
			b.removeListener(invalidator);
		}
	
		ListenerCollection listeners = new ListenerCollection();
		
		@Override
		public void addListener(Listener l) {
			listeners.addListener(l);
		}
	
		@Override
		public void removeListener(Listener l) {
			listeners.removeListener(l);
		}
	}
