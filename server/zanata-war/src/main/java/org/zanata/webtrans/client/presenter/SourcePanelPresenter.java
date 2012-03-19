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

import java.util.HashMap;
import java.util.Map;

import net.customware.gwt.dispatch.client.DispatchAsync;
import net.customware.gwt.presenter.client.EventBus;

import org.zanata.webtrans.client.editor.table.SourcePanel;
import org.zanata.webtrans.client.editor.table.TableEditorPresenter;
import org.zanata.webtrans.client.resources.NavigationMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.shared.model.TransUnit;

import com.google.inject.Inject;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 */
public class SourcePanelPresenter
{
   private final NavigationMessages messages;
   private final EventBus eventBus;
   private final DispatchAsync dispatcher;
   private final Map<Integer, SourcePanel> sourcePanelMap;

   // private final int pageSize;

   @Inject
   public SourcePanelPresenter(EventBus eventBus, CachingDispatchAsync dispatcher, final NavigationMessages messages)
   {
      // super(display, eventBus);
      this.messages = messages;
      this.eventBus = eventBus;
      this.dispatcher = dispatcher;

      sourcePanelMap = new HashMap<Integer, SourcePanel>();
      // pageSize = tableEditorPresenter.getPageSize();

      for (int i = 0; i < 50; i++)
      {
         SourcePanel sourcePanel = new SourcePanel();
         sourcePanelMap.put(i, sourcePanel);
      }
   }

   // public interface Display extends WidgetDisplay
   // {
   // }

   public SourcePanel getSourcePanel(int row, TransUnit value)
   {
      SourcePanel sourcePanel;

      if (sourcePanelMap.containsKey(row))
      {
         sourcePanel = sourcePanelMap.get(row);
      }
      else
      {
         sourcePanel = new SourcePanel();
         sourcePanelMap.put(row, sourcePanel);
      }
      sourcePanel.renderWidget(value, messages);
      return sourcePanel;
   }

   // @Override
   protected void onBind()
   {

   }

   // @Override
   protected void onUnbind()
   {
   }

   // @Override
   protected void onRevealDisplay()
   {
   }
}
