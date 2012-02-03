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

class LoggerWindow extends JPanel with Logger[IO] {
  def info(message: String) = {}.pure
  def debug(message: String) = {}.pure
  def warning(message: String) = {}.pure
  def error(message: String) = {}.pure
  
  
  
  val table = new JTable()
  
  val tcm = new DefaultTableColumnModel()
  
  tcm.addColumn()
  
  
  table.setColumnModel(tcm)
  
  val scrollPane = new JScrollPane(table)
  table.setFillsViewportHeight(true)
}