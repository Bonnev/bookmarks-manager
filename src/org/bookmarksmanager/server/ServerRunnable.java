package org.bookmarksmanager.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bookmarksmanager.account.AccountManager;
import org.bookmarksmanager.bookmark.BookmarkManager;

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
			
			Messagable addResult = BookmarkManager.addLink(url);

			writer.println(addResult.getMessage());
			break;
		case "list-all":
			AbstractManagerResult<?, String> listAllResult = BookmarkManager.listAll();

			writer.println(listAllResult.getMessage());
			if(listAllResult.getValue() != null) {
				writer.println(listAllResult.getValue());
			}
			break;
		case "search":
			AbstractManagerResult<?, String> searchResult = null;
			
			String byWhat = tokens[1];
			if(byWhat.equals("-tags")) {
				List<String> tags = Arrays.stream(tokens).skip(2).collect(Collectors.toList());
				searchResult = BookmarkManager.searchByTags(tags);
				
			} else if(byWhat.equals("-title")) {
				String title = tokens[2];
				searchResult = BookmarkManager.searchByTitle(title);
				
			} else {
				writer.println("Malformed command!");
				break;
			}

			writer.println(searchResult.getMessage());
			if(searchResult.getValue() != null) {
				writer.println(searchResult.getValue());
			}
			break;
		}
	}
}
