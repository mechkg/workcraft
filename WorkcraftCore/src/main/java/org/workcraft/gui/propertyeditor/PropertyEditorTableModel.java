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

package org.workcraft.gui.propertyeditor;

import javax.swing.table.AbstractTableModel;

import org.workcraft.exceptions.NotSupportedException;

import checkers.nullness.quals.Nullable;

import pcollections.PVector;

@SuppressWarnings("serial")
public class PropertyEditorTableModel extends AbstractTableModel {
	static final String [] columnNames = { "property", "value" };
	@Nullable PVector<EditableProperty> properties = null;

	@Override
	public String getColumnName(int col) {
		return columnNames[col];
	}

	public void setProperties(PVector<EditableProperty> properties) {
		this.properties = properties;

		fireTableDataChanged();
		fireTableStructureChanged();
	}

	public void clearObject() {
		setProperties(null);
	}

	public int getColumnCount() {
		if (properties == null)
			return 0;
		else
			return 2;
	}

	public int getRowCount() {
		if (properties == null)
			return 0;
		else
			return properties.size();
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		if (col == 0)
			return false;
		else
			return true;// TODO: isWritable?
	}

	public Object getValueAt(int row, int col) {
		if (col ==0 )
			return properties.get(row).name();
		else 
			return null;
	}

	@Override
	public void setValueAt(Object value, int row, int col) {
		if(value == null)
			return;
		else
			throw new NotSupportedException();
	}
}
