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

package org.workcraft.gui.propertyeditor.colour;


import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;

import org.workcraft.gui.propertyeditor.GenericCellEditor;
import org.workcraft.gui.propertyeditor.GenericEditorProvider;
import org.workcraft.util.Action;

public class ColorCellEditor implements GenericEditorProvider<Color>{
	
	private static final class GenericCellEditorImplementation implements GenericCellEditor<Color> {
		final JButton button;
		final JColorChooser colorChooser;
		public GenericCellEditorImplementation(final Color initialValue, final Action accept, final Action cancel) {
			button = new JButton();
			button.setBorderPainted(false);
			button.setFocusable(false);
			button.setBackground(initialValue);
			
			colorChooser = new JColorChooser();
			colorChooser.setColor(initialValue);
			final JDialog dialog = JColorChooser.createDialog(null,
					"Pick a Color",
					true,  //modal
					colorChooser,
					new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							accept.run();
						}
					},  //OK button handler
					new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							cancel.run();
						}
					}); //CANCEL button handler
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
//					The user has clicked the cell, so
//					bring up the dialog.
					dialog.setVisible(true);
				}
			});
		}

		@Override
		public Component component() {
			return button;
		}

		@Override
		public Color getValue() {
			return colorChooser.getColor();
		}
	}

	@Override
	public GenericCellEditor<Color> createEditor(Color initialValue, Action accept, Action cancel) {
		return new GenericCellEditorImplementation(initialValue, accept, cancel);
	}
}
