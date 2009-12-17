package org.fedorahosted.flies.webtrans.editor;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

public abstract class TranslationStatsBarPresenter extends WidgetPresenter<TranslationStatsBarPresenter.Display>{

	public interface Display extends WidgetDisplay, HasTransUnitCount {
	}
	
	public TranslationStatsBarPresenter(final Display display, final EventBus eventBus) {
		super(display, eventBus);
	}

}
