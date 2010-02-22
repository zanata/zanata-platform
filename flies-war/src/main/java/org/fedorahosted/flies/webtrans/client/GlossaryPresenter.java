package org.fedorahosted.flies.webtrans.client;

import java.util.ArrayList;

import org.fedorahosted.flies.gwt.model.Concept;
import org.fedorahosted.flies.gwt.rpc.GetGlossaryConcept;
import org.fedorahosted.flies.gwt.rpc.GetGlossaryConceptResult;
import org.fedorahosted.flies.gwt.rpc.GetProjectStatusCount;
import org.fedorahosted.flies.gwt.rpc.GetProjectStatusCountResult;
import org.fedorahosted.flies.webtrans.client.rpc.CachingDispatchAsync;
import org.fedorahosted.flies.webtrans.editor.ConceptView;
import org.fedorahosted.flies.webtrans.editor.ToolBoxPresenter.Display;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import net.customware.gwt.dispatch.client.DispatchAsync;
import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

public class GlossaryPresenter extends WidgetPresenter<GlossaryPresenter.Display> {
	private final WorkspaceContext workspaceContext;
	private final CachingDispatchAsync dispatcher;
	
	public interface Display extends WidgetDisplay {
		Button getSearchButton();
		TextBox getGlossaryTextBox();
		void clearResults();
		void createTable(ArrayList<Concept> concepts);
	}

	@Inject
	public GlossaryPresenter(Display display, EventBus eventBus, CachingDispatchAsync dispatcher, WorkspaceContext workspaceContext) {
		super(display, eventBus);
		this.dispatcher = dispatcher;
		this.workspaceContext = workspaceContext;
	}

	@Override
	public Place getPlace() {
		return null;
	}

	@Override
	protected void onBind() {
		
		display.getSearchButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				display.clearResults();
				dispatcher.execute(new GetGlossaryConcept(display.getGlossaryTextBox().getText(), workspaceContext.getLocaleId(), 1L), new AsyncCallback<GetGlossaryConceptResult>() {
					@Override
					public void onFailure(Throwable caught) {
					}
					@Override
					public void onSuccess(GetGlossaryConceptResult result) {
						ArrayList<Concept> concepts = result.getConcepts();
						display.createTable(concepts);
					}
			});
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
	}

	@Override
	public void revealDisplay() {
	}

}
