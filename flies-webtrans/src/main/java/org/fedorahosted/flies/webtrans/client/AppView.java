package org.fedorahosted.flies.webtrans.client;

import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Widget;

public class AppView extends DockPanel implements AppPresenter.Display {

	private Widget main;
	private Widget west;
	private Widget north;
	private Widget south;
	
	public AppView() {
		setSpacing(3);
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
		if(this.main != null) {
			remove(main);
		}
		this.main = main;
		add(main, DockPanel.CENTER );
		setCellWidth(main, "100%");
	}
	
	@Override
	public void setWest(Widget west) {
		if(this.west != null) {
			remove(west);
		}
		this.west = west;
		add(west, DockPanel.WEST );
		setCellWidth(west, "220px");
	}
	
	@Override
	public void setNorth(Widget north) {
		if(this.north != null) {
			remove(north);
		}
		this.north = north;
		add(north, DockPanel.NORTH );
		setCellHeight(north, "20px");
	}

	@Override
	public void setSouth(Widget south) {
		if(this.south != null) {
			remove(south);
		}
		this.south = south;
		add(south, DockPanel.SOUTH );
		setCellHeight(south, "20px");
	}
	
}
