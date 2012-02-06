package org.workcraft.gui.logger
import org.workcraft.logging.Logger
import scalaz.effects.IO
import scalaz.effects.IO._
import scalaz.Scalaz._
import javax.swing.JPanel
import javax.swing.JTable
import javax.swing.JScrollPane
import javax.swing.table.TableColumnModel
import javax.swing.table.DefaultTableColumnModel
import org.workcraft.logging.MessageClass
import java.awt.BorderLayout

class LoggerWindow extends JPanel with Logger[IO] {
  def log(message: String, klass: MessageClass) = {}.pure

  val table = new JTable()
  
  
  table.setModel(new LoggerTableModel())
  table.getColumnModel().setColumnSelectionAllowed(false)
  table.getColumnModel().getColumn(0).setMaxWidth(100)
  table.getColumnModel().getColumn(1).setMaxWidth(100)
  table.setShowVerticalLines(false)
  table.setShowHorizontalLines(false)
    
  val scrollPane = new JScrollPane(table)
  table.setFillsViewportHeight(true)
  
  setLayout(new BorderLayout())
  add(scrollPane, BorderLayout.CENTER)
}