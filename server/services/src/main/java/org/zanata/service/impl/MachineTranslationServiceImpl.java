/*
 * Copyright 2018, Red Hat, Inc. and individual contributors
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
package org.zanata.service.impl;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.async.Async;
import org.zanata.async.AsyncTaskResult;
import org.zanata.async.handle.MachineTranslationPrefillTaskHandle;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.config.MTServiceToken;
import org.zanata.config.MTServiceURL;
import org.zanata.config.MTServiceUser;
import org.zanata.dao.TextFlowDAO;
import org.zanata.exception.ZanataServiceException;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.model.type.TranslationSourceType;
import org.zanata.service.LocaleService;
import org.zanata.service.MachineTranslationService;
import org.zanata.service.TranslationService;
import org.zanata.service.VersionStateCache;
import org.zanata.service.mt.TextFlowsToMTDoc;
import org.zanata.service.mt.dto.MTDocument;
import org.zanata.service.mt.dto.TypeString;
import org.zanata.transaction.TransactionUtil;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.model.TransUnitUpdateRequest;
import org.zanata.webtrans.shared.search.FilterConstraints;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @author Patrick Huang
 * <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RequestScoped
public class MachineTranslationServiceImpl implements
        MachineTranslationService {
    private static final Logger log =
            LoggerFactory.getLogger(MachineTranslationServiceImpl.class);

    private URI mtServiceURL;
    private String mtUser;
    private String mtToken;
    private TextFlowsToMTDoc textFlowsToMTDoc;
    private TextFlowDAO textFlowDAO;
    private LocaleService localeService;
    private EntityManager entityManager;
    private TransactionUtil transactionUtil;
    private TranslationService translationService;
    private VersionStateCache versionStateCache;

    public MachineTranslationServiceImpl() {
    }

    @Inject
    public MachineTranslationServiceImpl(@MTServiceURL URI mtServiceURL,
            @MTServiceUser String mtUser,
            @MTServiceToken String mtToken,
            TextFlowsToMTDoc textFlowsToMTDoc,
            TextFlowDAO textFlowDAO,
            LocaleService localeService,
            EntityManager entityManager,
            TransactionUtil transactionUtil,
            TranslationService translationService,
            VersionStateCache versionStateCache) {
        this.mtServiceURL = mtServiceURL;
        this.mtUser = mtUser;
        this.mtToken = mtToken;
        this.textFlowsToMTDoc = textFlowsToMTDoc;
        this.textFlowDAO = textFlowDAO;
        this.localeService = localeService;
        this.entityManager = entityManager;
        this.transactionUtil = transactionUtil;
        this.translationService = translationService;
        this.versionStateCache = versionStateCache;
    }

    @Override
    public List<String> getSuggestion(@Nonnull HTextFlow textFlow,
            @Nonnull LocaleId fromLocale,
            @Nonnull LocaleId toLocale) {

        HDocument document = textFlow.getDocument();
        String docId = document.getDocId();
        String versionSlug = document.getProjectIteration().getSlug();
        String projectSlug = document.getProjectIteration().getProject().getSlug();

        MTDocument doc = textFlowsToMTDoc.fromSingleTextFlow(projectSlug,
                versionSlug, docId, fromLocale, textFlow);

        try {
            MTDocument result = getTranslationFromMT(doc, toLocale);
            return result.getContents().stream().map(TypeString::getValue)
                    .collect(Collectors.toList());
        } catch (ZanataServiceException e) {
            log.error("failed to get translations from machine translation");
            return Collections.emptyList();
        }

    }

    private MTDocument getTranslationFromMT(MTDocument request,
            LocaleId toLocale) throws ZanataServiceException {
        ResteasyClient client = new ResteasyClientBuilder().build();

        ResteasyWebTarget webTarget = client.target(mtServiceURL).path("api")
                .path("document").path("translate")
                .queryParam("toLocaleCode", toLocale.getId());
        Response response = webTarget.request(MediaType.APPLICATION_JSON_TYPE)
                .header("X-Auth-User", mtUser)
                .header("X-Auth-Token", mtToken)
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .header("Accept", MediaType.APPLICATION_JSON)
                .post(Entity.json(request));

        if (response.getStatus() == 200) {
            MTDocument result = response.readEntity(MTDocument.class);
            if (!result.getWarnings().isEmpty()) {
                log.warn("Machine translation returns warning: {}", result.getWarnings());
            }
            return result;
        } else {
            log.warn("Machine translation return status: {}, header: {}, body: {}",
                    response.getStatus(), response.getHeaders(),
                    response.readEntity(String.class));
            throw new ZanataServiceException("failed to get translations from machine translation");
        }
    }

    @Async
    @Override
    public Future<Void> prefillWithMachineTranslation(
            Long versionId, LocaleId targetLocaleId,
            @Nonnull MachineTranslationPrefillTaskHandle taskHandle) {
        // need to reload all entities
        HProjectIteration version = entityManager.find(HProjectIteration.class, versionId);
        HLocale targetLocale = localeService.getByLocaleId(targetLocaleId);
        Map<String, HDocument> documents = version.getDocuments();
        if (documents.isEmpty()) {
            log.warn("no document in this version {}", version.userFriendlyToString());
            return AsyncTaskResult.completed();
        }

        // TODO this assumes all documents using same source locale
        // TODO some document don't have source locale (null)
        LocaleId fromLocale =
                documents.values().iterator().next().getSourceLocaleId();

        Stopwatch overallStopwatch = Stopwatch.createStarted();
        Long targetVersionId = version.getId();
        String projectSlug = version.getProject().getSlug();
        String versionSlug = version.getSlug();

        Map<String, List<HTextFlow>> docUrlToTextFlows = Maps.newHashMap();
        List<MTDocument> mtDocuments = Lists.newArrayList();
        for (HDocument doc : documents.values()) {
            DocumentId documentId = new DocumentId(doc.getId(),
                    doc.getDocId());
            List<HTextFlow> untranslatedTextFlows=
                    textFlowDAO
                            .getAllTextFlowByDocumentIdWithConstraints(
                                    documentId, targetLocale,
                                    FilterConstraints.builder()
                                            .keepNone().includeNew()
                                            .build());
            MTDocument mtDocument = textFlowsToMTDoc
                    .fromTextFlows(projectSlug, versionSlug,
                            doc.getDocId(), fromLocale,
                            untranslatedTextFlows);
            docUrlToTextFlows.put(mtDocument.getUrl(), untranslatedTextFlows);
            mtDocuments.add(mtDocument);
        }


        log.info("prepare to send {} of documents to MT", mtDocuments.size());

        List<MTDocument> result = mtDocuments.stream()
                .map(mtDoc -> getTranslationFromMT(mtDoc,
                        targetLocale.getLocaleId()))
                .collect(Collectors.toList());

        log.info("get back result: {}", result.size());

        taskHandle.setMaxProgress(mtDocuments.size());


        boolean cancelled = false;
        for (int i = 0; i < mtDocuments.size() && !(cancelled = taskHandle.isCancelled()); i++) {
            MTDocument sourceDoc = mtDocuments.get(i);
            MTDocument transDoc = result.get(i);

            translateInBatch(docUrlToTextFlows.get(sourceDoc.getUrl()), transDoc,
                    targetLocale);

            taskHandle.increaseProgress(1);
        }
        versionStateCache.clearVersionStatsCache(targetVersionId);

        log.info("{} prefill translation with machine translations for version {}, {}",
                cancelled ? "CANCELLED" : "COMPLETED",
                version.userFriendlyToString(), overallStopwatch);
        return AsyncTaskResult.completed();
    }

    private void translateInBatch(List<HTextFlow> textFlows,
            MTDocument transDoc,
            HLocale targetLocale) {
        int index = 0;
        while (index < textFlows.size()) {
            int bound = Math.min(index + BATCH_SIZE, textFlows.size());
            List<HTextFlow> batch = textFlows.subList(index, bound);
            List<TypeString> transContentBatch =
                    transDoc.getContents().subList(index, bound);

            List<TransUnitUpdateRequest> updateRequests = Lists.newArrayList();
            index += bound;
            for (int i = 0; i < batch.size(); i++) {
                HTextFlow textFlow = batch.get(i);
                HTextFlowTarget maybeTarget =
                        textFlow.getTargets().get(targetLocale.getId());
                int baseRevision = maybeTarget == null ? 0 : maybeTarget.getVersionNum();
                List<String> translation = Lists.newArrayList(
                        transContentBatch.get(i).getValue());
                // TODO saved state should be configurable from UI
                // TODO TranslationSourceType only says MT but without provider name
                TransUnitUpdateRequest updateRequest =
                        new TransUnitUpdateRequest(
                                new TransUnitId(textFlow.getId()),
                                translation, ContentState.NeedReview,
                                baseRevision,
                                TranslationSourceType.MACHINE_TRANS
                                        .getAbbr());
                updateRequest.addRevisionComment("Translated by Google");
                updateRequests.add(updateRequest);
            }
            try {
                transactionUtil.run(() -> {
                    translationService.translate(targetLocale.getLocaleId(), updateRequests);
                });
            } catch (Exception e) {
                log.error("error prefilling translation with machine translation", e);
            }
        }
    }

    private boolean shouldTranslate(ContentState contentState) {
        // right now we only target untranslated. We might target more in the future
        return contentState.isUntranslated();
    }

}
