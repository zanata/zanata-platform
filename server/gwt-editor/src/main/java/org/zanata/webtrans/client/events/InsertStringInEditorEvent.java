package org.zanata.webtrans.client.events;

import com.google.gwt.event.shared.GwtEvent;

public class InsertStringInEditorEvent extends
        GwtEvent<InsertStringInEditorHandler> {

    /**
     * Handler type.
     */
    private static Type<InsertStringInEditorHandler> TYPE;

    /**
     * Gets the type associated with this event.
     *
     * @return returns the handler type
     */
    public static Type<InsertStringInEditorHandler> getType() {
        return TYPE != null ? TYPE : (TYPE =
                new Type<InsertStringInEditorHandler>());
    }

    private String valueToReplace, suggestion;

    /**
     * @param sourceResult
     * @param targetResult
     */
    public InsertStringInEditorEvent(String valueToReplace, String suggestion) {
        this.valueToReplace = valueToReplace;
        this.suggestion = suggestion;
    }

    @Override
    protected void dispatch(InsertStringInEditorHandler handler) {
        handler.onInsertString(this);
    }

    @Override
    public GwtEvent.Type<InsertStringInEditorHandler> getAssociatedType() {
        return getType();
    }

    public String getValueToReplace() {
        return valueToReplace;
    }

    public String getSuggestion() {
        return suggestion;
    }
}
