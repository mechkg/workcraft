package org.workcraft.gui.propertyeditor.choice;

import org.workcraft.util.Pair;

class ComboboxItemWrapper {
	private final Object value;
	private final String text;
	public ComboboxItemWrapper(Pair<String,? extends Object> pair) {
		this(pair.getFirst(), pair.getSecond());
	}
	public ComboboxItemWrapper(String text, Object value) {
		this.value = value;
		this.text = text;
	}
	public Object getValue() {
		return value;
	}
	@Override
	public String toString() {
		return text;
	}
}
