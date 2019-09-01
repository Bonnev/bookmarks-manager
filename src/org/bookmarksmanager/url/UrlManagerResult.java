package org.bookmarksmanager.url;

import org.bookmarksmanager.server.AbstractManagerResult;
import org.bookmarksmanager.url.UrlManager.UrlManagerResultType;

public class UrlManagerResult <V> extends AbstractManagerResult<UrlManagerResultType, V> {
	public UrlManagerResult(UrlManagerResultType type, V value) {
		this.type = type;
		this.value = value;
	}
}
