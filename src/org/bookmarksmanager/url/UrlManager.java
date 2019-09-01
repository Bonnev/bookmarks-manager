package org.bookmarksmanager.url;

import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.bookmarksmanager.bookmark.Bookmark;
import org.bookmarksmanager.server.Messagable;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class UrlManager {
	private final static String[] STOP_WORDS = new String[] {"as","a","an","the","which","to","of","and","on","in","at","where","whereas","what","who","whom",
			"i","you","he","she","it","my","mine","your","yours","her","hers","him","his","its","while","whilst", "why", "can",
			"did", "do", "how", "they", "their", "theirs"};

	private final static String STOP_WORDS_REGEX = Arrays.stream(STOP_WORDS).map(word -> String.format("\\b%s\\b", word)).collect(Collectors.joining("|"));
	
	private final static int MAX_WORD_COUNT = 300;

	public static enum UrlManagerResultType implements Messagable {
		SuccessGenerated("Bookmark successfully generated!"),
		LinkInvalid("Given link is invalid!"),
		LinkMalformed("Given link is malformed!"),
		HttpStatusWrong("The received HTTP status code was not 200!"),
		Unexpected("An unexpected error has occurred!");

		private String message;

		private UrlManagerResultType(String message) {
			this.message = message;
		}

		public String getMessage() {
			return message;
		}
	}
	
	public synchronized static UrlManagerResult<Bookmark> generateBookmark(String url) {
		try {
			Document document = Jsoup.connect(url).get(); 

			List<String> keywords = extractKeywords(document);

			Bookmark bookmark = new Bookmark(url, document.title(), keywords);

			return new UrlManagerResult<Bookmark>(UrlManagerResultType.SuccessGenerated, bookmark);
		} catch (MalformedURLException e) {
			return new UrlManagerResult<Bookmark>(UrlManagerResultType.LinkMalformed, null);
		} catch (UnknownHostException e) {
			return new UrlManagerResult<Bookmark>(UrlManagerResultType.LinkInvalid, null);
		} catch (HttpStatusException e) {
			return new UrlManagerResult<Bookmark>(UrlManagerResultType.HttpStatusWrong, null);
		}catch (Throwable e) {
			return new UrlManagerResult<Bookmark>(UrlManagerResultType.Unexpected, null);
		}
	}

	private static List<String> extractKeywords(Document document) {
		StringBuilder allText = new StringBuilder();
		
		// get all text from tags h1,h2,h3,h4,h5,h6,a,li,p
		Elements textElements = document.select("h1,h2,h3,h4,h5,h6,a,li,p");
		for (Element textElement : textElements) {
			allText.append(' ').append(textElement.text());
		}

		// perform the following operation to the whole string
		String normalized = allText.toString()
				.toLowerCase() // convert to lower case
				.replaceAll("([^a-z]+|\b[a-z]\b|" + STOP_WORDS_REGEX + ")", " ") // remove non-english, single-letter and stop words
				.replaceAll("\\s{1,}", " ") // replace all whitespace sequences with a space
				.replaceAll("\\b([a-z]+)(ed|ing|ly|ment)\\b", "$1") // remove ed|ing|ly|ment
				.trim(); // remove trailing whitespace

		// split into words
		String[] allWords = normalized.split(" ");

		Map<String, Integer> occurrences = new HashMap<>();

		// map by occurrences
		for (String word : allWords) {
			Integer count = occurrences.getOrDefault(word, 0);
			occurrences.put(word, count + 1);
		}

		Map<Integer, List<String>> sortedByOccurrence = new TreeMap<>(Collections.reverseOrder());

		// sort by occurrences
		for (Map.Entry<String, Integer> occurrence : occurrences.entrySet()) {
			if(!sortedByOccurrence.containsKey(occurrence.getValue())) {
				sortedByOccurrence.put(occurrence.getValue(), new ArrayList<String>());
			}
			sortedByOccurrence.get(occurrence.getValue()).add(occurrence.getKey());
		}

		// get first MAX words and store in result
		ArrayList<String> resultWords = new ArrayList<>();
		int current = 0;
		outer: for (List<String> occurrenceWords : sortedByOccurrence.values()) {
			for (String word : occurrenceWords) {
				resultWords.add(word);
				
				current++;
				if(current >= MAX_WORD_COUNT) {
					break outer;
				}
			}
		}

		return resultWords;
	}
}
