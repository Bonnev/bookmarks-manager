package org.bookmarksmanager.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.bookmarksmanager.storage.StorageManager;
import org.bookmarksmanager.storage.StorageManager.StorageManagerResultType;

public class BookmarksServer {
	private final static int PORT = 8080;

	public static void main(String[] args) {
		StorageManagerResultType loadUsersResult = StorageManager.loadUsers();

		if(loadUsersResult == StorageManagerResultType.ProblemLoadingUsers) {
			System.out.println(loadUsersResult.getMessage());
		}

		StorageManagerResultType loadBookmarksResult = StorageManager.loadBookmarks();

		if(loadBookmarksResult == StorageManagerResultType.ProblemLoadingBookmarks) {
			System.out.println(loadBookmarksResult.getMessage());
		}

		try (ServerSocket serverSocket = new ServerSocket(PORT)) {
			System.out.printf("Server running on localhost:%d%n", PORT);

			while (true) {
				Socket socket = serverSocket.accept();

				BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

				String password = reader.readLine();
				if (!password.equals("h!")) {
					writer.println("Who are you?");
					System.out.println("Unauthorized access with message \"" + password + "\"! Connecton declined!");
					continue;
				}

				writer.println("he!!o");
				writer.println("Hello! Welcome to the bookmark manager!");

				System.out.println("A new client has connected!");

				ServerRunnable runnable = new ServerRunnable(socket);
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM_dd_HH_mm_ss");
				String threadName = "Client_Connection_" + LocalDateTime.now().format(formatter);
				new ServerThread(runnable, threadName).start();
			}
		} catch (IOException e) {
			System.out.println("Attachment failed... Maybe another server is running or port " + PORT + " ?");
		}
	}
}
