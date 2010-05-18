package org.fedorahosted.flies.webtrans.client.editor;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

/**
 * Common superclass for all Document-based editors
 * 
 * @author asgeirf
 */
public abstract class DocumentEditorPresenter<D extends WidgetDisplay> extends WidgetPresenter<D> {

	public DocumentEditorPresenter(D display, EventBus eventBus) {
		super(display, eventBus);
	}

	
	
}
