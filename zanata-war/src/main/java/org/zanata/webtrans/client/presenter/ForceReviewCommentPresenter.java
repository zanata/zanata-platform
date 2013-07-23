/*
 * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
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

package org.zanata.webtrans.client.presenter;

import org.zanata.webtrans.client.events.CommentBeforeSaveEvent;
import org.zanata.webtrans.client.events.CommentBeforeSaveEventHandler;
import org.zanata.webtrans.client.events.NavTransUnitEvent;
import org.zanata.webtrans.client.events.TransUnitSaveEvent;
import org.zanata.webtrans.client.rpc.AbstractAsyncCallback;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.service.GetTransUnitActionContextHolder;
import org.zanata.webtrans.client.view.ForceReviewCommentDisplay;
import org.zanata.webtrans.shared.rpc.AddReviewCommentAction;
import org.zanata.webtrans.shared.rpc.AddReviewCommentResult;
import com.google.inject.Inject;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class ForceReviewCommentPresenter extends WidgetPresenter<ForceReviewCommentDisplay>
      implements ForceReviewCommentDisplay.Listener, CommentBeforeSaveEventHandler
{

   private final CachingDispatchAsync dispatcher;
   private final GetTransUnitActionContextHolder contextHolder;

   private TransUnitSaveEvent saveEvent;

   @Inject
   public ForceReviewCommentPresenter(ForceReviewCommentDisplay display, EventBus eventBus, CachingDispatchAsync dispatcher, GetTransUnitActionContextHolder contextHolder)
   {
      super(display, eventBus);
      this.dispatcher = dispatcher;
      this.contextHolder = contextHolder;
      display.setListener(this);

      eventBus.addHandler(CommentBeforeSaveEvent.TYPE, this);
   }

   @Override
   public void onCommentBeforeSave(CommentBeforeSaveEvent event)
   {
      saveEvent = event.getSaveEvent();
      display.center();
   }

   @Override
   public void addComment(String content)
   {
      dispatcher.execute(new AddReviewCommentAction(saveEvent.getTransUnitId(), content,
            contextHolder.getContext().getDocument().getId()), new AbstractAsyncCallback<AddReviewCommentResult>()
      {
         @Override
         public void onSuccess(AddReviewCommentResult result)
         {
            display.clearInput();
            eventBus.fireEvent(saveEvent);
            eventBus.fireEvent(NavTransUnitEvent.NEXT_ENTRY_EVENT);
            saveEvent = null;
            display.hide();
         }
      });

   }

   @Override
   protected void onBind()
   {
   }

   @Override
   protected void onUnbind()
   {
   }

   @Override
   protected void onRevealDisplay()
   {
   }
}
