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
import java.util.List;
import java.util.Map;

import net.customware.gwt.presenter.client.EventBus;

import org.zanata.webtrans.client.editor.table.SourcePanel;
import org.zanata.webtrans.client.resources.NavigationMessages;
import org.zanata.webtrans.shared.model.TransUnit;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.inject.Inject;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 */
public class SourcePanelPresenter
{
   private final NavigationMessages messages;
   private final EventBus eventBus;
   private final Map<Integer, SourcePanel> sourcePanelMap;
   private String selectedSource;

   @Inject
   public SourcePanelPresenter(EventBus eventBus, final NavigationMessages messages)
   {
      this.messages = messages;
      this.eventBus = eventBus;

      sourcePanelMap = new HashMap<Integer, SourcePanel>();
   }

   private final ValueChangeHandler<Boolean> selectSourceHandler = new ValueChangeHandler<Boolean>()
   {
      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event)
      {
         selectedSource = ((RadioButton) event.getSource()).getTitle();
         Log.info("selectedSource:" + selectedSource);
      }
   };

   public void setSelectedSource(int row)
   {
      SourcePanel sourcePanel = sourcePanelMap.get(row);
      if (sourcePanel != null)
      {
         HasValue<Boolean> selectSourceButton = sourcePanel.getSelectSourceButtonList().get(0);
         if (selectSourceButton != null)
         {
            selectSourceButton.setValue(true, true);
         }
      }
   }

   public String getSelectedSource()
   {
      return selectedSource;
   }

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

      sourcePanel.updateData(value, messages);

      List<HasValue<Boolean>> selectSourceList = sourcePanel.getSelectSourceButtonList();

      for (HasValue<Boolean> selectSource : selectSourceList)
      {
         selectSource.addValueChangeHandler(selectSourceHandler);
      }
      return sourcePanel;
   }
}
