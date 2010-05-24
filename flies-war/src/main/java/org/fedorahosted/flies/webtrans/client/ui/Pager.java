package org.fedorahosted.flies.webtrans.client.ui;
import org.fedorahosted.flies.webtrans.client.Resources;
import org.fedorahosted.flies.webtrans.client.WebTransMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

public class Pager extends Composite implements HasPager {

	private static PagerUiBinder uiBinder = GWT.create(PagerUiBinder.class);
	
	interface PagerUiBinder extends UiBinder<HTMLPanel, Pager> {
	}
	
	@UiField
	Anchor firstPage, lastPage, nextPage, prevPage;

	@UiField
	Image firstPageDisabled, lastPageDisabled, nextPageDisabled, prevPageDisabled;
	
	@UiField
	TextBox gotoPage;
	
	@UiField
	Label pageCountLabel;
	
	@UiField(provided=true) 
	Resources resources;
	
	private int pageCount = PAGECOUNT_UNKNOWN;
	private int currentPage;
	
	public static final int PAGECOUNT_UNKNOWN = -1;
	
	private final WebTransMessages messages;
	
	public Pager(final WebTransMessages messages, final Resources resources) {
		this.messages = messages;
		this.resources = resources;
		initWidget(uiBinder.createAndBindUi(this));

		// set tooltips of page nav icons, i18n-ized w/ WebTransMessages.java
		firstPage.setTitle( messages.tooltipsWithShortcut( messages.firstPage(), messages.firstPageShortcut() ) );
		prevPage.setTitle( messages.tooltipsWithShortcut( messages.prevPage(), messages.prevPageShortcut() ) );
		nextPage.setTitle( messages.tooltipsWithShortcut( messages.nextPage(), messages.nextPageShortcut() ) );
		lastPage.setTitle( messages.tooltipsWithShortcut( messages.lastPage(), messages.lastPageShortcut() ) );
	}

	@Override
	protected void onLoad() {
		super.onLoad();
		gotoPage.addKeyUpHandler(new KeyUpHandler() {
			
			@Override
			public void onKeyUp(KeyUpEvent event) {
				if( event.getNativeKeyCode() == KeyCodes.KEY_ENTER ) {
					try{
						int newValue = Integer.parseInt( gotoPage.getText() );
						setValue(newValue);
					}
					catch(NumberFormatException nfe){}
				}
			}
		});

		firstPage.addClickHandler(clickHandler);
		lastPage.addClickHandler(clickHandler);
		prevPage.addClickHandler(clickHandler);
		nextPage.addClickHandler(clickHandler);
		refresh();
	}
	
	private void refresh(){
		String page = pageCount == PAGECOUNT_UNKNOWN ? "" : "of " + pageCount;
		pageCountLabel.setText(page);
		setEnabled(firstPage, firstPageDisabled, currentPage != 1);
		setEnabled(prevPage, prevPageDisabled, currentPage != 1);
		setEnabled(nextPage, nextPageDisabled, currentPage != pageCount );
		setEnabled(lastPage, lastPageDisabled, currentPage != pageCount && pageCount != PAGECOUNT_UNKNOWN);

		gotoPage.setText( String.valueOf(currentPage) );
	}
	
	@Override
	public void setPageCount(int pageCount) {
		this.pageCount = pageCount;
		refresh();
	}
	
	@Override
	public int getPageCount() {
		return pageCount;
	}

	@Override
	public Integer getValue() {
		return currentPage;
	}

	@Override
	public void setValue(Integer value) {
		setValue(value, true);
	}

	@Override
	public void setValue(Integer value, boolean fireEvents) {
		if(value != this.currentPage){
			this.currentPage = value;
			if(fireEvents) {
				ValueChangeEvent.fire(this, value);
			}
			refresh();
		}
	}

	@Override
	public HandlerRegistration addValueChangeHandler(
			ValueChangeHandler<Integer> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}

	private final ClickHandler clickHandler = new ClickHandler() {
		
		@Override
		public void onClick(ClickEvent event) {
			if(event.getSource() == firstPage) {
				setValue(1);
			}
			else if(event.getSource() == lastPage) {
				setValue(pageCount);
			} 
			else if(event.getSource() == nextPage) {
				setValue(currentPage +1);
			} 
			else if(event.getSource() == prevPage) {
				setValue(currentPage -1);
			} 
		}
	};
	
	private void setEnabled(Anchor link, Image disabledLink, boolean enabled) {
		link.setVisible(enabled);
		disabledLink.setVisible(!enabled);
	}
}
