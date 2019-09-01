package org.bookmarksmanager.server;

import org.bookmarksmanager.account.User;

public class ServerThread extends Thread {
	private User currentUser;

	public ServerThread(Runnable target, String name) {
		super(target, name);
	}

	public User getCurrentUser() {
		return currentUser;
	}

	public void setCurrentUser(User currentUser) {
		this.currentUser = currentUser;
	}
}
