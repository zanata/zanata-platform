package com.weborient.codemirror.client;

import com.google.gwt.user.client.ui.MouseListener;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author samangiahi
 *
 */
public abstract class AbstractIconMouseListener implements MouseListener {

	public abstract void onMouseOver(Widget sender);
	public abstract void onMouseOut(Widget sender);
	
	public void onMouseEnter(Widget sender) {
		onMouseOver(sender);
	}

	public void onMouseLeave(Widget sender) {
		onMouseOut(sender);
	}

	public void onMouseDown(Widget sender, int x, int y) {
		//TODO implement mouse down functionality, if needed
	}
	
	public void onMouseMove(Widget sender, int x, int y) {
		//TODO implement mouse move functionality, if needed
	}
	
	public void onMouseUp(Widget sender, int x, int y) {
		//TODO implement mouse up functionality, if needed
	}

}
