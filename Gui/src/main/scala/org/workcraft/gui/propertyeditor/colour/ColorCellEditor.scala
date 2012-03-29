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
import org.workcraft.scala.effects.IO


object ColorCellEditor extends GenericEditorProvider[Color] {
  override  def createEditor(initialValue:Color, accept:IO[Unit], cancel: IO[Unit]):GenericCellEditor[Color] = {
    return new GenericCellEditorImplementation(initialValue, accept, cancel)
  }
  class GenericCellEditorImplementation(initialValue:Color, accept:IO[Unit], cancel: IO[Unit]) extends GenericCellEditor[Color] {
    
    val button : JButton = new JButton()
    button.setBorderPainted(false)
    button.setFocusable(false)
    button.setBackground(initialValue)
    val colorChooser = new JColorChooser()
    colorChooser.setColor(initialValue)
    val dialog:JDialog = JColorChooser.createDialog(null, "Pick a Color", true, colorChooser, new ActionListener(){
      override def actionPerformed(e : ActionEvent) = accept.unsafePerformIO
    }, new ActionListener(){override def actionPerformed(e : ActionEvent) = cancel.unsafePerformIO})
    button.addActionListener(new ActionListener(){
//					The user has clicked the cell, so
//					bring up the dialog.
      override def actionPerformed(e : ActionEvent) = dialog.setVisible(true)
    })
    
    override def component:Component = button
    override def getValue:Color = colorChooser.getColor
  }
}
