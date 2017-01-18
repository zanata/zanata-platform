package org.zanata.webtrans.client.events;

import java.util.Date;

import com.google.gwt.event.shared.GwtEvent;

public class DocValidationResultEvent extends
        GwtEvent<DocValidationResultHandler> {
    private Date endTime;

    public DocValidationResultEvent(Date endTime) {
        this.endTime = endTime;
    }

    /**
     * Handler type.
     */
    private static final Type<DocValidationResultHandler> TYPE = new Type<>();

    /**
     * Gets the type associated with this event.
     *
     * @return returns the handler type
     */
    public static Type<DocValidationResultHandler> getType() {
        return TYPE;
    }

    @Override
    public
            com.google.gwt.event.shared.GwtEvent.Type<DocValidationResultHandler>
            getAssociatedType() {
        return getType();
    }

    @Override
    protected void dispatch(DocValidationResultHandler handler) {
        handler.onCompleteRunDocValidation(this);
    }

    public Date getEndTime() {
        return endTime;
    }
}
