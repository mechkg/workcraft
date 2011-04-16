package org.workcraft.gui.propertyeditor.choice;

import java.awt.Component;

import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.gui.propertyeditor.EditableProperty;
import org.workcraft.gui.propertyeditor.GenericCellEditor;
import org.workcraft.gui.propertyeditor.GenericEditorProvider;
import org.workcraft.gui.propertyeditor.RendererProvider;
import org.workcraft.gui.propertyeditor.string.StringProperty;
import org.workcraft.util.Action;
import org.workcraft.util.Pair;

import pcollections.PVector;

public class ChoiceProperty {
	private static final <T> GenericEditorProvider<T> createEditorProvider(final PVector<Pair<String,T>> choice) { 
		return new GenericEditorProvider<T>(){

			@Override
			public GenericCellEditor<T> createEditor(T initialValue, Action accept, Action cancel) {
				return new ChoiceCellEditor<T>(initialValue, choice, accept);
			}
		};
	}
	
	private static final <T> RendererProvider<T> createRendererProvider(final PVector<Pair<String,T>> choice) {
		return new RendererProvider<T>() {
			String lookupValue(PVector<Pair<String,T>> choice, T value) {
				for(Pair<String,T> p : choice)
					if(p.getSecond().equals(value))
						return p.getFirst();
				throw new RuntimeException("The property value was outside of the specified choice");
			}
			
			@Override
			public Component createRenderer(T value) {
				return StringProperty.RENDERER_PROVIDER.createRenderer(lookupValue(choice, value));
			}
		};
	}

	public static <T> EditableProperty create(String name, PVector<Pair<String,T>> choice, ModifiableExpression<T> property) {
		return EditableProperty.Util.create(name, createEditorProvider(choice), createRendererProvider(choice), property);
	}
}
