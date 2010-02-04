package com.weborient.codemirror.client;

import com.google.gwt.core.client.GWT;

/**
 * @author samangiahi
 *
 */
public interface Constants {
	
	public String baseDir = GWT.getModuleBaseURL(); 
	// ICONS
	public final static String TOOLBAR_UNDO_BUTTON_ICON = baseDir + "images/toolbar_undo_button.png"; 
	public final static String UNDO_BUTTON_HOVER_ICON = baseDir + "images/undo_button_hover.png";
	
	public final static String TOOLBAR_REFRESH_BUTTON_ICON = baseDir + "images/toolbar_refresh_button.png"; 
	public final static String REFRESH_BUTTON_HOVER_ICON = baseDir + "images/refresh_button_hover.png";
	
	public final static String TOOLBAR_REDO_BUTTON_ICON = baseDir + "images/toolbar_redo_button.png";
	public final static String REDO_BUTTON_HOVER_ICON = baseDir + "images/redo_button_hover.png";
	
	//TOOLBAR
	public final static String TOOLBAR_HEIGHT = "30px";
	public final static String TOOLBAR_WIDTH = "100%";
}
