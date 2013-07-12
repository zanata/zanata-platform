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

package org.zanata.webtrans.server.rpc;

import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.dao.TextFlowTargetReviewCommentsDAO;
import org.zanata.model.HTextFlowTargetReviewComment;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.shared.model.ReviewComment;
import org.zanata.webtrans.shared.model.ReviewCommentId;
import org.zanata.webtrans.shared.rpc.GetReviewCommentsAction;
import org.zanata.webtrans.shared.rpc.GetReviewCommentsResult;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Name("webtrans.gwt.GetReviewCommentsHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(GetReviewCommentsAction.class)
public class GetReviewCommentsHandler extends AbstractActionHandler<GetReviewCommentsAction, GetReviewCommentsResult>
{
   @In
   private TextFlowTargetReviewCommentsDAO textFlowTargetReviewCommentsDAO;

   @Override
   public GetReviewCommentsResult execute(GetReviewCommentsAction action, ExecutionContext context) throws ActionException
   {
      List<HTextFlowTargetReviewComment> hComments = textFlowTargetReviewCommentsDAO.getReviewComments(action.getTransUnitId(), action.getWorkspaceId().getLocaleId());

      List<ReviewComment> comments = Lists.transform(hComments, new Function<HTextFlowTargetReviewComment, ReviewComment>()
      {
         @Override
         public ReviewComment apply(HTextFlowTargetReviewComment input)
         {
            return new ReviewComment(new ReviewCommentId(input.getId()), input.getComment(), input.getCommenterName(), input.getCreationDate(), input.getTargetVersion());
         }
      });
      // we re-wrap the list because gwt rpc doesn't like other list implementation
      return new GetReviewCommentsResult(Lists.newArrayList(comments));
   }

   @Override
   public void rollback(GetReviewCommentsAction action, GetReviewCommentsResult result, ExecutionContext context) throws ActionException
   {

   }
}
