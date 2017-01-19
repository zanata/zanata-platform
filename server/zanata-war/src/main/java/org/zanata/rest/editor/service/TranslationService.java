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

import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import org.apache.commons.lang.StringUtils;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.common.LocaleId;
import org.zanata.dao.LocaleDAO;
import org.zanata.dao.TextFlowDAO;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.rest.editor.dto.TransUnit;
import org.zanata.rest.editor.dto.TransUnits;
import org.zanata.rest.editor.dto.TranslationData;
import org.zanata.rest.editor.service.resource.TranslationResource;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;
import org.zanata.service.TranslationService.TranslationResult;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.model.TransUnitUpdateRequest;
import com.google.common.collect.Lists;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@RequestScoped
@Named("editor.translationService")
@Path(TranslationResource.SERVICE_PATH)
@Transactional
public class TranslationService implements TranslationResource {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(TranslationService.class);

    @Inject
    private ZanataIdentity identity;
    @Inject
    private TextFlowDAO textFlowDAO;
    @Inject
    private LocaleDAO localeDAO;
    @Inject
    private TransUnitUtils transUnitUtils;
    @Inject
    private org.zanata.service.TranslationService translationServiceImpl;
    @Inject
    private LocaleService localeServiceImpl;

    /**
     * See {@link org.zanata.model.type.TranslationSourceType#JS_EDITOR_ENTRY}
     */
    private final String sourceType = "JS";

    @Override
    public Response get(String localeId, String ids) {
        TransUnits transUnits = new TransUnits();
        if (StringUtils.isEmpty(ids)) {
            return Response.ok(transUnits).build();
        }
        List<Long> idList = TransUnitUtils.filterAndConvertIdsToList(ids);
        if (idList.size() > TransUnitUtils.MAX_SIZE) {
            String msg = String.format("More than %d results.",
                    TransUnitUtils.MAX_SIZE);
            log.warn(msg);
            return Response.status(Response.Status.FORBIDDEN).entity(msg)
                    .build();
        }
        HLocale locale = localeServiceImpl.getByLocaleId(localeId);
        List<Object[]> results =
                textFlowDAO.getTextFlowAndTarget(idList, locale.getId());
        for (Object[] result : results) {
            HTextFlow hTextFlow = (HTextFlow) result[0];
            TransUnit tu;
            if (result.length < 2 || result[1] == null) {
                tu = transUnitUtils.buildTargetTransUnit(hTextFlow, null,
                        locale.getLocaleId());
            } else {
                HTextFlowTarget hTarget = (HTextFlowTarget) result[1];
                tu = transUnitUtils.buildTargetTransUnit(hTextFlow, hTarget,
                        locale.getLocaleId());
            }
            transUnits.put(hTextFlow.getId().toString(), tu);
        }
        return Response.ok(transUnits).build();
    }

    @Override
    public Response put(String localeId, TranslationData data) {
        TranslationData requestData = data;
        HLocale locale = localeDAO.findByLocaleId(new LocaleId(localeId));
        HTextFlow textFlow =
                textFlowDAO.findById(requestData.getId().longValue());
        if (textFlow == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        identity.checkPermission("modify-translation",
                textFlow.getDocument().getProjectIteration().getProject(),
                locale);
        // //Only support 1 translation update for the moment
        TransUnitUpdateRequest request = new TransUnitUpdateRequest(
                new TransUnitId(requestData.getId().longValue()),
                requestData.getContents(), requestData.getStatus(),
                requestData.getRevision(), sourceType);
        List<TranslationResult> translationResults = translationServiceImpl
                .translate(new LocaleId(localeId), Lists.newArrayList(request));
        TranslationResult result = translationResults.get(0);
        if (result.isVersionNumConflict()) {
            return Response.status(Response.Status.CONFLICT).build();
        } else if (!result.isTranslationSuccessful()) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .build();
        } else {
            requestData
                    .setStatus(result.getTranslatedTextFlowTarget().getState());
            requestData.setRevision(
                    result.getTranslatedTextFlowTarget().getVersionNum());
            return Response.ok(requestData).build();
        }
    }
}
