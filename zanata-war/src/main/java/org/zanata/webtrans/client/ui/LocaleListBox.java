package org.zanata.webtrans.client.ui;

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
public class LocaleListBox extends ListBox {

    private List<Locale> locales;

    public LocaleListBox() {
        super();
        locales = new ArrayList<Locale>();
    }

    /**
     * Adds a Locale to the list, adding the Locales displayName as the String to display
     *
     * @param locale
     */
    public void addItem(Locale locale) {
        super.addItem(locale.getDisplayName());
        locales.add(locale);
    }

    /**
     *
     * @param item the item to display in the list
     * @param locale the locale to be mapped with the item
     */
    public void addItem(String item, Locale locale) {
        super.addItem(item);
        locales.add(locale);
    }

    /**
     * Gets the Locale associated with the item at a given index.
     * Returns null if no locale is associated with the item at this index.     *
     * @param index
     * @return the locale at the given index, null if no locale is associated
     */
    public Locale getLocale(int index) {
        Locale locale = locales.get(index);
        if (locale == null) {
            return null;
        } else {
            return locales.get(index);
        }
    }
    
    /**
     * Gets the locale associated with the currently selected item.
     * @return 
     */
    public Locale getLocaleAtSelectedIndex(){
        int selectedIndex = super.getSelectedIndex();
        return locales.get(selectedIndex);
    }
    
    /**
     * Removes all items and locales from the list box.
     */
    @Override
    public void clear(){
        super.clear();
        locales.clear();
    }
    
    /**
     * Removes the item and the associated locale at the specified index.
     * @param index
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    @Override
    public void removeItem(int index){
        super.removeItem(index);
        locales.remove(index);
    }
}
