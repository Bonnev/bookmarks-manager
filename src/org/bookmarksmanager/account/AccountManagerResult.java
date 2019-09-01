package org.bookmarksmanager.account;

import org.bookmarksmanager.account.AccountManager.AccountManagerResultType;
import org.bookmarksmanager.server.AbstractManagerResult;

public class AccountManagerResult <V> extends AbstractManagerResult<AccountManagerResultType, V> {
	public AccountManagerResult(AccountManagerResultType type, V value) {
		this.type = type;
		this.value = value;
	}
}
