package org.fedorahosted.flies.webtrans.editor.filter;

import java.util.ArrayList;
import java.util.List;

import org.fedorahosted.flies.gwt.model.TransUnit;
import org.fedorahosted.flies.webtrans.editor.filter.OperatorFilter.Operator;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class OperatorFilterPresenter extends FilterPresenter<OperatorFilter<TransUnit>, OperatorFilterPresenter.Display> {
	private final List<PhraseFilterPresenter> filterUnitPresenters;
	
	@Inject
	public OperatorFilterPresenter(Display display, EventBus eventBus) {
		super(display, eventBus);
		filterUnitPresenters = new ArrayList<PhraseFilterPresenter>();
	}

	public interface Display extends WidgetDisplay{

		void addFilterUnit(Widget widget);
		void removeFilterUnit(Widget widget);

		Button getAddButton();
	}

	@Override
	public Place getPlace() {
		return null;
	}
	
	@Override
	protected void onBind() {
		
//		display.getFilterText().addValueChangeHandler(new ValueChangeHandler<String>() {
//			@Override
//			public void onValueChange(ValueChangeEvent<String> event) {
//				filter.setPhrase(event.getValue());
//			}
//		});
//		refreshDisplay();
//		
		display.getAddButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Log.info("Add TransUnitPresenter");
				PhraseFilter filter = new PhraseFilter("");
				PhraseFilterView view = new PhraseFilterView();
				PhraseFilterPresenter presenter = new PhraseFilterPresenter(view, eventBus);
				presenter.bind(filter);
				filterUnitPresenters.add(presenter);
				getFilter().add(presenter.getFilter());
				display.addFilterUnit(presenter.getDisplay().asWidget());
			}
		});
	}

	@Override
	protected void onPlaceRequest(PlaceRequest request) {
	}

	@Override
	protected void onUnbind() {
	}

	@Override
	public void refreshDisplay() {
//		display.getFilterText().setValue(filter.getPhrase());
	}

	@Override
	public void revealDisplay() {
	}

	public void addFilterUnit() {

	}
	
	
}
