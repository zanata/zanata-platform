package org.fedorahosted.flies.gwt.rpc;

import net.customware.gwt.dispatch.shared.Result;

public class EditingTranslationResult implements Result {

	private static final long serialVersionUID = 1L;

	private boolean success;
	
	@SuppressWarnings("unused")
	private EditingTranslationResult() {
	}
	
	public EditingTranslationResult(boolean success) {
		this.success = success;
	}
	
	public boolean isSuccess() {
		return success;
	}

}
