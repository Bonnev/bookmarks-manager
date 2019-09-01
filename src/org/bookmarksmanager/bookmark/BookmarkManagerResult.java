package org.bookmarksmanager.bookmark;

import org.bookmarksmanager.bookmark.BookmarkManager.BookmarkManagerResultType;
import org.bookmarksmanager.server.AbstractManagerResult;

public class BookmarkManagerResult <V> extends AbstractManagerResult<BookmarkManagerResultType, V> {
	public BookmarkManagerResult(BookmarkManagerResultType type, V value) {
		this.type = type;
		this.value = value;
	}
}
