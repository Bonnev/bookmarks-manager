package org.bookmarksmanager.storage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

import org.bookmarksmanager.account.AccountManager;
import org.bookmarksmanager.account.User;
import org.bookmarksmanager.bookmark.BookmarkManager;
import org.bookmarksmanager.bookmark.Collection;
import org.bookmarksmanager.server.Messagable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class StorageManager {
	public final static String USERS_FILENAME = "users.txt";
	
	public final static String BOOKMARKS_FILENAME = "bookmarks.txt";
	
	private final static Gson GSON = new GsonBuilder().create();
	
	private final static Type USERS_TYPE = new TypeToken<Map<String, User>>(){}.getType();
	
	private final static Type BOOKMARKS_TYPE = new TypeToken<Map<String, Map<String, Collection>>>(){}.getType();
	
	public static enum StorageManagerResultType implements Messagable {
		SuccessStoreUsers("Users stored successfully!"),
		ProblemStoringUsers("There was a problem storing the users!"),

		SuccessLoadUsers("Users loaded successfully!"),
		UsersFileNotFound("The users file was not found!"),
		ProblemLoadingUsers("There was a problem loading the users!"),

		SuccessStoreBookmarks("Bookmarks stored successfully!"),
		ProblemStoringBookmarks("There was a problem storing the bookmarks!"),

		SuccessLoadBookmarks("Bookmarks loaded successfully!"),
		BookmarksFileNotFound("The bookmarks file was not found!"),
		ProblemLoadingBookmarks("There was a problem loading the bookmarks!");

		private String message;

		private StorageManagerResultType(String message) {
			this.message = message;
		}

		public String getMessage() {
			return message;
		}
	}
	
	public synchronized static StorageManagerResultType storeUsers() {
		Map<String, User> users = AccountManager.getUsers();

		String json = GSON.toJson(users, USERS_TYPE);

		try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(USERS_FILENAME))) {
			bufferedWriter.write(json);
		} catch (IOException e) {
			return StorageManagerResultType.ProblemStoringUsers; 
		}

		return StorageManagerResultType.SuccessStoreUsers;
	}

	public synchronized static StorageManagerResultType loadUsers() {
		String json = null;

		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(USERS_FILENAME))) {
			json = bufferedReader.readLine();
		} catch (FileNotFoundException e) {
			return StorageManagerResultType.UsersFileNotFound; 
		} catch (IOException e) {
			return StorageManagerResultType.ProblemLoadingUsers; 
		}

		Map<String, User> users = GSON.fromJson(json, USERS_TYPE);

		AccountManager.setUsers(users);

		return StorageManagerResultType.SuccessLoadUsers;
	}

	public synchronized static StorageManagerResultType storeBookmarks() {
		Map<String, Map<String, Collection>> links = BookmarkManager.getLinks();

		String json = GSON.toJson(links, BOOKMARKS_TYPE);

		try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(BOOKMARKS_FILENAME))) {
			bufferedWriter.write(json);
		} catch (IOException e) {
			return StorageManagerResultType.ProblemStoringBookmarks; 
		}

		return StorageManagerResultType.SuccessStoreBookmarks;
	}

	public synchronized static StorageManagerResultType loadBookmarks() {
		String json = null;

		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(BOOKMARKS_FILENAME))) {
			json = bufferedReader.readLine();
		} catch (FileNotFoundException e) {
			return StorageManagerResultType.BookmarksFileNotFound; 
		} catch (IOException e) {
			return StorageManagerResultType.ProblemLoadingBookmarks; 
		}

		Map<String, Map<String, Collection>> bookmarks = GSON.fromJson(json, BOOKMARKS_TYPE);

		BookmarkManager.setLinks(bookmarks);

		return StorageManagerResultType.SuccessLoadBookmarks;
	}
}
