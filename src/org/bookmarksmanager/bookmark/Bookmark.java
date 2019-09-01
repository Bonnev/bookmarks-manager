package org.bookmarksmanager.bookmark;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * A model for a bookmark
 *
 * @author Bonnev
 */
public class Bookmark {
	private String link;
	private String title;
	private Set<String> keywords;

	public Bookmark(String link, String title, Collection<String> keywords) {
		this.link = link;
		this.title = title;
		this.keywords = new HashSet<>(keywords);
	}

	public String getLink() {
		return link;
	}

	public String getTitle() {
		return title;
	}

	public Set<String> getKeywords() {
		return keywords;
	}

	@Override
	public String toString() {
		return String.format("%s [%s]", title, link);
	}
}
