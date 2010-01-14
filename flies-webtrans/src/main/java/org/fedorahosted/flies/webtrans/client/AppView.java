package org.fedorahosted.flies.webtrans.client;

import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Widget;

public class AppView extends DockPanel implements AppPresenter.Display {

	public AppView() {
		setSpacing(3);
//		setBorderWidth(1);
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
	public void setMain(Widget main) {
		add(main, DockPanel.CENTER );
		setCellWidth(main, "100%");
	}
	
	@Override
	public void setWest(Widget west) {
		add(west, DockPanel.WEST );
//		setCellWidth(west, "220px");
	}
	
	@Override
	public void setNorth(Widget north) {
		add(north, DockPanel.NORTH );
//		setCellHeight(north, "20px");
	}

	@Override
	public void setSouth(Widget south) {
		add(south, DockPanel.SOUTH );
//		setCellHeight(south, "20px");
	}
	
}
