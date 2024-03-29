package org.bookmarksmanager.bookmark;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.bookmarksmanager.account.AccountManager;
import org.bookmarksmanager.account.AccountManager.AccountManagerResultType;
import org.bookmarksmanager.account.AccountManagerResult;
import org.bookmarksmanager.server.AbstractManagerResult;
import org.bookmarksmanager.server.Messagable;
import org.bookmarksmanager.storage.StorageManager;
import org.bookmarksmanager.url.UrlManager;
import org.bookmarksmanager.url.UrlManager.UrlManagerResultType;
import org.bookmarksmanager.url.UrlManagerResult;

public class BookmarkManager {
	private static final double FULL_MATCH_SCORE = 1.0;
	private static final double PARTIAL_MATCH_SCORE = 0.1;
	private static final int PARTIAL_MATCH_LENGTH = 3;
	
	/**
	 * A map of the links by username and collection-name
	 */
	private static Map<String, Map<String, Collection>> links = new HashMap<>();

	public static enum BookmarkManagerResultType implements Messagable {
		SuccessAddLink("Link added successfully!"),
		ListingAllLinks("Listing all links:"),
		ListingCollection("Listing collection's links:"),
		ListingAllTagLinks("Listing all links matching the tags:"),
		ListingAllTitleLinks("Listing all links containing the title:"),
		CollectionAlreadyExists("The collection to make already exists!"),
		CollectionNotExist("The collection you are referring does not exist!"),
		SuccessMakeCollection("Collection created successfully!"),
		SuccessRemoveCollection("Link removed successfully!");

		private String message;

		private BookmarkManagerResultType(String message) {
			this.message = message;
		}

		public String getMessage() {
			return message;
		}
	}

	public synchronized static Messagable addLink(String url) {
		makeCollection(Collection.DEFAULT_NAME);
		return addToCollection(Collection.DEFAULT_NAME, url);
	}
	
	public synchronized static Messagable addToCollection(String collectionName, String url) {
		try {
			AccountManagerResult<String> result = AccountManager.getCurrentUsername();

			if(result.getType() == AccountManagerResultType.NoOneIsLoggedIn) {
				return result.getType();
			}

			UrlManagerResult<Bookmark> bookmarkResult = UrlManager.generateBookmark(url);

			if(bookmarkResult.getType() != UrlManagerResultType.SuccessGenerated) {
				return bookmarkResult.getType();
			}

			String username = result.getValue();
			Bookmark bookmark = bookmarkResult.getValue();

			if(!links.get(username).containsKey(collectionName)) {
				return BookmarkManagerResultType.CollectionNotExist;
			}

			links.get(username).get(collectionName).addBookmark(bookmark);

			StorageManager.storeBookmarks();

			return BookmarkManagerResultType.SuccessAddLink;
		} catch (Throwable e) {
			return UrlManagerResultType.Unexpected;
		}
	}
	
	public synchronized static Messagable removeFromCollection(String collectionName, String url) {
		AccountManagerResult<String> result = AccountManager.getCurrentUsername();

		if(result.getType() == AccountManagerResultType.NoOneIsLoggedIn) {
			return result.getType();
		}

		String username = result.getValue();

		if(!links.get(username).containsKey(collectionName)) {
			return BookmarkManagerResultType.CollectionNotExist;
		}

		links.get(username).get(collectionName).removeBookmark(url);

		StorageManager.storeBookmarks();

		return BookmarkManagerResultType.SuccessRemoveCollection;
	}

	public synchronized static AbstractManagerResult<?, String> listAll() {
		AccountManagerResult<String> usernameResult = AccountManager.getCurrentUsername();

		if(usernameResult.getType() == AccountManagerResultType.NoOneIsLoggedIn) {
			return new AccountManagerResult<String>(usernameResult.getType(), null);
		}

		String username = usernameResult.getValue();
		StringBuilder resultString = new StringBuilder();

		for (String collectionName : links.get(username).keySet()) {
			AbstractManagerResult<?, String> result = listCollection(collectionName);
			resultString.append(result.getValue());
		}

		return new BookmarkManagerResult<String>(BookmarkManagerResultType.ListingAllLinks, resultString.toString());
	}
	
	public synchronized static AbstractManagerResult<?, String> listCollection(String collectionName) {
		AccountManagerResult<String> usernameResult = AccountManager.getCurrentUsername();

		if(usernameResult.getType() == AccountManagerResultType.NoOneIsLoggedIn) {
			return new AccountManagerResult<String>(usernameResult.getType(), null);
		}

		String username = usernameResult.getValue();

		StringBuilder result = new StringBuilder();

		Collection collection = links.get(username).get(collectionName);
			
		List<Bookmark> bookmarks = collection.getBookmarks();
		result.append("\nCollection ").append(collectionName).append(":");

		for (int i = 0; i < bookmarks.size(); i++) {
			Bookmark link = bookmarks.get(i);
			result.append("\n").append(i+1).append(". ").append(link);
		}

		return new BookmarkManagerResult<String>(BookmarkManagerResultType.ListingCollection, result.toString());
	}
	
	public synchronized static AbstractManagerResult<?, String> searchByTags(List<String> tags) {
		AccountManagerResult<String> usernameResult = AccountManager.getCurrentUsername();

		if(usernameResult.getType() == AccountManagerResultType.NoOneIsLoggedIn) {
			return new AccountManagerResult<String>(usernameResult.getType(), null);
		}

		String username = usernameResult.getValue();

		// get all bookmarks flattened
		List<Bookmark> bookmarks = links.get(username).values().stream()
				.flatMap(coll -> coll.getBookmarks().stream())
				.collect(Collectors.toList());

		Map<Bookmark, Double> matchScores = new HashMap<>();

		// map the bookmarks by their
		// match score for the given tags
		for(Bookmark bookmark : bookmarks) {
			Double score = calculateSocre(bookmark, tags);
			if(score > 0.0) { // if no match, don't include
				matchScores.put(bookmark, calculateSocre(bookmark, tags));
			}
		}

		Map<Double, List<Bookmark>> sortedByScore = new TreeMap<>(Collections.reverseOrder());

		// sort bookmarks by their score
		for(Map.Entry<Bookmark, Double> bookmark : matchScores.entrySet()) {
			if(!sortedByScore.containsKey(bookmark.getValue())) {
				sortedByScore.put(bookmark.getValue(), new ArrayList<Bookmark>());
			}
			
			sortedByScore.get(bookmark.getValue()).add(bookmark.getKey());
		}

		StringBuilder resultList = new StringBuilder();

		// prepare final string to return
		int i = 0;
		for(Entry<Double, List<Bookmark>> scoreBookmarks : sortedByScore.entrySet()) {
			for(Bookmark scoreBookmark : scoreBookmarks.getValue()) {
				resultList.append('\n').append(i+1)
						.append(" (score: ").append(scoreBookmarks.getKey()).append("): ")
						.append(scoreBookmark);
				i++;
			}
		}

		return new BookmarkManagerResult<String>(BookmarkManagerResultType.ListingAllTagLinks, resultList.toString());
	}
	
	public synchronized static AbstractManagerResult<?, String> searchByTitle(String title) {
		AccountManagerResult<String> usernameResult = AccountManager.getCurrentUsername();

		if(usernameResult.getType() == AccountManagerResultType.NoOneIsLoggedIn) {
			return new AccountManagerResult<String>(usernameResult.getType(), null);
		}

		String username = usernameResult.getValue();

		// get all bookmarks flattened
		List<Bookmark> bookmarks = links.get(username).values().stream()
				.flatMap(coll -> coll.getBookmarks().stream())
				.collect(Collectors.toList());
		
		bookmarks = bookmarks.stream()
				.filter(link -> link.getTitle().toLowerCase().contains(title.toLowerCase()))
				.collect(Collectors.toList());
		
		StringBuilder result = new StringBuilder();

		for (int i = 0; i < bookmarks.size(); i++) {
			Bookmark link = bookmarks.get(i);
			result.append("\n").append(i+1).append(". ").append(link);
		}
		
		return new BookmarkManagerResult<String>(BookmarkManagerResultType.ListingAllTitleLinks, result.toString());
	}

	private static Double calculateSocre(Bookmark bookmark, List<String> tags) {
		double score = 0;

		for (String tag : tags) {
			for(String keyword : bookmark.getKeywords()) {
				if (tag.equals(keyword)) {
					score += FULL_MATCH_SCORE;
					break;
				} else if (keyword.length() >= 3 && tag.length() >= 3 &&
						keyword.substring(0, PARTIAL_MATCH_LENGTH).equals(tag.substring(0, PARTIAL_MATCH_LENGTH))) {
					score += PARTIAL_MATCH_SCORE;
				}
			}
		}

		return score;
	}

	public static Map<String, Map<String, Collection>> getLinks() {
		return links;
	}

	public static void setLinks(Map<String, Map<String, Collection>> links) {
		BookmarkManager.links = links;
	}

	public static Messagable makeCollection(String collectionName) {
		AccountManagerResult<String> result = AccountManager.getCurrentUsername();

		if(result.getType() == AccountManagerResultType.NoOneIsLoggedIn) {
			return result.getType();
		}

		String username = result.getValue();

		if(links.get(username).containsKey(collectionName)) {
			return BookmarkManagerResultType.CollectionAlreadyExists;
		}

		links.get(username).put(collectionName, new Collection(collectionName));

		return BookmarkManagerResultType.SuccessMakeCollection;
	}
}
