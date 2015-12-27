package yr15.m12.chat.main;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

import javax.swing.*;

@SuppressWarnings("serial")
public class MyClient extends JPanel {
	public static final int INITAL_PORT = 5000;
	private static final int MIN = INITAL_PORT - 1000;
	private static final int MAX = INITAL_PORT + 1000;
	private static final String CLIENT_NAME = "My Client";
	private static final int TA_ROWS = 15;
	private static final int TA_COLS = 20;
	private static final int CNF_COLS = 15;
	private SpinnerNumberModel spinModel = new SpinnerNumberModel(INITAL_PORT, MIN, MAX, 1);
	private JSpinner portSpinner = new JSpinner(spinModel);
	private JTextField clientNameField = new JTextField(CLIENT_NAME, CNF_COLS);
	private Socket socket;
	private String serverName;
	private JTextArea chatArea = new JTextArea(TA_ROWS, TA_COLS);
	private JTextField entryField = new JTextField();
	private Action entryAction;
	private PrintStream out;

	// TODO: jtextfield actions for ssending text for connecting
	// joptionpane when connected
	
	public MyClient() {
		JPanel topPanel = new JPanel();
		topPanel.add(new JLabel("Port:"));
		topPanel.add(portSpinner);
		topPanel.add(new JLabel("Client Name:"));
		topPanel.add(clientNameField);
		topPanel.add(new JButton(new ConnectAction("Connect")));
		topPanel.add(new JButton(new ExitAxn("Exit", KeyEvent.VK_X)));
		
		chatArea.setFocusable(false);
		chatArea.setLineWrap(true);
		chatArea.setWrapStyleWord(true);
		JScrollPane chatSPane = new JScrollPane(chatArea);
		chatSPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		setLayout(new BorderLayout());
		add(topPanel, BorderLayout.PAGE_START);
		add(chatSPane, BorderLayout.CENTER);
		// TODO: finish constructor
	}
	
	public boolean isSocketOpen() {
		return socket != null && !socket.isClosed();
	}
	
	public void closeClientSocket() {
		if (isSocketOpen()) {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private class ConnectAction extends AbstractAction {
		public ConnectAction(String name) {
			super(name);
			int mnemonic = (int) name.charAt(0);
			putValue(MNEMONIC_KEY, mnemonic);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			
		}
	}
	
	private class ExitAxn extends AbstractAction {
		public ExitAxn(String name, int mnemonic) {
			super(name);
			putValue(MNEMONIC_KEY, mnemonic);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			closeClientSocket();
			Window win = SwingUtilities.getWindowAncestor(MyClient.this);
			win.dispose();
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			createAndShowGui();
		});
	}

	private static void createAndShowGui() {
		MyClient mainPanel = new MyClient();
		JFrame frame = new JFrame("My Client");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.add(mainPanel);
		frame.pack();
		frame.setLocationByPlatform(true);
		frame.setVisible(true);
	}

}
