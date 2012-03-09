package org.workcraft.gui.propertyeditor.choice

import java.awt.Component
import java.awt.event.ItemEvent
import java.awt.event.ItemListener
import javax.swing.JComboBox
import org.workcraft.gui.propertyeditor.GenericCellEditor
import org.workcraft.util.Action
import org.workcraft.util.Pair
import pcollections.PVector


class ChoiceCellEditor[T ] extends GenericCellEditor[T] {
  /*
  def this(initialValue:T, choice:PVector[Pair[StringT]], accept:Action) = {
    comboBox = new JComboBox()
    comboBox.setEditable(false)
    comboBox.setFocusable(false)
    for (val p <- choice) {
      var comboBoxItem:ComboboxItemWrapper = new ComboboxItemWrapper(p)
      comboBox.addItem(comboBoxItem)
      if (p.getSecond().equals(initialValue)) 
        comboBox.setSelectedItem(comboBoxItem)


    }
    comboBox.addItemListener(new ItemListener())
  }
  */
  override  def component():Component = {
    return comboBox
  }

  @SuppressWarningsC"unchecked")
   override  def getValue():T = {
    return comboBox.getSelectedItem().asInstanceOf[ComboboxItemWrapper] .getValue().asInstanceOf[T] 
  }
  private var comboBox:JComboBox = null
}
