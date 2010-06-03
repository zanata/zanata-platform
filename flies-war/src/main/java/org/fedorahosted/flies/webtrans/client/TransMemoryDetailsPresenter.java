package org.fedorahosted.flies.webtrans.client;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.fedorahosted.flies.webtrans.client.rpc.CachingDispatchAsync;
import org.fedorahosted.flies.webtrans.shared.model.TranslationMemoryItem;
import org.fedorahosted.flies.webtrans.shared.rpc.GetTransMemoryDetailsAction;
import org.fedorahosted.flies.webtrans.shared.rpc.TransMemoryDetailsList;
import org.fedorahosted.flies.webtrans.shared.rpc.TransMemoryDetails;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;

public class TransMemoryDetailsPresenter extends WidgetPresenter<TransMemoryDetailsPresenter.Display> {
	private final CachingDispatchAsync dispatcher;

	public interface Display extends WidgetDisplay {
		void hide();
		void show();
		HasText getSourceText();
		HasText getTargetText();
		HasText getSourceComment();
		HasText getTargetComment();
		HasText getProjectName();
		HasText getIterationName();
		HasText getDocumentName();
		HasChangeHandlers getDocumentListBox();
		int getSelectedDocumentIndex();
		HasClickHandlers getDismissButton();
		void clearDocs();
		void addDoc(String text);
	}
	
	TransMemoryDetailsList tmDetails;

	@Inject
	public TransMemoryDetailsPresenter(final Display display, EventBus eventBus, CachingDispatchAsync dispatcher) {
		super(display, eventBus);
		this.dispatcher = dispatcher;
		
		registerHandler(display.getDismissButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				display.hide();
			}
		}));
		registerHandler(display.getDocumentListBox().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				selectDoc(display.getSelectedDocumentIndex());
			}
		}));
	}

	public void show(final TranslationMemoryItem item) {
		// request TM details from the server
		dispatcher.execute(new GetTransMemoryDetailsAction(item.getTransUnitIdList()), new AsyncCallback<TransMemoryDetailsList>() {

			@Override
			public void onFailure(Throwable caught) {
				Log.error(caught.getMessage(), caught);
			}

			@Override
			public void onSuccess(TransMemoryDetailsList result) {
				tmDetails = result;
				display.getSourceText().setText(item.getSource());
				display.getTargetText().setText(item.getTarget());
				display.clearDocs();
				for (TransMemoryDetails detailsItem : tmDetails.getItems()) {
					String docText = detailsItem.getProjectName()+'/'+detailsItem.getIterationName()+'/'+detailsItem.getDocId();
					display.addDoc(docText);
				}
				selectDoc(0);
				
				display.show();
			}
		});
	}

	protected void selectDoc(int selected) {
		String sourceComment = "";
		String targetComment = "";
		String project = "";
		String iter = "";
		String doc = "";
		if (selected >= 0) {
			TransMemoryDetails item = tmDetails.getItems().get(selected);
			sourceComment = item.getSourceComment();
			targetComment = item.getTargetComment();
			project = item.getProjectName();
			iter = item.getIterationName();
			doc = item.getDocId();
		}
		display.getSourceComment().setText(sourceComment);
		display.getTargetComment().setText(targetComment);
		display.getProjectName().setText(project);
		display.getIterationName().setText(iter);
		display.getDocumentName().setText(doc);
	}

	@Override
	public Place getPlace() {
		return null;
	}

	@Override
	protected void onBind() {
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
