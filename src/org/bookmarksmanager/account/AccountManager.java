package org.bookmarksmanager.account;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.bookmarksmanager.server.AccountManagerResult;
import org.bookmarksmanager.server.Messagable;
import org.bookmarksmanager.server.ServerThread;

public class AccountManager {
	/**
	 * A map of the users by their username
	 */
	private static Map<String, User> users = new HashMap<>();

	public static enum AccountManagerResultType implements Messagable {
		SuccessRegister("Registered sucessfully!"),
		SuccessLogin("Logged in sucessfully!"),
		SuccessLogout("Logged out successfully!"),
		UsernameTaken("Username already taken!"),
		UsernameNotExist("User does not exist!"),
		PasswordTooShort("Password must be at least 5 symbols long!"),
		PasswordWrong("Pasword given is wrong!"),
		AnotherUserLoggedIn("Another user is already logged in!"),
		NoOneIsLoggedIn("No one is logged in yet!");

		private String message;

		private AccountManagerResultType(String message) {
			this.message = message;
		}

		public String getMessage() {
			return message;
		}
	}

	public synchronized static AccountManagerResultType register(String username, String password) {
		ServerThread currentThread = (ServerThread) Thread.currentThread();
		if(currentThread.getCurrentUser() != null) {
			return AccountManagerResultType.AnotherUserLoggedIn;
		}

		if(users.containsKey(username)) {
			return AccountManagerResultType.UsernameTaken;
		}

		if(password.length() < 5) {
			return AccountManagerResultType.PasswordTooShort;
		}

		String hashed = hashPassword(password);

		users.put(username, new User(username, hashed));
		return AccountManagerResultType.SuccessRegister;
	}
	
	public synchronized static AccountManagerResultType login(String username, String password) {
		ServerThread currentThread = (ServerThread) Thread.currentThread();
		if(currentThread.getCurrentUser() != null) {
			return AccountManagerResultType.AnotherUserLoggedIn;
		}

		if(!users.containsKey(username)) {
			return AccountManagerResultType.UsernameNotExist;
		}

		String hashed = hashPassword(password);

		if(!users.get(username).getPassword().equals(hashed)) {
			return AccountManagerResultType.PasswordWrong;
		}

		currentThread.setCurrentUser(users.get(username));
		return AccountManagerResultType.SuccessLogin;
	}

	public synchronized static AccountManagerResultType logout() {
		ServerThread currentThread = (ServerThread) Thread.currentThread();
		if(currentThread.getCurrentUser() == null) {
			return AccountManagerResultType.NoOneIsLoggedIn;
		}

		currentThread.setCurrentUser(null);
		return AccountManagerResultType.SuccessLogout;
	}
	
	public synchronized static AccountManagerResult getCurrentUsername() {
		ServerThread currentThread = (ServerThread) Thread.currentThread();
		if(currentThread.getCurrentUser() == null) {
			return new AccountManagerResult(AccountManagerResultType.NoOneIsLoggedIn, null);
		}

		return new AccountManagerResult(AccountManagerResultType.AnotherUserLoggedIn, currentThread.getCurrentUser().getUsername());
	}

	private static String hashPassword(String password) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-512");
			byte[] hashedPassword = md.digest(password.getBytes(StandardCharsets.UTF_8));
			String hash = DatatypeConverter.printHexBinary(hashedPassword);

			return hash;
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Could not has the password!");
		}

		return null;
	}
}
