package org.workcraft.gui.propertyeditor

import javax.swing.table.AbstractTableModel
import org.workcraft.exceptions.NotSupportedException
import pcollections.PVector


class PropertyEditorTableModel extends AbstractTableModel {
  import PropertyEditorTableModel._
  override  def getColumnName(col:Int):String = {
    return columnNames(col)
  }

  def setProperties(_properties:Option[List[EditableProperty]]):Unit = {
    this.properties = _properties
    fireTableDataChanged()
    fireTableStructureChanged()
  }

  def clearObject():Unit = {
    setProperties(None)
  }

  def getColumnCount():Int = {
    properties match{
      case None => 0
      case Some(p) => 2
    }
  }

  def getRowCount:Int = {
    properties match {
      case None => 0
      case Some(p) => p.size
    }
  }

  override  def isCellEditable(row:Int, col:Int):Boolean = {
    if (col == 0) 
      return false
    else 
      return true


  }

  def getValueAt(row:Int, col:Int):Object = {
    if (col == 0)
      return properties.get.apply(row).name
    else 
      return null


  }

  override  def setValueAt(value:Object, row:Int, col:Int):Unit = {
    if (value == null) 
      return
    else 
      throw new NotSupportedException()


  }
  var properties:Option[List[EditableProperty]] = None
}

object PropertyEditorTableModel {
  val columnNames:Array[String] = Array("property", "value")
}
