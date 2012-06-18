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

import java.util.Collection;
import java.util.List;

import org.zanata.common.ContentState;
import org.zanata.webtrans.client.editor.table.TableEditorPresenter;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.resources.UiMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.ui.TransMemoryMergePopupPanelDisplay;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.rpc.MergeOption;
import org.zanata.webtrans.shared.rpc.NoOpResult;
import org.zanata.webtrans.shared.rpc.TransMemoryMerge;
import com.allen_sauer.gwt.log.client.Log;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;
import static org.zanata.webtrans.client.events.NotificationEvent.Severity.Error;
import static org.zanata.webtrans.client.events.NotificationEvent.Severity.Info;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class TransMemoryMergePresenter extends WidgetPresenter<TransMemoryMergePopupPanelDisplay> implements TransMemoryMergePopupPanelDisplay.Listener
{

   private TransMemoryMergePopupPanelDisplay display;
   private final EventBus eventBus;
   private final CachingDispatchAsync dispatcher;
   private final TableEditorPresenter tableEditorPresenter;
   private final UiMessages messages;

   @Inject
   public TransMemoryMergePresenter(TransMemoryMergePopupPanelDisplay display, EventBus eventBus, CachingDispatchAsync dispatcher, TableEditorPresenter tableEditorPresenter, UiMessages messages)
   {
      super(display, eventBus);
      this.display = display;
      this.eventBus = eventBus;
      this.dispatcher = dispatcher;
      this.tableEditorPresenter = tableEditorPresenter;
      this.messages = messages;
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
   public void proceedToMergeTM(String percentage, MergeOption differentProjectOption, MergeOption differentDocumentOption, MergeOption differentResIdOption)
   {
      Collection<TransUnit> newItems = getUntranslatedItems();

      if (newItems.isEmpty())
      {
         eventBus.fireEvent(new NotificationEvent(Info, messages.noTranslationToMerge()));
         display.hide();
         return;
      }

      display.showProcessing();
      TransMemoryMerge action = prepareTMMergeAction(newItems, percentage, differentProjectOption, differentDocumentOption, differentResIdOption);
      dispatcher.execute(action, new AsyncCallback<NoOpResult>()
      {
         @Override
         public void onFailure(Throwable caught)
         {
            Log.warn("TM merge failed", caught);
            eventBus.fireEvent(new NotificationEvent(Error, messages.mergeTMFailed()));
            display.hide();
         }

         @Override
         public void onSuccess(NoOpResult result)
         {
            eventBus.fireEvent(new NotificationEvent(Info, messages.mergeTMSuccess()));
            display.hide();
            tableEditorPresenter.initialiseTransUnitList();
         }
      });
   }

   private Collection<TransUnit> getUntranslatedItems()
   {
      List<TransUnit> currentItems = tableEditorPresenter.getDisplay().getRowValues();
      return Collections2.filter(currentItems, new Predicate<TransUnit>()
      {
         @Override
         public boolean apply(TransUnit input)
         {
            return input.getStatus() == ContentState.New;
         }
      });
   }

   private TransMemoryMerge prepareTMMergeAction(Collection<TransUnit> newItems, String percentage, MergeOption differentProjectOption, MergeOption differentDocumentOption, MergeOption differentResIdOption)
   {
      Integer threshold = Integer.valueOf(percentage);
      List<TransUnitId> unitIds = Lists.newArrayList(Collections2.transform(newItems, new Function<TransUnit, TransUnitId>()
      {
         @Override
         public TransUnitId apply(TransUnit from)
         {
            return from.getId();
         }
      }));
      return new TransMemoryMerge(threshold, unitIds, differentProjectOption, differentDocumentOption, differentResIdOption);
   }

   @Override
   public void cancelMergeTM()
   {
      display.hide();
   }

   public void prepareTMMerge()
   {
      display.showForm();
   }
}
