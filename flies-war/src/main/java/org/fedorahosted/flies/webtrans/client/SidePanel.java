package org.fedorahosted.flies.webtrans.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SidePanel extends Composite implements SidePanelPresenter.Display {

	private static SidePanelUiBinder uiBinder = GWT
			.create(SidePanelUiBinder.class);

	interface SidePanelUiBinder extends UiBinder<LayoutPanel, SidePanel> {
	}

	@UiField(provided=true)
	LayoutPanel usersPanelContainer;
	
	@UiField
	LayoutPanel filterPanelContainer;
	
	@UiField(provided=true)
	WorkspaceUsersView workspaceUsersView;
	
	private final int HEIGHT_USERPANEL_EXPANDED = 200;
	private final int HEIGHT_USERPANEL_COLLAPSED = 20;
	private final int USERPANEL_COLLAPSE_DELAY = 1;
	
	private final LayoutPanel rootPanel;

	private final Timer collapseTimer = new Timer() {
		@Override
		public void run() {
			collapseUsersPanel();
		}
	};
	
	private boolean collapseTriggered = false;
	private boolean collapsed = true;

	@Inject
	public SidePanel(WorkspaceUsersView workspaceUsersView) {
		this.workspaceUsersView = workspaceUsersView;
		usersPanelContainer = new LayoutPanel() {
			@Override
			public void onBrowserEvent(Event event) {
				if(event.getTypeInt() == Event.ONMOUSEOUT) {
					if (!collapsed) {
						collapseUsersPanelSoon();
					}					
				}
				else if(event.getTypeInt() == Event.ONMOUSEOVER) {
					if (collapsed) {
						expandUsersPanel();
					}
					else{
						cancelCollapseUsersPanel();
					}
				}
				super.onBrowserEvent(event);
			}
		};
		rootPanel = uiBinder.createAndBindUi(this);
		initWidget(rootPanel);
		usersPanelContainer.sinkEvents(Event.ONMOUSEOUT | Event.ONMOUSEOVER);
	}
	
	public void setFilterView(Widget filterView) {
		filterPanelContainer.add(filterView);
	}
	
	private void cancelCollapseUsersPanel() {
		if(collapseTriggered) {
			collapseTimer.cancel();
			collapseTriggered = false;
		}
	}

	private void collapseUsersPanelSoon() {
		collapseTriggered = true;
		collapseTimer.schedule(USERPANEL_COLLAPSE_DELAY);
	}

	@Override
	public void collapseUsersPanel() {
		if(collapsed) return;
		toggleUsersPanel();
	}
	
	private void toggleUsersPanel() {
		rootPanel.forceLayout();
		collapsed = !collapsed;
		
		int bottomHeight = collapsed ? HEIGHT_USERPANEL_COLLAPSED : HEIGHT_USERPANEL_EXPANDED;
		rootPanel
				.setWidgetBottomHeight(usersPanelContainer, 0, Unit.PX, bottomHeight,
						Unit.PX);
		rootPanel.animate(250);
	}
	
	@Override
	public void expandUsersPanel() {
		cancelCollapseUsersPanel();
		if(!collapsed) return;
		toggleUsersPanel();
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
