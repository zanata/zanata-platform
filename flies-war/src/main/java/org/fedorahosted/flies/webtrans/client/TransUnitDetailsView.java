package org.fedorahosted.flies.webtrans.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class TransUnitDetailsView extends Composite implements TransUnitDetailsPresenter.Display {

	private static TransUnitDetailsViewUiBinder uiBinder = GWT
			.create(TransUnitDetailsViewUiBinder.class);

	interface TransUnitDetailsViewUiBinder extends
			UiBinder<Widget, TransUnitDetailsView> {
	}

	public TransUnitDetailsView() {
		initWidget(uiBinder.createAndBindUi(this));
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
