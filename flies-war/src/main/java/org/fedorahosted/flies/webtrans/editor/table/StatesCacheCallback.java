package org.fedorahosted.flies.webtrans.editor.table;

import org.fedorahosted.flies.common.ContentState;

public interface StatesCacheCallback {
	void nextFuzzy(ContentState state);
	void prevFuzzy(ContentState state);
}
