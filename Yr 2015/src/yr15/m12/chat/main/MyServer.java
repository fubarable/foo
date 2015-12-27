package yr15.m12.chat.main;

import java.awt.Component;
import java.awt.Window;
import java.awt.Dialog.ModalityType;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

@SuppressWarnings("serial")
public class MyServer extends JPanel {
	public static final int INITAL_PORT = 5000;
	private static final int MIN = INITAL_PORT - 1000;
	private static final int MAX = INITAL_PORT + 1000;
	public static final String SOCKET_CLOSED = "socket closed";
	private static final String SERVER_NAME = "My Server";
	private static final int GAP = 5;
	private ServerSocket serverSocket;
	private SpinnerNumberModel spinModel = new SpinnerNumberModel(INITAL_PORT, MIN, MAX, 1);
	private JSpinner portSpinner = new JSpinner(spinModel);
	private JTextField serverNameField = new JTextField(SERVER_NAME);
	private List<Socket> sockets = new ArrayList<>();

	public MyServer() {
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.LINE_AXIS));
		topPanel.add(new JLabel("Port:"));
		topPanel.add(Box.createHorizontalStrut(GAP));
		topPanel.add(portSpinner);
		topPanel.add(Box.createHorizontalStrut(3 * GAP));
		topPanel.add(new JLabel("Server Name:"));
		topPanel.add(Box.createHorizontalStrut(GAP));
		topPanel.add(serverNameField);
		JPanel btnPanel = new JPanel(new GridLayout(1, 0, GAP, GAP));
		btnPanel.add(new JButton(new StartSocketAxn("Start Socket", KeyEvent.VK_S, this)));
		btnPanel.add(new JButton(new CloseSocketAxn("Close Socket", KeyEvent.VK_C)));
		btnPanel.add(new JButton(new TestSocketAxn("Test Socket", KeyEvent.VK_T)));
		btnPanel.add(new JButton(new ExitAxn("Exit", KeyEvent.VK_X)));
		
		setBorder(BorderFactory.createEmptyBorder(GAP, GAP, GAP, GAP));
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		add(topPanel);
		add(Box.createVerticalStrut(GAP));
		add(btnPanel);
	}

	public boolean isServerSocketOpen() {
		return serverSocket != null && !serverSocket.isClosed();
	}

	public void closeSocket() {
		if (isServerSocketOpen()) {
			try {
				serverSocket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	private class StartSocketAxn extends AbstractAction {
		private Component component;

		public StartSocketAxn(String name, int mnemonic, Component component) {
			super(name);
			putValue(MNEMONIC_KEY, mnemonic);
			this.component = component;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (isServerSocketOpen()) {
				String text = "Cannot start new ServerSocket as one is already running";
				JOptionPane.showMessageDialog(component, text, "Server Running", JOptionPane.ERROR_MESSAGE);
				return;
			} else {
				int port = (Integer) portSpinner.getValue();
				try {
					Thread serverThread = new Thread(new RunServer(port));
					serverThread.setDaemon(true);
					serverThread.start();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	private class RunServer implements Runnable {

		public RunServer(int port) throws IOException {
			serverSocket = new ServerSocket(port);
		}

		@Override
		public void run() {
			while (!serverSocket.isClosed()) {
				try {
					final Socket socket = serverSocket.accept();
					sockets.add(socket);
					SwingUtilities.invokeLater(() -> {
						try {
							launchHelperDialog(socket);
						} catch (Exception e) {
							e.printStackTrace();
						}
					});
				} catch (IOException e) {
					if (e.getMessage().equalsIgnoreCase(SOCKET_CLOSED)) {
						Component parent = MyServer.this;
						String message = SOCKET_CLOSED;
						String title = "Socket Exception";
						int messageType = JOptionPane.PLAIN_MESSAGE;
						JOptionPane.showMessageDialog(parent, message, title, messageType);
						if (!parent.isShowing()) {
							System.exit(0);
						}
					} else {
						e.printStackTrace();
					}
				}
			}
		}

		private void launchHelperDialog(final Socket socket) throws IOException {
			String serverName = serverNameField.getText();
			ServerHelperPanel serverHelperPanel = new ServerHelperPanel(serverName, socket);
			Component mainPanel = MyServer.this;
			Window win = SwingUtilities.getWindowAncestor(mainPanel);
			JDialog dialog = new JDialog(win, "Server Chat", ModalityType.MODELESS);
			dialog.add(serverHelperPanel);
			dialog.pack();
			dialog.setLocationByPlatform(true);
			dialog.setVisible(true);
		}
	}

	class ServerHelperPanel extends JPanel implements OutStreamable {
		private static final int ROWS = 15;
		private static final int COLS = 20;
		private JTextArea chatArea = new JTextArea(ROWS, COLS);
		private JTextField entryField = new JTextField();
		private Action entryAction;
		private PrintStream out;

		public ServerHelperPanel(String serverName, Socket socket) throws IOException {
			InputStream inputStream = socket.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
			InStreamWorker inStreamWorker = new InStreamWorker(br);
			inStreamWorker.execute();
			
			OutputStream outputStream = socket.getOutputStream();
			out = new PrintStream(outputStream);
			out.println(serverName); // send server name first of all

			chatArea.setFocusable(false);
			chatArea.setLineWrap(true);
			chatArea.setWrapStyleWord(true);
			JScrollPane chatSPane = new JScrollPane(chatArea);
			chatSPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

			entryAction = new EntryAction("Enter", KeyEvent.VK_E, out, this);
			entryField.setAction(entryAction);
			JButton entryBtn = new JButton(entryAction);

			JPanel bottomPanel = new JPanel();
			bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.LINE_AXIS));
			bottomPanel.add(entryField);
			bottomPanel.add(entryBtn);
		}

		@Override
		public String getEntryText() {
			String text = entryField.getText();
			entryField.selectAll();
			return text;
		}
		
		private class InStreamWorker extends SwingWorker<Void, String> {
			private BufferedReader br;

			public InStreamWorker(BufferedReader br) {
				this.br = br;
			}

			@Override
			protected Void doInBackground() throws Exception {
				String line = "";
				while ((line = br.readLine()) != null) {
					publish(line);
				}
				br.close();
				return null;
			}
			
			@Override
			protected void process(List<String> chunks) {
				for (String chunk : chunks) {
					chatArea.append(chunk + "\n");
				}
			}
		}
	}

	interface OutStreamable {
		String getEntryText();
	}

	private class EntryAction extends AbstractAction {
		private PrintStream out;
		private OutStreamable outStreamable;

		public EntryAction(String name, int mnemnic, PrintStream out, OutStreamable outStreamable) {
			super(name);
			putValue(MNEMONIC_KEY, mnemnic);
			this.out = out;
			this.outStreamable = outStreamable;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			String text = outStreamable.getEntryText();
			out.println(text);
		}
	}

	private class CloseSocketAxn extends AbstractAction {
		public CloseSocketAxn(String name, int mnemonic) {
			super(name);
			putValue(MNEMONIC_KEY, mnemonic);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			closeSocket();
		}
	}

	private class TestSocketAxn extends AbstractAction {
		public TestSocketAxn(String name, int mnemonic) {
			super(name);
			putValue(MNEMONIC_KEY, mnemonic);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			String text = isServerSocketOpen() ? "OK" : "Failed";
			Component parent = MyServer.this;
			JOptionPane.showMessageDialog(parent, text, "Server Running?", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	private class ExitAxn extends AbstractAction {
		public ExitAxn(String name, int mnemonic) {
			super(name);
			putValue(MNEMONIC_KEY, mnemonic);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			closeSocket();
			Window win = SwingUtilities.getWindowAncestor(MyServer.this);
			win.dispose();
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			createAndShowGui();
		});
	}

	private static void createAndShowGui() {
		MyServer myServer = new MyServer();
		JFrame frame = new JFrame("My Server");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.add(myServer);
		frame.pack();
		frame.setLocationByPlatform(true);
		frame.setVisible(true);
	}
}
