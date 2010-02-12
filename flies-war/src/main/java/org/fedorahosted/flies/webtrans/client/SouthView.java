package org.fedorahosted.flies.webtrans.client;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

public class SouthView implements SouthPresenter.Display {
	private static final String PANEL_HEIGHT = "150px";
	private final DisclosurePanel disclosurePanel = new DisclosurePanel("Translation Memory Tools");
	private final TabPanel tabPanel = new TabPanel();
	private final FlowPanel transPanel = new FlowPanel();
	private final TextArea glossary = new TextArea();
	private final HasValueChangeHandlers<Boolean> tmVisibility = new HasValueChangeHandlers<Boolean>() {
		HandlerManager handlerManager = new HandlerManager(this);
		@Override
		public void fireEvent(GwtEvent<?> event) {
			handlerManager.fireEvent(event);
		}
		@Override
		public HandlerRegistration addValueChangeHandler(
				ValueChangeHandler<Boolean> handler) {
			return handlerManager.addHandler(ValueChangeEvent.getType(), handler);
		}
	};
	private boolean tmSelected;
	
	public SouthView() {
		disclosurePanel.setWidth("100%");
		disclosurePanel.setOpen(false);
		transPanel.setHeight(PANEL_HEIGHT);
		tabPanel.add(new ScrollPanel(transPanel), "Translation Memory");
		glossary.setText("glossary............................................................\nglossary\nglossary");
		glossary.setHeight(PANEL_HEIGHT);
		tabPanel.add(glossary, "Glossary");
		disclosurePanel.add(tabPanel);
		tabPanel.setWidth("100%");
		final int tmTabIndex = tabPanel.getWidgetIndex(transPanel);
		tabPanel.addSelectionHandler(new SelectionHandler<Integer>() {
			@Override
			public void onSelection(SelectionEvent<Integer> event) {
				boolean tmSelectedNewValue = event.getSelectedItem() == tmTabIndex;
				Log.debug("tab selected: fireIfNotEqual visibility="+tmSelectedNewValue);
				ValueChangeEvent.fireIfNotEqual(tmVisibility, tmSelected, tmSelectedNewValue);
				tmSelected = tmSelectedNewValue;
			}
		});
		disclosurePanel.addOpenHandler(new OpenHandler<DisclosurePanel>() {
			@Override
			public void onOpen(OpenEvent<DisclosurePanel> event) {
				Log.debug("disclosure panel opened: fireIfNotEqual visibility="+tmSelected);
				ValueChangeEvent.fireIfNotEqual(tmVisibility, false, tmSelected);
			}
		});
		disclosurePanel.addCloseHandler(new CloseHandler<DisclosurePanel>() {
			@Override
			public void onClose(CloseEvent<DisclosurePanel> event) {
				Log.debug("disclosure panel closed: fireIfNotEqual visibility="+false);
				ValueChangeEvent.fireIfNotEqual(tmVisibility, tmSelected, false);
			}
		});
		//transPanel is set default
		this.tmSelected = true;
		tabPanel.selectTab(tmTabIndex);
	}

	@Override
	public Widget asWidget() {
		return disclosurePanel;
	}

	@Override
	public void startProcessing() {
	}

	@Override
	public void stopProcessing() {
	}

	@Override
	public HasText getGlossary() {
		return glossary;
	}

	@Override
	public HasWidgets getWidgets() {
		return transPanel;
	}

	@Override
	public HasValueChangeHandlers<Boolean> getTMVisibility() {
		return tmVisibility;
	}
}
