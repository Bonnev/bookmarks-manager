package org.bookmarksmanager.bookmark;

import java.util.ArrayList;
import java.util.List;

/**
 * A model for a collection of bookmarks
 *
 * @author Bonnev
 */
public class Collection {
	private String name;
	
	private List<Bookmark> bookmarks;

	public Collection(String name) {
		this.name = name;
		bookmarks = new ArrayList<>();
	}

	public void addBookmark(Bookmark bookmark) {
		bookmarks.add(bookmark);
	}
	
	public void removeBookmark(String url) {
		for (int i = 0; i < bookmarks.size(); i++) {
			if(bookmarks.get(i).getLink().equals(url)) {
				bookmarks.remove(i);
				break;
			}
		}
	}

	public List<Bookmark> getBookmarks() {
		return bookmarks;
	}

	public String getName() {
		return name;
	}
}
