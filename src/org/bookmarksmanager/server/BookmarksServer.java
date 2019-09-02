package org.bookmarksmanager.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.bookmarksmanager.storage.ChromeBookmark;
import org.bookmarksmanager.storage.StorageManager;
import org.bookmarksmanager.storage.StorageManager.StorageManagerResultType;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class BookmarksServer {
	private final static int PORT = 8080;

	public static void main(String[] args) {
		Gson gson = new GsonBuilder().create();
		ChromeBookmark cb = gson.fromJson("{" + 
				"               \"children\": [ {" + 
				"                  \"date_added\": \"13163433283228721\"," + 
				"                  \"id\": \"1872\"," + 
				"                  \"name\": \"MySpeed by Enounce\"," + 
				"                  \"sync_transaction_version\": \"1\"," + 
				"                  \"type\": \"url\"," + 
				"                  \"url\": \"http://jle.vi/myspeed\"" + 
				"               }, {" + 
				"                  \"date_added\": \"13163433283228796\"," + 
				"                  \"id\": \"1873\"," + 
				"                  \"name\": \"YouTube HTML5 Trial\"," + 
				"                  \"sync_transaction_version\": \"1\"," + 
				"                  \"type\": \"url\"," + 
				"                  \"url\": \"http://youtube.com/html5\"" + 
				"               }, {" + 
				"                  \"date_added\": \"13163433283228868\"," + 
				"                  \"id\": \"1874\"," + 
				"                  \"name\": \"Stitcher for Android\"," + 
				"                  \"sync_transaction_version\": \"1\"," + 
				"                  \"type\": \"url\"," + 
				"                  \"url\": \"http://www.stitcher.com/\"" + 
				"               }, {" + 
				"                  \"date_added\": \"13163433283228945\"," + 
				"                  \"id\": \"1875\"," + 
				"                  \"name\": \"Audible.com Audiobooks\"," + 
				"                  \"sync_transaction_version\": \"1\"," + 
				"                  \"type\": \"url\"," + 
				"                  \"url\": \"http://www.audibletrial.com/superhuman\"" + 
				"               } ]," + 
				"               \"date_added\": \"13163433283227966\"," + 
				"               \"date_modified\": \"13163433283228945\"," + 
				"               \"id\": \"1862\"," + 
				"               \"name\": \"Lecture 56 - SuperLearning by video or audio\"," + 
				"               \"sync_transaction_version\": \"1\"," + 
				"               \"type\": \"folder\"" + 
				"            }", ChromeBookmark.class);
		
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
