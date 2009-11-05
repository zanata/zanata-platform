package org.fedorahosted.flies.webtrans.editor;

import org.fedorahosted.flies.gwt.model.DocumentId;
import org.fedorahosted.flies.gwt.model.LocaleId;
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
	private final ProjectStatusPresenter projectStatusPresenter;
	private final TableEditorPresenter webTransTablePresenter;
	private final Pager pager;
	private final DispatchAsync dispatcher;

	
	public interface Display extends WidgetDisplay{
		HasThreeColWidgets getHeader();
		HasThreeColWidgets getFooter();
		void setEditor(Widget widget);
		void setStatus(String status);
	}

	@Inject
	public WebTransEditorPresenter(Display display, EventBus eventBus,
			final CachingDispatchAsync dispatcher,
			final TableEditorPresenter webTransTablePresenter,
			final DocumentStatusPresenter documentStatsBarPresenter,
			final ProjectStatusPresenter projectStatusPresenter) {
		super(display, eventBus);
		this.dispatcher = dispatcher;
		this.webTransTablePresenter = webTransTablePresenter;
		this.pager = new Pager();
		this.documentStatusPresenter = documentStatsBarPresenter;
		this.projectStatusPresenter = projectStatusPresenter;
	}

	@Override
	public Place getPlace() {
		return PLACE;
	}

	@Override
	protected void onBind() {
		webTransTablePresenter.bind();
		projectStatusPresenter.bind();
		documentStatusPresenter.bind();
        
        display.getFooter().setMiddleWidget(pager);
        pager.setVisible(false);

        display.getFooter().setRightWidget(documentStatusPresenter.getDisplay().asWidget());
        
		display.setEditor(webTransTablePresenter.getDisplay().asWidget());
		
		registerHandler(
			pager.addValueChangeHandler( new ValueChangeHandler<Integer>() {
				
				@Override
				public void onValueChange(ValueChangeEvent<Integer> event) {
					webTransTablePresenter.getDisplay().gotoPage(event.getValue()-1, false);
				}
			})
		);
		
		// TODO this uses incubator's HandlerRegistration
		webTransTablePresenter.addPageChangeHandler( new PageChangeHandler() {
			@Override
			public void onPageChange(PageChangeEvent event) {
				pager.setValue(event.getNewPage()+1);
			}
		});

		// TODO this uses incubator's HandlerRegistration
		webTransTablePresenter.addPageCountChangeHandler(new PageCountChangeHandler() {
			@Override
			public void onPageCountChange(PageCountChangeEvent event) {
				pager.setPageCount(event.getNewPageCount());
				pager.setVisible(true);
			}
		});
	
		
		
		
		
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
