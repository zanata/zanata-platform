package org.fedorahosted.flies.gwt.rpc;

import net.customware.gwt.dispatch.shared.Action;

import org.fedorahosted.flies.common.ContentState;
import org.fedorahosted.flies.common.EditState;
import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.gwt.model.ProjectContainerId;
import org.fedorahosted.flies.gwt.model.TransUnitId;

public class EditingTranslationAction implements Action<EditingTranslationResult> {
	
	private static final long serialVersionUID = 1L;

	private TransUnitId transUnitId;
	private LocaleId localeId;
	private EditState editState;
	
	@SuppressWarnings("unused")
	private EditingTranslationAction() {
	}
	
	public EditingTranslationAction(TransUnitId transUnitId, LocaleId localeId, EditState editState) {
		this.transUnitId = transUnitId;
		this.localeId = localeId;
		this.editState = editState;
	}
	
	public TransUnitId getTransUnitId() {
		return transUnitId;
	}
	
	public LocaleId getLocaleId() {
		return localeId;
	}

	public EditState getEditState() {
		return editState;
	}
}