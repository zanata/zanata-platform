package org.fedorahosted.flies.webtrans.client.ui;

import java.util.Iterator;

import org.fedorahosted.flies.webtrans.client.Resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class CollapsePanel extends Composite implements HasWidgets {

	private static CollapsePanelUiBinder uiBinder = GWT
			.create(CollapsePanelUiBinder.class);

	interface CollapsePanelUiBinder extends UiBinder<Widget, CollapsePanel> {
	}


	@UiField
	Anchor heading;
	
	@UiField
	LayoutPanel rootPanel, contentPanel;
	
	@UiField
	Image collapseImage;
	
	@UiField(provided=true)
	Resources resources;
	
	private boolean collapsed = false;
	
	@Inject
	public CollapsePanel(final Resources resources) {
		this.resources = resources;
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	public void setHeading(String heading) {
		this.heading.setText(heading);
	}
	
	public void setCollapsedByDefault(boolean collapsed) {
		setCollapsed(collapsed, false);
	}

	@Override
	public void add(Widget w) {
		contentPanel.add(w);
	}

	@Override
	public void clear() {
		contentPanel.clear();
	}

	@Override
	public Iterator<Widget> iterator() {
		return contentPanel.iterator();
	}

	@Override
	public boolean remove(Widget w) {
		return contentPanel.remove(w);
	}

	@UiHandler({"collapseImage", "heading"})
	protected void onCollapseClicked(ClickEvent event) {
		setCollapsed( !isCollapsed() );
	}

	public boolean isCollapsed() {
		return collapsed;
	}
	
	private void setCollapsed(boolean collapsed, boolean animate) {
		if(collapsed == this.collapsed)
			return;
		this.collapsed = collapsed;
		collapseImage.setResource( collapsed ? resources.collapseClosed() : resources.collapseOpen() );
		rootPanel.forceLayout();
		if(collapsed) {
			rootPanel.setWidgetTopHeight(contentPanel, 20, Unit.PX, 0, Unit.PX);
		}
		else {
			rootPanel.setWidgetTopBottom(contentPanel, 20, Unit.PX, 0, Unit.PX);
		}
		if(animate) {
			rootPanel.animate(500);
		}
		else{
			rootPanel.forceLayout();
		}
	}
	
	public void setCollapsed(boolean collapsed) {
		setCollapsed(collapsed, true);
		
	}
	
}
