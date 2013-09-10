package org.zanata.webtrans.client.ui;

import com.google.gwt.i18n.client.HasDirection;
import com.google.gwt.user.client.ui.ListBox;
import java.util.ArrayList;
import java.util.List;
import org.zanata.webtrans.shared.model.Locale;

/**
 * The LocaleListBox combines a ListBox-item with a Locale object for easy
 * accessing the chosen Locale.
 *
 * @author Hannes Eskebaek <hannes.eskebaek@databyran.se>
 */
public class LocaleListBox extends ListBox
{
   private List<Locale> locales;

   public LocaleListBox()
   {
      super();
      locales = new ArrayList<Locale>();
   }

   /**
    * Adds a Locale to the list, adding the Locales displayName as the list
    * item
    *
    * @param locale
    */
   public void addItem(Locale locale)
   {
      super.addItem(locale.getDisplayName());
      locales.add(locale);
   }

   /**
    *
    * @param item the item to display in the list
    * @param locale the locale to be mapped with the item
    */
   public void addItem(String item, Locale locale)
   {
      super.addItem(item, locale.getDisplayName());
      locales.add(locale);
   }

   /**
    * Adds a Locale to the list, adding the Locales displayName as the list
    * item, specifying its direction
    *
    * @param item the item to display in the list
    * @param locale the locale to be mapped with the item
    */
   public void addItem(Locale locale, HasDirection.Direction dir)
   {
      super.addItem(locale.getDisplayName(), dir);
      locales.add(locale);
   }

   /**
    *
    * @param item the item to display in the list
    * @param dir the items direction
    * @param locale the locale to be mapped with the item
    */
   public void addItem(String item, HasDirection.Direction dir, Locale locale)
   {
      super.addItem(item, dir, locale.getDisplayName());
      locales.add(locale);
   }

   /**
    * Inserts a locale into the list box at the given index. Using the Locales
    * displayName as the list item.
    *
    * @param locale
    */
   public void insertItem(Locale locale, int index)
   {
      super.insertItem(locale.getDisplayName(), index);
      locales.add(index, locale);
   }

   /**
    * Inserts a locale into the list box at the given index, specifying its
    * direction. Using the Locales displayName as the list item.
    *
    * @param locale
    */
   public void insertItem(Locale locale, HasDirection.Direction dir, int index)
   {
      super.insertItem(locale.getDisplayName(), dir, index);
      locales.add(index, locale);
   }

   /**
    * @param item the item to display in the list
    * @param locale the locale to be mapped with the item
    * @param index the index where it should be inserted
    */
   public void insertItem(String item, Locale locale, int index)
   {
      super.insertItem(item, locale.getDisplayName(), index);
      locales.add(index, locale);
   }

   /**
    * @param item the item to display in the list
    * @param dir the direction of the item
    * @param locale the locale to be mapped with the item
    * @param index the index where it should be inserted
    */
   public void insertItem(String item, HasDirection.Direction dir, Locale locale, int index)
   {
      super.insertItem(item, dir, locale.getDisplayName(), index);
      locales.add(index, locale);
   }

   /**
    * Gets the Locale associated with the item at a given index. Returns null
    * if no locale is associated with the item at this index.
    *
    *
    * @param index
    * @return the locale at the given index, null if no locale is associated
    */
   public Locale getLocale(int index)
   {
      Locale locale = locales.get(index);
      if (locale == null)
      {
         return null;
      }
      else
      {
         return locales.get(index);
      }
   }

   /**
    * Gets the locale associated with the currently selected item.
    *
    * @return
    */
   public Locale getLocaleAtSelectedIndex()
   {
      int selectedIndex = super.getSelectedIndex();
      return locales.get(selectedIndex);
   }

   /**
    * Removes all items and locales from the list box.
    */
   @Override
   public void clear()
   {
      super.clear();
      locales.clear();
   }

   /**
    * Removes the item and the associated locale at the specified index.
    *
    * @param index
    * @throws IndexOutOfBoundsException if the index is out of range
    */
   @Override
   public void removeItem(int index)
   {
      super.removeItem(index);
      locales.remove(index);
   }

   /**
    * @return the list with the locales that this ListBox contains
    */
   public List<Locale> getLocales()
   {
      return locales;
   }

}
