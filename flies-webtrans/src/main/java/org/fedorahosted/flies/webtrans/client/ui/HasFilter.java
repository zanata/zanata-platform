package org.fedorahosted.flies.webtrans.client.ui;

import java.util.List;

public interface HasFilter<T> {
	/**
	 * Sets the complete list of objects which will be displayed by the 
	 * component, subject to any active filter.
	 * @param list
	 */
	public void setList(List<T> completeList);
}
