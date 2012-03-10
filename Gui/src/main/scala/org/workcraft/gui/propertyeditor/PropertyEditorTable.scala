package org.workcraft.gui.propertyeditor

import java.awt.Component
import java.util.ArrayList
import java.util.HashMap
import javax.swing.AbstractCellEditor
import javax.swing.JTable
import javax.swing.table.TableCellEditor
import javax.swing.table.TableCellRenderer
import org.workcraft.scala.Expressions._
import org.workcraft.scala.effects.IO
import org.workcraft.scala.effects.IO._


class PropertyEditorTable extends JTable with PropertyEditor {
  val model:PropertyEditorTableModel = new PropertyEditorTableModel
  {
    setModel(model)
    setTableHeader(null)
    setFocusable(false)
  }
  
  override  def getCellEditor(row:Int, col:Int):TableCellEditor =
    if (col == 0) 
      return super.getCellEditor(row, col)
    else {
      val editorProvider:EditorProvider = cellEditors.get(row).eval.unsafePerformIO
      return new AbstractTableCellEditor {
			    val editor = editorProvider.getEditor(ioPure.pure(cancelCellEditing))
				override def getCellEditorValue = null
				override def stopCellEditing : Boolean = {
			    	editor.commit
			    	super.stopCellEditing
			    }
				override def getTableCellEditorComponent(table : JTable, value : Object, isSelected : Boolean, row : Int, column : Int) : Component =
				    editor.getComponent
    	}
    }

  override  def getCellRenderer(row:Int, col:Int):TableCellRenderer =
    if (col == 0) 
      super.getCellRenderer(row, col)
    else 
      new TableCellRenderer{
		    override def getTableCellRendererComponent(table : JTable, value : Object, isSelected : Boolean, hasFocus : Boolean, row : Int, column : Int) = {
			cellRenderers.get(row).updateExpression.eval.unsafePerformIO
			cellRenderers.get(row).component
		    }
    }

  def clearObject():Unit = {
    model.clearObject()
  }

  def setObject(o:List[EditableProperty]):Unit = {
    model.setProperties(Some(o))
    cellRenderers = new ArrayList[ReactiveComponent]()
    cellEditors = new ArrayList[Expression[EditorProvider]]
    for (val p <- o) {
      cellEditors.add(p.editorMaker)
      cellRenderers.add(p.renderer(constant(false), constant(false)))
    }
  }
  var cellRenderers:ArrayList[ReactiveComponent] = null
  var cellEditors:ArrayList[Expression[EditorProvider]] = null
  abstract class AbstractTableCellEditor extends AbstractCellEditor with TableCellEditor {
  }
}
