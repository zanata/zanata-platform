package org.fedorahosted.flies.webtrans.client.ui;

import com.google.gwt.user.client.Element;

public class CodeMirror {

	public static native void doHighlight(String text, Element elem)/*-{
		elem.innerHTML = '';
    	$wnd.highlightText(text, elem);
	}-*/;	
}
