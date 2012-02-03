package org.workcraft.gui.logger
import javax.swing.table.AbstractTableModel
import java.util.Date

sealed trait MessageClass 

object MessageClass {
  case object Info extends MessageClass
  case object Debug extends MessageClass
  case object Warning extends MessageClass
  case object Error extends MessageClass
}

case class LogMessage (val date: Date, val cls: MessageClass, val message: String)

class LoggerTableModel extends AbstractTableModel {
  val columnNames = Array("Time", "Class", "Message")
  
  val log = new scala.collection.mutable.ArrayBuffer[LogMessage]
  
  log.append(LogMessage(new Date(), MessageClass.Info, "Hi"))
  
  override def getColumnName(col:Int) = columnNames(col)
  
  def getColumnCount = 3
  
  def getRowCount = log.length
  
  override def isCellEditable(row:Int, col:Int) = false

  override def getValueAt(row: Int, col: Int) =  col match {
    case 1 => log(row).date
    case 2 => log(row).cls
    case 3 => log(row).message
  }
}

/*
SuppressWarnings("serial")
public class PropertyEditorTableModel extends AbstractTableModel {
	static final String [] columnNames = { "property", "value" };
	@Nullable PVector<EditableProperty> properties = null;

	@Override
	public String getColumnName(int col) {
		return columnNames[col];
	}

	public void setProperties(PVector<EditableProperty> properties) {
		this.properties = properties;

		fireTableDataChanged();
		fireTableStructureChanged();
	}

	public void clearObject() {
		setProperties(null);
	}

	public int getColumnCount() {
		if (properties == null)
			return 0;
		else
			return 2;
	}

	public int getRowCount() {
		if (properties == null)
			return 0;
		else
			return properties.size();
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		if (col == 0)
			return false;
		else
			return true;// TODO: isWritable?
	}

	public Object getValueAt(int row, int col) {
		if (col ==0 )
			return properties.get(row).name();
		else 
			return null;
	}

	@Override
	public void setValueAt(Object value, int row, int col) {
		if(value == null)
			return;
		else
			throw new NotSupportedException();
	}
}
*/