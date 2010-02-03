package org.fedorahosted.flies.webtrans.client.ui;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.user.client.ui.TextBox;

public class FilterBox extends TextBox {

	private final String text = " Type to Filter ";
	
	public FilterBox() {
		setStylePrimaryName("gwt-FilterBox");
		clearFilter();
	    
	    addFocusHandler(new FocusHandler() {

			@Override
			public void onFocus(FocusEvent event) {
				
				if (getText().equals(text)) {
					setText("");
					removeStyleDependentName("Vacant");
					addStyleDependentName("Occupied");
				}
				
			}
	    	
	    });
	    
	    addBlurHandler(new BlurHandler() {

			@Override
			public void onBlur(BlurEvent event) {
				
				if (getText().equals(""))
					clearFilter();
			}
	    	
	    });
	}

	public void clearFilter() {
		setText(text);
		removeStyleDependentName("Occupied");
		addStyleDependentName("Vacant");
	}
}