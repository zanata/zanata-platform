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
package org.zanata.action

import org.assertj.core.api.Assertions.assertThat
import org.jglue.cdiunit.InRequestScope
import org.jglue.cdiunit.InSessionScope
import org.jglue.cdiunit.deltaspike.SupportDeltaspikeCore
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.zanata.config.MTServiceURL
import org.zanata.dao.DocumentDAO
import org.zanata.dao.LocaleDAO
import org.zanata.dao.ProjectIterationDAO
import org.zanata.dao.WebHookDAO
import org.zanata.file.FilePersistService
import org.zanata.i18n.Messages
import org.zanata.model.HDocument
import org.zanata.model.HProject
import org.zanata.model.HProjectIteration
import org.zanata.rest.service.VirusScanner
import org.zanata.security.ZanataIdentity
import org.zanata.service.DocumentService
import org.zanata.service.LocaleService
import org.zanata.service.TranslationFileService
import org.zanata.service.TranslationService
import org.zanata.service.TranslationStateCache
import org.zanata.service.VersionStateCache
import org.zanata.service.impl.WebhookServiceImpl
import org.zanata.test.CdiUnitRunner
import org.zanata.util.UrlUtil
import java.net.URI
import javax.enterprise.inject.Produces
import javax.inject.Inject

/**
 * @author djansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
@InSessionScope
@InRequestScope
@RunWith(CdiUnitRunner::class)
@SupportDeltaspikeCore
class VersionHomeActionTest {

    @Mock @Produces
    private lateinit var copyVersionManager: CopyVersionManager
    @Mock @Produces
    private lateinit var mergeTranslationsManager: MergeTranslationsManager
    @Mock @Produces
    private lateinit var copyTransManager: CopyTransManager
    @Mock @Produces
    private lateinit var projectIterationDAO: ProjectIterationDAO
    @Mock @Produces
    private lateinit var documentDAO: DocumentDAO
    @Mock @Produces
    private lateinit var localeServiceImpl: LocaleService
    @Mock @Produces
    private lateinit var versionStateCacheImpl: VersionStateCache
    @Mock @Produces
    private lateinit var translationStateCacheImpl: TranslationStateCache
    @Mock @Produces
    private lateinit var msgs: Messages
    @Mock @Produces
    private lateinit var documentServiceImpl: DocumentService
    @Mock @Produces
    private lateinit var identity: ZanataIdentity
    @Mock @Produces
    private lateinit var translationFileServiceImpl: TranslationFileService
    @Mock @Produces
    private lateinit var virusScanner: VirusScanner
    @Mock @Produces
    private lateinit var localeDAO: LocaleDAO
    @Mock @Produces
    private lateinit var translationServiceImpl: TranslationService
    @Mock @Produces
    private lateinit var conversationScopeMessages: org.zanata.seam.scope.ConversationScopeMessages
    @Mock @Produces
    private lateinit var filePersistService: FilePersistService
    @Mock @Produces
    private lateinit var urlUtil: UrlUtil
    @Mock @Produces
    private lateinit var webhookService: WebhookServiceImpl
    @Mock @Produces
    private lateinit var webHookDAO: WebHookDAO
    @Produces @MTServiceURL
    private var mtServiceURL = URI.create("nothing")
    @Mock @Produces
    private lateinit var copyVersionHandler: VersionHomeAction.CopyVersionHandler

    @Inject
    private lateinit var action: VersionHomeAction

    @Test
    fun hasDocumentsReturnsTrueWhenNotEmpty() {
        val hProjectIteration = HProjectIteration().apply {
            slug = "myversion"
            project = HProject().apply {
                slug = "myproject"
            }
            documents = mutableMapOf("testdoc" to HDocument().apply {
                name = "testdoc"
                docId = "testdoc"})
        }
        action.projectSlug = "myproject"
        action.versionSlug = "myversion"
        `when`<HProjectIteration>(projectIterationDAO
                .getBySlug("myproject", "myversion"))
                .thenReturn(hProjectIteration)
        assertThat(action.hasDocuments()).isTrue()
    }

    @Test
    fun hasDocumentsReturnsFalseWhenEmpty() {
        val hProjectIteration = HProjectIteration().apply {
            slug = "myversion"
            project = HProject().apply {
                slug = "myproject"
            }
            documents = mutableMapOf()
        }
        action.projectSlug = "myproject"
        action.versionSlug = "myversion"
        `when`<HProjectIteration>(projectIterationDAO
                .getBySlug("myproject", "myversion"))
                .thenReturn(hProjectIteration)
        assertThat(action.hasDocuments()).isFalse()
    }
}
