/*
 * Copyright 2012, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package org.zanata.webtrans.client.ui;

import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.shared.rpc.RevertTransUnitUpdates;
import org.zanata.webtrans.shared.rpc.UpdateTransUnitResult;
import com.google.common.base.Strings;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.inject.Inject;

import net.customware.gwt.presenter.client.EventBus;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class RevertTransUnitUpdateLink extends InlineLabel implements UndoLink
{
   private final CachingDispatchAsync dispatcher;
   private final WebTransMessages messages;
   private final EventBus eventBus;

   //state variables
   private HandlerRegistration handlerRegistration;
   private String linkStyleName;
   private boolean canUndo = false;

   @Inject
   public RevertTransUnitUpdateLink(CachingDispatchAsync dispatcher, WebTransMessages messages, EventBus eventBus)
   {
      super(messages.undo());
      this.dispatcher = dispatcher;
      this.messages = messages;
      this.eventBus = eventBus;
   }

   /**
    * When the click handler gets clicked, it will:
    * <ul>
    *    <li>call RevertTransUnitUpdatesHandler to revert changes</li>
    *    <li>set text to undo in progress</li>
    *    <li>on success it will remove click handler, send notification for undo success and remove any link style from itself</li>
    *    <li>on failure it will re-enable click handler and revert text to undo</li>
    * </ul>
    * @param updateTransUnitResult result from update translation rpc call.
    */
   @Override
   public void prepareUndoFor(UpdateTransUnitResult updateTransUnitResult)
   {
      ClickHandler clickHandler = new RevertTransUnitUpdateClickHandler(updateTransUnitResult);
      handlerRegistration = addClickHandler(clickHandler);
      canUndo = true;
   }

   @Override
   public void setLinkStyle(String styleName)
   {
      linkStyleName = styleName;
      setStyleName(linkStyleName);
   }

   private void enableLink()
   {
      if (!Strings.isNullOrEmpty(linkStyleName))
      {
         setStyleName(linkStyleName);
      }
      canUndo = true;
   }

   private void disableLink()
   {
      if (!Strings.isNullOrEmpty(linkStyleName))
      {
         removeStyleName(linkStyleName);
      }
      canUndo = false;
   }

   private class RevertTransUnitUpdateClickHandler implements ClickHandler
   {
      private final RevertTransUnitUpdates revertAction;

      RevertTransUnitUpdateClickHandler(UpdateTransUnitResult updateTransUnitResult)
      {
         this.revertAction = new RevertTransUnitUpdates(updateTransUnitResult.getUpdateInfoList());
      }

      @Override
      public void onClick(ClickEvent event)
      {
         if (!canUndo)
         {
            return;
         }
         setText(messages.undoInProgress());
         disableLink();
         //we ensure the undo can only be click once.
         handlerRegistration.removeHandler();
         dispatcher.execute(revertAction, new AsyncCallback<UpdateTransUnitResult>()
         {
            @Override
            public void onFailure(Throwable caught)
            {
               eventBus.fireEvent(new NotificationEvent(NotificationEvent.Severity.Error, messages.undoFailure()));
               setText(messages.undo());
               enableLink();
            }

            @Override
            public void onSuccess(UpdateTransUnitResult result)
            {
               eventBus.fireEvent(new NotificationEvent(NotificationEvent.Severity.Info, messages.undoSuccess()));
               setText(messages.undone());
            }
         });

      }
   }
}
