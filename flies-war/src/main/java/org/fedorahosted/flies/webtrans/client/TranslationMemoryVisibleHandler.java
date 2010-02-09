package org.fedorahosted.flies.webtrans.client;

import com.google.gwt.event.shared.EventHandler;

public interface TranslationMemoryVisibleHandler extends EventHandler{
	void onTransMemorySelected(TranslationMemoryVisibleEvent tabSelectionEvent);
}
