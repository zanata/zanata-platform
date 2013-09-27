package org.zanata.webtrans.client.ui;

public interface SearchFieldListener {
    void onSearchFieldValueChange(String value);

    void onSearchFieldBlur();

    void onSearchFieldFocus();

    void onSearchFieldClick();

    void onSearchFieldCancel();
}
