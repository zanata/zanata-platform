package org.fedorahosted.flies.webtrans.editor;

import org.fedorahosted.flies.gwt.rpc.GetTransUnitsResult;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.PopupPanel;

public abstract class GetTransUnitsCallback implements AsyncCallback<GetTransUnitsResult>{

	@Override
	public void onFailure(Throwable caught) {
		
	}
	
}
