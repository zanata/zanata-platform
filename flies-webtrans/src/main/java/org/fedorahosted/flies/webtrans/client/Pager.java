package org.fedorahosted.flies.webtrans.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

public class Pager extends Composite implements HasPageCount, HasValue<Integer>, HasValueChangeHandlers<Integer>{

	private final Button firstPage;
	private final TextBox gotoPage;
	private final Button lastPage;
	private final Button nextPage;
	private final Button previousPage;
	private final Label pageCountLabel;
	
	private int pageCount;
	private int currentPage;
	
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

	public Pager() {
		HorizontalPanel panel = new HorizontalPanel();
		initWidget(panel);
		firstPage = new Button("&lt;&lt;");
		lastPage = new Button(">>");
		nextPage = new Button(">");
		previousPage = new Button("&lt;");
		gotoPage = new TextBox();
		gotoPage.setMaxLength(8);
		pageCountLabel = new Label(" of 0");
		
		panel.add(firstPage);
		panel.add(previousPage);
		panel.add(gotoPage);
		panel.add(pageCountLabel);
		panel.add(nextPage);
		panel.add(lastPage);
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
	}
	
	private void refresh(){
		pageCountLabel.setText("of "+ pageCount);
		firstPage.setEnabled( currentPage != 1);
		previousPage.setEnabled( currentPage != 1);
		nextPage.setEnabled( currentPage != pageCount );
		lastPage.setEnabled( currentPage != pageCount);
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
	
}
