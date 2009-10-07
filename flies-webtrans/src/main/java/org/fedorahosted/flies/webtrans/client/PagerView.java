package org.fedorahosted.flies.webtrans.client;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class PagerView extends Composite implements PagerPresenter.Display {

	private final Button firstPage;
	private final TextBox gotoPage;
	private final Button lastPage;
	private final Button nextPage;
	private final Button previousPage;
	private final Label pageCountLabel;
	
	private int pageCount;
	
	public PagerView() {
		HorizontalPanel panel = new HorizontalPanel();
		initWidget(panel);
		firstPage = new Button("&lt;&lt;");
		lastPage = new Button(">>");
		nextPage = new Button(">");
		previousPage = new Button("&lt;");
		gotoPage = new TextBox();
		gotoPage.setText(String.valueOf(pageCount) );
		gotoPage.setMaxLength(8);
		pageCountLabel = new Label("of " + pageCount);
		
		panel.add(firstPage);
		panel.add(previousPage);
		panel.add(gotoPage);
		panel.add(pageCountLabel);
		panel.add(nextPage);
		panel.add(lastPage);
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

	@Override
	public int getPageCount() {
		return pageCount;
	}
	
	public void setPageCount(int pageCount) {
		this.pageCount = pageCount;
	}
	
	@Override
	public HasClickHandlers getFirstPage() {
		return firstPage;
	}

	@Override
	public HasValue<String> getGotoPage() {
		return gotoPage;
	}

	@Override
	public HasClickHandlers getLastPage() {
		return lastPage;
	}

	@Override
	public HasClickHandlers getNextPage() {
		return nextPage;
	}

	@Override
	public HasClickHandlers getPreviousPage() {
		return previousPage;
	}
	
	

}
