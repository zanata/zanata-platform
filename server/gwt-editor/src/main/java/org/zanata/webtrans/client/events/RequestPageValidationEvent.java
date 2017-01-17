package org.zanata.webtrans.client.events;

import com.google.gwt.event.shared.GwtEvent;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 */
public class RequestPageValidationEvent extends
        GwtEvent<RequestPageValidationHandler> {
    /**
     * Handler type.
     */
    public static final Type<RequestPageValidationHandler> TYPE =
            new Type<>();

    public RequestPageValidationEvent() {
    }

    @Override
    protected void dispatch(RequestPageValidationHandler handler) {
        handler.onRequestPageValidation(this);
    }

    @Override
    public GwtEvent.Type<RequestPageValidationHandler> getAssociatedType() {
        return TYPE;
    }
}
