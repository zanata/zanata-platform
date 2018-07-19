/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.rest.editor.service;

import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.common.LocaleId;
import org.zanata.dao.DocumentDAO;
import org.zanata.model.HDocument;
import org.zanata.rest.dto.TranslationSourceType;
import org.zanata.rest.RestUtil;
import org.zanata.rest.dto.stats.ContainerTranslationStatistics;
import org.zanata.rest.dto.stats.TranslationStatistics.StatUnit;
import org.zanata.rest.editor.dto.EditorTranslationStatistics;
import org.zanata.rest.editor.service.resource.StatisticResource;
import com.google.common.collect.Lists;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@RequestScoped
@Named("editor.statisticService")
@Path(StatisticResource.SERVICE_PATH)
@Transactional(readOnly = true)
public class StatisticsService implements StatisticResource {
    @Inject
    private DocumentDAO documentDAO;

    @Override
    public Response getDocumentStatistics(
            @PathParam("projectSlug") String projectSlug,
            @PathParam("versionSlug") String versionSlug,
            @PathParam("docId") String docId,
            @PathParam("localeId") String localeId) {
        docId = RestUtil.convertFromDocumentURIId(docId);
        HDocument doc = documentDAO.getByProjectIterationAndDocId(projectSlug,
                versionSlug, docId);
        if (doc == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        LocaleId locId = new LocaleId(localeId);
        ContainerTranslationStatistics docStats =
                getDocStatistics(doc.getId(), locId);
        int[] mtStats = documentDAO
            .getStatisticsBySourceType(doc.getId(), locId,
                TranslationSourceType.MACHINE_TRANS);

        EditorTranslationStatistics docWordStatistic =
            EditorTranslationStatistics
                .getInstance(docStats.getStats(localeId, StatUnit.WORD));
        docWordStatistic.setMt(mtStats[1]);

        EditorTranslationStatistics docMsgStatistic =
            EditorTranslationStatistics
                .getInstance(docStats.getStats(localeId, StatUnit.MESSAGE));
        docMsgStatistic.setMt(mtStats[0]);

        Object entity = new GenericEntity<List<EditorTranslationStatistics>>(
                Lists.newArrayList(docWordStatistic, docMsgStatistic)) {};
        return Response.ok(entity).build();
    }
    // TODO: need to merge with StatisticsServiceImpl.getDocStatistics

    public ContainerTranslationStatistics getDocStatistics(Long documentId,
            LocaleId localeId) {
        ContainerTranslationStatistics result =
                documentDAO.getStatistics(documentId, localeId);
        return result;
    }

    public StatisticsService() {
    }

    @java.beans.ConstructorProperties({ "documentDAO" })
    protected StatisticsService(final DocumentDAO documentDAO) {
        this.documentDAO = documentDAO;
    }
}
