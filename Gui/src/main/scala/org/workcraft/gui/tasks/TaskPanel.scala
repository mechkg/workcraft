package org.workcraft.tasks.gui

import info.clearthought.layout.TableLayout
import info.clearthought.layout.TableLayoutConstants

import java.awt.Color
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JProgressBar
import javax.swing.border.Border

class TaskPanel extends JPanel {
  private val sz = Array(Array(TableLayoutConstants.FILL, 80.0, 100.0), Array(20.0, 20.0, 20.0))
  private val lineBorder = BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), BorderFactory.createEmptyBorder(3, 3, 3, 3))

  setBorder(lineBorder)

  private val lt = new TableLayout(sz)
  lt.setHGap(3)
  lt.setVGap(3)
  setLayout(lt)

  private val label = new JLabel("Initialising...")
  label.setMinimumSize(new Dimension(100, 20))
  label.setPreferredSize(new Dimension(300, 20))

  private val progressBar = new JProgressBar()
  progressBar.setIndeterminate(true)
  progressBar.setMinimum(0)
  progressBar.setMaximum(1000)

  progressBar.setMinimumSize(new Dimension(100, 20))
  progressBar.setPreferredSize(new Dimension(300, 20))

  private val btnCancel = new JButton("Cancel")
  btnCancel.addActionListener(new ActionListener() {
    override def actionPerformed(e: ActionEvent) = cancel
  })

  add(label, "0,0,2,0")
  add(progressBar, "0,1,2,1")
  add(btnCancel, "2,2")

  def progressUpdate(progress: Double) = {
    progressBar.setIndeterminate(false)
    progressBar.setValue((progress * 1000).toInt)
  }

  @volatile
  var _cancelRequested = false
  
  def cancelRequested = _cancelRequested

  def cancel = {
    _cancelRequested = true;
    btnCancel.setEnabled(false);
    btnCancel.setText("Cancelling...");
  }
}