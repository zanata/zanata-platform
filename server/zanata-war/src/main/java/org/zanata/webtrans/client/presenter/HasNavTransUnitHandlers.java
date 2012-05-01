package org.zanata.webtrans.client.presenter;

import org.zanata.webtrans.client.events.NavTransUnitHandler;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

public interface HasNavTransUnitHandlers extends HasHandlers
{
   HandlerRegistration addNavTransUnitHandler(NavTransUnitHandler handler);
}
