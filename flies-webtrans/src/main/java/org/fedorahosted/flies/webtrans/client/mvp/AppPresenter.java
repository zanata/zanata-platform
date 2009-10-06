package org.fedorahosted.flies.webtrans.client.mvp;

import com.google.gwt.user.client.ui.HasWidgets;
import com.google.inject.Inject;

public class AppPresenter {
	private HasWidgets container;
	private TransUnitListPresenter transUnitListPresenter;

	@Inject
	public AppPresenter(final TransUnitListPresenter transUnitListPresenter) {
		this.transUnitListPresenter = transUnitListPresenter;		
	}
	
	private void showMain() {
		container.clear();
		container.add(transUnitListPresenter.getDisplay().asWidget());
	}

	public void go(final HasWidgets container) {
		this.container = container;
		
		showMain();
	}
}
