package org.fedorahosted.flies.webtrans.client.ui;
import org.fedorahosted.flies.webtrans.client.Resources;
import org.fedorahosted.flies.webtrans.client.WebTransMessages;
import org.fedorahosted.flies.webtrans.editor.table.NavigationMessages;

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

	interface PagerUiBinder extends UiBinder<HTMLPanel, Pager> {
	}
	
	private static PagerUiBinder uiBinder = GWT.create(PagerUiBinder.class);
	
	@UiField
	Anchor firstPage, lastPage, nextPage, prevPage;

	@UiField
	Image firstPageImage, prevPageImage, nextPageImage, lastPageImage;
	
	@UiField
	Label firstPageDisabled, lastPageDisabled, nextPageDisabled, previousPageDisabled;
	
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
		firstPageDisabled.setVisible(false);
		lastPageDisabled.setVisible(false);
		nextPageDisabled.setVisible(false);
		previousPageDisabled.setVisible(false);

		firstPage.setText( messages.firstPage() );

		firstPage.setTitle( messages.firstPageShortcut() );
		firstPageDisabled.setText( messages.firstPage() );
		
		prevPage.setText( messages.prevPage() );
		prevPage.setTitle( messages.prevPageShortcut() );
		previousPageDisabled.setText( messages.prevPage() );
		
		nextPage.setText( messages.nextPage() );
		nextPage.setTitle( messages.nextPageShortcut() );
		nextPageDisabled.setText( messages.nextPage() );
		
		lastPage.setText( messages.lastPage() );
		lastPage.setTitle( messages.lastPageShortcut() );
		lastPageDisabled.setText( messages.lastPage() );
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

	private void setEnabled(Anchor link, Label diabledLabel, boolean enabled) {
		link.setVisible(enabled);
		diabledLabel.setVisible(!enabled);
	}
	
	private void refresh(){
		String page = pageCount == PAGECOUNT_UNKNOWN ? "" : "of " + pageCount;
		pageCountLabel.setText(page);
		setEnabled(firstPage, firstPageDisabled, currentPage != 1);
		setEnabled(prevPage, previousPageDisabled, currentPage != 1);

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
			ValueChangeEvent.fire(this, value);
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

}
