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

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.jglue.cdiunit.InRequestScope
import org.jglue.cdiunit.InSessionScope
import org.jglue.cdiunit.deltaspike.SupportDeltaspikeCore
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.zanata.common.DocumentType
import org.zanata.common.EntityStatus
import org.zanata.common.LocaleId
import org.zanata.common.MergeType
import org.zanata.common.ProjectType
import org.zanata.config.MTServiceURL
import org.zanata.dao.DocumentDAO
import org.zanata.dao.LocaleDAO
import org.zanata.dao.ProjectIterationDAO
import org.zanata.dao.WebHookDAO
import org.zanata.exception.AuthorizationException
import org.zanata.file.FilePersistService
import org.zanata.i18n.Messages
import org.zanata.model.HDocument
import org.zanata.model.HLocale
import org.zanata.model.HProject
import org.zanata.model.HProjectIteration
import org.zanata.rest.dto.TranslationSourceType
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
import org.zanata.ui.faces.FacesMessages
import org.zanata.util.DateUtil
import org.zanata.util.UrlUtil
import java.net.URI
import java.time.Instant
import java.util.Date
import javax.enterprise.inject.Produces
import javax.faces.application.FacesMessage
import javax.inject.Inject
import kotlin.test.fail

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
    private lateinit var facesMessages: FacesMessages
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
        val hProjectIteration = genericProjectVersion().apply {
            documents = mutableMapOf("testdoc" to HDocument().apply {
                name = "testdoc"
                docId = "testdoc"})
        }
        action.projectSlug = "myproject"
        action.versionSlug = "myversion"
        whenever(projectIterationDAO
                .getBySlug("myproject", "myversion"))
                .thenReturn(hProjectIteration)
        assertThat(action.hasDocuments()).isTrue()
    }

    @Test
    fun hasDocumentsReturnsFalseWhenEmpty() {
        val hProjectIteration = genericProjectVersion().apply {
            documents = mutableMapOf()
        }
        action.projectSlug = "myproject"
        action.versionSlug = "myversion"
        whenever(projectIterationDAO
                .getBySlug("myproject", "myversion"))
                .thenReturn(hProjectIteration)
        assertThat(action.hasDocuments()).isFalse()
    }

    @Test
    fun userCanOnlyUploadADocumentToActiveProjectVersion() {
        val hProjectIteration = genericProjectVersion().apply {
            status = EntityStatus.ACTIVE
            project.status = EntityStatus.ACTIVE
            documents = mutableMapOf()
        }
        action.projectSlug = "myproject"
        action.versionSlug = "myversion"
        whenever(projectIterationDAO
                .getBySlug("myproject", "myversion"))
                .thenReturn(hProjectIteration)
        whenever(identity.hasPermissionWithAnyTargets(eq("import-template"), any()))
                .thenReturn(true)

        hProjectIteration.project.status = EntityStatus.READONLY
        hProjectIteration.status = EntityStatus.ACTIVE
        assertThat(action.isDocumentUploadAllowed).isFalse()

        hProjectIteration.project.status = EntityStatus.ACTIVE
        hProjectIteration.status = EntityStatus.READONLY
        assertThat(action.isDocumentUploadAllowed).isFalse()

        hProjectIteration.project.status = EntityStatus.ACTIVE
        hProjectIteration.status = EntityStatus.ACTIVE
        assertThat(action.isDocumentUploadAllowed).isTrue()
    }

    @Test
    fun userFriendlyTimeSinceLastChanged() {
        val document = HDocument()
        val aMinuteAndOneSecond = 61000L
        document.lastChanged = Date.from(Instant.now())

        assertThat(action.getLastUpdatedDescription(document))
                .isEqualTo("moments ago")

        // Note test may be unreliable (uses clock)
        document.lastChanged = Date.from(Instant.now()
                .minusMillis(10*aMinuteAndOneSecond))
        assertThat(action.getLastUpdatedDescription(document))
                .isEqualTo("10 minutes ago")

        document.lastChanged = Date.from(Instant.now()
                .minusMillis(60*aMinuteAndOneSecond))
        assertThat(action.getLastUpdatedDescription(document))
                .isEqualTo("1 hour ago")
    }

    @Test
    fun getFormattedDate() {
        val document = HDocument()
        val oct25th1996 = Date(846201600000)
        document.lastChanged = oct25th1996
        assertThat(action.getFormattedDate(document))
                .isEqualTo(DateUtil.formatShortDate(oct25th1996))
    }

    @Test
    fun failIfDocumentDeleteIsNotAllowed() {
        val hProjectIteration = genericProjectVersion().apply {
            status = EntityStatus.READONLY
            documents = mutableMapOf("testdoc" to HDocument().apply {
                name = "testdoc"
                docId = "testdoc"})
        }
        action.projectSlug = "myproject"
        action.versionSlug = "myversion"
        whenever(projectIterationDAO
                .getBySlug("myproject", "myversion"))
                .thenReturn(hProjectIteration)
        try {
            action.checkDocumentRemovalAllowed()
            fail("Should have resulted in AuthorizationException")
        } catch (e: AuthorizationException) {
            // OK
        }
    }

    @Test
    fun continueIfDocumentDeleteIsAllowed() {
        val hProjectIteration = genericProjectVersion()
        action.projectSlug = "myproject"
        action.versionSlug = "myversion"
        whenever(identity
                .hasPermissionWithAnyTargets("import-template", hProjectIteration))
                .thenReturn(true)
        whenever(projectIterationDAO
                .getBySlug("myproject", "myversion"))
                .thenReturn(hProjectIteration)
        action.checkDocumentRemovalAllowed()
    }

    @Test
    fun isAPoProjectCheck() {
        val hProjectIteration = genericProjectVersion()
        hProjectIteration.status = EntityStatus.ACTIVE
        hProjectIteration.project.defaultProjectType = ProjectType.File

        action.projectSlug = "myproject"
        action.versionSlug = "myversion"
        whenever(projectIterationDAO
                .getBySlug("myproject", "myversion"))
                .thenReturn(hProjectIteration)

        assertThat(action.isPoProject).isFalse()

        hProjectIteration.projectType = ProjectType.File
        assertThat(action.isPoProject).isFalse()

        hProjectIteration.projectType = ProjectType.Gettext
        assertThat(action.isPoProject).isTrue()

        hProjectIteration.projectType = ProjectType.Podir
        assertThat(action.isPoProject).isTrue()
    }

    @Test
    fun cannotUploadANullTranslationFile() {
        val locale = HLocale(LocaleId.EN)
        val version = genericProjectVersion()
        action.projectSlug = "myproject"
        action.versionSlug = "myversion"
        whenever(projectIterationDAO
                .getBySlug("myproject", "myversion"))
                .thenReturn(version)
        assertThat(action.uploadTranslationFile(locale)).isEqualTo("failure")
    }

    @Test
    fun cannotUploadAnUnsupportedTranslationFile() {
        val locale = HLocale(LocaleId.EN)
        action.projectSlug = "myproject"
        action.versionSlug = "myversion"
        action.translationFileUpload.fileName = "test.jar"
        whenever(projectIterationDAO
                .getBySlug("myproject", "myversion"))
                .thenReturn(genericProjectVersion())

        // Does not break
        assertThat(action.uploadTranslationFile(locale)).isEqualTo("success")
        // but sends a message
        verify(facesMessages).addGlobal(FacesMessage.SEVERITY_ERROR,
                "test.jar-Cannot upload files of this type")
    }

    @Test
    fun displayWarningsForUploadedTranslationFile() {
        val locale = HLocale(LocaleId.EN)
        action.projectSlug = "myproject"
        action.versionSlug = "myversion"
        action.translationFileUpload.fileName = "test.po"
        action.translationFileUpload.docId = "test.po"
        action.translationFileUpload.isAssignCreditToUploader = true
        whenever(projectIterationDAO
                .getBySlug("myproject", "myversion"))
                .thenReturn(genericProjectVersion())
        whenever(translationFileServiceImpl.hasMultipleDocumentTypes("test.po"))
                .thenReturn(false)
        whenever(translationFileServiceImpl.getDocumentTypes("test.po"))
                .thenReturn(mutableSetOf(DocumentType.GETTEXT))

        // This function call though
        whenever(translationServiceImpl.translateAllInDoc(eq(action.projectSlug),
                eq(action.versionSlug),
                eq(action.translationFileUpload.docId),
                eq(locale.localeId),
                eq(null),
                eq(mutableSetOf("gettext")),
                eq(MergeType.AUTO),
                eq(true),
                eq(TranslationSourceType.WEB_UPLOAD)))
                .thenReturn(listOf("The file was not super green"))
        assertThat(action.uploadTranslationFile(locale)).isEqualTo("success")
        verify(facesMessages).addGlobal(FacesMessage.SEVERITY_INFO,
                "File test.po uploaded. There were some warnings, see below.")
        // TODO: test actual message content
        verify(facesMessages).addGlobal(any<FacesMessage>())
    }

    @Test
    fun uploadATranslationFile() {
        val locale = HLocale(LocaleId.EN)
        action.projectSlug = "myproject"
        action.versionSlug = "myversion"
        action.translationFileUpload.fileName = "test.po"
        whenever(projectIterationDAO
                .getBySlug("myproject", "myversion"))
                .thenReturn(genericProjectVersion())
        whenever(translationFileServiceImpl.hasMultipleDocumentTypes("test.po"))
                .thenReturn(false)
        whenever(translationFileServiceImpl.getDocumentTypes("test.po"))
                .thenReturn(mutableSetOf(DocumentType.GETTEXT))
        // Assume translationFileServiceImpl.parseTranslationFile and
        // translationServiceImpl.translateAllInDoc are successful
        assertThat(action.uploadTranslationFile(locale)).isEqualTo("success")
    }

    fun genericProjectVersion(): HProjectIteration {
        return HProjectIteration().apply {
            slug = "myversion"
            project = HProject().apply {
                slug = "myproject"
            }
        }
    }
}
