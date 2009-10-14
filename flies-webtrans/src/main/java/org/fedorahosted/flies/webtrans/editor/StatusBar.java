package org.fedorahosted.flies.webtrans.editor;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasMouseOutHandlers;
import com.google.gwt.event.dom.client.HasMouseOverHandlers;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.widgetideas.client.ProgressBar;

public class StatusBar extends Composite implements MouseOverHandler, MouseOutHandler, HasMouseOverHandlers, HasMouseOutHandlers {
	
	private final static int popupoffset = 35;
	private final ProgressBar bar;
	private PopupWindow popupWindow;
	
	private static class PopupWindow extends DecoratedPopupPanel {
		    public PopupWindow() {
		      super(true);
		      setWidget(new Label("Status of Translation Unit"));
		    }
	}

	
	public StatusBar() {
		HorizontalPanel panel = new HorizontalPanel();
		initWidget(panel);
		
		bar = new ProgressBar(0.0, 100.0,0.0);
		bar.setProgress(75.0);
		bar.setWidth("200px");
		
		panel.add(bar);
		addMouseOverHandler(this);
		addMouseOutHandler(this);
	}
	
	@Override
	public void onMouseOver(MouseOverEvent event) {
		// TODO Auto-generated method stub
		popupWindow = new PopupWindow();
		int top = bar.getAbsoluteTop();
	    int left = bar.getAbsoluteLeft();
		popupWindow.setPopupPosition(left, top-popupoffset);
		popupWindow.show();
	}

	@Override
	public HandlerRegistration addMouseOverHandler(MouseOverHandler handler) {
		// TODO Auto-generated method stub
		return addDomHandler(handler, MouseOverEvent.getType());
	}

	@Override
	public void onMouseOut(MouseOutEvent event) {
		// TODO Auto-generated method stub
		popupWindow.hide();
	}

	@Override
	public HandlerRegistration addMouseOutHandler(MouseOutHandler handler) {
		// TODO Auto-generated method stub
		return addDomHandler(handler, MouseOutEvent.getType());
	}

}
