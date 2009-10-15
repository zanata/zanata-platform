package org.fedorahosted.flies.webtrans.editor;

import java.util.ArrayList;

import net.customware.gwt.dispatch.client.DispatchAsync;
import net.customware.gwt.presenter.client.EventBus;

import org.fedorahosted.flies.gwt.model.DocumentId;
import org.fedorahosted.flies.gwt.model.TransUnit;
import org.fedorahosted.flies.gwt.rpc.GetTransUnits;
import org.fedorahosted.flies.gwt.rpc.GetTransUnitsResult;
import org.fedorahosted.flies.webtrans.client.NotificationEvent;
import org.fedorahosted.flies.webtrans.client.NotificationEvent.Severity;


import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.gen2.table.client.MutableTableModel;
import com.google.gwt.gen2.table.client.TableModelHelper.Request;
import com.google.gwt.gen2.table.client.TableModelHelper.SerializableResponse;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class WebTransTableModel extends MutableTableModel<TransUnit> {

	private final DispatchAsync dispatcher;
	private final EventBus eventBus;
	
	@Inject
	public WebTransTableModel(DispatchAsync dispatcher, EventBus eventBus) {
		this.dispatcher = dispatcher;
		this.eventBus = eventBus;
	}
	
	@Override
	protected boolean onRowInserted(int beforeRow) {
		return false;
	}

	@Override
	protected boolean onRowRemoved(int row) {
		return true;
	}

	@Override
	protected boolean onSetRowValue(int row, TransUnit rowValue) {
		return true;
	}

	@Override
	public void requestRows(
			final Request request,
			final Callback<TransUnit> callback) {
		int numRows = request.getNumRows();
		int startRow = request.getStartRow();
		Log.debug("Table requesting" + numRows + " starting from "+ startRow);
		dispatcher.execute(new GetTransUnits(new DocumentId(1), startRow, numRows), new AsyncCallback<GetTransUnitsResult>() {
			@Override
			public void onSuccess(GetTransUnitsResult result) {
				SerializableResponse<TransUnit> response = new SerializableResponse<TransUnit>(
						result.getUnits());
				Log.debug("Got " + result.getUnits().size() +" rows back");
				callback.onRowsReady(request, response);
				setRowCount(result.getTotalCount());
			}
			@Override
			public void onFailure(Throwable caught) {
				eventBus.fireEvent( new NotificationEvent(Severity.Warning, "Failed to load data from Server"));
			}
		});
	}
	
}
