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
import javax.swing.JOptionPane

class PropertyEditorTable extends JTable with PropertyEditor {
  val model: PropertyEditorTableModel = new PropertyEditorTableModel
  setModel(model)
  setTableHeader(null)
  setFocusable(false)

  override def getCellEditor(row: Int, col: Int): TableCellEditor =
    if (col == 0)
      super.getCellEditor(row, col)
    else
      cellEditors.get(row)

  override def getCellRenderer(row: Int, col: Int): TableCellRenderer =
    if (col == 0)
      super.getCellRenderer(row, col)
    else
      new TableCellRenderer {
        override def getTableCellRendererComponent(table: JTable, value: Object, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int) = {
          cellRenderers.get(row)
        }
      }

  def clearObject(): Unit = {
    model.clearObject()
  }

  def setObject(o: List[EditableProperty]): Unit = {
    model.setProperties(Some(o))
    cellRenderers = new ArrayList[Component]()
    cellEditors = new ArrayList[AbstractTableCellEditor]()
    for (val p <- o) {
      cellEditors.add(new AbstractTableCellEditor {
        val editor = p.createEditor(ioPure.pure(stopCellEditing), ioPure.pure{cancelCellEditing})
        
        override def getCellEditorValue = null
        override def stopCellEditing: Boolean = {
          editor.commit.unsafePerformIO match {
            case Some(e) => JOptionPane.showMessageDialog(PropertyEditorTable.this.getParent(), e, "Error", JOptionPane.ERROR_MESSAGE); false
            case None => super.stopCellEditing()
          }
        }
        override def getTableCellEditorComponent(table: JTable, value: Object, isSelected: Boolean, row: Int, column: Int): Component =
          editor.getComponent
      })

      cellRenderers.add(p.renderer(false, false))
    }
  }

  var cellRenderers: ArrayList[Component] = null
  var cellEditors: ArrayList[AbstractTableCellEditor] = null

  abstract class AbstractTableCellEditor extends AbstractCellEditor with TableCellEditor {
  }
}