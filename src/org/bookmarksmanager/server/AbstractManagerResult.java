package org.bookmarksmanager.server;

public abstract class AbstractManagerResult implements Messagable, Valuable {
	protected String value;
	
	@Override
	public String getValue() {
		return value;
	}

	@Override
	public abstract String getMessage();

}
