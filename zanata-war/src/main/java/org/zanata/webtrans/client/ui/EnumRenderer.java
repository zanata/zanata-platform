package org.zanata.webtrans.client.ui;

import com.google.gwt.text.shared.AbstractRenderer;

/**
 * Translates enum entries. Use setEmptyValue() if you want to have a custom
 * empty value. Default empty value is "".
 *
 * @param <T>
 *            an enumeration entry which is to be registered in
 *            {@link Translations}
 * @author Els Dessin http://stackoverflow.com/a/4931399/14379
 */
public class EnumRenderer<T extends Enum<?>> extends AbstractRenderer<T> {
    private String emptyValue = "";

    /**
     * Subclasses can override to return localised strings
     */
    @Override
    public String render(T object) {
        if (object == null) {
            return emptyValue;
        }
        return object.toString();
    }

    public String getEmptyValue() {
        return emptyValue;
    }
}
