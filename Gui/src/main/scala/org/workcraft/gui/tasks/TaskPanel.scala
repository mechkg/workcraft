package org.workcraft.gui.tasks

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
import org.workcraft.scala.Expressions._
import org.workcraft.scala.effects.IO
import org.workcraft.scala.effects.IO._
import scalaz.Scalaz._

class TaskPanel (progress: Expression[Option[Double]], description: Expression[String], cancelAction: IO[Unit]) extends JPanel {
  private val sz = Array(Array(TableLayoutConstants.FILL, 80.0, 100.0), Array(20.0, 20.0, 20.0))
  private val lineBorder = BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), BorderFactory.createEmptyBorder(3, 3, 3, 3))
  
  private val refresh = (progress: Option[Double], description: String) => ioPure.pure {
    label.setText(description)
    
    progress match { 
      case Some(p) => progressBar.setIndeterminate(false); progressBar.setValue((1000.0 * p).toInt)
      case None => progressBar.setIndeterminate(true)
      }
  } 
  
  

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

  def cancel = {
    btnCancel.setEnabled(false)
    btnCancel.setText("Cancelling...")
    
    cancelAction.unsafePerformIO
  }
  
  private val refresher = swingAutoRefresh(progress <|*|> description, refresh.tupled)
}