package org.bookmarksmanager.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerRunnable implements Runnable {
	//private Socket socket;
	private BufferedReader reader;
	private PrintWriter writer;

	public ServerRunnable(Socket socket) {
		super();
		//this.socket = socket;
		try {
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			writer = new PrintWriter(socket.getOutputStream(), true);
		} catch (IOException e) {
			System.out.println("Cannot get streams off socket :(");
		}
	}

	@Override
	public void run() {
		try {
			String commandInput = reader.readLine();
			while (commandInput != null) {

				String command = commandInput.split(" ")[0];
				
				if(command.equals("hey")) {
					writer.println("This is going to be an awkward conversation...");
				}
				
				commandInput = reader.readLine();
			}
		} catch (IOException e) {
			System.out.println("Connection closed!");
		} finally {
			//ChatServer.disconnectedUser(username);
		}
	}

}
