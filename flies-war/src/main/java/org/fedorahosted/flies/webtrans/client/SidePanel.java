package org.fedorahosted.flies.webtrans.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Widget;

public class SidePanel extends Composite {

	private static SidePanelUiBinder uiBinder = GWT
			.create(SidePanelUiBinder.class);

	interface SidePanelUiBinder extends UiBinder<LayoutPanel, SidePanel> {
	}

	@UiField
	Label miniUsersPanel;

	@UiField(provided=true)
	LayoutPanel userPanel;
	
	@UiField	
	LayoutPanel userPanelContainer;
	
	@UiField
	LayoutPanel filterPanel;
	
	private final int HEIGHT_USERPANEL_EXPANDED = 200;
	private final int HEIGHT_USERPANEL_COLLAPSED = 20;
	private final int USERPANEL_COLLAPSE_DELAY = 1500;
	
	private final LayoutPanel self;

	private final Timer collapseTimer = new Timer() {
		@Override
		public void run() {
			collapseUserList();
		}
	};
	
	private boolean collapseTriggered = false;
	private boolean collapsed = true;

	public SidePanel() {
		userPanel = new LayoutPanel() {
			@Override
			public void onBrowserEvent(Event event) {
				if(event.getTypeInt() == Event.ONMOUSEOUT) {
					if (!collapsed) {
						collapseUsersPanelSoon();
					}					
				}
				else if(event.getTypeInt() == Event.ONMOUSEOVER) {
					if (!collapsed) {
						cancelCollapseUsersPanel();
					}					
				}
				super.onBrowserEvent(event);
			}
		};
		self = uiBinder.createAndBindUi(this);
		initWidget(self);
		userPanel.sinkEvents(Event.ONMOUSEOUT | Event.ONMOUSEOVER);
	}
	
	public void setFilterView(Widget filterView) {
		filterPanel.add(filterView);
	}
	
	public void setWorkspaceUsersView(Widget workspaceUsersView) {
		userPanelContainer.add(workspaceUsersView);
	}

	private void cancelCollapseUsersPanel() {
		if(collapseTriggered) {
			collapseTimer.cancel();
			collapseTriggered = false;
		}
	}

	@UiHandler("miniUsersPanel")
	public void onMiniUsersPanelOver(MouseOverEvent event) {
		expandUserList();
	}

	private void collapseUsersPanelSoon() {
		collapseTriggered = true;
		collapseTimer.schedule(USERPANEL_COLLAPSE_DELAY);
	}

	private void collapseUserList() {
		if(collapsed) return;
		toggleUserList();
	}
	
	private void toggleUserList() {
		self.forceLayout();
		collapsed = !collapsed;
		
		int bottomHeight = collapsed ? HEIGHT_USERPANEL_COLLAPSED : HEIGHT_USERPANEL_EXPANDED;
		self
				.setWidgetBottomHeight(userPanel, 0, Unit.PX, bottomHeight,
						Unit.PX);
		self.animate(250);
	}
	
	private void expandUserList() {
		cancelCollapseUsersPanel();
		if(!collapsed) return;
		toggleUserList();
	}
}
