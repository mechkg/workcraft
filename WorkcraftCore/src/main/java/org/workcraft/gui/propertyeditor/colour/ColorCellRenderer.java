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

import javax.swing.BorderFactory;
import javax.swing.JLabel;

import org.workcraft.gui.propertyeditor.RendererProvider;

public class ColorCellRenderer {
	
	private static final boolean isBordered = true;
	
	public static RendererProvider<Color> INSTANCE = new RendererProvider<Color>(){

		@Override
		public Component createRenderer(Color value) {
			JLabel label = new JLabel();
			label.setOpaque(true); //MUST do this for background to show up.
			label.setFocusable(false);

	        label.setBackground((Color)value);
	        if (isBordered) {
	        	Color borderBackground = Color.GREEN; // TODO: label.isSelected ? table.getSelectionBackground() : table.getBackground()
                label.setBorder(BorderFactory.createMatteBorder(2,5,2,5,borderBackground));
	        }
	        
	        return label;
		}
		
	};
}
