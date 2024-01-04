package airplaneseats;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.stream.Collectors;

import chat.Server.ServerClient;

public class Server extends Thread {

	private ServerSocket server;
	HashMap<String, Boolean> seats = new HashMap<String, Boolean>();
	ArrayList<ServerClient> clients = new ArrayList<ServerClient>();

	public Server() {
		try {
			server = new ServerSocket(5000);
			initSeats();
			this.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void initSeats() {
		for (Integer i = 0; i < 10; i++) {
			for (Integer j = 0; j < 10; j++) {
				seats.put(i + "-" + j, false);
			}
		}
		seats.put("5-5", true);
	}

	public void sendToAll(String seat) {
		for (ServerClient client : clients) {
			try {
				client.output.writeUTF(seat);
				client.output.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void run() {
		Socket s;
		while (true) {
			try {
				s = server.accept();
				ServerClient client = new ServerClient(s);
				String str = seats.entrySet().stream().filter(entry -> entry.getValue()).map(entry -> entry.getKey())
						.collect(Collectors.joining(","));
				client.output.writeUTF(str);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public ServerSocket getServer() {
		return server;
	}

	public void setServer(ServerSocket server) {
		this.server = server;
	}

	public HashMap<String, Boolean> getSeats() {
		return seats;
	}

	public void setSeats(HashMap<String, Boolean> seats) {
		this.seats = seats;
	}

	public class ServerClient extends Thread {
		private Socket socket;
		private DataInputStream input;
		private DataOutputStream output;
		private String userName;

		public ServerClient(Socket socket) {
			this.socket = socket;
			try {
				input = new DataInputStream(socket.getInputStream());
				output = new DataOutputStream(socket.getOutputStream());
				userName = socket.getInetAddress().getHostName();
				clients.add(this);

			} catch (IOException e) {
				e.printStackTrace();
			}
			this.start();
		}

		@Override
		public void run() {
			String seat;
			while (true) {
				try {
					seat = input.readUTF();
					synchronized (seats) {
						seats.put(seat, true);
						sendToAll(socket.getInetAddress().getHostName());
						sendToAll(seat);
					}
				} catch (IOException e) {
					try {
						socket.close();
						input.close();
						output.close();
					} catch (IOException e1) {
						
					}
					clients.remove(this);
				}
			}
		}

		public Socket getSocket() {
			return socket;
		}

		public void setSocket(Socket socket) {
			this.socket = socket;
		}

		public DataInputStream getInput() {
			return input;
		}

		public void setInput(DataInputStream input) {
			this.input = input;
		}

		public DataOutputStream getOutput() {
			return output;
		}

		public void setOutput(DataOutputStream output) {
			this.output = output;
		}

		public String getUserName() {
			return userName;
		}

		public void setUserName(String userName) {
			this.userName = userName;
		}

	}
}