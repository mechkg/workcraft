package traditional.user;

import util.listeners.Listener;

public class SumExpression {
	private final SimpleVariable<Integer> a;
	private final SimpleVariable<Integer> b;

	Integer value;
	
	public SumExpression(SimpleVariable<Integer> a, SimpleVariable<Integer> b) {
		this.a = a;
		this.b = b;
		Listener invalidator = new Listener() {
			@Override
			public void changed() {
				invalidate();
			}
		};
		a.listeners().addListener(invalidator);
		b.listeners().addListener(invalidator);
	}
	
	public int getValue()
	{
		validate();
		return value;
	}
	
	private void validate() {
		if(value == null)
			value = a.getValue() + b.getValue();
	}

	private void invalidate() {
		value = null;
	}
}
