package org.jboss.shotoku.cache.admin;

import java.text.DateFormat;
import java.util.Date;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

public class CacheFacesTools {
	/**
	 * Adds a global faces message, prepending the current time.
	 * @param msg Message to add.
	 */
	public static void addTimedFacesMessage(String msg) {
		FacesContext.getCurrentInstance().addMessage(null,
    			new FacesMessage("(" + DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date()) + "): " + msg, ""));
	}
}
