package org.fedorahosted.flies.webtrans.client;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

/**
 * Resources used by the entire application.
 */
public interface Resources extends ClientBundle {

	// @Source("Style.css")
	// Style style();

	@Source("flies_logo_small.png")
	ImageResource logo();

}