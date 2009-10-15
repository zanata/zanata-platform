package org.fedorahosted.flies.webtrans.editor;

import java.util.ArrayList;

import net.customware.gwt.dispatch.client.DispatchAsync;

import org.fedorahosted.flies.gwt.model.DocumentId;
import org.fedorahosted.flies.gwt.model.TransUnit;
import org.fedorahosted.flies.gwt.rpc.GetTransUnits;
import org.fedorahosted.flies.gwt.rpc.GotTransUnits;


import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.gen2.table.client.MutableTableModel;
import com.google.gwt.gen2.table.client.TableModelHelper.Request;
import com.google.gwt.gen2.table.client.TableModelHelper.SerializableResponse;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class WebTransTableModel extends MutableTableModel<TransUnit> {

	private final DispatchAsync dispatcher;
	
	@Inject
	public WebTransTableModel(DispatchAsync dispatcher) {
		this.dispatcher = dispatcher;
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
		
		Log.info("Requesting " + numRows + " rows");
		
		dispatcher.execute(new GetTransUnits(new DocumentId(1), startRow, numRows), new AsyncCallback<GotTransUnits>() {

			@Override
			public void onSuccess(GotTransUnits result) {
				SerializableResponse<TransUnit> response = new SerializableResponse<TransUnit>(
						result.getUnits());
				callback.onRowsReady(request, response);
				setRowCount(result.getTotalCount());
				Log.info("got rows");
			}

			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
				Log.info("got failure: ",caught);
			}

			
		});
	}
	
}
