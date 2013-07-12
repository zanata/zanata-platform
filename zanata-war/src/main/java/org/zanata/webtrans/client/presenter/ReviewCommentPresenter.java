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

import org.zanata.webtrans.client.events.ReviewCommentEvent;
import org.zanata.webtrans.client.events.ReviewCommentEventHandler;
import org.zanata.webtrans.client.rpc.AbstractAsyncCallback;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.service.GetTransUnitActionContextHolder;
import org.zanata.webtrans.client.service.NavigationService;
import org.zanata.webtrans.client.view.ReviewCommentDisplay;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.rpc.AddReviewCommentAction;
import org.zanata.webtrans.shared.rpc.AddReviewCommentResult;
import org.zanata.webtrans.shared.rpc.GetReviewCommentsAction;
import org.zanata.webtrans.shared.rpc.GetReviewCommentsResult;
import com.allen_sauer.gwt.log.client.Log;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Singleton
public class ReviewCommentPresenter extends WidgetPresenter<ReviewCommentDisplay> implements ReviewCommentDisplay.Listener, ReviewCommentEventHandler
{
   private final ReviewCommentDisplay display;
   private final CachingDispatchAsync dispatcher;
   private final ReviewCommentDataProvider dataProvider;
   private final GetTransUnitActionContextHolder contextHolder;
   private final NavigationService navigationService;
   private TransUnitId transUnitId;

   @Inject
   public ReviewCommentPresenter(ReviewCommentDisplay display, EventBus eventBus, CachingDispatchAsync dispatcher, ReviewCommentDataProvider dataProvider, GetTransUnitActionContextHolder contextHolder, NavigationService navigationService)
   {
      super(display, eventBus);
      this.display = display;
      this.dispatcher = dispatcher;
      this.dataProvider = dataProvider;
      this.contextHolder = contextHolder;
      this.navigationService = navigationService;

      display.setListener(this);
      display.setDataProvider(dataProvider);
   }

   @Override
   protected void onBind()
   {
      eventBus.addHandler(ReviewCommentEvent.TYPE, this);
   }

   @Override
   public void onShowReviewComment(ReviewCommentEvent event)
   {
      this.transUnitId = event.getTransUnitId();
      Integer currentTargetVersion = navigationService.getByIdOrNull(transUnitId).getVerNum();
      display.setCurrentTargetVersion(currentTargetVersion);
      dataProvider.setLoading(true);
      dispatcher.execute(new GetReviewCommentsAction(transUnitId), new AbstractAsyncCallback<GetReviewCommentsResult>()
      {
         @Override
         public void onSuccess(GetReviewCommentsResult result)
         {
            dataProvider.setList(result.getComments());
            dataProvider.setLoading(false);
         }
      });
      display.center();
   }

   @Override
   public void addComment(String content)
   {
      dispatcher.execute(new AddReviewCommentAction(transUnitId, content, contextHolder.getContext().getDocument().getId()), new AbstractAsyncCallback<AddReviewCommentResult>()
      {
         @Override
         public void onSuccess(AddReviewCommentResult result)
         {
            dataProvider.getList().add(result.getComment());
            display.clearInput();
         }
      });
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
