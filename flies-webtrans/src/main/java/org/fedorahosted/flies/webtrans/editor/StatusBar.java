package org.fedorahosted.flies.webtrans.editor;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.widgetideas.client.ProgressBar;

public class StatusBar extends Composite implements ClickHandler,HasClickHandlers {
	private static class PopupWindow extends DecoratedPopupPanel {
		    public PopupWindow() {
		      super(true);
		      setWidget(new Label("Click outside of this popup to close it"));
		    }
	}

	
	public StatusBar() {
		HorizontalPanel panel = new HorizontalPanel();
		initWidget(panel);
		
		ProgressBar bar = new ProgressBar(0.0, 100.0,0.0);
		bar.setProgress(75.0);
		bar.setWidth("200px");
		
		panel.add(bar);
		addClickHandler(this);
	}
	
	@Override
	public void onClick(ClickEvent event) {
		// TODO Auto-generated method stub
		new PopupWindow().show();
	}

	@Override
	public HandlerRegistration addClickHandler(ClickHandler handler) {
		// TODO Auto-generated method stub
		return addDomHandler(handler, ClickEvent.getType());
	}

}
