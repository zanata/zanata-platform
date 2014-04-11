package org.zanata.webtrans.client.history;

import java.util.List;

import com.google.common.base.Strings;

enum EditorTokens implements TokensConverter {
    INSTANCE;

    static final String KEY_TEXT_FLOW_ID = "textflow";
    static final String KEY_SEARCH_DOC_TEXT = "search";
    static final String KEY_DOCUMENT = "doc";
    static final String KEY_MESSAGE_FILTER_UNTRANSLATED = "untranslated";
    static final String KEY_MESSAGE_FILTER_TRANSLATED = "translated";
    static final String KEY_MESSAGE_FILTER_FUZZY = "fuzzy";
    static final String KEY_MESSAGE_FILTER_APPROVED = "approved";
    static final String KEY_MESSAGE_FILTER_REJECTED = "rejected";
    static final String KEY_MESSAGE_FILTER_ERROR = "error";
    static final String VALUE_MESSAGE_FILTER = "show";
    static final String KEY_RES_ID = "resid";
    static final String KEY_MSG_CONTEXT = "msgcontext";
    static final String KEY_SOURCE_COMMENT = "sourcecomment";
    static final String KEY_TARGET_COMMENT = "targetcomment";
    static final String KEY_LAST_MODIFIED_BY = "lastmodifiedby";
    static final String KEY_CHANGED_BEFORE = "changedbefore";
    static final String KEY_CHANGED_AFTER = "changedafter";

    @Override
    public void populateHistoryToken(HistoryToken historyToken, Token token) {
        String key = token.getKey();
        String value = token.getValue();
        if (key.equals(EditorTokens.KEY_DOCUMENT)) {
            historyToken.setDocumentPath(value);
        } else if (key.equals(EditorTokens.KEY_SEARCH_DOC_TEXT)) {
            historyToken.setSearchText(value);
        } else if (key.equals(EditorTokens.KEY_TEXT_FLOW_ID)) {
            historyToken.setTextFlowId(value);
        }
        if (key.equals(KEY_MESSAGE_FILTER_UNTRANSLATED)) {
            historyToken.setFilterUntranslated(true);
        }
        if (key.equals(KEY_MESSAGE_FILTER_FUZZY)) {
            historyToken.setFilterFuzzy(true);
        }
        if (key.equals(KEY_MESSAGE_FILTER_TRANSLATED)) {
            historyToken.setFilterTranslated(true);
        }
        if (key.equals(KEY_MESSAGE_FILTER_APPROVED)) {
            historyToken.setFilterApproved(true);
        }
        if (key.equals(KEY_MESSAGE_FILTER_REJECTED)) {
            historyToken.setFilterRejected(true);
        }
        if (key.equals(KEY_MESSAGE_FILTER_ERROR)) {
            historyToken.setFilterHasError(true);
        }
        if (key.equals(KEY_RES_ID)) {
            historyToken.setResId(value);
        }
        if (key.equals(KEY_CHANGED_AFTER)) {
            historyToken.setChangedAfter(value);
        }
        if (key.equals(KEY_CHANGED_BEFORE)) {
            historyToken.setChangedBefore(value);
        }
        if (key.equals(KEY_MSG_CONTEXT)) {
            historyToken.setMsgContext(value);
        }
        if (key.equals(KEY_SOURCE_COMMENT)) {
            historyToken.setSourceComment(value);
        }
        if (key.equals(KEY_TARGET_COMMENT)) {
            historyToken.setTargetComment(value);
        }
        if (key.equals(KEY_LAST_MODIFIED_BY)) {
            historyToken.setLastModifiedBy(value);
        }
    }

    @Override
    public void toTokenString(HistoryToken historyToken, List<Token> tokens) {
        if (!historyToken.getDocumentPath().equals(
                HistoryToken.DEFAULT_DOCUMENT_PATH)) {
            tokens.add(new Token(KEY_DOCUMENT, historyToken.getDocumentPath()));
        }

        setIfExists(tokens, KEY_SEARCH_DOC_TEXT, historyToken.getSearchText());

        if (historyToken.getTextFlowId() != null) {
            tokens.add(new Token(KEY_TEXT_FLOW_ID, historyToken.getTextFlowId()
                    .toString()));
        }

        if (historyToken.isFilterUntranslated() != historyToken.isFilterFuzzy()
                || historyToken.isFilterUntranslated() != historyToken
                        .isFilterTranslated()
                || historyToken.isFilterUntranslated() != historyToken
                        .isFilterHasError()
                || historyToken.isFilterUntranslated() != historyToken
                        .isFilterApproved()
                || historyToken.isFilterUntranslated() != historyToken
                        .isFilterRejected()) {
            // if filter options is set (not showing everything)
            if (historyToken.isFilterUntranslated()) {
                tokens.add(new Token(KEY_MESSAGE_FILTER_UNTRANSLATED,
                        VALUE_MESSAGE_FILTER));
            }
            if (historyToken.isFilterFuzzy()) {
                tokens.add(new Token(KEY_MESSAGE_FILTER_FUZZY,
                        VALUE_MESSAGE_FILTER));
            }
            if (historyToken.isFilterTranslated()) {
                tokens.add(new Token(KEY_MESSAGE_FILTER_TRANSLATED,
                        VALUE_MESSAGE_FILTER));
            }
            if (historyToken.isFilterApproved()) {
                tokens.add(new Token(KEY_MESSAGE_FILTER_APPROVED,
                        VALUE_MESSAGE_FILTER));
            }
            if (historyToken.isFilterRejected()) {
                tokens.add(new Token(KEY_MESSAGE_FILTER_REJECTED,
                        VALUE_MESSAGE_FILTER));
            }
            if (historyToken.isFilterHasError()) {
                tokens.add(new Token(KEY_MESSAGE_FILTER_ERROR,
                        VALUE_MESSAGE_FILTER));
            }
        }
        setIfExists(tokens, KEY_RES_ID, historyToken.getResId());
        setIfExists(tokens, KEY_MSG_CONTEXT, historyToken.getMsgContext());
        setIfExists(tokens, KEY_SOURCE_COMMENT, historyToken.getSourceComment());
        setIfExists(tokens, KEY_TARGET_COMMENT, historyToken.getTargetComment());
        setIfExists(tokens, KEY_LAST_MODIFIED_BY,
                historyToken.getLastModifiedBy());
        setIfExists(tokens, KEY_CHANGED_AFTER, historyToken.getChangedAfter());
        setIfExists(tokens, KEY_CHANGED_BEFORE, historyToken.getChangedBefore());
    }

    private static void
            setIfExists(List<Token> tokens, String key, String value) {
        if (!Strings.isNullOrEmpty(value)) {
            tokens.add(new Token(key, value));
        }
    }
}
