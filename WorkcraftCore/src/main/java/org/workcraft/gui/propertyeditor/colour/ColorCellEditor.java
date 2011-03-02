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
	
	Color currentColor;  
	JButton button;
	
	static JColorChooser colorChooser = null;
	static JDialog dialog = null;
	
	public  ColorCellEditor() {

		button = new JButton();
		button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				cellClicked();
			}
		});
		button.setBorderPainted(false);
		button.setFocusable(false);

//		Set up the dialog that the button brings up.

		colorChooser = new JColorChooser();
		ActionListener okListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {//User pressed dialog's "OK" button.
				currentColor = colorChooser.getColor();
			}
		};
		dialog = JColorChooser.createDialog(null,
				"Pick a Color",
				true,  //modal
				colorChooser,
				okListener,  //OK button handler
				null); //no CANCEL button handler
	}

	public void cellClicked() {
//			The user has clicked the cell, so
//			bring up the dialog.

			button.setBackground(currentColor);
			colorChooser.setColor(currentColor);
			dialog.setVisible(true);
	//		TODO: fireEditingStopped(); //Make the renderer reappear.
	}

	@Override
	public GenericCellEditor<Color> createEditor(Color initialValue, Action accept, Action cancel) {
		return new GenericCellEditor<Color>(){

			@Override
			public Component component() {
				return button;
			}

			@Override
			public Color getValue() {
				return currentColor;
			}
		};
	}
}
