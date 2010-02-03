package org.fedorahosted.flies.repository.model;

public class Utility {
	public static String toString(HDocument doc) {
		return String.format("name:%s path:%s docID:%s locale:%s rev:%d", doc.getName(), doc.getPath(), doc.getDocId(), doc.getLocale(), doc.getRevision());
	}
}
