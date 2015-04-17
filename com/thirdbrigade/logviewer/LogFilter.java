
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JSpinner.DateEditor;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SpringLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

public class LogFilter
  extends JPanel
  implements ActionListener, ItemListener, DocumentListener, ChangeListener
{
  private static final long serialVersionUID = 1L;
  private static ImageIcon arrowUp = new ImageIcon(ClassLoader.getSystemResource("icons/nav_up_blue.png"));
  private static ImageIcon arrowDown = new ImageIcon(ClassLoader.getSystemResource("icons/nav_down_blue.png"));
  private static SimpleDateFormat longFormat = new SimpleDateFormat("MMMMM dd, yyyy HH:mm:ss");
  private static SimpleDateFormat chineseFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  private static SimpleDateFormat shortFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
  public static final int COMPARE_EQUALS = 0;
  public static final int COMPARE_NOT_EQUALS = 1;
  public static final int COMPARE_CONTAINS = 2;
  public static final int COMPARE_NOT_CONTAINS = 3;
  public static final int COMPARE_BEGINS = 4;
  public static final int COMPARE_NOT_BEGINS = 5;
  public static final int COMPARE_ENDS = 6;
  public static final int COMPARE_NOT_ENDS = 7;
  public static final int COMPARE_GREATER_THAN = 8;
  public static final int COMPARE_GREATER_EQUAL = 9;
  public static final int COMPARE_LESS_THAN = 10;
  public static final int COMPARE_LESS_EQUAL = 11;
  private static String[] functionNames = { "equals", "does not equal", "contains", "does not contain", "begins with", "does not begin with", "ends with", "does not end with", "greater than", "greater than or equal to", "less than", "less than or equal to" };
  private String[] columnNames = null;
  private LogFilter logFilter = null;
  private JPanel headPanel = null;
  private JLabel headLabel = null;
  private JButton sizeButton = null;
  private JPanel bodyPanel = null;
  private JLabel filterLabel = null;
  private JPanel filterPanel = null;
  private DatePanel datePanel = null;
  private FilterPanel filter1Panel = null;
  private BooleanPanel booleanPanel = null;
  private FilterPanel filter2Panel = null;
  private ButtonPanel buttonPanel = null;
  private Date fileMinDate = new Date();
  private Date fileMaxDate = new Date();
  private boolean expanded = false;
  private JSpinner minDateField = null;
  private JSpinner maxDateField = null;
  private SpinnerDateModel minDateModel = null;
  private SpinnerDateModel maxDateModel = null;
  private JButton applyButton = new JButton("Apply");
  private JButton resetButton = new JButton("Reset");
  private JRadioButton andButton = new JRadioButton("and");
  private JRadioButton orButton = new JRadioButton("or");
  private JCheckBox caseSensitive = new JCheckBox("Case sensitive");
  
  private class DatePanel
    extends JPanel
  {
    private static final long serialVersionUID = 1L;
    private JLabel minLabel = new JLabel("Date range from:");
    private JLabel maxLabel = new JLabel(" to: ");
    
    public DatePanel()
    {
      setLayout(new FlowLayout());
      setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
      
      LogFilter.this.minDateModel = new SpinnerDateModel(LogFilter.this.fileMinDate, LogFilter.this.fileMinDate, LogFilter.this.fileMaxDate, 13);
      LogFilter.this.maxDateModel = new SpinnerDateModel(LogFilter.this.fileMaxDate, LogFilter.this.fileMinDate, LogFilter.this.fileMaxDate, 13);
      
      LogFilter.this.minDateField = new JSpinner(LogFilter.this.minDateModel);
      LogFilter.this.minDateField.setFont(new JLabel().getFont());
      JSpinner.DateEditor minEditor = new JSpinner.DateEditor(LogFilter.this.minDateField, "MMMMM dd, yyyy HH:mm:ss");
      minEditor.getTextField().setBorder(BorderFactory.createEmptyBorder(2, 3, 2, 3));
      LogFilter.this.minDateField.setEditor(minEditor);
      
      LogFilter.this.maxDateField = new JSpinner(LogFilter.this.maxDateModel);
      LogFilter.this.maxDateField.setFont(new JLabel().getFont());
      JSpinner.DateEditor maxEditor = new JSpinner.DateEditor(LogFilter.this.maxDateField, "MMMMM dd, yyyy HH:mm:ss");
      maxEditor.getTextField().setBorder(BorderFactory.createEmptyBorder(2, 3, 2, 3));
      LogFilter.this.maxDateField.setEditor(maxEditor);
      
      LogFilter.this.minDateField.setPreferredSize(new Dimension(175, 21));
      LogFilter.this.maxDateField.setPreferredSize(new Dimension(175, 21));
      
      add(this.minLabel);
      add(LogFilter.this.minDateField);
      add(this.maxLabel);
      add(LogFilter.this.maxDateField);
      
      reset();
      
      LogFilter.this.minDateModel.addChangeListener(LogFilter.this.logFilter);
      LogFilter.this.maxDateModel.addChangeListener(LogFilter.this.logFilter);
    }
    
    public void reset()
    {
      LogFilter.this.minDateModel.setStart(LogFilter.this.fileMinDate);
      LogFilter.this.minDateModel.setEnd(LogFilter.this.fileMaxDate);
      LogFilter.this.minDateModel.setValue(LogFilter.this.fileMinDate);
      
      LogFilter.this.maxDateModel.setStart(LogFilter.this.fileMinDate);
      LogFilter.this.maxDateModel.setEnd(LogFilter.this.fileMaxDate);
      LogFilter.this.maxDateModel.setValue(LogFilter.this.fileMaxDate);
    }
    
    public void updateRange(Object source)
    {
      if (source == LogFilter.this.minDateModel) {
        LogFilter.this.maxDateModel.setStart((Date)LogFilter.this.minDateModel.getValue());
      } else if (source == LogFilter.this.maxDateModel) {
        LogFilter.this.minDateModel.setEnd((Date)LogFilter.this.maxDateModel.getValue());
      }
    }
    
    public boolean isValidFilter()
    {
      return true;
    }
    
    public boolean shouldDisplay(Object[] log)
    {
      try
      {
        Date min = (Date)LogFilter.this.minDateModel.getValue();
        Date max = (Date)LogFilter.this.maxDateModel.getValue();
        Date date = null;
        String s = (String)log[0];
        if (s.indexOf('/') > 0) {
          date = LogFilter.shortFormat.parse(s);
        } else if (s.indexOf('-') > 0) {
        	date = LogFilter.chineseFormat.parse(s);
        } else {
          date = LogFilter.longFormat.parse(s);
        }
        if (date.compareTo(min) < 0) {
          return false;
        }
        if (date.compareTo(max) > 0) {
          return false;
        }
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
      return true;
    }
  }
  
  private class FilterPanel
    extends JPanel
  {
    private static final long serialVersionUID = 1L;
    private JComboBox columnCombo = new JComboBox();
    private JComboBox functionCombo = new JComboBox(LogFilter.functionNames);
    private JTextField valueField = new JTextField();
    
    public FilterPanel()
    {
      setLayout(new GridLayout(1, 3, 10, 10));
      setMaximumSize(new Dimension(10000, 21));
      setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 4));
      
      this.columnCombo.addItemListener(LogFilter.this.logFilter);
      this.functionCombo.addItemListener(LogFilter.this.logFilter);
      this.valueField.getDocument().addDocumentListener(LogFilter.this.logFilter);
      
      add(this.columnCombo);
      add(this.functionCombo);
      add(this.valueField);
    }
    
    public void reset()
    {
      this.columnCombo.removeAllItems();
      this.columnCombo.addItem("");
      for (int i = 0; i < LogFilter.this.columnNames.length; i++) {
        this.columnCombo.addItem(LogFilter.this.columnNames[i]);
      }
      this.columnCombo.setSelectedIndex(0);
      this.functionCombo.setSelectedIndex(0);
      this.valueField.setText("");
      enableActions();
    }
    
    public void enableActions()
    {
      this.functionCombo.setEnabled(isColumnSelected());
      this.valueField.setEnabled(isColumnSelected());
    }
    
    public boolean isColumnSelected()
    {
      return this.columnCombo.getSelectedIndex() > 0;
    }
    
    public boolean isValidFilter()
    {
      if (!isColumnSelected()) {
        return true;
      }
      if (this.valueField.getText().trim().equals("")) {
        return false;
      }
      return true;
    }
    
    public boolean shouldDisplay(Object[] log)
    {
      int col = this.columnCombo.getSelectedIndex() - 1;
      if (col < 0) {
        return true;
      }
      String value = (String)log[col];
      String target = this.valueField.getText().trim();
      if (!LogFilter.this.caseSensitive.isSelected())
      {
        value = value.toLowerCase();
        target = target.toLowerCase();
      }
      switch (this.functionCombo.getSelectedIndex())
      {
      case 0: 
        return value.equals(target);
      case 1: 
        return !value.equals(target);
      case 2: 
        return value.indexOf(target) >= 0;
      case 3: 
        return value.indexOf(target) < 0;
      case 4: 
        return value.startsWith(target);
      case 5: 
        return !value.startsWith(target);
      case 6: 
        return value.endsWith(target);
      case 7: 
        return !value.endsWith(target);
      case 8: 
        return compareNumeric(value, target) > 0;
      case 9: 
        return compareNumeric(value, target) >= 0;
      case 10: 
        return compareNumeric(value, target) < 0;
      case 11: 
        return compareNumeric(value, target) <= 0;
      }
      return true;
    }
    
    private int compareNumeric(String a, String b)
    {
      if ((a == null) && (b == null)) {
        return 0;
      }
      if (a == null) {
        return -1;
      }
      if (b == null) {
        return 1;
      }
      boolean numeric = true;
      for (int i = 0; i < a.length(); i++) {
        if (!Character.isDigit(a.charAt(i))) {
          numeric = false;
        }
      }
      for (int i = 0; i < b.length(); i++) {
        if (!Character.isDigit(b.charAt(i))) {
          numeric = false;
        }
      }
      if (numeric) {
        try
        {
          long v1 = Long.parseLong(a);
          long v2 = Long.parseLong(b);
          if (v1 < v2) {
            return -1;
          }
          if (v1 > v2) {
            return 1;
          }
          return 0;
        }
        catch (Exception ex) {}
      }
      return a.compareTo(b);
    }
  }
  
  private class BooleanPanel
    extends JPanel
  {
    private static final long serialVersionUID = 1L;
    private ButtonGroup buttonGroup = new ButtonGroup();
    
    public BooleanPanel()
    {
      setLayout(new GridLayout(1, 5, 10, 10));
      setMaximumSize(new Dimension(10000, 21));
      setBorder(null);
      
      add(LogFilter.this.andButton);
      add(LogFilter.this.orButton);
      add(new JLabel(""));
      add(new JLabel(""));
      add(new JLabel(""));
      
      this.buttonGroup.add(LogFilter.this.andButton);
      this.buttonGroup.add(LogFilter.this.orButton);
      LogFilter.this.andButton.setSelected(true);
      
      LogFilter.this.andButton.addActionListener(LogFilter.this.logFilter);
      LogFilter.this.orButton.addActionListener(LogFilter.this.logFilter);
    }
    
    public void reset()
    {
      LogFilter.this.andButton.setSelected(true);
      enableActions();
    }
    
    public void enableActions()
    {
      LogFilter.this.andButton.setEnabled((LogFilter.this.filter1Panel.isColumnSelected()) && (LogFilter.this.filter2Panel.isColumnSelected()));
      LogFilter.this.orButton.setEnabled((LogFilter.this.filter1Panel.isColumnSelected()) && (LogFilter.this.filter2Panel.isColumnSelected()));
    }
    
    public boolean shouldDisplay(Object[] log)
    {
      boolean flag1 = true;
      boolean flag2 = true;
      if (LogFilter.this.filter1Panel.isValidFilter()) {
        flag1 = LogFilter.this.filter1Panel.shouldDisplay(log);
      }
      if (LogFilter.this.filter2Panel.isValidFilter()) {
        flag2 = LogFilter.this.filter2Panel.shouldDisplay(log);
      }
      if (LogFilter.this.andButton.isSelected()) {
        return (flag1) && (flag2);
      }
      return (flag1) || (flag2);
    }
  }
  
  private class ButtonPanel
    extends JPanel
  {
    private static final long serialVersionUID = 1L;
    private SpringLayout layout = new SpringLayout();
    
    public ButtonPanel()
    {
      setPreferredSize(new Dimension(100, 30));
      setLayout(this.layout);
      setBorder(null);
      
      LogFilter.this.applyButton.addActionListener(LogFilter.this.logFilter);
      LogFilter.this.resetButton.addActionListener(LogFilter.this.logFilter);
      LogFilter.this.caseSensitive.addActionListener(LogFilter.this.logFilter);
      
      this.layout.putConstraint("North", LogFilter.this.resetButton, 0, "North", this);
      this.layout.putConstraint("East", LogFilter.this.resetButton, -10, "East", this);
      add(LogFilter.this.resetButton);
      
      this.layout.putConstraint("North", LogFilter.this.applyButton, 0, "North", this);
      this.layout.putConstraint("East", LogFilter.this.applyButton, -10, "West", LogFilter.this.resetButton);
      add(LogFilter.this.applyButton);
      
      this.layout.putConstraint("North", LogFilter.this.caseSensitive, 0, "North", this);
      this.layout.putConstraint("West", LogFilter.this.caseSensitive, 10, "West", this);
      add(LogFilter.this.caseSensitive);
      
      LogFilter.this.applyButton.setEnabled(false);
    }
    
    public void enableActions()
    {
      boolean enabled = true;
      if (!LogFilter.this.filter1Panel.isValidFilter()) {
        enabled = false;
      }
      if (!LogFilter.this.filter2Panel.isValidFilter()) {
        enabled = false;
      }
      if (!LogFilter.this.datePanel.isValidFilter()) {
        enabled = false;
      }
      LogFilter.this.applyButton.setEnabled(enabled);
    }
  }
  
  public LogFilter()
  {
    this.logFilter = this;
    
    longFormat.setLenient(false);
    shortFormat.setLenient(false);
    
    setLayout(new BorderLayout(0, 0));
    setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    
    this.headPanel = new JPanel();
    this.headPanel.setLayout(new BorderLayout());
    this.headPanel.setBorder(BorderFactory.createEtchedBorder());
    add(this.headPanel, "North");
    
    this.headLabel = new JLabel(" ");
    this.headLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    this.headPanel.add(this.headLabel, "West");
    
    this.sizeButton = new JButton("Filter", arrowDown);
    this.sizeButton.setHorizontalTextPosition(2);
    this.sizeButton.setBorderPainted(false);
    this.sizeButton.setContentAreaFilled(false);
    this.sizeButton.setFocusPainted(false);
    this.sizeButton.addActionListener(this);
    this.headPanel.add(this.sizeButton, "East");
    
    this.bodyPanel = new JPanel();
    this.bodyPanel.setLayout(new BorderLayout());
    this.bodyPanel.setBorder(BorderFactory.createEtchedBorder());
    add(this.bodyPanel, "Center");
    
    this.filterLabel = new JLabel("Logs must match the following criteria:");
    this.filterLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
    this.bodyPanel.add(this.filterLabel, "North");
    
    this.filterPanel = new JPanel();
    this.filterPanel.setLayout(new BoxLayout(this.filterPanel, 3));
    this.filterPanel.setMaximumSize(new Dimension(10000, 70));
    this.filterPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
    this.bodyPanel.add(this.filterPanel, "West");
    
    this.filterPanel.add(this.datePanel = new DatePanel());
    this.filterPanel.add(this.filter1Panel = new FilterPanel());
    this.filterPanel.add(this.booleanPanel = new BooleanPanel());
    this.filterPanel.add(this.filter2Panel = new FilterPanel());
    
    this.buttonPanel = new ButtonPanel();
    this.bodyPanel.add(this.buttonPanel, "South");
    
    this.bodyPanel.setVisible(false);
  }
  
  public void actionPerformed(ActionEvent e)
  {
    if (e.getSource() == this.sizeButton)
    {
      this.expanded = (!this.expanded);
      if (this.expanded)
      {
        this.sizeButton.setIcon(arrowUp);
        this.bodyPanel.setVisible(true);
      }
      else
      {
        this.sizeButton.setIcon(arrowDown);
        this.bodyPanel.setVisible(false);
      }
      revalidate();
      getParent().doLayout();
    }
    else if (e.getSource() == this.resetButton)
    {
      reset();
    }
    else if (e.getSource() == this.applyButton)
    {
      LogViewer.logModel.applyFilter();
      this.applyButton.setEnabled(false);
    }
    else
    {
      enableActions();
    }
  }
  
  public void init()
  {
    this.fileMinDate = new Date();
    this.fileMaxDate = new Date();
    if (LogViewer.logModel.getRowCount() > 0)
    {
      String s1 = (String)LogViewer.logModel.getValueAt(0, 0);
      if (s1 != null) {
        if (s1.indexOf('/') > 0) {
          try
          {
            this.fileMaxDate = shortFormat.parse(s1);
          }
          catch (Exception ex)
          {
            ex.printStackTrace();
          }
        } else if ( s1.indexOf('-') > 0) {
        	try {
				this.fileMinDate = chineseFormat.parse(s1);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        } else {
          try
          {
            this.fileMaxDate = longFormat.parse(s1);
          }
          catch (Exception ex)
          {
            ex.printStackTrace();
          }
        }
      }
      String s2 = (String)LogViewer.logModel.getValueAt(LogViewer.logModel.getRowCount() - 1, 0);
      if (s2 != null) {
        if (s2.indexOf('/') > 0) {
          try
          {
            this.fileMinDate = shortFormat.parse(s2);
          }
          catch (Exception ex)
          {
            ex.printStackTrace();
          }
        } else if ( s2.indexOf('-') > 0) {
        	try {
				this.fileMinDate = chineseFormat.parse(s2);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        else {
          try
          {
            this.fileMinDate = longFormat.parse(s2);
          }
          catch (Exception ex)
          {
            ex.printStackTrace();
          }
        }
      }
      this.columnNames = new String[LogViewer.logModel.getColumnCount()];
      for (int col = 0; col < this.columnNames.length; col++) {
        this.columnNames[col] = LogViewer.logModel.getColumnName(col);
      }
    }
  }
  
  public void reset()
  {
    this.datePanel.reset();
    this.filter1Panel.reset();
    this.filter2Panel.reset();
    this.booleanPanel.reset();
    LogViewer.logModel.applyFilter();
    this.applyButton.setEnabled(false);
  }
  
  public void enableActions()
  {
    this.filter1Panel.enableActions();
    this.filter2Panel.enableActions();
    this.booleanPanel.enableActions();
    this.buttonPanel.enableActions();
  }
  
  public void insertUpdate(DocumentEvent e)
  {
    enableActions();
  }
  
  public void removeUpdate(DocumentEvent e)
  {
    enableActions();
  }
  
  public void changedUpdate(DocumentEvent e)
  {
    enableActions();
  }
  
  public void itemStateChanged(ItemEvent e)
  {
    enableActions();
  }
  
  public void stateChanged(ChangeEvent e)
  {
    this.datePanel.updateRange(e.getSource());
    enableActions();
  }
  
  public boolean shouldDisplay(Object[] log)
  {
    if (!this.datePanel.shouldDisplay(log)) {
      return false;
    }
    if (!this.booleanPanel.shouldDisplay(log)) {
      return false;
    }
    return true;
  }
  
  public void setStatus(String s)
  {
    this.headLabel.setText(s);
  }
}
