package org.bookmarksmanager.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import org.bookmarksmanager.client.BookmarksClient.Status;

public class ClientRunnable implements Runnable {

	private BufferedReader reader;

	public ClientRunnable(Socket socket) {
		try {
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (IOException e) {
			System.out.println("Could not create the Runnable...");
		}
	}

	@Override
	public void run() {
		try {
			String nextLine = reader.readLine();
			while (nextLine != null) {
				System.out.println("--> " + nextLine);

				nextLine = reader.readLine();
			}
		} catch (IOException e) {
			if(BookmarksClient.getStatus() != Status.UserDisconnected) {
				System.out.println("Connection lost!");
				BookmarksClient.disconnected();
			}
		}
	}
}
