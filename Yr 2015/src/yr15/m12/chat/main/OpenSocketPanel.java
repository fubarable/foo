package yr15.m12.chat.main;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

@SuppressWarnings("serial")
public class OpenSocketPanel extends JPanel {
	public static final int INITAL_PORT = 5000;
	private static final int MIN = INITAL_PORT - 1000;
	private static final int MAX = INITAL_PORT + 1000;
	private static final int SNF_COLS = 15;
	private SpinnerNumberModel spinModel = new SpinnerNumberModel(INITAL_PORT, MIN, MAX, 1);
	private JSpinner portSpinner = new JSpinner(spinModel);
	private JTextField socketNameField = new JTextField(SNF_COLS);

	public OpenSocketPanel(String name) {
		socketNameField.setText(name);
		add(new JLabel("Socket Name:"));
		add(socketNameField);
		add(new JLabel("Port:"));
		add(portSpinner);
	}

	public String getSocketName() {
		return socketNameField.getText();
	}

	public int getPort() {
		return (Integer) portSpinner.getValue();
	}

	public static void main(String[] args) {
		OpenSocketPanel openSocketPanel = new OpenSocketPanel("My Client");
		String title = "Open Socket";
		int result = JOptionPane.showConfirmDialog(null, openSocketPanel, title, JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			System.out.println("Socket Name: " + openSocketPanel.getSocketName());
			System.out.println("Port Number: " + openSocketPanel.getPort());
		}
	}

}
