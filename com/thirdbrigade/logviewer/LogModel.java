
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import javax.swing.table.AbstractTableModel;

public class LogModel
  extends AbstractTableModel
  implements Comparator<Object[]>
{
  private static final long serialVersionUID = 1L;
  private static SimpleDateFormat longFormat = new SimpleDateFormat("MMMMM dd, yyyy HH:mm:ss");
  private static SimpleDateFormat shortFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
  private static SimpleDateFormat chineseFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  private LogTable table = null;
  private String[] titles = null;
  private int[] widths = null;
  private ArrayList<Object[]> values = null;
  private ArrayList<Object[]> master = null;
  
  public LogModel(LogTable table)
  {
    this.table = table;
    this.titles = new String[0];
    this.widths = new int[0];
    this.values = new ArrayList();
    this.master = new ArrayList();
  }
  
  public int getRowCount()
  {
    return this.values.size();
  }
  
  public void setColumnCount(int count)
  {
    this.titles = new String[count];
    this.widths = new int[count];
    for (int i = 0; i < count; i++)
    {
      this.titles[i] = "";
      this.widths[i] = 100;
    }
  }
  
  public int getColumnCount()
  {
    return this.titles.length;
  }
  
  public int getColumnWidth(int col)
  {
    if (col >= this.titles.length) {
      return 0;
    }
    return this.widths[col];
  }
  
  public void setValueAt(Object obj, int row, int col)
  {
    if (col >= this.titles.length) {
      return;
    }
    if (row > this.values.size()) {
      return;
    }
    if ((row == this.values.size()) && (col == 0))
    {
      String[] array = new String[this.titles.length];
      for (int i = 0; i < this.titles.length; i++) {
        array[i] = "";
      }
      this.master.add(array);
      this.values.add(array);
    }
    Object[] array = (Object[])this.values.get(row);
    array[col] = obj;
    
    Graphics g = this.table.getGraphics();
    if (g == null) {
      return;
    }
    FontMetrics metrics = g.getFontMetrics();
    int w = metrics.stringWidth(obj.toString()) + 20;
    if (w > this.widths[col]) {
      this.widths[col] = w;
    }
  }
  
  public Object getValueAt(int row, int col)
  {
    if (row >= this.values.size()) {
      return null;
    }
    if (col >= this.titles.length) {
      return null;
    }
    Object[] array = (Object[])this.values.get(row);
    return array[col];
  }
  
  public void setColumnName(String name, int col)
  {
    if (col >= this.titles.length) {
      return;
    }
    this.titles[col] = name;
  }
  
  public String getColumnName(int col)
  {
    if (col >= this.titles.length) {
      return "";
    }
    return this.titles[col];
  }
  
  public int findColumn(String name)
  {
    for (int col = 0; col < this.titles.length; col++) {
      if (this.titles[col].equalsIgnoreCase(name)) {
        return col;
      }
    }
    return -1;
  }
  
  public Object[] getRow(int row)
  {
    if (row >= this.values.size()) {
      return null;
    }
    return (Object[])this.values.get(row);
  }
  
  public void sort()
  {
    Collections.sort(this.master, this);
    Collections.sort(this.values, this);
  }
  
  public void applyFilter()
  {
    this.values = new ArrayList();
    for (int row = 0; row < this.master.size(); row++)
    {
      Object[] log = (Object[])this.master.get(row);
      if (LogViewer.logFilter.shouldDisplay(log)) {
        this.values.add(log);
      }
    }
    LogViewer.logFilter.setStatus(LogViewer.logFile.getName() + ": showing " + this.values.size() + " of " + this.master.size() + " logs");
    LogViewer.logTable.adjustSize();
    fireTableDataChanged();
  }
  
  public int compare(Object[] o1, Object[] o2)
  {
    try
    {
      Date d1 = null;
      String s1 = (String)o1[0];
      if (s1.indexOf('/') > 0) {
        d1 = shortFormat.parse(s1);
      } else if (s1.indexOf('-') > 0) {
          d1 = chineseFormat.parse(s1);
      } else {
        d1 = longFormat.parse(s1);
      }
      Date d2 = null;
      String s2 = (String)o2[0];
      if (s2.indexOf('/') > 0) {
        d2 = shortFormat.parse(s2);
      } else if (s2.indexOf('-') > 0) {
          d2 = chineseFormat.parse(s2);
      } else {
        d2 = longFormat.parse(s2);
      }
      return d2.compareTo(d1);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    return 0;
  }
}
