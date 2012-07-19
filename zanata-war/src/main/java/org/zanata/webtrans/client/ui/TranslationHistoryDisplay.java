package org.zanata.webtrans.client.ui;

import java.util.List;

import org.zanata.webtrans.shared.model.TransHistoryItem;
import com.google.gwt.view.client.AbstractDataProvider;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.inject.ImplementedBy;

import net.customware.gwt.presenter.client.widget.WidgetDisplay;

@ImplementedBy(TranslationHistoryView.class)
public interface TranslationHistoryDisplay extends WidgetDisplay
{
   void center();

   void hide();

   void setDataProvider(AbstractDataProvider<TransHistoryItem> dataProvider);
}
