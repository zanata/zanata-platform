package org.jboss.shotoku.tags;

import java.util.StringTokenizer;
import java.util.Vector;

public class TagSearch {
	Vector<String> tagRequestTokens;

	Vector<String> tagForbiddenTokens;

	Vector<String> tagRequiredTokens;

	Vector<String> authorRequestTokens;

	Vector<String> authorForbiddenTokens;

	Vector<String> authorRequiredTokens;

	Vector<String> keywordRequestTokens;

	Vector<String> keywordForbiddenTokens;

	Vector<String> keywordRequiredTokens;

	/**
	 * Construtor for search. Use nulls or zero length strings if you don't want
	 * some of the rules to be checked.
	 * 
	 * @param tagString
	 * @param authorString
	 * @param keywordString
	 */
	public TagSearch(String tagString, String authorString, String keywordString) {
		if (checkQuery(tagString)) {
			tagRequiredTokens = new Vector<String>();
			tagForbiddenTokens = new Vector<String>();
			tagRequestTokens = new Vector<String>();
			tokenizeString(tagString.trim(), tagRequiredTokens,
					tagForbiddenTokens, tagRequestTokens);
		}

		if (checkQuery(authorString)) {
			authorRequiredTokens = new Vector<String>();
			authorForbiddenTokens = new Vector<String>();
			authorRequestTokens = new Vector<String>();
			tokenizeString(authorString.trim(), authorRequiredTokens,
					authorForbiddenTokens, authorRequestTokens);
		}

		if (checkQuery(keywordString)) {
			keywordRequiredTokens = new Vector<String>();
			keywordForbiddenTokens = new Vector<String>();
			keywordRequestTokens = new Vector<String>();
			tokenizeString(keywordString.trim(), keywordRequiredTokens,
					keywordForbiddenTokens, keywordRequestTokens);
		}
	}

	private boolean checkQuery(String query) {
		return query != null && query.trim().length() > 0;
	}

	private void tokenizeString(String query, Vector<String> requiredTokens,
			Vector<String> forbiddenTokens, Vector<String> requestTokens) {

		if (query != null && query.length() > 0) {

		} else {
			return;
		}

		StringTokenizer st = new StringTokenizer(query, " \t,");

		//
		// Parse incoming search string
		//

		while (st.hasMoreTokens()) {
			String token = st.nextToken().toLowerCase();

			switch (token.charAt(0)) {
			case '+':
				token = token.substring(1);
				requiredTokens.add(token);
				break;

			case '-':
				token = token.substring(1);
				forbiddenTokens.add(token);
				break;

			default:
				requestTokens.add(token);
				break;
			}
		}
	}

	public boolean matches(Tag tag) {

		return matchTokens(tag.getAuthor(), authorRequiredTokens,
				authorForbiddenTokens, authorRequestTokens)
				&& matchTokens(tag.getName(), tagRequiredTokens,
						tagForbiddenTokens, tagRequestTokens)
				&& matchTokens(tag.getData(), keywordRequiredTokens,
						keywordForbiddenTokens, keywordRequestTokens);
	}

	private boolean matchTokens(String content, Vector<String> requiredTokens,
			Vector<String> forbiddenTokens, Vector<String> requestTokens) {

		try {
			boolean ret = false;

			if (requestTokens == null) {
				return true;
			}

			for (int i = 0; i < forbiddenTokens.size(); i++) {
				if (content.indexOf(forbiddenTokens.get(i)) != -1) {
					// there is forbidden token - return 0
					return false;
				}
			}

			for (int i = 0; i < requiredTokens.size(); i++) {
				if (content.indexOf(requiredTokens.get(i)) == -1) {
					// lack of one of the required tokens - return 0
					return false;
				} else {
					ret = true;
				}
			}

			/*
			 * if (requiredTokens.size() > 0) { // there is at least one
			 * requirted token and it has been found return true; }
			 */

			for (int i = 0; i < requestTokens.size(); i++) {
				int fromIndex = -1;

				while ((fromIndex = content.indexOf(requestTokens.get(i),
						fromIndex + 1)) != -1) {
					// found at least one required token
					return true;
				}
			}

			// nothing found - maybe there was a only required tokens search
			return ret;
		} catch (NullPointerException e) {
			// some of compared strings was null. This shuldn't happen,
			// therefore return false
			return false;
		}
	}
}
