package org.zanata.webtrans.client;

import org.zanata.webtrans.client.events.DocumentSelectionHandler;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

public interface HasDocumentSelectionHandlers extends HasHandlers
{
   HandlerRegistration addDocumentSelectionHandler(DocumentSelectionHandler handler);
}
