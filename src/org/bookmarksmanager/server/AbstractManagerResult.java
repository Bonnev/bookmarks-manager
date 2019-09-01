package org.bookmarksmanager.server;

public abstract class AbstractManagerResult<T extends Messagable, V> implements Messagable, Valuable<V> {
	protected V value;
	protected T type;
	
	@Override
	public V getValue() {
		return value;
	}

	@Override
	public String getMessage() {
		return type.getMessage();
	}

	public T getType() {
		return type;
	}
}
