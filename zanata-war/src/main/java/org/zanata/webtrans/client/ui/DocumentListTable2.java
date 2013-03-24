/*
 * Copyright 2012, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.webtrans.client.ui;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.zanata.webtrans.client.Application;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.util.DateUtil;
import org.zanata.webtrans.client.view.DocumentListDisplay;
import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;

import com.google.common.base.Strings;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.Widget;

public class DocumentListTable2 extends FlexTable
{
   private static int PATH_COLUMN = 0;
   private static int DOC_COLUMN = 1;
   private static int STATS_COLUMN = 2;
   private static int TRANSLATED_COLUMN = 3;
   private static int UNTRANSLATED_COLUMN = 4;
   private static int REMAINING_COLUMN = 5;
   private static int LAST_UPLOAD_COLUMN = 6;
   private static int LAST_TRANSLATED_COLUMN = 7;
   private static int ACTION_COLUMN = 8;
   
   private final UserWorkspaceContext userWorkspaceContext;
   private final DocumentListDisplay.Listener listener;
   private final WebTransMessages messages;

   public DocumentListTable2(final UserWorkspaceContext userWorkspaceContext, final DocumentListDisplay.Listener listener, final WebTransMessages messages)
   {
      super();
      setStylePrimaryName("DocumentListTable");

      this.userWorkspaceContext = userWorkspaceContext;
      this.listener = listener;
      this.messages = messages;
   }

   public void buildTable(List<DocumentInfo> documentInfoList)
   {
      for (int i = 0; i < documentInfoList.size(); i++)
      {
         DocumentInfo info = documentInfoList.get(i);

         this.setWidget(i + 1, PATH_COLUMN, new InlineLabel(info.getPath()));
         this.setWidget(i + 1, DOC_COLUMN, getDocWidget(info));

         this.setWidget(i + 1, STATS_COLUMN, getStatsWidget(info));
         this.setWidget(i + 1, TRANSLATED_COLUMN, new InlineLabel(messages.statusBarLabelHours(info.getStats().getWordCount().getApproved())));
         this.setWidget(i + 1, UNTRANSLATED_COLUMN, new InlineLabel(messages.statusBarLabelHours(info.getStats().getWordCount().getUntranslated())));
         this.setWidget(i + 1, REMAINING_COLUMN, new InlineLabel(messages.statusBarLabelHours(info.getStats().getRemainingHours())));

         this.setWidget(i + 1, LAST_UPLOAD_COLUMN, new InlineLabel(getAuditInfo(info.getLastModifiedBy(), info.getLastChanged())));
         this.setWidget(i + 1, LAST_TRANSLATED_COLUMN, new InlineLabel(getAuditInfo(info.getLastTranslatedBy(), info.getLastTranslatedDate())));

         this.setWidget(i + 1, ACTION_COLUMN, getActionWidget(info));
      }
      
      this.getColumnFormatter().addStyleName(PATH_COLUMN, "pathCol");
      this.getColumnFormatter().addStyleName(DOC_COLUMN, "docCol");
      this.getColumnFormatter().addStyleName(STATS_COLUMN, "statisticCol");
      this.getColumnFormatter().addStyleName(TRANSLATED_COLUMN, "translatedCol");
      this.getColumnFormatter().addStyleName(UNTRANSLATED_COLUMN, "untranslatedCol");
      this.getColumnFormatter().addStyleName(REMAINING_COLUMN, "remainingCol");
      this.getColumnFormatter().addStyleName(LAST_UPLOAD_COLUMN, "auditCol");
      this.getColumnFormatter().addStyleName(LAST_TRANSLATED_COLUMN, "auditCol");
      this.getColumnFormatter().addStyleName(ACTION_COLUMN, "actionCol");

   }

   private Widget getDocWidget(final DocumentInfo docInfo)
   {
      InlineLabel docLabel = new InlineLabel(docInfo.getName());
      docLabel.addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            listener.fireDocumentSelection(docInfo);
         }
      });
      return docLabel;
   }

   private Widget getStatsWidget(DocumentInfo docInfo)
   {
      final TransUnitCountGraph graph = new TransUnitCountGraph(messages, true);
      graph.setStats(docInfo.getStats(), true);
      graph.addMouseOutHandler(new MouseOutHandler()
      {
         @Override
         public void onMouseOut(MouseOutEvent event)
         {
            graph.onMouseOut();
         }
      });
      graph.addMouseOverHandler(new MouseOverHandler()
      {
         @Override
         public void onMouseOver(MouseOverEvent event)
         {
            graph.onMouseOver(getElement().getFirstChildElement());
         }
      });
      return graph;
   }

   private Widget getActionWidget(final DocumentInfo docInfo)
   {
      HorizontalPanel panel = new HorizontalPanel();
      for (Map.Entry<String, String> entry : docInfo.getDownloadExtensions().entrySet())
      {
         Anchor anchor = new Anchor(entry.getKey());
         anchor.setStyleName("downloadFileLink");
         anchor.setHref(Application.getFileDownloadURL(userWorkspaceContext.getWorkspaceContext().getWorkspaceId(), entry.getValue()));
         anchor.setTarget("_blank");
         panel.add(anchor);
      }
      PushButton uploadButton = new PushButton("Upload");
      uploadButton.addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            listener.showUploadDialog(docInfo);
         }
      });
      panel.add(uploadButton);

      return panel;
   }


   private String getAuditInfo(String by, Date date)
   {
      StringBuilder sb = new StringBuilder();

      if (date != null)
      {
         sb.append(DateUtil.formatShortDate(date));
      }
      if (!Strings.isNullOrEmpty(by))
      {
         sb.append(" by ");
         sb.append(by);
      }
      return sb.toString();
   }
}
