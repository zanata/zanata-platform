package org.zanata.webtrans.client.ui;

import org.zanata.webtrans.client.resources.EnumMessages;
import org.zanata.webtrans.shared.rpc.HasSearchType.SearchType;

import com.google.inject.Inject;

/**
 * Translates SearchType entries.
 */
public class SearchTypeRenderer extends EnumRenderer<SearchType> {
    private final EnumMessages messages;

    @Inject
    public SearchTypeRenderer(EnumMessages messages) {
        this.messages = messages;
    }

    @Override
    public String render(SearchType st) {
        switch (st) {
        case EXACT:
            return messages.searchTypeExact();
        case FUZZY:
            return messages.searchTypeFuzzy();
        case RAW:
            return messages.searchTypeRaw();
        default:
            return getEmptyValue();
        }
    }
}
