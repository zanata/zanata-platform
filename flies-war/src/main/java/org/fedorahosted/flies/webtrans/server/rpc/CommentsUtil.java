package org.fedorahosted.flies.webtrans.server.rpc;

import org.fedorahosted.flies.model.HSimpleComment;

public class CommentsUtil {

	public static String toString(HSimpleComment comment) {
		if (comment == null)
			return null;
		else
			return comment.getComment();
	}

}
