/*
 * Copyright 2010, Red Hat, Inc. and individual contributors
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
package org.zanata.rest.editor.service;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.dao.TextFlowDAO;
import org.zanata.dao.TextFlowTargetReviewCommentsDAO;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.model.HTextFlowTargetHistory;
import org.zanata.model.HTextFlowTargetReviewComment;
import org.zanata.rest.editor.service.resource.TransUnitHistoryResource;
import org.zanata.service.LocaleService;
import org.zanata.webtrans.server.rpc.GetTranslationHistoryHandler;
import org.zanata.webtrans.shared.model.ReviewComment;
import org.zanata.webtrans.shared.model.ReviewCommentId;
import org.zanata.webtrans.shared.model.TransHistoryItem;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.rpc.GetTranslationHistoryResult;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

/**
 * @author Earl Floden <a href="mailto:efloden@redhat.com">efloden@redhat.com</a>
 */
@RequestScoped
@Named("editor.transUnitHistoryService")
@Path(TransUnitHistoryResource.SERVICE_PATH)
@Transactional(readOnly = true)
public class TransUnitHistoryService implements TransUnitHistoryResource {
    @Inject
    private LocaleService localeServiceImpl;

    @Inject
    private TextFlowDAO textFlowDAO;

    @Inject
    private TransUnitUtils transUnitUtils;

    @Inject
    private TextFlowTargetReviewCommentsDAO textFlowTargetReviewCommentsDAO;

    @Override
    public Response get(String localeId, Long transUnitId) {
        HLocale hLocale = localeServiceImpl.getByLocaleId(localeId);
        TransUnitId tUnitId = new TransUnitId(transUnitId);
        HTextFlow hTextFlow =
                textFlowDAO.findById(transUnitId, false);
        HTextFlowTarget hTextFlowTarget =
                hTextFlow.getTargets().get(hLocale.getId());
        Map<Integer, HTextFlowTargetHistory> history =
                hTextFlowTarget.getHistory();
        Iterable<TransHistoryItem> historyItems =
            Iterables.transform(history.values(),
                new GetTranslationHistoryHandler.TargetHistoryToTransHistoryItemFunction());
        List<ReviewComment> reviewComments = getReviewComments(tUnitId, hLocale);
        GetTranslationHistoryResult result =
            new GetTranslationHistoryResult(Lists.newArrayList(historyItems),
            null, Lists.newArrayList(reviewComments));
        return Response.ok(result).build();
    }

    protected List<ReviewComment>
        getReviewComments(TransUnitId transUnitId, HLocale hLocale) {
        List<HTextFlowTargetReviewComment> hComments =
            textFlowTargetReviewCommentsDAO.getReviewComments(
                transUnitId,
                hLocale.getLocaleId());
        return Lists.transform(hComments, input -> new ReviewComment(
                new ReviewCommentId(input.getId()),
                input.getCommentText(), input.getCommenterName(),
                input.getCreationDate()
            ));
    }
}
