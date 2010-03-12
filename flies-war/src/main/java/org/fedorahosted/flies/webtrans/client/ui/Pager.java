package org.fedorahosted.flies.webtrans.client.ui;
import org.fedorahosted.flies.webtrans.client.Resources;
import org.fedorahosted.flies.webtrans.editor.table.NavigationConsts;

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
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

public class Pager extends Composite implements HasPager {

	interface PagerUiBinder extends UiBinder<HTMLPanel, Pager> {
	}

	private static PagerUiBinder uiBinder = GWT.create(PagerUiBinder.class);
	
	@UiField
	Anchor firstPage, lastPage, nextPage, previousPage;

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
	
	public Pager(Resources resources) {
		this.resources = resources;
		initWidget(uiBinder.createAndBindUi(this));
		firstPageDisabled.setVisible(false);
		lastPageDisabled.setVisible(false);
		nextPageDisabled.setVisible(false);
		previousPageDisabled.setVisible(false);

		firstPage.setText(NavigationConsts.FIRST_PAGE_DESC);
		// TODO This is preliminary, until the ShortcutRegistry is created.
		// Then we should set "title" attribute in the xml, and maybe something
		// like ShortcutRegistry.register(firstPage.getText(), firstPage.getTitle())
		firstPage.setTitle(NavigationConsts.FIRST_PAGE_SHORTCUT);
		firstPageDisabled.setText(NavigationConsts.FIRST_PAGE_DESC);
		
		previousPage.setText(NavigationConsts.PREV_PAGE_DESC);
		previousPage.setTitle(NavigationConsts.PREV_PAGE_SHORTCUT);
		previousPageDisabled.setText(NavigationConsts.PREV_PAGE_DESC);
		
		nextPage.setText(NavigationConsts.NEXT_PAGE_DESC);
		nextPage.setTitle(NavigationConsts.NEXT_PAGE_SHORTCUT);
		nextPageDisabled.setText(NavigationConsts.NEXT_PAGE_DESC);
		
		lastPage.setText(NavigationConsts.LAST_PAGE_DESC);
		lastPage.setTitle(NavigationConsts.LAST_PAGE_SHORTCUT);
		lastPageDisabled.setText(NavigationConsts.LAST_PAGE_DESC);
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
		previousPage.addClickHandler(clickHandler);
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
		setEnabled(previousPage, previousPageDisabled, currentPage != 1);

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
			else if(event.getSource() == previousPage) {
				setValue(currentPage -1);
			} 
		}
	};

}
