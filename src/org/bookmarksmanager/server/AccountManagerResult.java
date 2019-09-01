package org.bookmarksmanager.server;

import org.bookmarksmanager.account.AccountManager.AccountManagerResultType;

public class AccountManagerResult extends AbstractManagerResult {
	private AccountManagerResultType type;

	public AccountManagerResult(AccountManagerResultType type, String value) {
		this.type = type;
		this.value = value;
	}

	@Override
	public String getMessage() {
		return type.getMessage();
	}

	public AccountManagerResultType getType() {
		return type;
	}
}
