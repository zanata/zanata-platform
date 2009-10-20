package org.fedorahosted.flies.webtrans.client.ui;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.user.client.ui.TextBox;

public class FilterBox extends TextBox {

	private String text;
	
	public FilterBox() {
		addStyleName("gwt-FilterBox-Vacant");
		
		text = " Type to Filter ";
		
	    setText(text);
	    
	    addFocusHandler(new FocusHandler() {

			@Override
			public void onFocus(FocusEvent event) {
				
				if (getText().equals(text)) {
					setText("");
					setStyleName("gwt-FilterBox-Occupied");
				}
				
			}
	    	
	    });
	    
	    addBlurHandler(new BlurHandler() {

			@Override
			public void onBlur(BlurEvent event) {
				
				if (getText().equals("")) {
					setText(text);
					setStyleName("gwt-FilterBox-Vacant");
				}
				
			}
	    	
	    });
	}
}