package org.fedorahosted.flies.webtrans.editor;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ToolBoxPresenter extends WidgetPresenter<ToolBoxPresenter.Display> {
	
	private GlossaryPresenter glossaryPresenter;
	
	public interface Display extends WidgetDisplay {
		void setTerminologyView(Widget view);
	}

	@Inject
	public ToolBoxPresenter(Display display, EventBus eventBus, GlossaryPresenter glossaryPresenter) {
		super(display, eventBus);
		this.glossaryPresenter = glossaryPresenter;
	}

	@Override
	public Place getPlace() {
		return null;
	}

	@Override
	protected void onBind() {
		glossaryPresenter.bind();
		display.setTerminologyView(glossaryPresenter.getDisplay().asWidget());
	}

	@Override
	protected void onPlaceRequest(PlaceRequest request) {
	}

	@Override
	protected void onUnbind() {
	}

	@Override
	public void refreshDisplay() {
	}

	@Override
	public void revealDisplay() {
	}

}
