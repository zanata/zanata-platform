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

import org.zanata.webtrans.client.view.ReviewCommentDisplay;
import org.zanata.webtrans.shared.model.ReviewComment;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Singleton;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Singleton
public class ReviewCommentDataProvider extends ListDataProvider<ReviewComment>
{
   public ReviewCommentDataProvider()
   {
      super(ReviewCommentDisplay.COMMENT_PROVIDES_KEY);
   }

   public void setLoading(boolean loading)
   {
      if (loading)
      {
         updateRowCount(0, false);
      }
      else
      {
         updateRowCount(getList().size(), true);
      }
   }
}
