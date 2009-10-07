package org.fedorahosted.flies.webtrans.client.mvp;

import net.customware.gwt.presenter.client.EventBus;

import org.fedorahosted.flies.webtrans.client.Application.WindowResizeEvent;

import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.inject.Inject;

public class AppPresenter {
	
	private HasWidgets container;
	private final TransUnitListPresenter transUnitListPresenter;
	private final WestNavigationPresenter westNavigationPresenter;
	private final EventBus eventBus;
	
	@Inject
	public AppPresenter(final EventBus eventBus, final TransUnitListPresenter transUnitListPresenter,
				final WestNavigationPresenter leftNavigationPresenter ) {
		
		this.transUnitListPresenter = transUnitListPresenter;		
		this.westNavigationPresenter = leftNavigationPresenter;
		this.eventBus = eventBus;
	}
	
	private void showMain() {
		container.clear();
		
		final DockPanel dockPanel = new DockPanel();
		dockPanel.add(transUnitListPresenter.getDisplay().asWidget(), DockPanel.CENTER );
		dockPanel.add(westNavigationPresenter.getDisplay().asWidget(), DockPanel.WEST );
		eventBus.addHandler(WindowResizeEvent.getType(), new ResizeHandler() {
			@Override
			public void onResize(ResizeEvent event) {
				dockPanel.setHeight(event.getHeight() + "px");
				dockPanel.setWidth(event.getWidth() + "px");
			}
			
		});
		
		container.add(dockPanel);
	}

	public void go(final HasWidgets container) {
		this.container = container;
		
		showMain();
	}
}
