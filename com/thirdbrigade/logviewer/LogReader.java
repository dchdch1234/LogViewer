import java.awt.Cursor;
import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;



public class LogReader {
	private static final long serialVersionUID = 1L;
	
	private static final String[] DPI_COLUMN_NAME = { "Time",
			"Time (microseconds)", "Computer", "Reason", "Tag(s)",
			"Application Type", "Action", "Rank", "Direction", "Flow",
			"Interface", "Protocol", "Flags", "Source IP", "Source MAC",
			"Source Port", "Destination IP", "Destination MAC",
			"Destination Port", "Packet Size", "Note", "Repeat Count",
			"End Time", "Position In Buffer", "Position In Stream",
			"Data Flags", "Data Index", "Data" };
	
	private static final String[] FW_COLUMN_NAME = { "Time",
			"Time (microseconds)", "Computer", "Reason", "Tag(s)", "Action",
			"Rank", "Direction", "Interface", "Frame Type", "Protocol",
			"Flags", "Source IP", "Source MAC", "Source Port",
			"Destination IP", "Destination MAC", "Destination Port",
			"Packet Size", "Repeat Count", "End Time", "Flow", "Status",
			"Note", "Data Flags", "Data Index", "Data" };
	
	private static final String[] HOST_COLUMN_NAME = {"Time","Level","Event ID","Event","Tag(s)","Target","Action By","Description"};

	public void load(File f, int logtype) {
		LogViewer.logViewer.setCursor(Cursor.getPredefinedCursor(3));
		try {

			BufferedReader in = new BufferedReader(new InputStreamReader(
					new FileInputStream(f), getFileEncoding(f)));

			ArrayList<String> list = readLine(in);
			if (list == null) {
				return;
			}
			LogViewer.logModel = new LogModel(LogViewer.logTable);
			LogViewer.logModel.setColumnCount(list.size());
			String[] COLUMN_NAME = {};
			switch (logtype){
				case 0:
					COLUMN_NAME=DPI_COLUMN_NAME;
					break;
				case 1:
					COLUMN_NAME=FW_COLUMN_NAME;
					break;
				case 2:
					COLUMN_NAME=HOST_COLUMN_NAME;
					break;
				default:					
					break;
			}
			
			for (int col = 0; col < list.size(); col++) {
				// Use EN column name
				if (col < COLUMN_NAME.length)
					LogViewer.logModel.setColumnName(COLUMN_NAME[col], col);
				else
					LogViewer.logModel.setColumnName((String) list.get(col),
							col);
			}

			
			int row = 0;
			for (;;) {
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
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		LogViewer.logViewer.setCursor(Cursor.getDefaultCursor());
	}

	private ArrayList<String> readLine(BufferedReader rdr) throws Exception {
		String str;
		ArrayList<String> list = new ArrayList<String>();

		if ((str = rdr.readLine()) == null) {
			return null;
		}
		// System.out.println(str);
		String[] array = str.split(",");

		for (int i = 0; i < array.length; i++) {
			String s = array[i];
			if (s.startsWith("\"")) {
				String tmp = s;
				while ((!s.endsWith("\"")) && (i < array.length - 1)) {
					s = array[(++i)];
					tmp = tmp + ", " + s;
				}
				s = tmp;
				if (i == array.length - 1) { // Not found " in the current line
					while (!s.endsWith("\"")) {
						s += ("\n" + rdr.readLine());
					}
				}
				if (s.length() > 1) {
					s = s.substring(1);
				}
				if (s.endsWith("\"")) {
					s = s.substring(0, s.length() - 1);
				}
			}
			list.add(s.trim());
		}

		System.out.println(list.toString());
		return list;

	}

	private String getFileEncoding(File f) {
		
		String[][] colname = {
	            {"UTF-8","Time"},
	            {"Shift_JIS", "時刻"},
	            {"GB2312","时间"}
	        };
		
		String encode = "UTF-8";

		for ( int i=0; i<3; i++) {
			try {
				FileInputStream fin = new FileInputStream(f);
				InputStreamReader in = new InputStreamReader(fin, colname[i][0]);
				BufferedReader br = new BufferedReader(in);
				
				if ( colname[i][1].equals(readLine(br).get(0))) {
					//Match found. Set encode and end loop.
					encode = colname[i][0];
					i = 3;
				}
				
				fin.close();
				in.close();
				br.close();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return encode;

	}
}
