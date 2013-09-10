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
package org.zanata.webtrans.client.view;

import java.util.ArrayList;
import java.util.List;

import org.zanata.webtrans.client.history.History;
import org.zanata.webtrans.client.history.HistoryToken;
import org.zanata.webtrans.client.presenter.UserConfigHolder;
import org.zanata.webtrans.client.ui.HasSelectableSource;
import org.zanata.webtrans.client.ui.SourcePanel;
import org.zanata.webtrans.client.ui.TransUnitDetailsPanel;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitId;

import com.google.common.base.Preconditions;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.zanata.webtrans.client.resources.UiMessages;
import org.zanata.webtrans.shared.model.TextFlowTarget;

public class SourceContentsView extends Composite implements SourceContentsDisplay
{
   
   public static final int COLUMNS = 1;
   public static final int DEFAULT_ROWS = 1;
   private final Grid sourcePanelContainer;
   private List<HasSelectableSource> sourcePanelList;
   private final TransUnitDetailsPanel transUnitDetailsPanel;
   
   private TransUnit transUnit;
   private final UserConfigHolder configHolder;
   private final History history;
   private Label referenceLabel; 
   private UiMessages messages;

   @Inject
   public SourceContentsView(Provider<TransUnitDetailsPanel> transUnitDetailsPanelProvider, UserConfigHolder configHolder, History history, final UiMessages messages)
   {
      this.configHolder = configHolder;
      this.history = history;
      this.messages = messages;
      sourcePanelList = new ArrayList<HasSelectableSource>();
      FlowPanel root = new FlowPanel();
      root.setSize("100%", "100%");

      FlowPanel container = new FlowPanel();
      container.setSize("100%", "100%");

      sourcePanelContainer = new Grid(DEFAULT_ROWS, COLUMNS);
      sourcePanelContainer.addStyleName("sourceTable");

      container.add(sourcePanelContainer);
      root.add(container);

      referenceLabel = new Label(messages.noReferenceFoundText());
      referenceLabel.addStyleName("referenceLabel");
      root.add(referenceLabel);
      hideReference(); //Reference is hidden by default

      InlineLabel bookmarkIcon = createBookmarkIcon();
      root.add(bookmarkIcon);

      transUnitDetailsPanel = transUnitDetailsPanelProvider.get();
      root.add(transUnitDetailsPanel);

      initWidget(root);
   }

   private InlineLabel createBookmarkIcon()
   {
      InlineLabel bookmarkIcon = new InlineLabel();
      bookmarkIcon.setStyleName("bookmark icon-bookmark-1");
      bookmarkIcon.addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            HistoryToken historyToken = history.getHistoryToken();
            historyToken.setTextFlowId(transUnit.getId().toString());
            history.newItem(historyToken);
         }
      });
      return bookmarkIcon;
   }

   @Override
   public List<HasSelectableSource> getSourcePanelList()
   {
      return sourcePanelList;
   }

   @Override
   public void setValue(TransUnit value)
   {
      setValue(value, false);
   }

   @Override
   public void setValue(TransUnit value, boolean fireEvents)
   {
      transUnit = value;
      transUnitDetailsPanel.setDetails(value);
      sourcePanelContainer.resizeRows(value.getSources().size());
      sourcePanelList.clear();

      int rowIndex = 0;
      boolean useCodeMirrorEditor = configHolder.getState().isUseCodeMirrorEditor();
      for (String source : value.getSources())
      {
         SourcePanel sourcePanel = new SourcePanel(transUnit.getId(), useCodeMirrorEditor);
         sourcePanel.setValue(source, value.getSourceComment(), value.isPlural());
         sourcePanelContainer.setWidget(rowIndex, 0, sourcePanel);
         sourcePanelList.add(sourcePanel);
         rowIndex++;
      }
      toggleTransUnitDetails(configHolder.getState().isShowOptionalTransUnitDetails());
   }

   @Override
   public void highlightSearch(String search)
   {
      for (Widget sourceLabel : sourcePanelContainer)
      {
         ((SourcePanel) sourceLabel).highlightSearch(search);
      }
   }

   @Override
   public void setSourceSelectionHandler(ClickHandler clickHandler)
   {
      Preconditions.checkState(!sourcePanelList.isEmpty(), "empty source panel list. Did you forget to call setValue() before this?");
      for (HasSelectableSource hasSelectableSource : sourcePanelList)
      {
         hasSelectableSource.addClickHandler(clickHandler);
      }
   }

   @Override
   public void refresh()
   {
      for (HasSelectableSource hasSelectableSource : sourcePanelList)
      {
         hasSelectableSource.refresh();
      }
   }

   @Override
   public void toggleTransUnitDetails(boolean showTransUnitDetails)
   {
      if (transUnitDetailsPanel.hasNoMetaInfo() && !showTransUnitDetails)
      {
         transUnitDetailsPanel.setVisible(false);
      }
      else
      {
         transUnitDetailsPanel.setVisible(true);
      }
   }

   @Override
   public void updateTransUnitDetails(TransUnit transUnit)
   {
      transUnitDetailsPanel.setDetails(transUnit);
   }

   @Override
   public TransUnitId getId()
   {
      return transUnit.getId();
   }

   @Override
   public void showReference(TextFlowTarget reference)
   {
      if (reference == null)
      {
         referenceLabel.setText(messages.noReferenceFoundText());
      }
      else
      {
         referenceLabel.setText(messages.inLocale() + " " + reference.getDisplayName() + ": " + reference.getContent());
      }
      referenceLabel.setVisible(true);
   }

   @Override
   public void hideReference()
   {
      referenceLabel.setVisible(false);
   }
}
