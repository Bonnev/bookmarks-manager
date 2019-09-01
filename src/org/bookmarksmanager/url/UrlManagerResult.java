package org.bookmarksmanager.url;

import org.bookmarksmanager.server.AbstractManagerResult;
import org.bookmarksmanager.url.UrlManager.UrlManagerResultType;

public class UrlManagerResult extends AbstractManagerResult {
	private UrlManagerResultType type;

	public UrlManagerResult(UrlManagerResultType type, String value) {
		this.type = type;
		this.value = value;
	}

	@Override
	public String getMessage() {
		return type.getMessage();
	}

	public UrlManagerResultType getType() {
		return type;
	}
}
