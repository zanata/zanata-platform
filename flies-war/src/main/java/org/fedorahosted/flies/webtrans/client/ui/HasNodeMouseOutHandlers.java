package org.fedorahosted.flies.webtrans.client.ui;

import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

/**
 * This is very similar to HasMouseOverHandlers, except that the MouseOverEvents'
 * source will be TreeNodes, not the FilterTree itself (even though the caller
 * registers with the FilterTree, not the Nodes)
 * 
 * @author kaio
 *
 */
public interface HasNodeMouseOutHandlers extends HasHandlers {
  HandlerRegistration addNodeMouseOutHandler(MouseOutHandler handler);
}