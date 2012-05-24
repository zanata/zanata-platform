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

import org.zanata.webtrans.client.editor.TransUnitsDataProvider;
import org.zanata.webtrans.client.editor.table.GetTransUnitActionContext;
import org.zanata.webtrans.client.editor.table.PageNavigation;
import org.zanata.webtrans.client.events.DocumentSelectionEvent;
import org.zanata.webtrans.client.events.DocumentSelectionHandler;
import org.zanata.webtrans.client.view.TransUnitEditDisplay;
import org.zanata.webtrans.client.view.TransUnitListDisplay;
import org.zanata.webtrans.shared.model.TransUnit;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.inject.Inject;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class TransUnitEditPresenter extends WidgetPresenter<TransUnitEditDisplay> implements DocumentSelectionHandler
{

   private final TransUnitEditDisplay display;
   private final EventBus eventBus;
   private final PageNavigation pageNavigation;
   private final TransUnitListDisplay transUnitListDisplay;
   private final AsyncDataProvider<TransUnit> dataProvider;

   @Inject
   public TransUnitEditPresenter(TransUnitEditDisplay display, EventBus eventBus, PageNavigation pageNavigation, TransUnitListDisplay transUnitListDisplay)
   {
      super(display, eventBus);
      this.display = display;
      this.eventBus = eventBus;
      this.pageNavigation = pageNavigation;
      this.transUnitListDisplay = transUnitListDisplay;
      display.setDisplayTable(transUnitListDisplay);
      dataProvider = new TransUnitsDataProvider();
      dataProvider.addDataDisplay(transUnitListDisplay);
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
   public void onDocumentSelected(DocumentSelectionEvent event)
   {
      GetTransUnitActionContext context = GetTransUnitActionContext.of(event.getDocumentId()).setCount(10);
      pageNavigation.init(context);

      dataProvider.updateRowCount(pageNavigation.getTotalCount(), true);
   }

}
