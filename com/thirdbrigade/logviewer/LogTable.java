
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class LogTable extends JTable implements TableColumnModelListener,
		MouseListener, ListSelectionListener {
	private static final long serialVersionUID = 1L;
	private TableSorter sorter = null;
	private JPopupMenu popupMenu = null;
	private JMenuItem detailsItem = null;

	public LogTable(AbstractTableModel model) {
		super(model);
		getTableHeader().setReorderingAllowed(false);
		getTableHeader().setResizingAllowed(true);
		getTableHeader().addMouseListener(this);
		setAutoResizeMode(0);
		setColumnSelectionAllowed(false);
		setRowSelectionAllowed(true);
		addMouseListener(this);
		setSelectionMode(0);
		getColumnModel().addColumnModelListener(this);

		this.popupMenu = new JPopupMenu();
		this.popupMenu.add(this.detailsItem = new JMenuItem("Show Details"));
		this.detailsItem.setActionCommand("details");
		this.detailsItem.addActionListener(LogViewer.logViewer);

		setBackground(Color.WHITE);
		setOpaque(true);
		if (model != null) {
			setModel(model);
		}
	}

	public void setModel(AbstractTableModel model) {
		this.sorter = new TableSorter(model);
		super.setModel(this.sorter);
		this.sorter.setTableHeader(getTableHeader());
	}

	public void columnMarginChanged(ChangeEvent e) {
		TableColumn col = getTableHeader().getResizingColumn();
		if ((col == null) && (e != null)) {
			return;
		}
		TableColumnModel tcm = getColumnModel();
		Dimension d = getPreferredSize();
		d.width = tcm.getTotalColumnWidth();
		setPreferredSize(d);
		revalidate();
		repaint(1L);
	}

	public void adjustSize() {
		int width = 0;
		for (int col = 0; col < LogViewer.logModel.getColumnCount(); col++) {
			width += LogViewer.logModel.getColumnWidth(col);
		}
		LogViewer.logTable.setPreferredSize(new Dimension(width,
				LogViewer.logModel.getRowCount()
						* LogViewer.logTable.getRowHeight()));
		LogViewer.logTable.revalidate();

		TableColumnModel tcm = LogViewer.logTable.getColumnModel();
		for (int col = 0; col < LogViewer.logModel.getColumnCount(); col++) {
			tcm.getColumn(col).setPreferredWidth(
					LogViewer.logModel.getColumnWidth(col));
		}
	}

	public void mouseClicked(MouseEvent e) {
		if (e.getButton() == 1) {
			if ((e.getSource() instanceof JTableHeader)) {
				int x = e.getX();
				int col = this.columnModel.getColumnIndexAtX(x);
				if (col == -1) {
					return;
				}
				Rectangle r = this.tableHeader.getTable().getCellRect(0, col,
						false);
				if (e.getClickCount() > 1) {
					if ((x < r.x + 3) && (col > 0)) {
						col--;
					}
					int width = LogViewer.logModel.getColumnWidth(col);
					this.columnModel.getColumn(col).setWidth(width);
					this.columnModel.getColumn(col).setPreferredWidth(width);

					columnMarginChanged(null);
				} else {
					if (e.getX() < r.x + 3) {
						return;
					}
					if (e.getX() > r.x + r.width - 3) {
						return;
					}
					int status = this.sorter.getSortingStatus(col);
					if (!e.isControlDown()) {
						this.sorter.cancelSorting();
					}
					status += (e.isShiftDown() ? -1 : 1);
					status = (status + 4) % 3 - 1;
					this.sorter.setSortingStatus(col, status);
				}
			} else if ((e.getClickCount() > 1)
					&& (!LogViewer.logDetail.isVisible())) {
				LogViewer.logDetail.showPacket();
			}
		} else {
			int row = rowAtPoint(e.getPoint());
			if (row < 0) {
				return;
			}
			getSelectionModel().setSelectionInterval(row, row);
			this.popupMenu.show(e.getComponent(), e.getX(), e.getY());
		}
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void valueChanged(ListSelectionEvent e) {
		super.valueChanged(e);
		int row = getSelectedRow();
		if (row < 0) {
			return;
		}
		if (LogViewer.logDetail.isVisible()) {
			LogViewer.logDetail.showPacket();
		}
	}

	public int getSelectedRow() {
		int row = super.getSelectedRow();
		if (row < 0) {
			return row;
		}
		return this.sorter.modelIndex(row);
	}
}
