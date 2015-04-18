

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.UIManager;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.filechooser.FileFilter;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;

public class LogViewer
  extends JFrame
  implements ActionListener, MenuListener, DropTargetListener
{
  private static final long serialVersionUID = 1L;
  private static final int FRAME_WIDTH = 1000;
  private static final int FRAME_HEIGHT = 800;
  public static LogViewer logViewer = null;
  public static LogFilter logFilter = null;
  public static LogTable logTable = null;
  public static LogReader logReader = null;
  public static LogModel logModel = null;
  public static LogDetail logDetail = null;
  public static File logFile = null;
  private JPanel mainPanel = null;
  private JScrollPane scrollPane = null;
  private JMenuBar menuBar = null;
  private JMenu fileMenu = null;
  private JMenuItem fileOpenItem = null;
  private JMenuItem fileExitItem = null;
  private JMenu viewMenu = null;
  private JMenuItem viewShowItem = null;
  private JFileChooser chooser = null;
  
  private class LogFileFilter
    extends FileFilter
  {
    private LogFileFilter() {}
    
    public boolean accept(File f)
    {
      if (f.isDirectory()) {
        return true;
      }
      return f.getName().toLowerCase().endsWith(".csv");
    }
    
    public String getDescription()
    {
      return "Third Brigade Log File (.csv)";
    }
  }
  
  public LogViewer()
  {
    logViewer = this;
    try
    {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      UIManager.put("FileChooser.readOnly", new Boolean(true));
    }
    catch (Exception ex) {}
    ImageIcon icon = new ImageIcon(ClassLoader.getSystemResource("icons/document_text.png"));
    setIconImage(icon.getImage());
    JFrame.setDefaultLookAndFeelDecorated(true);
    setTitle("Third Brigade Log Viewer");
    
    this.mainPanel = new JPanel();
    this.mainPanel.setLayout(new BorderLayout());
    this.mainPanel.setBorder(null);
    setContentPane(this.mainPanel);
    
    logFilter = new LogFilter();
    this.mainPanel.add(logFilter, "North");
    logFilter.setVisible(false);
    
    logTable = new LogTable(null);
    this.scrollPane = new JScrollPane(logTable);
    this.scrollPane.getViewport().setBackground(Color.WHITE);
    this.mainPanel.add(this.scrollPane, "Center");
    
    logReader = new LogReader();
    
    setJMenuBar(this.menuBar = new JMenuBar());
    this.menuBar.add(this.fileMenu = new JMenu("File"));
    this.fileMenu.add(this.fileOpenItem = new JMenuItem("Open..."));
    this.fileOpenItem.setActionCommand("open");
    this.fileOpenItem.addActionListener(this);
    this.fileMenu.addSeparator();
    this.fileMenu.add(this.fileExitItem = new JMenuItem("Exit"));
    this.fileExitItem.setActionCommand("exit");
    this.fileExitItem.addActionListener(this);
    this.menuBar.add(this.viewMenu = new JMenu("View"));
    this.viewMenu.addMenuListener(this);
    this.viewMenu.add(this.viewShowItem = new JMenuItem("Details"));
    this.viewShowItem.setActionCommand("details");
    this.viewShowItem.addActionListener(this);
    
    this.chooser = new JFileChooser();
    this.chooser.setDialogTitle("Select Log File");
    this.chooser.setFileFilter(new LogFileFilter());
    this.chooser.setCurrentDirectory(new File("."));      
    
    setSize(1000, 800);
    validate();
    
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = getSize();
    if (frameSize.height > screenSize.height) {
      frameSize.height = screenSize.height;
    }
    if (frameSize.width > screenSize.width) {
      frameSize.width = screenSize.width;
    }
    setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
    
    new DropTarget(this,DnDConstants.ACTION_COPY_OR_MOVE, this, true);  
  }
  
  public void actionPerformed(ActionEvent e)
  {
    String cmd = e.getActionCommand();
    if (cmd == null) {
      return;
    }
    if (cmd.equals("open"))
    {
      if (this.chooser.showOpenDialog(this) == 0)
      {
        openFile(chooser.getSelectedFile());
      }
    }
    else if (cmd.equals("details")) {
      logDetail.showPacket();
    } else if (cmd.equals("exit")) {
      System.exit(0);
    }
  }
  

  
  private void openFile(File f){
	  
	  int logtype = -1;  //-1:unknown, 0:dpi, 1:firewall
	  String filename = f.getName().toLowerCase();
	  if (filename.contains("dpievent")) {
		  logtype = 0;
	  } 
	  else if ( filename.contains("firewallevent") ){
		  logtype = 1;
	  }
	  
	  else if ( filename.contains("hostevent") ){
		  logtype = 2;
	  }
	  
	  if ( logtype == -1 ) {
		  Object[] options = {"DPI event","Firewall event", "Host event", "Other"};
		  logtype = JOptionPane.showOptionDialog(this, "Please choose log type", "Log type", 0 ,JOptionPane.PLAIN_MESSAGE, null, options, options[0] );
	  }
		  
	  
      logFile = f;
      logReader.load(logFile, logtype);
      logFilter.setVisible(true);
      logFilter.init();
      logFilter.reset();
      logModel.applyFilter();
      logDetail = new LogDetail(logtype);
      logDetail.setVisible(false);
  }

  
  public void menuDeselected(MenuEvent e) {}
  
  public void menuCanceled(MenuEvent e) {}
  
  public void menuSelected(MenuEvent e)
  {
    int row = logTable.getSelectedRow();
    this.viewShowItem.setEnabled(row != -1);
  }
  
  protected void processWindowEvent(WindowEvent e)
  {
    if (e.getID() == 201) {
      System.exit(0);
    }
  }
  
  
  public static void main(String[] args)
  {
    LogViewer viewer = new LogViewer();
    viewer.setVisible(true);
  }

@Override
public void dragEnter(DropTargetDragEvent dtde) {
	// TODO Auto-generated method stub
	 
}

@Override
public void dragExit(DropTargetEvent dte) {
	// TODO Auto-generated method stub
	
}

@Override
public void dragOver(DropTargetDragEvent dtde) {
	// TODO Auto-generated method stub
	
}

@Override
public void drop(DropTargetDropEvent dtde) {
	// TODO Auto-generated method stub
		dtde.acceptDrop(DnDConstants.ACTION_REFERENCE);
		if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
			try {
				Transferable tr = dtde.getTransferable();
				Object obj = tr.getTransferData(DataFlavor.javaFileListFlavor);
				@SuppressWarnings("unchecked")
				List<File> files = (List<File>) obj;
				openFile(files.get(0));

			} catch (UnsupportedFlavorException ex) {

			} catch (IOException ex) {

			}
		}

  
}

@Override
public void dropActionChanged(DropTargetDragEvent dtde) {
	// TODO Auto-generated method stub
	
}
}
