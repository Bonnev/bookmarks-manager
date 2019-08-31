package org.bookmarksmanager.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class BookmarksClient {
	private final static String HOST = "localhost";
	private final static int PORT = 8080;
	
	private static PrintWriter writer;
	private static BufferedReader reader;
	private static Socket socket;
	private static Status status = Status.Idle;

	public static enum Status {
		Idle,
		Connected,
		UserDisconnected
	}

	public static void main(String[] args) {
		
		if(!connect()) {
			disconnected();
		}

		try (Scanner scanner = new Scanner(System.in)) {
			while (true) {
				String input = scanner.nextLine();
				
				if(input.equals("exit")) {
					disconnect();
					break;
				}
				
				writer.println(input);
			}
		} catch(Exception e) {
			System.out.println("There was a problem: " + e.getMessage());
			disconnected();
		}
	}

	private static boolean connect() {
		try {
			socket = new Socket(HOST, PORT);
			writer = new PrintWriter(socket.getOutputStream(), true);
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			writer.println("h!");
			
			String response = reader.readLine();
			if (!response.equals("he!!o")) {
				throw new IllegalStateException();
			}

			String welcomeMessage = reader.readLine();
			System.out.println(welcomeMessage);

			ClientRunnable clientRunnable = new ClientRunnable(socket);
			new Thread(clientRunnable, "Listen_thread").start();

			status = Status.Connected;
			return true;
		} catch(IOException | IllegalStateException e) {
			System.out.println("Could not connect to server...");
			return false;
		}
	}

	private static void disconnect() {
		try {
			status = Status.UserDisconnected;

			if(socket != null) {
				socket.close();
			}
		} catch (IOException e) {
			System.out.println("Could not disconnect!");
		}
	}

	static void disconnected() {
		try {
			if(socket != null) {
				socket.close();
			}

			do {
				System.out.println("Trying to reconnect in 5 seconds...");
				Thread.sleep(5000);
			} while(!connect() && BookmarksClient.getStatus() != Status.UserDisconnected);
		} catch (IOException e) {
			System.out.println("Could not disconnect!");
		} catch (InterruptedException e) {
			System.out.println("Could not wait...");
		}
	}

	public static Status getStatus() {
		return status;
	}
}
