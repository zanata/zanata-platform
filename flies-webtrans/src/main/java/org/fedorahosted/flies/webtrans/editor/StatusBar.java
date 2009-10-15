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

public class StatusBar extends Composite implements HasTransUnitCount, ClickHandler, MouseOverHandler, MouseOutHandler, HasClickHandlers, HasMouseOverHandlers, HasMouseOutHandlers {
	
	public int fuzzy;
	public int untranslated;
	public int translated;
	private final static int popupoffset = 35;
	private final ProgressBar bar = new ProgressBar();
	private PopupWindow popupWindow;
	
	private class PopupWindow extends DecoratedPopupPanel {
		    public PopupWindow() {
		      super(true);
		      this.setWidget(new Label("Status of Translation Unit"));
		    }
	}

	
	public StatusBar() {
		setFuzzy(20);
		setUntranslated(50);
		setTranslated(30);
		
		HorizontalPanel panel = new HorizontalPanel();
		initWidget(panel);
	    		
		bar.setTextVisible(true); 
		bar.setMaxProgress(100.0);
		bar.setWidth("200px");
		bar.setProgress(calcCurrentProgress());
		
		panel.add(bar);
		addClickHandler(this);
		addMouseOverHandler(this);
		addMouseOutHandler(this);
	}
	
	private double calcCurrentProgress() {
		// TODO Auto-generated method stub
		int fuzzy = getFuzzy();
		int untrans = getUntranslated();
		int trans = getTranslated();
		if( trans < 0 || untrans < 0 || fuzzy < 0 || (trans+untrans+fuzzy) == 0) {
			return 0.0;
		} else {
			return ((double) trans)/(fuzzy+untrans+trans)*100;
		}
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
				return "["+getUntranslated()+"/"+getTranslated()+"/"+getFuzzy()+"] ";
				}
				};
			bar.setTextFormatter(formatter);
			bar.setProgress(calcCurrentProgress());
		} else {
			bar.setTextFormatter(null);
			bar.setProgress(calcCurrentProgress());
		}
	}

	@Override
	public HandlerRegistration addClickHandler(ClickHandler handler) {
		// TODO Auto-generated method stub
		return addDomHandler(handler, ClickEvent.getType());
	}

	@Override
	public int getFuzzy() {
		// TODO Auto-generated method stub
		return fuzzy;
	}

	@Override
	public int getTranslated() {
		// TODO Auto-generated method stub
		return translated;
	}

	@Override
	public int getUntranslated() {
		// TODO Auto-generated method stub
		return untranslated;
	}

	@Override
	public void setFuzzy(int fuzzy) {
		// TODO Auto-generated method stub
		this.fuzzy = fuzzy;
	}

	@Override
	public void setStatus(int fuzzy, int translated, int untranslated) {
		// TODO Auto-generated method stub
		this.fuzzy = fuzzy;
		this.translated = translated;
		this.untranslated = untranslated;
	}

	@Override
	public void setTranslated(int translated) {
		// TODO Auto-generated method stub
		this.translated = translated;
	}

	@Override
	public void setUntranslated(int untranslated) {
		// TODO Auto-generated method stub
		this.untranslated = untranslated;
	}

}
