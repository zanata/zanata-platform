package org.zanata.webtrans.client.view;

import java.util.List;

import org.zanata.webtrans.client.presenter.KeyShortcutPresenter;
import org.zanata.webtrans.client.resources.WebTransMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class KeyShortcutView  extends PopupPanel implements KeyShortcutPresenter.Display
{

   private static KeyShortcutViewUiBinder uiBinder = GWT.create(KeyShortcutViewUiBinder.class);

   interface KeyShortcutViewUiBinder extends UiBinder<VerticalPanel, KeyShortcutView>
   {
   }

   @UiField VerticalPanel keyShortcutPanel;

   final WebTransMessages messages;

   @Inject
   public KeyShortcutView(final WebTransMessages webTransMessages)
   {
      setWidget(uiBinder.createAndBindUi(this));
      setStyleName("notificationPanel");
      setAutoHideEnabled(true);
      setAutoHideOnHistoryEventsEnabled(true);
      setGlassEnabled(true);
      this.messages = webTransMessages;
   }

   @Override
   public void clearPanel()
   {
      keyShortcutPanel.clear();
      Label heading = new Label(messages.availableKeyShortcutsTitle());
      heading.addStyleName("keyShortcutHeading");
      keyShortcutPanel.add(heading);
   }
   
   @Override
   public void addContext(String title, List<String> shortcuts)
   {
      VerticalPanel panel = new VerticalPanel();
      Label categoryTitle = new Label(title);
      categoryTitle.addStyleName("keyShortcutCategoryTitle");
      panel.add(categoryTitle);
      for (String shortcut : shortcuts)
      {
         panel.add(new Label(shortcut));
      }
      panel.addStyleName("keyShortcutCategory");
      keyShortcutPanel.add(panel);
   }

   @Override
   public void showPanel()
   {
      center();
   }

   @Override
   public Widget asWidget()
   {
      return this;
   }
}
