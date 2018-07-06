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
package org.zanata.feature.document

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.zanata.workflow.LoginWorkFlow
import org.zanata.workflow.ProjectWorkFlow
import java.io.File

import org.assertj.core.api.Assertions.assertThat
import org.zanata.feature.testharness.DetailedTest

/**
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
@DetailedTest
class DownloadDocumentTest {

    private val DOWNLOADEDPO = "/tmp/About_Fedora.po"
    private val DOWNLOADEDPOT = "/tmp/About_Fedora.pot"

    @BeforeEach
    fun before() {
        arrayListOf(File(DOWNLOADEDPO), File(DOWNLOADEDPOT)).forEach { file ->
            if (file.exists()) {
                assertThat(file.delete()).isTrue()
            }
        }
    }

    @Test
    fun testSourceDownload() {
        LoginWorkFlow().signIn("admin", "admin")
                .gotoProjectsTab()
        ProjectWorkFlow()
                .goToProjectByName("about fedora")
                .gotoVersion("master")
                .gotoDocumentTab()
                .clickDownloadPotOnDocument("About_Fedora")

        val downloadedFile = File(DOWNLOADEDPOT)
        assertThat(downloadedFile.exists()).isTrue()
    }

    @Test
    fun testTranslationDownload() {
        LoginWorkFlow().signIn("admin", "admin")
                .gotoProjectsTab()
        ProjectWorkFlow()
                .goToProjectByName("about fedora")
                .gotoVersion("master")
                .clickLocale("pl")
                .clickDownloadTranslatedPo("About_Fedora")

        val downloadedFile = File(DOWNLOADEDPO)
        assertThat(downloadedFile.exists()).isTrue()
    }
}
