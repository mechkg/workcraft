/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
* 
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.gui.propertyeditor.string;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTextField;

import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.gui.propertyeditor.EditableProperty;
import org.workcraft.gui.propertyeditor.GenericCellEditor;
import org.workcraft.gui.propertyeditor.GenericEditorProvider;
import org.workcraft.gui.propertyeditor.RendererProvider;
import org.workcraft.util.Action;

public class StringProperty {

	private static final GenericEditorProvider<String> EDITOR_PROVIDER = new GenericEditorProvider<String>() {
		
		@Override
		public GenericCellEditor<String> createEditor(String initialValue, Action accept, Action cancel) {
			final JTextField textField = new JTextField();
			textField.setFocusable(true);
			textField.setText(initialValue);
			
			return new GenericCellEditor<String>() {

				@Override
				public Component component() {
					return textField;
				}

				@Override
				public String getValue() {
					return textField.getText();
				}
			};
		}
	};
	
	public static final RendererProvider<String> RENDERER_PROVIDER = new RendererProvider<String>() {
		
		@Override
		public Component createRenderer(String value) {
			// TODO: think about missing features from DefaultTableCellRenderer 
			return new JLabel(value);
		}
	};

	public static EditableProperty create(String name, final ModifiableExpression<String> property) {
		return EditableProperty.Util.create(name, EDITOR_PROVIDER, RENDERER_PROVIDER, property);
	}
		
	public static class ParseException extends Exception {
		private static final long serialVersionUID = 1L;
		public ParseException(Throwable cause) {
			super(cause);
		}
	}
}
