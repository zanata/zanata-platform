package org.fedorahosted.flies.webtrans.editor;

import org.fedorahosted.flies.gwt.model.DocumentId;
import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.gwt.model.ProjectContainerId;
import org.fedorahosted.flies.gwt.model.TransUnit;
import org.fedorahosted.flies.gwt.rpc.GetStatusCount;
import org.fedorahosted.flies.gwt.rpc.GetStatusCountResult;
import org.fedorahosted.flies.webtrans.client.DocumentSelectionEvent;
import org.fedorahosted.flies.webtrans.client.DocumentSelectionHandler;
import org.fedorahosted.flies.webtrans.client.WorkspaceContext;
import org.fedorahosted.flies.webtrans.client.events.TransUnitUpdatedEvent;
import org.fedorahosted.flies.webtrans.client.events.TransUnitUpdatedEventHandler;
import org.fedorahosted.flies.webtrans.client.rpc.CachingDispatchAsync;
import org.fedorahosted.flies.webtrans.client.ui.Pager;
import org.fedorahosted.flies.webtrans.editor.table.TableEditorPresenter;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.gen2.table.client.MutableTableModel;
import com.google.gwt.gen2.table.event.client.PageChangeEvent;
import com.google.gwt.gen2.table.event.client.PageChangeHandler;
import com.google.gwt.gen2.table.event.client.PageCountChangeEvent;
import com.google.gwt.gen2.table.event.client.PageCountChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import net.customware.gwt.dispatch.client.DispatchAsync;
import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

public class WebTransEditorPresenter extends WidgetPresenter<WebTransEditorPresenter.Display>{

	public static final Place PLACE = new Place("WebTransEditor");

	private final DocumentStatusPresenter documentStatusPresenter;
	private final TableEditorPresenter webTransTablePresenter;
	
	public interface Display extends WidgetDisplay{
		void setEditor(Widget widget);
		void setToolBox(Widget toolbox);
	}

	@Inject
	public WebTransEditorPresenter(Display display, EventBus eventBus,
			final CachingDispatchAsync dispatcher,
			final TableEditorPresenter webTransTablePresenter,
			final DocumentStatusPresenter documentStatsBarPresenter) {
		super(display, eventBus);
		this.webTransTablePresenter = webTransTablePresenter;
		this.documentStatusPresenter = documentStatsBarPresenter;
	}

	@Override
	public Place getPlace() {
		return PLACE;
	}

	@Override
	protected void onBind() {
		webTransTablePresenter.bind();
		documentStatusPresenter.bind();

		display.setEditor(webTransTablePresenter.getDisplay().asWidget());
		
		webTransTablePresenter.gotoFirstPage();
		
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
