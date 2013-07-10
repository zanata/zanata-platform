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

package org.zanata.webtrans.client.ui;

import org.zanata.webtrans.client.util.DateUtil;
import org.zanata.webtrans.shared.model.ReviewComment;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineHTML;

public class ReviewCommentItemLine extends Composite
{
   private static ReviewCommentItemLineUiBinder ourUiBinder = GWT.create(ReviewCommentItemLineUiBinder.class);
   private static ReviewCommentItemTemplate template = GWT.create(ReviewCommentItemTemplate.class);

   @UiField(provided = true)
   InlineHTML heading;
   @UiField(provided = true)
   InlineHTML commentContent;
   @UiField(provided = true)
   InlineHTML commentTime;

   public ReviewCommentItemLine(ReviewComment comment)
   {
      heading = new InlineHTML(template.heading(comment.getCommenterName()));
      commentContent = new InlineHTML(template.content(comment.getComment()));
      commentTime = new InlineHTML(template.timestamp(DateUtil.formatShortDate(comment.getCreationDate())));

      initWidget(ourUiBinder.createAndBindUi(this));
   }

   interface ReviewCommentItemLineUiBinder extends UiBinder<HTMLPanel, ReviewCommentItemLine>
   {
   }

   interface ReviewCommentItemTemplate extends SafeHtmlTemplates
   {
      @Template("<div class='text--meta'>{0} left a comment</div>")
      SafeHtml heading(String person);

      @Template("<div class='l--pad-v-half'>{0}</div>")
      SafeHtml content(String comment);

      @Template("<ul class='text--meta list--horizontal'><li>{0}</li></ul>")
      SafeHtml timestamp(String commentTime);
   }
}