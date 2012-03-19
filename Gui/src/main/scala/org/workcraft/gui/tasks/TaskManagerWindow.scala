package org.workcraft.gui.tasks

import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Rectangle
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.lang.reflect.InvocationTargetException
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.ScrollPaneConstants
import javax.swing.Scrollable
import javax.swing.SwingUtilities
import javax.swing.border.Border
import java.awt.FlowLayout
import org.workcraft.scala.Expressions.Expression

class TaskManagerWindow(activeTasks: Expression[List[Int]]) extends JPanel {
  setLayout(new BorderLayout())

  val scroll = new JScrollPane()
  scroll.setViewportView(content)
  scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER)
  add(scroll, BorderLayout.CENTER)

  val content = new ScrollPaneWidthTrackingPanel()
  content.setLayout(new FlowLayout(FlowLayout.LEADING))

  setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), BorderFactory.createEmptyBorder(2, 2, 2, 2)))

  class ScrollPaneWidthTrackingPanel extends JPanel with Scrollable {

    def getPreferredScrollableViewportSize(): Dimension = {
      return getPreferredSize()
    }

    def getScrollableBlockIncrement(visibleRect: Rectangle, orientation: Int, direction: Int): Int = {
      return Math.max(visibleRect.height * 9 / 10, 1)
    }

    def getScrollableTracksViewportHeight(): Boolean = {
      return false
    }

    def getScrollableTracksViewportWidth(): Boolean = {
      return true
    }

    def getScrollableUnitIncrement(visibleRect: Rectangle, orientation: Int, direction: Int): Int = {
      return Math.max(visibleRect.height / 10, 1)
    }
  }
}