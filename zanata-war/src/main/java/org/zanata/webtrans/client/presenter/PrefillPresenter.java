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
package org.zanata.webtrans.client.presenter;

import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.history.History;
import org.zanata.webtrans.client.history.HistoryToken;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.ui.PrefillPopupPanelDisplay;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.rpc.NoOpResult;
import org.zanata.webtrans.shared.rpc.PrefillTranslation;

import com.google.common.base.Preconditions;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;
import static org.zanata.webtrans.client.events.NotificationEvent.Severity.Error;
import static org.zanata.webtrans.client.events.NotificationEvent.Severity.Info;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class PrefillPresenter extends WidgetPresenter<PrefillPopupPanelDisplay> implements PrefillPopupPanelDisplay.Listener
{

   private PrefillPopupPanelDisplay display;
   private final EventBus eventBus;
   private final CachingDispatchAsync dispatcher;
   private final DocumentListPresenter docListPresenter;

   @Inject
   public PrefillPresenter(PrefillPopupPanelDisplay display, EventBus eventBus, CachingDispatchAsync dispatcher, DocumentListPresenter docListPresenter)
   {
      super(display, eventBus);
      this.display = display;
      this.eventBus = eventBus;
      this.dispatcher = dispatcher;
      this.docListPresenter = docListPresenter;
      display.setListener(this);
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

   @Override
   public void proceedToPrefill(String approvedPercent)
   {
      int threshold = display.getApprovedThreshold();
      DocumentId docId = docListPresenter.getCurrentSelectedDocIdOrNull();
      Preconditions.checkNotNull(docId, "doc id is null!!");
      //TODO implement
      dispatcher.execute(new PrefillTranslation(threshold, docId), new AsyncCallback<NoOpResult>()
      {
         @Override
         public void onFailure(Throwable caught)
         {
            eventBus.fireEvent(new NotificationEvent(Error, "prefill failed"));
            display.hide();
         }

         @Override
         public void onSuccess(NoOpResult result)
         {
            eventBus.fireEvent(new NotificationEvent(Info, "prefill success"));
            display.hide();
         }
      });
   }

   public void preparePrefill()
   {
      display.center();
   }
}
