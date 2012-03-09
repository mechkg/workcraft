package org.workcraft.gui.propertyeditor.colour

import java.awt.Color
import java.awt.Component
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JButton
import javax.swing.JColorChooser
import javax.swing.JDialog
import org.workcraft.gui.propertyeditor.GenericCellEditor
import org.workcraft.gui.propertyeditor.GenericEditorProvider
import org.workcraft.util.Action


class ColorCellEditor extends GenericEditorProvider[Color] {
  override  def createEditor(initialValue:Color, accept:Action, cancel:Action):GenericCellEditor[Color] = {
    return new GenericCellEditorImplementation(initialValue, accept, cancel)
  }
  class GenericCellEditorImplementation with GenericCellEditor[Color] {
    /*
    def this(initialValue:Color, accept:Action, cancel:Action) = {
      button = new JButton()
      button.setBorderPainted(false)
      button.setFocusable(false)
      button.setBackground(initialValue)
      colorChooser = new JColorChooser()
      colorChooser.setColor(initialValue)
      val dialog:JDialog = JColorChooser.createDialog(null, "Pick a Color", true, colorChooser, new ActionListener(), new ActionListener())
      button.addActionListener(new ActionListener())
    }
    */
    override  def component():Component = {
      return button
    }

    override  def getValue():Color = {
      return colorChooser.getColor()
    }
    val button:JButton = null
    val colorChooser:JColorChooser = null
  }
}
