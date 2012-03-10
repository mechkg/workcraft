package org.workcraft.gui.propertyeditor.choice

import java.awt.Component
import java.awt.event.ItemEvent
import java.awt.event.ItemListener
import javax.swing.JComboBox
import org.workcraft.gui.propertyeditor.GenericCellEditor
import org.workcraft.util.Action
import org.workcraft.util.Pair
import pcollections.PVector
import org.workcraft.scala.effects.IO


class ChoiceCellEditor[T](initialValue:T, choice:List[(String, T)], accept:IO[Unit]) extends GenericCellEditor[T] {
  
   val comboBox = new JComboBox()
   comboBox.setEditable(false)
   comboBox.setFocusable(false)
   for (val p <- choice) {
      var comboBoxItem:ComboboxItemWrapper = new ComboboxItemWrapper(p)
      comboBox.addItem(comboBoxItem)
      if (p._2 == initialValue) 
        comboBox.setSelectedItem(comboBoxItem)
   }
   comboBox.addItemListener(new ItemListener{
     override def itemStateChanged(event : ItemEvent) = accept.unsafePerformIO
   })
  
  override  def component:Component = {
    return comboBox
  }

   override  def getValue:T = {
    return comboBox.getSelectedItem().asInstanceOf[ComboboxItemWrapper].getValue.asInstanceOf[T] 
  }
}
