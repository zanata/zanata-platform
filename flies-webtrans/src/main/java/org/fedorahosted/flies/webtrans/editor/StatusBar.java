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
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.widgetideas.client.ProgressBar;
import com.google.gwt.widgetideas.client.ProgressBar.TextFormatter;

public class StatusBar extends Composite implements ClickHandler, MouseOverHandler, MouseOutHandler, HasClickHandlers, HasMouseOverHandlers, HasMouseOutHandlers {
	
	private final static int popupoffset = 35;
	private final ProgressBar bar = new ProgressBar();
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
	
		bar.setTextVisible(true); 
		bar.setMaxProgress(100.0);
		bar.setWidth("200px");
		bar.setProgress(30.0);
		
		panel.add(bar);
		addClickHandler(this);
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

	@Override
	public void onClick(ClickEvent event) {
		// TODO Auto-generated method stub
		if(bar.getTextFormatter()==null) {
			TextFormatter formatter = new TextFormatter() {
				protected String getText(ProgressBar
				bar, double curProgress) {
				return "[50/30/20] ";
				}
				};
			bar.setTextFormatter(formatter);
			bar.setProgress(30.0);
		} else {
			bar.setTextFormatter(null);
			bar.setProgress(30.0);
		}
	}

	@Override
	public HandlerRegistration addClickHandler(ClickHandler handler) {
		// TODO Auto-generated method stub
		return addDomHandler(handler, ClickEvent.getType());
	}

}
