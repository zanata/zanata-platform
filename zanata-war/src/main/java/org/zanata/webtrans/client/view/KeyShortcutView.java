package org.zanata.webtrans.client.view;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.zanata.webtrans.client.keys.Keys;
import org.zanata.webtrans.client.keys.KeyShortcut;
import org.zanata.webtrans.client.keys.ShortcutContext;
import org.zanata.webtrans.client.presenter.KeyShortcutPresenter;
import org.zanata.webtrans.client.resources.WebTransMessages;

import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;

public class KeyShortcutView extends PopupPanel implements KeyShortcutPresenter.Display
{

   private static KeyShortcutViewUiBinder uiBinder = GWT.create(KeyShortcutViewUiBinder.class);

   interface KeyShortcutViewUiBinder extends UiBinder<VerticalPanel, KeyShortcutView>
   {
   }

   interface Styles extends CssResource
   {
      String keyShortcutCategoryTitle();

      String keyShortcutTable();
   }

   @UiField
   FlowPanel shortcutContainer;
   
   @UiField
   Label heading;
   
   @UiField
   Styles style;

   private final WebTransMessages messages;

   private final Map<Integer, String> keyDisplayMap;

   @Inject
   public KeyShortcutView(final WebTransMessages webTransMessages)
   {
      setWidget(uiBinder.createAndBindUi(this));
      heading.setText(webTransMessages.availableKeyShortcutsTitle());
      
      setStyleName("notificationPanel");
      setAutoHideEnabled(true);
      setAutoHideOnHistoryEventsEnabled(true);
      setGlassEnabled(true);
      
      this.messages = webTransMessages;
      
      keyDisplayMap = new HashMap<Integer, String>();

      keyDisplayMap.put(Keys.ALT_KEY, "Alt");
      keyDisplayMap.put(Keys.SHIFT_KEY, "Shift");
      keyDisplayMap.put(Keys.META_KEY, "Meta");
      keyDisplayMap.put(Keys.CTRL_KEY, "Ctrl");
      keyDisplayMap.put(KeyCodes.KEY_DOWN, "Down");
      keyDisplayMap.put(KeyCodes.KEY_UP, "Up");
      keyDisplayMap.put(KeyCodes.KEY_ENTER, "Enter");
      keyDisplayMap.put(KeyCodes.KEY_PAGEDOWN, "PageDown");
      keyDisplayMap.put(KeyCodes.KEY_PAGEUP, "PageUp");
      keyDisplayMap.put(KeyCodes.KEY_ESCAPE, "Esc");
   }

   @Override
   public void clearPanel()
   {
      shortcutContainer.clear();
   }

   private final TextColumn<KeyShortcut> keyColumn = new TextColumn<KeyShortcut>()
   {
      @Override
      public String getValue(KeyShortcut keyShortcut)
      {
         return getModifier(keyShortcut);
      }
   };

   private final TextColumn<KeyShortcut> descColumn = new TextColumn<KeyShortcut>()
   {
      @Override
      public String getValue(KeyShortcut keyShortcut)
      {
         return keyShortcut.getDescription();
      }
   };

   public void addContext(ShortcutContext context, Collection<Set<KeyShortcut>> shorcutSets)
   {
      Label categoryTitle = new Label(getContextName(context));
      categoryTitle.addStyleName(style.keyShortcutCategoryTitle());
      shortcutContainer.add(categoryTitle);

      CellTable<KeyShortcut> table = new CellTable<KeyShortcut>();
      table.setStyleName(style.keyShortcutTable());

      table.addColumn(keyColumn);
      table.addColumn(descColumn);

      ListDataProvider<KeyShortcut> dataProvider = new ListDataProvider<KeyShortcut>();
      dataProvider.addDataDisplay(table);

      for (Set<KeyShortcut> shortcutSet : shorcutSets)
      {
         for (KeyShortcut shortcut : shortcutSet)
         {
            if (shortcut.getContext() == context && shortcut.isDisplayInView())
            {
               dataProvider.getList().add(shortcut);
            }
         }
      }
      Collections.sort(dataProvider.getList());
      
      shortcutContainer.add(table);
   }

   private String getModifier(KeyShortcut shortcut)
   {
      StringBuilder sb = new StringBuilder();
      if ((shortcut.getModifiers() & Keys.CTRL_KEY) != 0)
      {
         sb.append(keyDisplayMap.get(Keys.CTRL_KEY));
         sb.append("+");
      }
      if ((shortcut.getModifiers() & Keys.SHIFT_KEY) != 0)
      {
         sb.append(keyDisplayMap.get(Keys.SHIFT_KEY));
         sb.append("+");
      }
      if ((shortcut.getModifiers() & Keys.META_KEY) != 0)
      {
         sb.append(keyDisplayMap.get(Keys.META_KEY));
         sb.append("+");
      }
      if ((shortcut.getModifiers() & Keys.ALT_KEY) != 0)
      {
         sb.append(keyDisplayMap.get(Keys.ALT_KEY));
         sb.append("+");
      }
      if (!Strings.isNullOrEmpty(keyDisplayMap.get(shortcut.getKeyCode())))
      {
         sb.append(keyDisplayMap.get(shortcut.getKeyCode()));
      }
      else
      {
         sb.append((char) shortcut.getKeyCode());
      }
      return sb.toString();
   }

   private String getContextName(ShortcutContext context)
   {
      String contextName = "";
      switch (context)
      {
      case Application:
         contextName = messages.applicationScope();
         break;
      case ProjectWideSearch:
         contextName = messages.projectWideSearchAndReplace();
         break;
      case Edit:
         contextName = messages.editScope();
         break;
      case Navigation:
         contextName = messages.navigationScope();
         break;
      }
      return contextName;
   }

   @Override
   public void showPanel()
   {
      this.center();
   }

   @Override
   public Widget asWidget()
   {
      return this;
   }
}
