package org.fedorahosted.flies.webtrans.client;

import net.customware.gwt.presenter.client.EventBus;

import org.fedorahosted.flies.webtrans.client.Application.WindowResizeEvent;
import org.fedorahosted.flies.webtrans.client.ui.Pager;
import org.fedorahosted.flies.webtrans.editor.TransUnitListPresenter;
import org.fedorahosted.flies.webtrans.editor.WebTransEditorFooter;
import org.fedorahosted.flies.webtrans.editor.WebTransEditorHeader;
import org.fedorahosted.flies.webtrans.editor.WebTransEditorPresenter;

import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.widgetideas.client.GlassPanel;
import com.google.inject.Inject;

public class AppPresenter {
	
	private HasWidgets container;
	private final WestNavigationPresenter westNavigationPresenter;
	private final EventBus eventBus;
	private final WebTransEditorPresenter webTransEditorPresenter;
	private final LoginPresenter loginPresenter;
	@Inject
	public AppPresenter(final EventBus eventBus, 
				final WestNavigationPresenter leftNavigationPresenter,
				final WebTransEditorPresenter webTransEditorPresenter,
				final LoginPresenter loginPresenter) {
		
		this.westNavigationPresenter = leftNavigationPresenter;
		this.webTransEditorPresenter = webTransEditorPresenter;
		this.eventBus = eventBus;
		this.loginPresenter = loginPresenter;
	}
	
	private void showMain() {
		container.clear();
		
		final DockPanel dockPanel = new DockPanel();
		//final Label appFooter = new HTML("<span style=\"float: left\">Flies page footer goes here</span><span style=\"float: right\">Flies page footer goes here</span>");
		//appFooter.setHeight("1em");
		//dockPanel.add(appFooter, DockPanel.SOUTH);
		
		loginPresenter.bind();

		westNavigationPresenter.bind();
		
		final Widget center = webTransEditorPresenter.getDisplay().asWidget();
		Widget west = westNavigationPresenter.getDisplay().asWidget();
		dockPanel.add(center, DockPanel.CENTER );
		dockPanel.add(west, DockPanel.WEST );
		dockPanel.setCellWidth(center, "100%");
		dockPanel.setCellWidth(west, "220px");
		eventBus.addHandler(WindowResizeEvent.getType(), new ResizeHandler() {
			@Override
			public void onResize(ResizeEvent event) {
				dockPanel.setHeight(event.getHeight() + "px");
				dockPanel.setWidth(event.getWidth() + "px");
			}
			
		});
		
		container.add(dockPanel);
		
		
		eventBus.addHandler(NotificationEvent.getType(), new NotificationEventHandler() {
			
			@Override
			public void onNotification(NotificationEvent event) {
				PopupPanel popup = new PopupPanel(true);
				popup.addStyleDependentName("Notification");
				popup.addStyleName("Severity-"+ event.getSeverity().name());
				popup.setWidth(center.getOffsetWidth()-40 + "px");
				popup.setWidget(new Label(event.getMessage()));
				popup.setPopupPosition(center.getAbsoluteLeft()+20, center.getAbsoluteTop()+30);
				popup.show();
			}
		});
	}

	public void go(final HasWidgets container) {
		this.container = container;
		
		showMain();
	}
}
