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
import java.util.Date

class LoggerWindow extends JPanel with Logger[IO] {
  def log(message: String, klass: MessageClass) = {
    ltm.log.append(LogMessage(new Date(), message, klass)) 
    ltm.fireTableRowsInserted(ltm.log.length,ltm.log.length)
    ltm.fireTableDataChanged()
  }.pure

  val table = new JTable() 
  
  val ltm = new LoggerTableModel()
  
  table.setModel(ltm)
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