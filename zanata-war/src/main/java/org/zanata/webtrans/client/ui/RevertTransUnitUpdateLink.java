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

import java.util.Collection;
import java.util.List;

import net.customware.gwt.presenter.client.EventBus;


import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.shared.model.TransUnitUpdateInfo;
import org.zanata.webtrans.shared.rpc.RevertTransUnitUpdates;
import org.zanata.webtrans.shared.rpc.UpdateTransUnitResult;

import com.allen_sauer.gwt.log.client.Log;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.inject.Inject;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class RevertTransUnitUpdateLink extends InlineLabel implements UndoLink
{
   private final CachingDispatchAsync dispatcher;
   private final WebTransMessages messages;
   private final EventBus eventBus;

   // state variables
   private HandlerRegistration handlerRegistration;
   private String linkStyleName;
   private boolean canUndo = false;
   
   private HasUndoHandler undoHandler = new HasUndoHandler()
   {
      @Override
      public void preUndo(List<TransUnitUpdateInfo> updateInfoList)
      {
      }
      
      @Override
      public void executeUndo(List<TransUnitUpdateInfo> updateInfoList)
      {
         undoExecution(this, updateInfoList);
      }

      @Override
      public void postSuccess(UpdateTransUnitResult result)
      {
      }
   };

   @Inject
   public RevertTransUnitUpdateLink(CachingDispatchAsync dispatcher, WebTransMessages messages, EventBus eventBus)
   {
      super(messages.undo());
      this.dispatcher = dispatcher;
      this.messages = messages;
      this.eventBus = eventBus;
   }

   /**
    * Give the UpdateTransUnitResult object returned from trans unit update
    * handler, then it will create a click handler. When the click handler gets
    * clicked, it will:
    * <ul>
    * <li>call RevertTransUnitUpdatesHandler to revert changes</li>
    * <li>set text to undo in progress</li>
    * <li>on success it will remove click handler, send notification for undo
    * success and remove any link style from itself</li>
    * <li>on failure it will re-enable click handler and revert text to undo</li>
    * </ul>
    * 
    * @param updateTransUnitResult result from update translation rpc call.
    * @see RevertTransUnitUpdateClickHandler
    */
   @Override
   public void prepareUndoFor(UpdateTransUnitResult updateTransUnitResult)
   {
      ClickHandler clickHandler = new RevertTransUnitUpdateClickHandler(undoHandler, updateTransUnitResult.getUpdateInfoList());
      handlerRegistration = addClickHandler(clickHandler);
      canUndo = true;
   }

   @Override
   public void prepareUndoFor(UpdateTransUnitResult updateTransUnitResult, HasUndoHandler undoHandler)
   {
      this.undoHandler = undoHandler;
      ClickHandler clickHandler = new RevertTransUnitUpdateClickHandler(undoHandler, updateTransUnitResult.getUpdateInfoList());
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

   // we should make this a presenter if the size or logic grows
   private class RevertTransUnitUpdateClickHandler implements ClickHandler
   {
      private final HasUndoHandler undoHandler;
      private final List<TransUnitUpdateInfo> updateInfoList;

      RevertTransUnitUpdateClickHandler(HasUndoHandler undoHandler, List<TransUnitUpdateInfo> updateInfoList)
      {
         this.undoHandler = undoHandler;
         this.updateInfoList = updateInfoList;
      }

      @Override
      public void onClick(ClickEvent event)
      {
         undoHandler.preUndo(updateInfoList);
         undoHandler.executeUndo(updateInfoList);
      }
   }

   private static enum UnsuccessfulUpdatePredicate implements Predicate<TransUnitUpdateInfo>
   {
      INSTANCE;

      @Override
      public boolean apply(TransUnitUpdateInfo input)
      {
         return !input.isSuccess();
      }
   }
   
   @Override
   public HasUndoHandler getUndoHandler()
   {
      return undoHandler;
   }
   
   @Override
   public void undoExecution(final HasUndoHandler undoHandler, List<TransUnitUpdateInfo> updateInfoList)
   {
      RevertTransUnitUpdates revertAction = new RevertTransUnitUpdates(updateInfoList);

      if (!canUndo)
         {
            return;
         }
         setText(messages.undoInProgress());
         disableLink();

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
               if (result.isAllSuccess())
               {
                  eventBus.fireEvent(new NotificationEvent(NotificationEvent.Severity.Info, messages.undoSuccess()));
                  setText(messages.undone());
               }
               else
               {
                  //most likely the undo link became stale i.e. entity state has changed on the server
                  Collection<TransUnitUpdateInfo> unsuccessful = Collections2.filter(result.getUpdateInfoList(), UnsuccessfulUpdatePredicate.INSTANCE);
                  int unsuccessfulCount = unsuccessful.size();
                  int successfulCount = result.getUpdateInfoList().size() - unsuccessfulCount;
                  Log.info("undo not all successful. #" + unsuccessfulCount + " unsucess and #" + successfulCount + " success");
                  eventBus.fireEvent(new NotificationEvent(NotificationEvent.Severity.Info, messages.undoUnsuccessful(unsuccessfulCount, successfulCount)));
                  setText("");
               }
               //we ensure the undo can only be click once.
               handlerRegistration.removeHandler();
            }
         });
   }
}
