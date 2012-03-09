package org.workcraft.gui.propertyeditor

import javax.swing.table.AbstractTableModel
import org.workcraft.exceptions.NotSupportedException
import checkers.nullness.quals.Nullable
import pcollections.PVector


class PropertyEditorTableModel extends AbstractTableModel {
  override  def getColumnName(col:Int):String = {
    return columnNames(col)
  }

  def setProperties(properties:PVector[EditableProperty]):Unit = {
    this.properties = properties
    fireTableDataChanged()
    fireTableStructureChanged()
  }

  def clearObject():Unit = {
    setProperties(null)
  }

  def getColumnCount():Int = {
    if (properties == null) 
      return 0
    else 
      return 2


  }

  def getRowCount():Int = {
    if (properties == null) 
      return 0
    else 
      return properties.size()


  }

  override  def isCellEditable(row:Int, col:Int):Boolean = {
    if (col == 0) 
      return false
    else 
      return true


  }

  def getValueAt(row:Int, col:Int):Object = {
    if (col == 0) 
      return properties.get(row).name()
    else 
      return null


  }

  override  def setValueAt(value:Object, row:Int, col:Int):Unit = {
    if (value == null) 
      return
    else 
      throw new NotSupportedException()


  }
  @Nullable
   var properties:PVector[EditableProperty] = null
}

object PropertyEditorTableModel {
  val columnNames:Array[String] = ("property", "value")
}
