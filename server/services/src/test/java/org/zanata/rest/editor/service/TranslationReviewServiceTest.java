/*
 * Copyright 2018, Red Hat, Inc. and individual contributors
 *  as indicated by the @author tags. See the copyright.txt file in the
 *  distribution for a full listing of individual contributors.
 *
 *  This is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *
 *  This software is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this software; if not, write to the Free
 *  Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.zanata.rest.editor.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.common.ContentState;
import org.zanata.common.ContentType;
import org.zanata.common.EntityStatus;
import org.zanata.common.IssuePriority;
import org.zanata.common.LocaleId;
import org.zanata.dao.ReviewCriteriaDAO;
import org.zanata.dao.TextFlowTargetDAO;
import org.zanata.dao.TextFlowTargetReviewCommentsDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.model.ReviewCriteria;
import org.zanata.rest.editor.dto.ReviewData;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 **/
public class TranslationReviewServiceTest {
    private TranslationReviewService service;

    @Mock
    private TextFlowTargetDAO textFlowTargetDAO;

    @Mock
    private LocaleService localeServiceImpl;

    @Mock
    private ZanataIdentity identity;

    @Mock
    private ReviewCriteriaDAO reviewCriteriaDAO;

    @Mock
    private TextFlowTargetReviewCommentsDAO textFlowTargetReviewCommentsDAO;

    @Mock
    private HAccount authenticatedAccount;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        service = new TranslationReviewService(
                textFlowTargetDAO,
                localeServiceImpl,
                identity,
                reviewCriteriaDAO,
                textFlowTargetReviewCommentsDAO,
                authenticatedAccount);
    }

    @Test
    public void testNullData() {
        Response response = service.put(null, null);
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testNullLocale() {
        String locale = "de";
        ReviewData data = new ReviewData();
        when(localeServiceImpl.getByLocaleId(locale)).thenReturn(null);

        Response response = service.put(locale, data);
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testNullTft() {
        String locale = "de";
        LocaleId localeId = new LocaleId(locale);
        HLocale hLocale = new HLocale(localeId);
        ReviewData data = new ReviewData();
        data.setTransUnitId(1L);

        when(localeServiceImpl.getByLocaleId(locale)).thenReturn(hLocale);
        when(textFlowTargetDAO
                .getTextFlowTarget(data.getTransUnitId(), localeId))
                .thenReturn(null);

        Response response = service.put(locale, data);
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testNullTftUntranslated() {
        String locale = "de";
        ReviewData data = new ReviewData();
        data.setTransUnitId(1L);

        HTextFlowTarget tft =
                buildTft(locale, EntityStatus.ACTIVE, ContentState.New);
        HLocale hLocale = tft.getLocale();

        when(localeServiceImpl.getByLocaleId(locale)).thenReturn(hLocale);
        when(textFlowTargetDAO.getTextFlowTarget(data.getTransUnitId(),
                hLocale.getLocaleId())).thenReturn(tft);

        Response response = service.put(locale, data);
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testNonActiveProjectVersion() {
        String locale = "de";
        ReviewData data = new ReviewData();
        data.setTransUnitId(1L);

        HTextFlowTarget tft = buildTft(locale, EntityStatus.READONLY,
                ContentState.Translated);
        HLocale hLocale = tft.getLocale();

        when(localeServiceImpl.getByLocaleId(locale)).thenReturn(hLocale);
        when(textFlowTargetDAO.getTextFlowTarget(data.getTransUnitId(),
                hLocale.getLocaleId())).thenReturn(tft);

        Response response = service.put(locale, data);
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    public void testReviewCriteriaNull() {
        String locale = "de";
        ReviewData data = new ReviewData();
        data.setTransUnitId(1L);
        data.setReviewCriteriaId(1L);

        HTextFlowTarget tft = buildTft(locale, EntityStatus.ACTIVE,
                ContentState.Translated);
        HLocale hLocale = tft.getLocale();

        when(localeServiceImpl.getByLocaleId(locale)).thenReturn(hLocale);
        when(textFlowTargetDAO.getTextFlowTarget(data.getTransUnitId(),
                hLocale.getLocaleId())).thenReturn(tft);
        when(reviewCriteriaDAO.findById(data.getReviewCriteriaId())).thenReturn(null);

        Response response = service.put(locale, data);
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testPut() {
        String locale = "de";
        ReviewData data = new ReviewData();
        data.setTransUnitId(1L);
        data.setReviewCriteriaId(1L);

        HTextFlowTarget tft = buildTft(locale, EntityStatus.ACTIVE,
                ContentState.Translated);
        HLocale hLocale = tft.getLocale();

        ReviewCriteria reviewCriteria = new ReviewCriteria(IssuePriority.Major,
                true, "description");

        HPerson person = new HPerson();
        person.setName("name");

        when(authenticatedAccount.getPerson()).thenReturn(person);
        when(localeServiceImpl.getByLocaleId(locale)).thenReturn(hLocale);
        when(textFlowTargetDAO.getTextFlowTarget(data.getTransUnitId(),
                hLocale.getLocaleId())).thenReturn(tft);
        when(reviewCriteriaDAO.findById(data.getReviewCriteriaId()))
                .thenReturn(reviewCriteria);

        Response response = service.put(locale, data);

        verify(textFlowTargetReviewCommentsDAO).makePersistent(any());
        verify(textFlowTargetReviewCommentsDAO).flush();

        assertThat(response.getStatus())
                .isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.getEntity()).isNotNull();
        assertThat(response.getEntity()).isEqualTo(data);
    }

    private HTextFlowTarget buildTft(String localeId, EntityStatus status,
            ContentState contentState) {
        HProject project = new HProject();
        project.setStatus(status);

        HProjectIteration version = new HProjectIteration();
        version.setStatus(status);
        version.setProject(project);

        HDocument doc =
                new HDocument("", ContentType.PO, new HLocale(LocaleId.EN_US));
        doc.setProjectIteration(version);
        HLocale hLocale = new HLocale(new LocaleId(localeId));
        HTextFlow tf = new HTextFlow(doc, "resId", "content");

        HTextFlowTarget tft = new HTextFlowTarget(tf, hLocale);
        tft.setState(contentState);
        return tft;
    }
}
