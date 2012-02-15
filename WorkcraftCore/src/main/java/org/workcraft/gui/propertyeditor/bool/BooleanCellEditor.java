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

package org.workcraft.gui.propertyeditor.bool;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;

import org.workcraft.gui.propertyeditor.GenericCellEditor;
import org.workcraft.gui.propertyeditor.GenericEditorProvider;
import org.workcraft.util.Action;

public class BooleanCellEditor {

	public static GenericEditorProvider<Boolean> INSTANCE = new GenericEditorProvider<Boolean>() {
		@Override
		public GenericCellEditor<Boolean> createEditor(Boolean initialValue, final Action accept, Action cancel) {
			final JCheckBox checkBox = new JCheckBox();
			checkBox.setOpaque(false);
			checkBox.setFocusable(false);
			checkBox.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					accept.run();
				}
			});
			return new GenericCellEditor<Boolean>() {
				@Override
				public Component component() {
					return checkBox;
				}

				@Override
				public Boolean getValue() {
					return checkBox.isSelected();
				}
			};
		}
	};
}
