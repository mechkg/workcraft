package org.workcraft.gui.docking.tab

import java.awt.event.MouseListener
import javax.swing.JLabel
import javax.swing.BorderFactory
import java.awt.Color
import javax.swing.SwingConstants
import java.awt.Font
import java.awt.event.MouseEvent

class TabButton (label: String, toolTipText: String, onClick: () => Unit) extends JLabel(label) with MouseListener {
  val mouseOutBorder = BorderFactory.createEmptyBorder(1, 1, 1, 1)
  val mouseOverBorder = BorderFactory.createLineBorder(Color.GRAY)
  
  setVerticalAlignment(SwingConstants.CENTER)
  setFont(getFont().deriveFont(Font.PLAIN))
  setOpaque(false)
  setForeground(Color.GRAY)
  addMouseListener(this)
  setToolTipText(toolTipText)
  
  def mouseClicked(e:MouseEvent) = onClick()

  def mouseEntered(e: MouseEvent) = setForeground(new Color(200,0,0))

  def mouseExited(e: MouseEvent) = setForeground(Color.GRAY)

  def mousePressed(e: MouseEvent) = {}
  
  def mouseReleased(e: MouseEvent) = {}	
}