package net.openl10n.flies.webtrans.client;

import net.openl10n.flies.webtrans.client.editor.table.NavigationMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TransUnitNavigationView extends Composite implements TransUnitNavigationPresenter.Display
{

   private static TransUnitNavigationViewUiBinder uiBinder = GWT.create(TransUnitNavigationViewUiBinder.class);

   interface TransUnitNavigationViewUiBinder extends UiBinder<Widget, TransUnitNavigationView>
   {
   }

   @UiField
   Image nextEntry, prevEntry, nextFuzzy, prevFuzzy, nextUntranslated, prevUntranslated;

   private final NavigationMessages messages;

   @UiField(provided = true)
   Resources resources;

   @Inject
   public TransUnitNavigationView(final NavigationMessages messages, final Resources resources)
   {
      this.resources = resources;
      this.messages = messages;
      initWidget(uiBinder.createAndBindUi(this));

      prevEntry.setTitle(messages.actionToolTip(messages.prevEntry(), messages.prevEntryShortcut()));
      nextEntry.setTitle(messages.actionToolTip(messages.nextEntry(), messages.nextEntryShortcut()));
      prevFuzzy.setTitle(messages.actionToolTip(messages.prevFuzzy(), messages.prevFuzzyShortcut()));
      nextFuzzy.setTitle(messages.actionToolTip(messages.nextFuzzy(), messages.nextFuzzyShortcut()));
      prevUntranslated.setTitle(messages.actionToolTip(messages.prevUntranslated(), messages.prevUntranslatedShortcut()));
      nextUntranslated.setTitle(messages.actionToolTip(messages.nextUntranslated(), messages.nextUntranslatedShortcut()));
   }

   @Override
   public HasClickHandlers getPrevEntryButton()
   {
      return prevEntry;
   }

   @Override
   public HasClickHandlers getNextEntryButton()
   {
      return nextEntry;
   }

   @Override
   public HasClickHandlers getPrevFuzzyButton()
   {
      return prevFuzzy;
   }

   @Override
   public HasClickHandlers getNextFuzzyButton()
   {
      return nextFuzzy;
   }

   @Override
   public HasClickHandlers getPrevUntranslatedButton()
   {
      return prevUntranslated;
   }

   @Override
   public HasClickHandlers getNextUntranslatedButton()
   {
      return nextUntranslated;
   }

   @Override
   public Widget asWidget()
   {
      return this;
   }

   @Override
   public void startProcessing()
   {
   }

   @Override
   public void stopProcessing()
   {
   }

}
