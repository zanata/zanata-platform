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
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.widgetideas.client.ProgressBar;
import com.google.gwt.widgetideas.client.ProgressBar.TextFormatter;

public class TranslationStatsBar extends Composite implements TranslationStatsBarPresenter.Display,
		ClickHandler, MouseOverHandler, MouseOutHandler, HasTransUnitCount,
		HasClickHandlers, HasMouseOverHandlers, HasMouseOutHandlers {

	public int fuzzy;
	public int untranslated;
	public int translated;
	private final static int popupoffset = 35;
	private final ProgressBar bar;
	private final PopupWindow popupWindow;

	private class PopupWindow extends DecoratedPopupPanel {
		public PopupWindow() {
			super(true);
			this.setWidget(new Label("Status of Translation Unit"));
		}
	}

	public TranslationStatsBar() {
		bar = new ProgressBar();
		bar.setMaxProgress(100.0);
		bar.setWidth("150px");
		refreshProgress();
		initWidget(bar);
		addClickHandler(this);
		addMouseOverHandler(this);
		addMouseOutHandler(this);

		popupWindow = new PopupWindow();

	}

	public double calcCurrentProgress() {
		int fuzzy = getFuzzy();
		int untrans = getUntranslated();
		int trans = getTranslated();
		if (trans < 0 || untrans < 0 || fuzzy < 0
				|| (trans + untrans + fuzzy) == 0) {
			return 0.0;
		} else {
			return ((double) trans) / (fuzzy + untrans + trans) * 100;
		}
	}

	public void refreshProgress() {
		bar.setProgress(calcCurrentProgress());
	}

	@Override
	public void onMouseOver(MouseOverEvent event) {
		int top = bar.getAbsoluteTop();
		int left = bar.getAbsoluteLeft();
		popupWindow.setPopupPosition(left, top - popupoffset);
		popupWindow.show();
	}

	@Override
	public HandlerRegistration addMouseOverHandler(MouseOverHandler handler) {
		return addDomHandler(handler, MouseOverEvent.getType());
	}

	@Override
	public void onMouseOut(MouseOutEvent event) {
		popupWindow.hide();
	}

	@Override
	public HandlerRegistration addMouseOutHandler(MouseOutHandler handler) {
		return addDomHandler(handler, MouseOutEvent.getType());
	}

	private TextFormatter formatter = new TextFormatter() {
		protected String getText(ProgressBar bar, double curProgress) {
			return "[" + getUntranslated() + "/" + getTranslated() + "/"
					+ getFuzzy() + "] ";
		}
	};

	@Override
	public void onClick(ClickEvent event) {
		bar.setTextFormatter( bar.getTextFormatter() == null ? formatter : null);
		refreshProgress();
	}

	@Override
	public HandlerRegistration addClickHandler(ClickHandler handler) {
		return addDomHandler(handler, ClickEvent.getType());
	}

	@Override
	public int getFuzzy() {
		return fuzzy;
	}

	@Override
	public int getTranslated() {
		return translated;
	}

	@Override
	public int getUntranslated() {
		return untranslated;
	}

	@Override
	public void setFuzzy(int fuzzy) {
		this.fuzzy = fuzzy;
		refreshProgress();
	}

	@Override
	public void setStatus(int fuzzy, int translated, int untranslated) {
		this.fuzzy = fuzzy;
		this.translated = translated;
		this.untranslated = untranslated;
		refreshProgress();
	}

	@Override
	public void setTranslated(int translated) {
		this.translated = translated;
		refreshProgress();
	}

	@Override
	public void setUntranslated(int untranslated) {
		this.untranslated = untranslated;
		refreshProgress();
	}

	@Override
	public Widget asWidget() {
		return this;
	}

	@Override
	public void startProcessing() {
	}

	@Override
	public void stopProcessing() {
	}
	
}
