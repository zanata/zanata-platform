package net.openl10n.flies.webtrans.client;

import net.openl10n.flies.webtrans.client.events.NavTransUnitHandler;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

public interface HasNavTransUnitHandlers extends HasHandlers
{
   HandlerRegistration addNavTransUnitHandler(NavTransUnitHandler handler);
}
