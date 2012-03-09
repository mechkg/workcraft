package org.workcraft.gui.propertyeditor

import java.awt.Component
import java.util.ArrayList
import java.util.HashMap
import javax.swing.AbstractCellEditor
import javax.swing.JTable
import javax.swing.table.TableCellEditor
import javax.swing.table.TableCellRenderer
import org.workcraft.Framework
import org.workcraft.dependencymanager.advanced.core.Expression
import org.workcraft.dependencymanager.advanced.core.Expressions
import org.workcraft.dependencymanager.advanced.core.GlobalCache
import org.workcraft.util.Action
import pcollections.PVector


class PropertyEditorTable extends JTable with PropertyEditor {
  /*
  def this(framework:Framework) = {
    this()
    model = new PropertyEditorTableModel()
    setModel(model)
    setTableHeader(null)
    setFocusable(false)
  }
  */
  override  def getCellEditor(row:Int, col:Int):TableCellEditor = {
    if (col == 0) 
      return super.getCellEditor(row, col)
    else {
      val editorProvider:EditorProvider = GlobalCache.eval(cellEditors.get(row))
      return new AbstractTableCellEditor()
    }

  }

  override  def getCellRenderer(row:Int, col:Int):TableCellRenderer = {
    if (col == 0) 
      return super.getCellRenderer(row, col)
    else 
      return new TableCellRenderer()


  }

  def clearObject():Unit = {
    model.clearObject()
  }

  def setObject(o:PVector[EditableProperty]):Unit = {
    model.setProperties(o)
    cellRenderers = new ArrayList[ReactiveComponent]()
    cellEditors = new ArrayList[Expression[]]()
    for (val p <- o) {
      cellEditors.add(p.editorMaker())
      cellRenderers.add(p.renderer(Expressions.constant(false), Expressions.constant(false)))
    }
  }
  var propertyClasses:HashMap[Class[]EditableProperty] = null
  var cellRenderers:ArrayList[ReactiveComponent] = null
  var cellEditors:ArrayList[Expression[]] = null
  var model:PropertyEditorTableModel = null
  class AbstractTableCellEditor extends AbstractCellEditor with TableCellEditor {
  }
}
