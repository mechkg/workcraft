package org.workcraft.gui.logger
import javax.swing.table.AbstractTableModel
import java.util.Date
import org.workcraft.logging.MessageClass
import java.text.SimpleDateFormat

case class LogMessage (val date: Date, val message: String, val cls: MessageClass)

class LoggerTableModel extends AbstractTableModel {
  val columnNames = Array("Time", "Class", "Message")
  val dateFormatString = "HH:mm:ss"
  val dateFormat = new SimpleDateFormat(dateFormatString)
  
  val log_ = new scala.collection.mutable.ListBuffer[LogMessage]
  
  def log (date: Date, message: String, cls: MessageClass) = {
    log_.append(LogMessage(new Date(), message, cls))
    fireTableDataChanged
  }
  
  override def getColumnName(col:Int) = columnNames(col)
  
  val getColumnCount = 3
  
  def getRowCount = log_.length
  
  override def isCellEditable(row:Int, col:Int) = false

  override def getValueAt(row: Int, col: Int) = col match {
    case 0 => dateFormat.format(log_(row).date)
    case 1 => log_(row).cls
    case 2 => log_(row).message
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