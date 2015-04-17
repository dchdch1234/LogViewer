
import java.awt.Cursor;
import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

public class LogReader
{
  private static final long serialVersionUID = 1L;
  private static final String[] COLUMN_NAME= {"Time","Time (microseconds)","Computer","Reason","Tag(s)","Application Type","Action","Rank","Direction","Flow","Interface","Protocol","Flags","Source IP","Source MAC","Source Port","Destination IP","Destination MAC","Destination Port","Packet Size","Note","Repeat Count","End Time","Position In Buffer","Position In Stream","Data Flags","Data Index","Data"};
  public void load(File f)
  {
    LogViewer.logViewer.setCursor(Cursor.getPredefinedCursor(3));
    try
    {
      BufferedReader in = new BufferedReader(new FileReader(f));
      

      ArrayList<String> list = readLine(in);
      if (list == null) {
        return;
      }
      LogViewer.logModel = new LogModel(LogViewer.logTable);
      LogViewer.logModel.setColumnCount(list.size());
      
      for (int col = 0; col < list.size(); col++) {
    	  // Use EN column name
    	  if ( col < COLUMN_NAME.length )
    		  LogViewer.logModel.setColumnName(COLUMN_NAME[col], col);
    	  else
    		  LogViewer.logModel.setColumnName((String)list.get(col), col);
      }
      
           
      int row = 0;
      for (;;)
      {
        list = readLine(in);
        if (list == null) {
          break;
        }
        for (int col = 0; col < list.size(); col++) {
          LogViewer.logModel.setValueAt(list.get(col), row, col);
        }
        row++;
      }
      LogViewer.logModel.sort();
      

      LogViewer.logTable.setModel(LogViewer.logModel);
      

      LogViewer.logTable.adjustSize();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    LogViewer.logViewer.setCursor(Cursor.getDefaultCursor());
  }
  
  private ArrayList<String> readLine(BufferedReader rdr)
    throws Exception
  {
    String str;
    do
    {
      str = rdr.readLine();
      if (str == null) {
        return null;
      }
    } while ((str.length() < 1) || 
      (str.indexOf(",") < 0));
    //System.out.println(str);
    String[] array = str.split(",");
    
    ArrayList<String> list = new ArrayList();
    for (int i = 0; i < array.length; i++)
    {
      String s = array[i];
      if (s.startsWith("\""))
      {
        String tmp = s;
        while ((!s.endsWith("\"")) && (i < array.length))
        {
          s = array[(++i)];
          tmp = tmp + ", " + s;
        }
        s = tmp;
        if (s.length() > 1) {
          s = s.substring(1);
        }
        if (s.endsWith("\"")) {
          s = s.substring(0, s.length() - 1);
        }
      }
      list.add(s.trim());
    }
    return list;

  }
}
