package org.bookmarksmanager.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.bookmarksmanager.account.AccountManager;
import org.bookmarksmanager.url.UrlManager;
import org.bookmarksmanager.url.UrlManagerResult;

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
				processCommand(commandInput);

				commandInput = reader.readLine();
			}
		} catch (IOException e) {
			System.out.println("Connection closed!");
		}
	}

	private void processCommand(String input) {
		String[] tokens = input.split(" ");
		String command = tokens[0];

		switch(command.toLowerCase()) {
		case "hey":
			writer.println("This is going to be an awkward conversation...");
			break;
		case "register":
			String registerUsername = tokens[1];
			String registerPassword = tokens[2];

			Messagable registerResult = AccountManager.register(registerUsername, registerPassword);

			writer.println(registerResult.getMessage());
			break;
		case "login":
			String loginUsername = tokens[1];
			String loginPassword = tokens[2];

			Messagable loginResult = AccountManager.login(loginUsername, loginPassword);

			writer.println(loginResult.getMessage());
			break;
		case "logout":
			Messagable logoutResult = AccountManager.logout();

			writer.println(logoutResult.getMessage());
			break;
		case "add":
			String url = tokens[1];
			
			AbstractManagerResult addResult = UrlManager.addLink(url);

			writer.println(addResult.getMessage());
			if(addResult.getValue() != null) {
				writer.println(addResult.getValue());
			}
			break;
		case "list-all":
			UrlManagerResult listAllResult = UrlManager.listAll();

			writer.println(listAllResult.getMessage());
			if(listAllResult.getValue() != null) {
				writer.println(listAllResult.getValue());
			}
			break;
		}
	}
}
