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

package org.zanata.dao;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import javax.inject.Named;
import org.zanata.common.LocaleId;
import org.zanata.model.HTextFlowTargetReviewComment;
import org.zanata.webtrans.shared.model.TransUnitId;

/**
 *
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Named("textFlowTargetReviewCommentsDAO")

@javax.enterprise.context.Dependent
public class TextFlowTargetReviewCommentsDAO extends
        AbstractDAOImpl<HTextFlowTargetReviewComment, Long> {
    @SuppressWarnings("unused")
    public TextFlowTargetReviewCommentsDAO() {
        super(HTextFlowTargetReviewComment.class);
    }

    @SuppressWarnings("unused")
    public TextFlowTargetReviewCommentsDAO(Session session) {
        super(HTextFlowTargetReviewComment.class, session);
    }

    public List<HTextFlowTargetReviewComment> getReviewComments(
            TransUnitId textFlowId, LocaleId localeId) {
        Query query =
                getSession()
                        .createQuery(
                                "select c from HTextFlowTargetReviewComment c join fetch c.commenter where c.textFlowTarget.textFlow.id = :textFlowId and c.textFlowTarget.locale.localeId = :localeId");
        query.setParameter("textFlowId", textFlowId.getValue());
        query.setParameter("localeId", localeId);
        query.setComment("TextFlowTargetReviewCommentsDAO.getReviewComments");
        query.setCacheable(true);
        return query.list();
    }
}
