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

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.workcraft.Framework;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.Expressions;
import org.workcraft.dependencymanager.advanced.core.GlobalCache;
import org.workcraft.util.Action;

import pcollections.PVector;

@SuppressWarnings("serial")
public class PropertyEditorTable extends JTable implements PropertyEditor {
	HashMap<Class<?>, EditableProperty> propertyClasses;
	ArrayList<ReactiveComponent> cellRenderers;
	ArrayList<Expression<? extends EditorProvider>> cellEditors;

	PropertyEditorTableModel model;

	public PropertyEditorTable(Framework framework) {
		super();

		model = new PropertyEditorTableModel();
		setModel(model);
		
		setTableHeader(null);
		setFocusable(false);
	}

	abstract class AbstractTableCellEditor extends AbstractCellEditor implements TableCellEditor {
	}
	
	@Override
	public TableCellEditor getCellEditor(int row, int col) {
		if (col == 0)
			return super.getCellEditor(row, col);
		else {
			final EditorProvider editorProvider = GlobalCache.eval(cellEditors.get(row));
			return new AbstractTableCellEditor(){
				SimpleCellEditor editor = editorProvider.getEditor(new Action() {
					@Override
					public void run() {
						cancelCellEditing();
					}
				});
				
				@Override
				public Object getCellEditorValue() {
					return null;
				}
				
				@Override
				public boolean stopCellEditing() {
					editor.commit();
					//TODO: think about commit failing
					return super.stopCellEditing();
				}
				
				@Override
				public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
					return editor.getComponent();
				}
			};
		}
	}


	@Override
	public TableCellRenderer getCellRenderer(int row, int col) {
		if (col == 0)
			return super.getCellRenderer(row, col);
		else
			return new TableCellRenderer(){
				@Override
				public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
					GlobalCache.eval(cellRenderers.get(row).updateExpression());
					return cellRenderers.get(row).component();
				}
			};
	}

	public void clearObject() {
		model.clearObject();
	}

	public void setObject(PVector<EditableProperty> o) {
		model.setProperties(o);

		cellRenderers = new ArrayList<ReactiveComponent>();
		cellEditors = new ArrayList<Expression<? extends EditorProvider>>();
		

		for (EditableProperty p : o) {
			cellEditors.add(p.editorMaker());
			cellRenderers.add(p.renderer(Expressions.constant(false), Expressions.constant(false)));
		}
	}
}
