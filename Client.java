package airplaneseats;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import chat.Client.ConnectServer;

public class Client extends JFrame implements Runnable {
	private Socket socket;
	private DataInputStream input;
	private DataOutputStream output;
	private String chosenSeat = "";
	private JPanel panelSeats = new JPanel();

	private static final int NUM_ROWS = 10;
	private static final int SEATS_PER_ROW = 10;
	
	private JFrame frame = new JFrame("Airplane Seat Reservation");
	private String[] seats = new String[100];
	private JButton btnSend = new JButton("SEND");

	public Client() {
		connect();
		initFrame();
		showMe();
	}

	public void connect() {
		try {
			socket = new Socket("192.168.1.138", 5000);
		//	socket = new Socket("localhost", 5000);

			input = new DataInputStream(socket.getInputStream());
			output = new DataOutputStream(socket.getOutputStream());
			String str = input.readUTF();
			seats = str.split(",");
			Thread thread = new Thread(this);
			thread.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void initFrame() {
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLayout(new BorderLayout());
		JPanel panelUp = new JPanel();
		Label title = new Label("Welcome to our site!");
		title.setFont(new Font("Calibri", 3, 18));
		panelUp.add(title);

		panelSeats = new JPanel(new GridLayout(NUM_ROWS, SEATS_PER_ROW));
		for (int row = 0; row < NUM_ROWS; row++) {
			for (int seat = 0; seat < SEATS_PER_ROW; seat++) {
				JButton seatButton = new JButton(row + "-" + seat);
				for (String s : seats) {
					if ((row + "-" + seat).equals(s)) {
						seatButton.setBackground(Color.RED);
						seatButton.setEnabled(false);
					} else
						seatButton.setBackground(Color.CYAN);
				}
				seatButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						Color color = seatButton.getBackground();
						if (color == Color.BLUE) {
							chosenSeat = "";
							seatButton.setBackground(Color.CYAN);
							btnSend.setEnabled(false);
						} else {
							for (Component component : panelSeats.getComponents()) {
								if (component instanceof JButton) {
									JButton button = (JButton) component;
									if (!(button.getBackground().equals(Color.RED))
											&& !(button.getBackground().equals(Color.GREEN)))
										button.setBackground(Color.CYAN);
								}
							}
							chosenSeat = seatButton.getText();
							seatButton.setBackground(Color.BLUE);
							btnSend.setEnabled(true);
						}
					}
				});
				panelSeats.add(seatButton);
			}
		}
		btnSend.setEnabled(false);
		btnSend.addActionListener(e -> {
			try {
				output.writeUTF(chosenSeat);
				btnSend.setEnabled(false);
			} catch (IOException e2) {
				e2.printStackTrace();
			}
		});

		JPanel panelDown = new JPanel();
		Label enjoyLabel = new Label("Enjoy the flight...");
		enjoyLabel.setFont(new Font("Calibri", 3, 18));
		panelDown.add(enjoyLabel);

		JPanel panelLegend = new JPanel();
		panelLegend.setLayout(new GridBagLayout());
		panelLegend.setBackground(Color.WHITE);
		panelUp.setBackground(Color.WHITE);
		
		Label cyan = new Label("Available seat");
		Label green = new Label("Seat taken by you");
		Label red = new Label("Seat taken by another customer");
		
		cyan.setFont(new Font("Calibri", 3, 12));
		cyan.setBackground(Color.CYAN);
		green.setFont(new Font("Calibri", 3, 12));
		green.setBackground(Color.GREEN);
		red.setFont(new Font("Calibri", 3, 12));
		red.setBackground(Color.RED);
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = MAXIMIZED_HORIZ;
		panelLegend.add(cyan, gbc);
		gbc.gridy = 1;
		panelLegend.add(green, gbc);
		gbc.gridy = 2;
		panelLegend.add(red, gbc);

		frame.add(panelUp, BorderLayout.NORTH);
		frame.add(panelSeats, BorderLayout.CENTER);
		frame.add(btnSend, BorderLayout.SOUTH);
		frame.add(panelLegend, BorderLayout.WEST);
		
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				try {
					socket.close();
					input.close();
					output.close();
				} catch (IOException e1) {
					System.exit(0);
				}
				System.exit(0);
			}
		});
	}

	public void showMe() {
		frame.setSize(800, 600);
		frame.setVisible(true);
	}

	@Override
	public void run() {
		while (true) {
			try {
				String hostName = input.readUTF();
				String seat = input.readUTF();
				Color color;
				System.out.println(hostName);
				System.out.println(InetAddress.getLocalHost().getHostAddress());
				System.out.println(InetAddress.getLocalHost().getHostName());
				System.out.println(InetAddress.getLocalHost().getLocalHost());

				if (hostName.equals(InetAddress.getLocalHost().getHostAddress())||hostName.equals(InetAddress.getLocalHost().getHostName())) {
					color = Color.GREEN;
				}
//				if (hostName.equals(InetAddress.getLocalHost().getHostName())) {
//					color = Color.GREEN;
//				}
				else
					color = Color.RED;
				for (Component component : panelSeats.getComponents()) {
					if (component instanceof JButton) {
						JButton button = (JButton) component;
						if (button.getText().trim().equals(seat.trim())) {
							button.setBackground(color);
							button.setEnabled(false);
						}
					}
				}

			} catch (IOException e) {
				System.exit(0);
			}
		}

	}

}
