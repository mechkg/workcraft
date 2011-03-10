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

package org.workcraft.gui.propertyeditor.choice;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;

import org.workcraft.gui.propertyeditor.GenericCellEditor;
import org.workcraft.util.Action;
import org.workcraft.util.Pair;

import pcollections.PVector;

public class ChoiceCellEditor<T> implements GenericCellEditor<T> {
	private JComboBox comboBox;
	
	public ChoiceCellEditor(T initialValue, PVector<Pair<String,T>> choice, final Action accept) {
		comboBox = new JComboBox();
		comboBox.setEditable(false);
		comboBox.setFocusable(false);

		for (Pair<String,T> p : choice) {
			ComboboxItemWrapper comboBoxItem = new ComboboxItemWrapper(p);
			comboBox.addItem(comboBoxItem);
			if(p.getSecond().equals(initialValue))
				comboBox.setSelectedItem(comboBoxItem);
		}
		comboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				accept.run();
			}
		});
	}

	@Override
	public Component component() {
		return comboBox;
	}

	@SuppressWarnings("unchecked") // ComboBoxModel forces cast to an object. We cast back, hoping for the best.
	@Override
	public T getValue() {
		return (T)((ComboboxItemWrapper)comboBox.getSelectedItem()).getValue();
	}
}
