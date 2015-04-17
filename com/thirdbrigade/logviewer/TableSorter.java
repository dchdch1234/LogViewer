

import java.awt.Component;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

public class TableSorter
  extends AbstractTableModel
{
  private static final long serialVersionUID = 1L;
  protected AbstractTableModel tableModel;
  public static final int DESCENDING = -1;
  public static final int NOT_SORTED = 0;
  public static final int ASCENDING = 1;
  private static Directive EMPTY_DIRECTIVE = new Directive(-1, 0);
  public static final Comparator<String> LEXICAL_COMPARATOR = new Comparator<String>()
  {

    public int compare(String o1, String o2)
    {
      return o1.compareToIgnoreCase(o2);
    }


  };
  public static final Comparator<String> NUMERIC_COMPARATOR = new Comparator<String>()
  {
    public int compare(String o1, String o2)
    {
      try
      {
        Double d1 = Double.valueOf(Double.parseDouble(o1.replaceAll(",", "")));
        Double d2 = Double.valueOf(Double.parseDouble(o2.replaceAll(",", "")));
        return d1.compareTo(d2);
      }
      catch (Exception ex) {}
      return o1.compareToIgnoreCase(o2);
    }

  };
  private Row[] viewToModel;
  private int[] modelToView;
  private JTableHeader tableHeader;
  private TableModelListener tableModelListener = new TableModelHandler();
  private List<Directive> sortingColumns = new ArrayList();
  private static ImageIcon arrowUp = new ImageIcon(ClassLoader.getSystemResource("icons/navigate_open.png"));
  private static ImageIcon arrowDown = new ImageIcon(ClassLoader.getSystemResource("icons/navigate_close.png"));
  
  public TableSorter() {}
  
  public TableSorter(AbstractTableModel tableModel)
  {
    setTableModel(tableModel);
  }
  
  private void clearSortingState()
  {
    this.viewToModel = null;
    this.modelToView = null;
  }
  
  public void setTableModel(AbstractTableModel tableModel)
  {
    if (this.tableModel != null) {
      this.tableModel.removeTableModelListener(this.tableModelListener);
    }
    this.tableModel = tableModel;
    if (this.tableModel != null) {
      this.tableModel.addTableModelListener(this.tableModelListener);
    }
    clearSortingState();
    fireTableStructureChanged();
  }
  
  public void setTableHeader(JTableHeader tableHeader)
  {
    if (this.tableHeader != null)
    {
      TableCellRenderer defaultRenderer = this.tableHeader.getDefaultRenderer();
      if ((defaultRenderer instanceof SortableHeaderRenderer)) {
        this.tableHeader.setDefaultRenderer(((SortableHeaderRenderer)defaultRenderer).tableCellRenderer);
      }
    }
    this.tableHeader = tableHeader;
    if (this.tableHeader != null) {
      this.tableHeader.setDefaultRenderer(new SortableHeaderRenderer(this.tableHeader.getDefaultRenderer()));
    }
  }
  
  public boolean isSorting()
  {
    return this.sortingColumns.size() != 0;
  }
  
  private Directive getDirective(int column)
  {
    for (int i = 0; i < this.sortingColumns.size(); i++)
    {
      Directive directive = (Directive)this.sortingColumns.get(i);
      if (directive.column == column) {
        return directive;
      }
    }
    return EMPTY_DIRECTIVE;
  }
  
  public int getSortingStatus(int column)
  {
    return getDirective(column).direction;
  }
  
  private void sortingStatusChanged()
  {
    clearSortingState();
    fireTableDataChanged();
    if (this.tableHeader != null) {
      this.tableHeader.repaint();
    }
  }
  
  public void setSortingStatus(int column, int status)
  {
    Directive directive = getDirective(column);
    if (directive != EMPTY_DIRECTIVE) {
      this.sortingColumns.remove(directive);
    }
    if (status != 0) {
      this.sortingColumns.add(new Directive(column, status));
    }
    sortingStatusChanged();
  }
  
  protected Icon getHeaderRendererIcon(int column, int size)
  {
    Directive directive = getDirective(column);
    if (directive.direction == 1) {
      return arrowUp;
    }
    if (directive.direction == -1) {
      return arrowDown;
    }
    return null;
  }
  
  public void cancelSorting()
  {
    this.sortingColumns.clear();
    sortingStatusChanged();
  }
  
  private Row[] getViewToModel()
  {
    if (this.viewToModel == null)
    {
      int tableModelRowCount = this.tableModel.getRowCount();
      this.viewToModel = new Row[tableModelRowCount];
      for (int row = 0; row < tableModelRowCount; row++) {
        this.viewToModel[row] = new Row(row);
      }
      if (isSorting()) {
        Arrays.sort(this.viewToModel);
      }
    }
    return this.viewToModel;
  }
  
  public int modelIndex(int viewIndex)
  {
    return getViewToModel()[viewIndex].modelIndex;
  }
  
  private int[] getModelToView()
  {
    if (this.modelToView == null)
    {
      int n = getViewToModel().length;
      this.modelToView = new int[n];
      for (int i = 0; i < n; i++) {
        this.modelToView[modelIndex(i)] = i;
      }
    }
    return this.modelToView;
  }
  
  public int getRowCount()
  {
    return this.tableModel == null ? 0 : this.tableModel.getRowCount();
  }
  
  public int getColumnCount()
  {
    return this.tableModel == null ? 0 : this.tableModel.getColumnCount();
  }
  
  public String getColumnName(int column)
  {
    return this.tableModel.getColumnName(column);
  }
  
  public Class<?> getColumnClass(int column)
  {
    return this.tableModel.getColumnClass(column);
  }
  
  public boolean isCellEditable(int row, int column)
  {
    return this.tableModel.isCellEditable(modelIndex(row), column);
  }
  
  public Object getValueAt(int row, int column)
  {
    return this.tableModel.getValueAt(modelIndex(row), column);
  }
  
  public void setValueAt(Object aValue, int row, int column)
  {
    this.tableModel.setValueAt(aValue, modelIndex(row), column);
  }
  
  private class Row
    implements Comparable
  {
    private int modelIndex;
    
    public Row(int index)
    {
      this.modelIndex = index;
    }
    
    public int compareTo(Object o)
    {
      int row1 = this.modelIndex;
      int row2 = ((Row)o).modelIndex;
      for (Iterator it = TableSorter.this.sortingColumns.iterator(); it.hasNext();)
      {
        TableSorter.Directive directive = (TableSorter.Directive)it.next();
        int column = directive.column;
        Object o1 = TableSorter.this.tableModel.getValueAt(row1, column);
        Object o2 = TableSorter.this.tableModel.getValueAt(row2, column);
        
        int comparison = 0;
        if ((o1 == null) && (o2 == null))
        {
          comparison = 0;
        }
        else if (o1 == null)
        {
          comparison = -1;
        }
        else if (o2 == null)
        {
          comparison = 1;
        }
        else
        {
          String s1 = o1.toString();
          String s2 = o2.toString();
          if ((s1.length() == 0) && (s2.length() == 0)) {
            comparison = 0;
          } else if (s1.length() == 0) {
            comparison = -1;
          } else if (s2.length() == 0) {
            comparison = 1;
          } else if (Character.isDigit(s1.charAt(0))) {
            comparison = TableSorter.NUMERIC_COMPARATOR.compare(s1, s2);
          } else {
            comparison = TableSorter.LEXICAL_COMPARATOR.compare(s1, s2);
          }
        }
        if (comparison != 0) {
          return directive.direction == -1 ? -comparison : comparison;
        }
      }
      return 0;
    }
  }
  
  private class TableModelHandler
    implements TableModelListener
  {
    private TableModelHandler() {}
    
    public void tableChanged(TableModelEvent e)
    {
      if (!TableSorter.this.isSorting())
      {
        TableSorter.this.clearSortingState();
        TableSorter.this.fireTableChanged(e);
        return;
      }
      if (e.getFirstRow() == -1)
      {
        TableSorter.this.cancelSorting();
        TableSorter.this.fireTableChanged(e);
        return;
      }
      int column = e.getColumn();
      if ((e.getFirstRow() == e.getLastRow()) && (column != -1) && (TableSorter.this.getSortingStatus(column) == 0) && (TableSorter.this.modelToView != null))
      {
        int viewIndex = TableSorter.this.getModelToView()[e.getFirstRow()];
        TableSorter.this.fireTableChanged(new TableModelEvent(TableSorter.this, viewIndex, viewIndex, column, e.getType()));
        return;
      }
      TableSorter.this.clearSortingState();
      TableSorter.this.fireTableDataChanged();
    }
  }
  
  private class SortableHeaderRenderer
    extends JLabel
    implements TableCellRenderer
  {
    private static final long serialVersionUID = 42L;
    private TableCellRenderer tableCellRenderer;
    
    public SortableHeaderRenderer(TableCellRenderer tableCellRenderer)
    {
      this.tableCellRenderer = tableCellRenderer;
    }
    
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
    {
      Component c = this.tableCellRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
      if ((c instanceof JLabel))
      {
        JLabel l = (JLabel)c;
        l.setHorizontalTextPosition(2);
        int modelColumn = table.convertColumnIndexToModel(column);
        l.setIcon(TableSorter.this.getHeaderRendererIcon(modelColumn, l.getFont().getSize()));
      }
      return c;
    }
  }
  
  private static class Directive
  {
    private int column;
    private int direction;
    
    public Directive(int column, int direction)
    {
      this.column = column;
      this.direction = direction;
    }
  }
}
