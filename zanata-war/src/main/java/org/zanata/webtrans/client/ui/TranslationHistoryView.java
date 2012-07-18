package org.zanata.webtrans.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class TranslationHistoryView extends DialogBox implements TranslationHistoryDisplay
{
   private VerticalPanel historyContainer = new VerticalPanel();
   private final ProcessingLabel processingLabel;

   @Inject
   public TranslationHistoryView(ProcessingLabel processingLabel)
   {
      super(true, true);
      this.processingLabel = processingLabel;
      setGlassEnabled(true);
      //TODO localise
      getCaption().setText("Translation History");
      setWidget(processingLabel);
   }

   @Override
   public void center()
   {
      processingLabel.start();
      super.center();
   }

   @Override
   public void hide()
   {
      processingLabel.stop();
      super.hide();
   }
}