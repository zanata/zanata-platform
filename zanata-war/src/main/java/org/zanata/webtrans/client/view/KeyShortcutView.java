package org.zanata.webtrans.client.view;

import java.util.HashMap;
import java.util.Map;

import org.zanata.webtrans.client.keys.Keys;
import org.zanata.webtrans.client.keys.KeyShortcut;
import org.zanata.webtrans.client.presenter.KeyShortcutPresenter;
import org.zanata.webtrans.client.resources.WebTransMessages;

import com.google.common.base.Strings;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
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

   private final Map<Integer, String> keyDisplayMap;

   @Inject
   public KeyShortcutView(final WebTransMessages webTransMessages)
   {
      setWidget(uiBinder.createAndBindUi(this));
      heading.setText(webTransMessages.availableKeyShortcutsTitle());
      
      setStyleName("keyShortcutPanel");
      setAutoHideEnabled(true);
      setAutoHideOnHistoryEventsEnabled(true);
      setGlassEnabled(true);

      keyDisplayMap = new HashMap<Integer, String>();

      keyDisplayMap.put(Keys.ALT_KEY, "Alt");
      keyDisplayMap.put(Keys.SHIFT_KEY, "Shift");
      keyDisplayMap.put(Keys.META_KEY, "Meta");
      keyDisplayMap.put(Keys.CTRL_KEY, "Ctrl");

      keyDisplayMap.put(Keys.KEY_NUM_1, "Num 1");
      keyDisplayMap.put(Keys.KEY_NUM_2, "Num 2");
      keyDisplayMap.put(Keys.KEY_NUM_3, "Num 3");
      keyDisplayMap.put(Keys.KEY_NUM_4, "Num 4");

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

   private final Column<KeyShortcut, SafeHtml> keysColumn = new Column<KeyShortcut, SafeHtml>(new SafeHtmlCell())
   {
      @Override
      public SafeHtml getValue(KeyShortcut shortcut)
      {
         SafeHtmlBuilder sb = new SafeHtmlBuilder();
         sb.appendEscapedLines(keysDisplayString(shortcut));
         return sb.toSafeHtml();
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

   public void addContext(String contextName, ListDataProvider<KeyShortcut> dataProvider)
   {
      Label categoryTitle = new Label(contextName);
      categoryTitle.addStyleName(style.keyShortcutCategoryTitle());
      shortcutContainer.add(categoryTitle);

      CellTable<KeyShortcut> table = new CellTable<KeyShortcut>();
      table.setStyleName(style.keyShortcutTable());

      table.addColumn(keysColumn);
      table.addColumn(descColumn);

      dataProvider.addDataDisplay(table);

      // TODO adjust how shortcuts are displayed in this table
      shortcutContainer.add(table);
   }

   private String keysDisplayString(KeyShortcut shortcut)
   {
      StringBuilder sb = new StringBuilder();

      boolean first = true;
      for (Keys keys : shortcut.getAllKeys())
      {
         int modifiers = keys.getModifiers();
         int keyCode = keys.getKeyCode();

         if (!first)
         {
            sb.append('\n');
         }
         first = false;

         if ((modifiers & Keys.CTRL_KEY) != 0)
         {
            sb.append(keyDisplayMap.get(Keys.CTRL_KEY));
            sb.append('+');
         }
         if ((modifiers & Keys.SHIFT_KEY) != 0)
         {
            sb.append(keyDisplayMap.get(Keys.SHIFT_KEY));
            sb.append('+');
         }
         if ((modifiers & Keys.META_KEY) != 0)
         {
            sb.append(keyDisplayMap.get(Keys.META_KEY));
            sb.append('+');
         }
         if ((modifiers & Keys.ALT_KEY) != 0)
         {
            sb.append(keyDisplayMap.get(Keys.ALT_KEY));
            sb.append('+');
         }
         if (!Strings.isNullOrEmpty(keyDisplayMap.get(keyCode)))
         {
            sb.append(keyDisplayMap.get(keyCode));
         }
         else
         {
            sb.append((char) keyCode);
         }
      }
      return sb.toString();
   }

   @Override
   public void showPanel()
   {
      // center() does not set vertical position properly
      this.setPopupPositionAndShow(new PositionCallback()
      {
         @Override
         public void setPosition(int offsetWidth, int offsetHeight)
         {
            int left = (Window.getClientWidth() - offsetWidth) / 2;
            setThisPosition(left, 100);
         }
      });

   }

   private void setThisPosition(int left, int top)
   {
      this.setPopupPosition(left, top);
   }

   @Override
   public Widget asWidget()
   {
      return this;
   }
}
