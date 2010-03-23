package org.fedorahosted.flies.webtrans.client.ui;

import com.google.gwt.user.client.Element;

public class Prettify {
	public static native void doHighlight(Element elem)/*-{
		elem.innerHTML = $wnd.prettyPrintOne(elem.innerHTML)
	}-*/;
}
