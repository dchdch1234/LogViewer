import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.border.Border;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

public class LogDetail extends JDialog implements ActionListener {
	private static final long serialVersionUID = 1L;
	private static final int DIALOG_WIDTH = 800;
	private static final int DIALOG_HEIGHT = 600;
	private static final int BYTES_PER_ROW = 16;
	private static final int PACKET_MODE = 1;
	private static final int PAYLOAD_MODE = 2;
	private static final int PAYDATA_MODE = 3;
	private int mode = 1;
	private JPanel mainPanel = null;
	private JPanel buttonPanel = null;
	private JButton closeButton = null;
	private JTabbedPane tabbedPane = null;
	private JPanel packetTab = null;
	private PairSection packetGeneral = null;
	private PairSection packetType = null;
	private PairSection packetSource = null;
	private PairSection packetDestination = null;
	private PairSection packetData = null;
	private JPanel payloadTab = null;
	private PairSection payloadGeneral = null;
	private PairSection payloadSource = null;
	private PairSection payloadDestination = null;
	private PairSection payloadData = null;
	private JPanel paydataTab = null;
	private TextSection paydataHex = null;
	private PairSection paydataPosition = null;
	private TextSection paydataAscii = null;
	private TextSection eventDescription = null;
	private int logtype = 0;

	private class PairSection extends JPanel {
		private static final long serialVersionUID = 1L;

		public PairSection(String title) {
			Border b1 = BorderFactory.createTitledBorder(title);
			Border b2 = BorderFactory.createEmptyBorder(0, 5, 5, 5);
			setBorder(BorderFactory.createCompoundBorder(b1, b2));
			setLayout(null);
		}

		public void addRow(String name) {
			add(new JLabel(name));
			add(new JLabel(""));
			setMinimumSize(new Dimension(750, getComponentCount() * 10 + 30));
			setPreferredSize(new Dimension(750, getComponentCount() * 10 + 30));
		}

		public void setValue(int row, String value) {
			JLabel label = (JLabel) getComponent(row * 2 + 1);
			label.setText(value);
		}

		public void setBounds(int x, int y, int width, int height) {
			super.setBounds(x, y, width, height);
			Component[] array = getComponents();
			if (array == null) {
				return;
			}
			y = 20;
			for (int i = 0; i < array.length - 1; i += 2) {
				array[i].setBounds(20, y, 150, 20);
				array[(i + 1)].setBounds(190, y, width - 210, 20);
				y += 20;
			}
		}
	}

	private class TextSection extends JPanel {
		private static final long serialVersionUID = 1L;
		private JScrollPane scrollPane = null;
		private JTextPane textPane = null;
		private StyledDocument document = null;

		public TextSection(String title) {
			Border b1 = BorderFactory.createTitledBorder(title);
			Border b2 = BorderFactory.createEmptyBorder(0, 5, 5, 5);
			setBorder(BorderFactory.createCompoundBorder(b1, b2));
			setLayout(new GridLayout(1, 1));
			setMinimumSize(new Dimension(750, 200));
			setMaximumSize(new Dimension(750, 200));
			setPreferredSize(new Dimension(750, 200));

			this.textPane = new JTextPane();
			this.textPane.setEditable(false);

			this.document = this.textPane.getStyledDocument();
			Style def = StyleContext.getDefaultStyleContext().getStyle(
					"default");
			Style regular = this.document.addStyle("regular", def);
			StyleConstants.setFontFamily(def, "Monospaced");
			Style s = this.document.addStyle("normal", regular);
			StyleConstants.setFontSize(s, 11);
			s = this.document.addStyle("highlight", regular);
			StyleConstants.setForeground(s, Color.RED);
			StyleConstants.setBold(s, true);

			this.scrollPane = new JScrollPane(this.textPane);
			add(this.scrollPane);
		}

		public void reset() {
			this.textPane.setText("");
		}

		public void addText(String text, String style) {
			try {
				this.document.insertString(this.document.getLength(), text,
						this.document.getStyle(style));
				this.textPane.setCaretPosition(0);
			} catch (Exception ex) {
			}
		}
	}

	public LogDetail(int logtype) {
		super(LogViewer.logViewer, false);
		setResizable(false);
		setSize(800, 600);
		setLocationRelativeTo(LogViewer.logViewer);

		this.mainPanel = new JPanel();
		this.mainPanel.setLayout(new BorderLayout());
		setContentPane(this.mainPanel);

		this.buttonPanel = new JPanel();
		this.buttonPanel.setLayout(new BorderLayout());
		this.buttonPanel
				.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 20));
		this.mainPanel.add(this.buttonPanel, "South");

		this.closeButton = new JButton("Close");
		this.closeButton.addActionListener(this);
		this.buttonPanel.add(this.closeButton, "East");

		this.tabbedPane = new JTabbedPane();
		this.tabbedPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		this.tabbedPane.setSize(800, 600);
		this.mainPanel.add(this.tabbedPane, "Center");

		this.tabbedPane.addTab("Packet Log Viewer", this.packetTab = new JPanel());
		this.packetTab.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		this.packetTab.setLayout(new BoxLayout(this.packetTab, 3));
		this.logtype = logtype;
		if (this.logtype != 2) {
			this.packetTab.add(this.packetGeneral = new PairSection(
					"General Information"));
			this.packetGeneral.addRow("Time:");
			this.packetGeneral.addRow("Repeated");
			this.packetGeneral.addRow("Host:");
			this.packetGeneral.addRow("Reason:");
			this.packetGeneral.addRow("Direction:");
			this.packetGeneral.addRow("Action:");
			this.packetGeneral.addRow("Rank:");
			this.packetGeneral.addRow("Interface:");

			this.packetTab
					.add(this.packetType = new PairSection("Packet Type"));
			this.packetType.addRow("Frame Type:");
			this.packetType.addRow("Protocol:");
			this.packetType.addRow("Flags:");

			this.packetTab.add(this.packetSource = new PairSection("Source"));
			this.packetSource.addRow("IP:");
			this.packetSource.addRow("MAC:");
			this.packetSource.addRow("Port:");

			this.packetTab.add(this.packetDestination = new PairSection(
					"Destination"));
			this.packetDestination.addRow("IP:");
			this.packetDestination.addRow("MAC:");
			this.packetDestination.addRow("Port:");

			this.packetTab
					.add(this.packetData = new PairSection("Packet Data"));
			this.packetData.addRow("Packet Size:");

			addGlue(this.packetTab);

			this.tabbedPane.addTab("Payload Log Viewer",
					this.payloadTab = new JPanel());
			this.payloadTab.setBorder(BorderFactory.createEmptyBorder(5, 5, 5,
					5));
			this.payloadTab.setLayout(new BoxLayout(this.payloadTab, 3));

			this.payloadTab.add(this.payloadGeneral = new PairSection(
					"General Information"));
			this.payloadGeneral.addRow("Time:");
			this.payloadGeneral.addRow("Repeated:");
			this.payloadGeneral.addRow("Host:");
			this.payloadGeneral.addRow("Reason:");
			this.payloadGeneral.addRow("Application Type:");
			this.payloadGeneral.addRow("Action:");
			this.payloadGeneral.addRow("Rank:");
			this.payloadGeneral.addRow("Note:");
			this.payloadGeneral.addRow("Direction:");
			this.payloadGeneral.addRow("Interface:");
			this.payloadGeneral.addRow("Protocol:");
			this.payloadGeneral.addRow("Flags:");

			this.payloadTab.add(this.payloadSource = new PairSection("Source"));
			this.payloadSource.addRow("IP:");
			this.payloadSource.addRow("MAC:");
			this.payloadSource.addRow("Port:");

			this.payloadTab.add(this.payloadDestination = new PairSection(
					"Destination"));
			this.payloadDestination.addRow("IP:");
			this.payloadDestination.addRow("MAC:");
			this.payloadDestination.addRow("Port:");

			this.payloadTab.add(this.payloadData = new PairSection(
					"Packet Data"));
			this.payloadTab.add(this.payloadData);
			this.payloadData.addRow("Packet Size:");

			addGlue(this.payloadTab);

			this.tabbedPane.addTab("Payload Data",
					this.paydataTab = new JPanel());
			this.paydataTab.setBorder(BorderFactory.createEmptyBorder(5, 5, 5,
					5));
			this.paydataTab.setLayout(new BoxLayout(this.paydataTab, 3));

			this.paydataTab.add(this.paydataHex = new TextSection("HEX"));
			this.paydataTab.add(this.paydataPosition = new PairSection(
					"Position"));
			this.paydataPosition.addRow("Match Position in Buffer:");
			this.paydataPosition.addRow("Match Position in Stream:");
			this.paydataTab.add(this.paydataAscii = new TextSection("ASCII"));

			addGlue(this.paydataTab);
		} else {
			this.packetTab.add(this.packetGeneral = new PairSection(
					"General Information"));
			this.packetGeneral.addRow("Time:");
			this.packetGeneral.addRow("Level:");
			this.packetGeneral.addRow("Event ID:");
			this.packetGeneral.addRow("Event:");
			this.packetGeneral.addRow("Tag(s):");
			this.packetGeneral.addRow("Target:");
			this.packetGeneral.addRow("Action By:");
			this.packetTab.add(this.eventDescription = new TextSection(
					"Description:"));
			addGlue(this.packetTab);
		}
	}

	private void showEvent() {
		int row = LogViewer.logTable.getSelectedRow();
		if (row < 0) {
			return;
		}
		Object[] array = LogViewer.logModel.getRow(row);
		if (array == null) {
			return;
		}
		this.packetGeneral.setValue(0, getColumnValue(array, "Time"));
		this.packetGeneral.setValue(1, getColumnValue(array, "Level"));
		this.packetGeneral.setValue(2, getColumnValue(array, "Event ID"));
		this.packetGeneral.setValue(3, getColumnValue(array, "Event"));
		this.packetGeneral.setValue(4, getColumnValue(array, "Tag(s)"));
		this.packetGeneral.setValue(5, getColumnValue(array, "Target"));
		this.packetGeneral.setValue(6, getColumnValue(array, "Action By"));
		this.eventDescription.addText(getColumnValue(array, "Description"),
				"normal");
		
		if (!isVisible()) {
			this.tabbedPane.removeAll();
			this.tabbedPane.addTab("Host Event Viewer", this.packetTab);
			setTitle("Host Event Viewer");
			pack();
			setVisible(true);
		} else {
			if (this.mode == 3) {
				if (this.paydataTab.getParent() == null) {
					this.tabbedPane.addTab("Payload Data", this.paydataTab);
				}
			} else if (this.paydataTab.getParent() != null) {
				this.tabbedPane.removeTabAt(1);
			}
			toFront();
		}

	}

	public void showPacket() {
		if (this.logtype == 2) {
			this.showEvent();
			return;
		}
		int row = LogViewer.logTable.getSelectedRow();
		if (row < 0) {
			return;
		}
		Object[] array = LogViewer.logModel.getRow(row);
		if (array == null) {
			return;
		}
		if (LogViewer.logModel.findColumn("Data") == -1) {
			this.mode = 1;

			this.packetGeneral.setValue(0, getColumnValue(array, "Time"));
			String repeatCount = getColumnValue(array, "Repeat Count");
			String endTime = getColumnValue(array, "End Time");
			if ((repeatCount.length() > 0) && (!repeatCount.equals("1"))
					&& (endTime.length() > 0)) {
				this.packetGeneral.setValue(1, repeatCount + " times through "
						+ toDate(endTime));
			} else {
				this.packetGeneral.setValue(1, "<not repeated>");
			}
			this.packetGeneral.setValue(2, getColumnValue(array, "Host"));
			this.packetGeneral.setValue(3, getColumnValue(array, "Reason"));
			this.packetGeneral.setValue(4, getColumnValue(array, "Direction"));
			this.packetGeneral.setValue(5, getColumnValue(array, "Action"));
			this.packetGeneral.setValue(6, getColumnValue(array, "Rank"));
			this.packetGeneral.setValue(7, getColumnValue(array, "Interface"));

			this.packetType.setValue(0, getColumnValue(array, "Frame Type"));
			this.packetType.setValue(1, getColumnValue(array, "Protocol"));
			this.packetType.setValue(2, getColumnValue(array, "Flags"));

			this.packetSource.setValue(0, getColumnValue(array, "Source IP"));
			this.packetSource.setValue(1, getColumnValue(array, "Source MAC"));
			this.packetSource.setValue(2, getColumnValue(array, "Source Port"));

			this.packetDestination.setValue(0,
					getColumnValue(array, "Destination IP"));
			this.packetDestination.setValue(1,
					getColumnValue(array, "Destination MAC"));
			this.packetDestination.setValue(2,
					getColumnValue(array, "Destination Port"));

			this.packetData.setValue(0, getColumnValue(array, "Packet Size"));
		} else {
			this.mode = 2;

			this.payloadGeneral.setValue(0, getColumnValue(array, "Time"));
			String repeatCount = getColumnValue(array, "Repeat Count");
			String endTime = getColumnValue(array, "End Time");
			if ((repeatCount.length() > 0) && (!repeatCount.equals("1"))
					&& (endTime.length() > 0)) {
				this.payloadGeneral.setValue(1, repeatCount + " times through "
						+ endTime);
			} else {
				this.payloadGeneral.setValue(1, "<not repeated>");
			}
			this.payloadGeneral.setValue(2, getColumnValue(array, "Host"));
			String reason = getColumnValue(array, "Reason");
			if (reason.length() == 0) {
				reason = getColumnValue(array, "Payload Filter");
			}
			this.payloadGeneral.setValue(3, reason);
			this.payloadGeneral.setValue(4,
					getColumnValue(array, "Application Type"));
			this.payloadGeneral.setValue(5, getColumnValue(array, "Action"));
			this.payloadGeneral.setValue(6, getColumnValue(array, "Rank"));
			this.payloadGeneral.setValue(7, getColumnValue(array, "Note"));
			this.payloadGeneral.setValue(8, getColumnValue(array, "Direction"));
			this.payloadGeneral.setValue(9, getColumnValue(array, "Interface"));
			this.payloadGeneral.setValue(10, getColumnValue(array, "Protocol"));
			this.payloadGeneral.setValue(11, getColumnValue(array, "Flags"));

			this.payloadSource.setValue(0, getColumnValue(array, "Source IP"));
			this.payloadSource.setValue(1, getColumnValue(array, "Source MAC"));
			this.payloadSource
					.setValue(2, getColumnValue(array, "Source Port"));

			this.payloadDestination.setValue(0,
					getColumnValue(array, "Destination IP"));
			this.payloadDestination.setValue(1,
					getColumnValue(array, "Destination MAC"));
			this.payloadDestination.setValue(2,
					getColumnValue(array, "Destination Port"));

			this.payloadData.setValue(0, getColumnValue(array, "Packet Size"));

			String data = getColumnValue(array, "Data");
			if (data.length() > 0) {
				this.mode = 3;

				int position = -1;
				String positionInBufferValue = getColumnValue(array,
						"Position in Buffer");
				if (positionInBufferValue.length() > 0) {
					try {
						position = Integer.parseInt(positionInBufferValue);
					} catch (Throwable t) {
					}
				}
				this.paydataPosition.setValue(0, positionInBufferValue);
				this.paydataPosition.setValue(1,
						getColumnValue(array, "Position in Stream"));

				this.paydataHex.reset();
				this.paydataAscii.reset();
				byte[] binary;
				try {
					binary = hexStringToByteArray(data);
				} catch (Exception e) {
					binary = new byte[0];
				}
				if ((position >= 0) && (position < binary.length)) {
					this.paydataAscii.addText(toASCII(binary, 0, position),
							"normal");
					this.paydataAscii.addText(
							toASCII(binary, position, position + 1),
							"highlight");
					this.paydataAscii.addText(
							toASCII(binary, position + 1, binary.length),
							"normal");
				} else {
					this.paydataAscii.addText(
							toASCII(binary, 0, binary.length), "normal");
				}
				int r = 0;
				int offset = 0;
				while (offset < binary.length) {
					offset = r * 16;
					String head = Integer.toString(offset);
					while (head.length() < 6) {
						head = " " + head;
					}
					this.paydataHex.addText(head + ":  ", "normal");
					for (int c = 0; c < 16; c++) {
						if (offset + c < binary.length) {
							byte b = binary[(offset + c)];
							this.paydataHex.addText(" " + toHex(b),
									offset + c == position ? "highlight"
											: "normal");
						} else {
							this.paydataHex.addText("   ", "normal");
						}
					}
					this.paydataHex.addText("    ", "normal");
					for (int c = 0; c < 16; c++) {
						if (offset + c >= binary.length) {
							break;
						}
						byte b = binary[(offset + c)];
						if ((b < 32) || (b > 126)) {
							b = 46;
						}
						this.paydataHex
								.addText(new Character((char) b).toString(),
										offset + c == position ? "highlight"
												: "normal");
					}
					this.paydataHex.addText("\n", "normal");
					r++;
				}
			}
		}
		if (!isVisible()) {
			this.tabbedPane.removeAll();
			if (this.mode == 1) {
				this.tabbedPane.addTab("Packet Log Viewer", this.packetTab);
				setTitle("Packet Log Viewer");
			} else {
				this.tabbedPane.addTab("Payload Log Viewer", this.payloadTab);
				setTitle("Payload Log Viewer");
				if (this.mode == 3) {
					this.tabbedPane.addTab("Payload Data", this.paydataTab);
				}
			}
			pack();
			setVisible(true);
		} else {
			if (this.mode == 3) {
				if (this.paydataTab.getParent() == null) {
					this.tabbedPane.addTab("Payload Data", this.paydataTab);
				}
			} else if (this.paydataTab.getParent() != null) {
				this.tabbedPane.removeTabAt(1);
			}
			toFront();
		}
	}

	public void actionPerformed(ActionEvent e) {
		setVisible(false);
	}

	private void addGlue(JPanel panel) {
		panel.add(Box.createVerticalGlue());
		panel.add(Box.createRigidArea(new Dimension(1, 1)));
	}

	private String toHex(byte hexByte) {
		char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'A', 'B', 'C', 'D', 'E', 'F' };
		return new String(new char[] { hexDigits[(hexByte >> 4 & 0xF)],
				hexDigits[(hexByte & 0xF)] });
	}

	private static byte[] hexStringToByteArray(String hexString)
			throws Exception {
		hexString = hexString.toUpperCase();
		int hexStringLength = hexString.length();
		int length = hexStringLength / 2;
		byte[] bytes = new byte[length];

		int i = 0;
		for (int j = 0; i < hexStringLength; i += 2) {
			int newByte = 0;
			newByte = hexnib(newByte, hexString.charAt(i));
			newByte = hexnib(newByte, hexString.charAt(i + 1));
			bytes[(j++)] = ((byte) newByte);
		}
		return bytes;
	}

	private static int hexnib(int b, char c) throws Exception {
		b <<= 4;
		int digitValue = c - '0';
		int alphaValue = c - 'A' + 10;
		if ((digitValue >= 0) && (digitValue <= 9)) {
			b += digitValue;
		} else if ((alphaValue >= 10) && (alphaValue <= 15)) {
			b += alphaValue;
		} else {
			throw new Exception("Invalid Hex Dump\n");
		}
		return b;
	}

	private static String toASCII(byte[] hexBytes, int start, int end) {
		StringBuilder string = new StringBuilder();
		for (int i = start; i < end; i++) {
			if (((hexBytes[i] < 32) || (hexBytes[i] > 126))
					&& (hexBytes[i] != 10) && (hexBytes[i] != 13)) {
				string.append('.');
			} else {
				string.append((char) hexBytes[i]);
			}
		}
		return string.toString();
	}

	private static String toDate(String s) {
		try {
			SimpleDateFormat in = new SimpleDateFormat("yyyy/MM/dd hh:mm");
			Date date = in.parse(s);
			SimpleDateFormat out = new SimpleDateFormat("MMMM d, yyyy HH:mm:ss");
			return out.format(date);
		} catch (Exception ex) {
		}
		return s;
	}

	private static String getColumnValue(Object[] theRow, String columnName) {
		int columnIndex = LogViewer.logModel.findColumn(columnName);
		if (columnIndex != -1) {
			try {
				return (String) theRow[columnIndex];
			} catch (Throwable t) {
				return "";
			}
		}
		return "";
	}
}
