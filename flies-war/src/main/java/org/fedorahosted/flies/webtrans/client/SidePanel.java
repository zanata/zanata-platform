package org.fedorahosted.flies.webtrans.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.layout.client.Layout.Alignment;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Widget;

public class SidePanel extends Composite {

	private static SidePanelUiBinder uiBinder = GWT
			.create(SidePanelUiBinder.class);

	interface SidePanelUiBinder extends UiBinder<LayoutPanel, SidePanel> {
	}

	@UiField
	Anchor miniLink;

	@UiField(provided = true)
	LayoutPanel userPanel;

	private final LayoutPanel self;

	public SidePanel() {
		userPanel = new LayoutPanel() {
			@Override
			public void onBrowserEvent(Event event) {
				if(event.getTypeInt() == Event.ONBLUR) {
					if (!toggled) {
						toggleUserList();
					}					
				}
				super.onBrowserEvent(event);
			}
		};
		self = uiBinder.createAndBindUi(this);
		initWidget(self);

		userPanel.sinkEvents(Event.ONBLUR);
	}

	int counter = 0;

	boolean toggled = true;

	@UiHandler("miniLink")
	public void onMiniLinkClicked(ClickEvent event) {
		toggleUserList();
	}

	private void toggleUserList() {
		self.forceLayout();
		toggled = !toggled;
		int bottomHeight = toggled ? 20 : 150;
		self
				.setWidgetBottomHeight(userPanel, 0, Unit.PX, bottomHeight,
						Unit.PX);
		self.animate(250);
	}
}
