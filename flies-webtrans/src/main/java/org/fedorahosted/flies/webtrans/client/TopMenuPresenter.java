package org.fedorahosted.flies.webtrans.client;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.fedorahosted.flies.webtrans.client.auth.Identity;
import org.fedorahosted.flies.webtrans.editor.table.TableEditorPresenter;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.inject.Inject;

public class TopMenuPresenter extends WidgetPresenter<TopMenuPresenter.Display> {
	
	public interface Display extends WidgetDisplay {
		//HasClickHandlers getLogoutLink();
		HasWidgets getWidgets();
		HasText getUsername();
		HasText getProjectName();
		HasClickHandlers getPrevEntryButton();
		HasClickHandlers getNextEntryButton();
		HasClickHandlers getPrevFuzzyButton();
		HasClickHandlers getNextFuzzyButton();
		HasClickHandlers getPrevUntranslatedButton();
		HasClickHandlers getNextUntranslatedButton();
	}
	
	private final WorkspaceContext workspaceContext;
	private final Identity identity;
	
	@Inject
	public TopMenuPresenter(Display display, EventBus eventBus, WorkspaceContext workspaceContext, Identity identity, TableEditorPresenter webTransTablePresenter) {
		super(display, eventBus);
		this.workspaceContext = workspaceContext;
		this.identity = identity;
	}

	@Override
	public Place getPlace() {
		return null;
	}

	@Override
	protected void onBind() {
		//display.getLogoutLink().addClickHandler(new ClickHandler() {
			
		//	@Override
		//	public void onClick(ClickEvent event) {
		//		identity.invalidate();
		//	}
		//});	
		
		refreshDisplay();
	}

	@Override
	protected void onPlaceRequest(PlaceRequest request) {
	}

	@Override
	protected void onUnbind() {
	}

	@Override
	public void refreshDisplay() {
		display.getUsername().setText(identity.getPerson().getName());
		display.getProjectName().setText(workspaceContext.getWorkspaceName());
	}

	@Override
	public void revealDisplay() {
	}
	

}
