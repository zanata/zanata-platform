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

package org.zanata.webtrans.client.view;

import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.ui.CellTableResources;
import org.zanata.webtrans.client.ui.DialogBoxCloseButton;
import org.zanata.webtrans.client.util.DateUtil;
import org.zanata.webtrans.shared.model.ReviewComment;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Singleton;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Singleton
public class ReviewCommentView extends DialogBox implements ReviewCommentDisplay
{
   private static final CellTableResources CELL_TABLE_RESOURCES = GWT.create(CellTableResources.class);
   private static AddReviewCommentViewUiBinder ourUiBinder = GWT.create(AddReviewCommentViewUiBinder.class);

   @UiField(provided = true)
   DialogBoxCloseButton closeButton;
   @UiField
   WebTransMessages messages;
   @UiField
   VerticalPanel commentsContainer;

   private final CellTable<ReviewComment> commentTable;
   private Listener listener;
   private TextBox commentInputBox;

   public ReviewCommentView()
   {
      super(true, true);
      closeButton = new DialogBoxCloseButton(this);
      HTMLPanel root = ourUiBinder.createAndBindUi(this);
      setGlassEnabled(true);

      commentTable = setUpTable();

      SimplePager simplePager = new SimplePager();
      simplePager.setDisplay(commentTable);

      commentsContainer.add(commentTable);
      commentsContainer.add(simplePager);
      commentsContainer.add(createCommentInput());

      setWidget(root);
   }

   private Widget createCommentInput()
   {
      FlowPanel panel = new FlowPanel();
      commentInputBox = new TextBox();
      panel.add(commentInputBox);
      // TODO pahuang localize
      Button addButton = new Button("Add", new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            listener.addComment(commentInputBox.getValue());
         }
      });
      panel.add(addButton);
      return panel;
   }

   private CellTable<ReviewComment> setUpTable()
   {
      CellTable<ReviewComment> table = new CellTable<ReviewComment>(15, CELL_TABLE_RESOURCES, COMMENT_PROVIDES_KEY);
      table.setEmptyTableWidget(new Label(messages.noContent()));
      table.setLoadingIndicator(new Label(messages.loading()));

      Column<ReviewComment, String> commentColumn = createCommentColumn();
      Column<ReviewComment, String> commenterColumn = createCommenterColumn();
      Column<ReviewComment, String> commentedDateColumn = createCommentedDateColumn();

      table.addColumn(commenterColumn, messages.modifiedBy());
      table.setColumnWidth(commenterColumn, 10, Style.Unit.PCT);

      table.addColumn(commentedDateColumn, messages.modifiedDate());
      table.setColumnWidth(commentedDateColumn, 20, Style.Unit.PCT);

      table.addColumn(commentColumn, messages.reviewComment());
      table.setColumnWidth(commentColumn, 70, Style.Unit.PCT);

      return table;
   }

   private static Column<ReviewComment, String> createCommentColumn()
   {
      return new Column<ReviewComment, String>(new TextCell())
      {
         @Override
         public String getValue(ReviewComment object)
         {
            return object.getComment();
         }
      };
   }

   private static Column<ReviewComment, String> createCommenterColumn()
   {
      return new Column<ReviewComment, String>(new TextCell())
      {
         @Override
         public String getValue(ReviewComment item)
         {
            return item.getCommenterName();
         }
      };
   }

   private static Column<ReviewComment, String> createCommentedDateColumn()
   {
      return new Column<ReviewComment, String>(new TextCell())
      {
         @Override
         public String getValue(ReviewComment item)
         {
            return DateUtil.formatShortDate(item.getCreationDate());
         }
      };
   }

   @Override
   public void clearInput()
   {
      commentInputBox.setText("");
   }

   @Override
   public void setDataProvider(ListDataProvider<ReviewComment> dataProvider)
   {
      dataProvider.addDataDisplay(commentTable);
   }

   @Override
   public void setListener(Listener listener)
   {
      this.listener = listener;
   }

   interface AddReviewCommentViewUiBinder extends UiBinder<HTMLPanel, ReviewCommentView>
   {
   }
}