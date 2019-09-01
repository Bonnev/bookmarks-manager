package org.bookmarksmanager.url;

import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bookmarksmanager.account.AccountManager;
import org.bookmarksmanager.account.AccountManager.AccountManagerResultType;
import org.bookmarksmanager.server.AbstractManagerResult;
import org.bookmarksmanager.server.AccountManagerResult;
import org.bookmarksmanager.server.Messagable;
import org.bookmarksmanager.server.ServerThread;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class UrlManager {
	/**
	 * A map of the links by username
	 */
	private static Map<String, List<String>> links = new HashMap<>();
	
	public static enum UrlManagerResultType implements Messagable {
		SuccessAddLink("Link added successfully!"),
		ListingAllLinks("Listing all links:"),
		LinkInvalid("Given link is invalid!"),
		LinkMalformed("Given link is malformed!"),
		Unexpected("An unexpected error has occurred!");

		private String message;

		private UrlManagerResultType(String message) {
			this.message = message;
		}

		public String getMessage() {
			return message;
		}
	}
	
	public synchronized static AbstractManagerResult addLink(String url) {
		Document doc;
		try {
			
			AccountManagerResult result = AccountManager.getCurrentUsername();
			
			if(result.getType() == AccountManagerResultType.NoOneIsLoggedIn) {
				return new AccountManagerResult(result.getType(), null);
			}
			
			String username = result.getValue();

			doc = Jsoup.connect(url).get();


			if(!links.containsKey(username)) {
				links.put(username, new ArrayList<>());
			}

			links.get(username).add(url);
			return new UrlManagerResult(UrlManagerResultType.SuccessAddLink, doc.title());
			/*System.out.println(doc.title());
			Elements newsHeadlines = doc.select("h1,h2,h3,h4,h5,h6,a,li,p");
			for (Element headline : newsHeadlines) {
				System.out.printf("\t%s%n", headline.text());
			}*/
		} catch (MalformedURLException e) {
			return new UrlManagerResult(UrlManagerResultType.LinkMalformed, null);
		} catch (UnknownHostException e) {
			return new UrlManagerResult(UrlManagerResultType.LinkInvalid, null);
		} catch (Throwable e) {
			return new UrlManagerResult(UrlManagerResultType.Unexpected, null);
		}
	}
	
	public synchronized static UrlManagerResult listAll() {
		ServerThread currentThread = (ServerThread) Thread.currentThread();
		String username = currentThread.getCurrentUser().getUsername();

		StringBuilder result = new StringBuilder();
		List<String> allLinks = links.get(username);

		result.append(1).append(". ").append(allLinks.get(0));

		for (int i = 1; i < allLinks.size(); i++) {
			String link = allLinks.get(i);
			result.append("\n").append(i+1).append(". ").append(link);
		}

		return new UrlManagerResult(UrlManagerResultType.ListingAllLinks, result.toString());
	}
}
