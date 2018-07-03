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
package org.zanata.feature.administration

import org.junit.Before
import org.junit.Test
import org.junit.experimental.categories.Category
import org.zanata.feature.Trace
import org.zanata.feature.testharness.TestPlan.DetailedTest
import org.zanata.feature.testharness.ZanataTestCase
import org.zanata.page.administration.TranslationMemoryPage
import org.zanata.util.TestFileGenerator
import org.zanata.workflow.BasicWorkFlow
import org.zanata.workflow.LoginWorkFlow
import org.zanata.workflow.TranslationMemoryWorkFlow
import org.assertj.core.api.Assertions.assertThat

/**
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
@Category(DetailedTest::class)
class EditTranslationMemoryTest : ZanataTestCase() {

    private val testFileGenerator = TestFileGenerator()

    @Before
    fun before() {
        BasicWorkFlow().goToHome().deleteCookiesAndRefresh()
        assertThat(LoginWorkFlow().signIn("admin", "admin").loggedInAs())
                .describedAs("Admin is logged in")
                .isEqualTo("admin")
    }

    @Trace(summary = "The administrator can create a new translation memory entry")
    @Test(timeout = MAX_SHORT_TEST_DURATION.toLong())
    fun createNewTranslationMemory() {
        val newTMId = "newtmtest"
        val tmDescription = "A new test TM"

        val translationMemoryPage = TranslationMemoryWorkFlow()
                .createTranslationMemory(newTMId, tmDescription)

        assertThat(translationMemoryPage
                .expectNotification("Successfully created"))
                .describedAs("The success message is displayed")
                .isTrue()

        assertThat(translationMemoryPage.listedTranslationMemorys)
                .describedAs("The new Translation Memory is listed")
                .contains(newTMId)

        assertThat(translationMemoryPage.getDescription(newTMId))
                .describedAs("The description is displayed correctly")
                .isEqualTo(tmDescription)
    }

    @Trace(summary = "The administrator must use a unique identifier to " +
            "create a new translation memory entry")
    @Test(timeout = MAX_SHORT_TEST_DURATION.toLong())
    fun translationMemoryIdsAreUnique() {
        val nonUniqueTMId = "doubletmtest"

        val tmMemoryPage = TranslationMemoryWorkFlow()
                .createTranslationMemory(nonUniqueTMId)

        assertThat(tmMemoryPage.listedTranslationMemorys)
                .describedAs("The new Translation Memory is listed")
                .contains(nonUniqueTMId)

        var translationMemoryEditPage = tmMemoryPage
                .clickCreateNew()
                .enterMemoryID(nonUniqueTMId)
                .enterTMDescription("Meh")
                .clickSaveAndExpectFailure()

        assertThat(translationMemoryEditPage.errors)
                .describedAs("The Id Is Not Available error is displayed")
                .contains(TranslationMemoryPage.ID_UNAVAILABLE)

        translationMemoryEditPage = translationMemoryEditPage
                .clickSaveAndExpectFailure()

        // RHBZ-1010771
        translationMemoryEditPage.assertNoCriticalErrors()

        assertThat(translationMemoryEditPage.errors)
                .describedAs("The Id Is Not Available error is displayed")
                .contains(TranslationMemoryPage.ID_UNAVAILABLE)
    }

    @Trace(summary = "The administrator can import data from a tmx data " +
            "file into a translation memory entry")
    @Test(timeout = MAX_SHORT_TEST_DURATION.toLong())
    fun importTranslationMemory() {
        val importTMId = "importmtest"
        val importFile = testFileGenerator.openTestFile("test-tmx.xml")

        val tmMemoryPage = TranslationMemoryWorkFlow()
                .createTranslationMemory(importTMId)
                .clickOptions(importTMId)
                .clickImport(importTMId)
                .enterImportFileName(importFile.absolutePath)
                .clickUploadButtonAndAcknowledge()

        assertThat(tmMemoryPage.getNumberOfEntries(importTMId))
                .describedAs("The Translation Memory has one entry")
                .isEqualTo("1")
    }

    /**
     * Updated to test import button is disabled if not file is selected.
     */
    @Trace(summary = "The system rejects empty TMX data files")
    @Test(timeout = MAX_SHORT_TEST_DURATION.toLong())
    fun rejectEmptyTranslation() {
        val rejectTMId = "rejectemptytmtest"

        val tmMemoryPage = TranslationMemoryWorkFlow()
                .createTranslationMemory(rejectTMId)
                .clickOptions(rejectTMId)
                .clickImport(rejectTMId)

        assertThat(tmMemoryPage.isImportButtonEnabled).isEqualTo(false)
    }

    @Trace(summary = "The administrator can delete a translation memory entry")
    @Test(timeout = MAX_SHORT_TEST_DURATION.toLong())
    fun deleteTranslationMemory() {
        val deleteTMId = "deletetmtest"

        var tmMemoryPage = TranslationMemoryWorkFlow()
                .createTranslationMemory(deleteTMId)

        assertThat(tmMemoryPage.listedTranslationMemorys)
                .describedAs("The new Translation Memory is listed")
                .contains(deleteTMId)

        tmMemoryPage = tmMemoryPage.clickOptions(deleteTMId)
                .clickDeleteTmAndAccept(deleteTMId)

        assertThat(tmMemoryPage.listedTranslationMemorys)
                .describedAs("The new Translation Memory is no longer listed")
                .doesNotContain(deleteTMId)
    }

    @Trace(summary = "The administrator can cancel the delete of a " +
            "translation memory entry")
    @Test(timeout = MAX_SHORT_TEST_DURATION.toLong())
    fun dontDeleteTranslationMemory() {
        val dontDeleteTMId = "dontdeletetmtest"

        var tmMemoryPage = TranslationMemoryWorkFlow()
                .createTranslationMemory(dontDeleteTMId)

        assertThat(tmMemoryPage.listedTranslationMemorys)
                .describedAs("The new Translation Memory is listed")
                .contains(dontDeleteTMId)

        tmMemoryPage = tmMemoryPage.clickOptions(dontDeleteTMId)
                .clickDeleteTmAndCancel(dontDeleteTMId)

        assertThat(tmMemoryPage.listedTranslationMemorys)
                .describedAs("The new Translation Memory is still listed")
                .contains(dontDeleteTMId)
    }

    @Trace(summary = "The administrator can clear the content of a " +
            "translation memory entry")
    @Test(timeout = MAX_SHORT_TEST_DURATION.toLong())
    fun clearTranslationMemory() {
        val clearTMId = "cleartmtest"
        val importFile = testFileGenerator.openTestFile("test-tmx.xml")

        var tmMemoryPage = TranslationMemoryWorkFlow()
                .createTranslationMemory(clearTMId)
                .clickOptions(clearTMId)
                .clickImport(clearTMId)
                .enterImportFileName(importFile.absolutePath)
                .clickUploadButtonAndAcknowledge()

        assertThat(tmMemoryPage.getNumberOfEntries(clearTMId))
                .describedAs("The TM has one item")
                .isEqualTo("1")

        tmMemoryPage = tmMemoryPage.clickOptions(clearTMId)
                .clickClearTMAndAccept(clearTMId)

        // TODO there seems to be some issue here, fix
        tmMemoryPage.reload()
        tmMemoryPage.expectNumberOfEntries(0, clearTMId)

        assertThat(tmMemoryPage.getNumberOfEntries(clearTMId))
                .describedAs("The translation memory entries is empty")
                .isEqualTo("0")
    }

    @Trace(summary = "The administrator can cancel clearing the content " +
            "of a translation memory entry")
    @Test(timeout = MAX_SHORT_TEST_DURATION.toLong())
    fun dontClearTranslationMemory() {
        val clearTMId = "dontcleartmtest"
        val importFile = testFileGenerator.openTestFile("test-tmx.xml")

        var tmMemoryPage = TranslationMemoryWorkFlow()
                .createTranslationMemory(clearTMId)
                .clickOptions(clearTMId)
                .clickImport(clearTMId)
                .enterImportFileName(importFile.absolutePath)
                .clickUploadButtonAndAcknowledge()

        assertThat(tmMemoryPage.getNumberOfEntries(clearTMId))
                .describedAs("The TM has one item")
                .isEqualTo("1")

        tmMemoryPage = tmMemoryPage.clickOptions(clearTMId)
                .clickClearTMAndCancel(clearTMId)

        assertThat(tmMemoryPage.getNumberOfEntries(clearTMId))
                .describedAs("The translation memory entries count is the same")
                .isEqualTo("1")
    }

    @Trace(summary = "The administrator must clear a translation memory " +
            "entry before it can be deleted")
    @Test(timeout = MAX_SHORT_TEST_DURATION.toLong())
    fun mustClearBeforeDelete() {
        val forceClear = "forcecleartodelete"
        val importFile = testFileGenerator.openTestFile("test-tmx.xml")

        var tmMemoryPage = TranslationMemoryWorkFlow()
                .createTranslationMemory(forceClear)
                .clickOptions(forceClear)
                .clickImport(forceClear)
                .enterImportFileName(importFile.absolutePath)
                .clickUploadButtonAndAcknowledge()

        assertThat(tmMemoryPage.getNumberOfEntries(forceClear))
                .describedAs("The TM has one item")
                .isEqualTo("1")
        assertThat(tmMemoryPage.clickOptions(forceClear).canDelete(forceClear))
                .describedAs("The item cannot yet be deleted")
                .isFalse()

        tmMemoryPage = tmMemoryPage.clickClearTMAndAccept(forceClear)

        // TODO there seems to be some issue here, fix
        tmMemoryPage.reload()
        tmMemoryPage.expectNumberOfEntries(0, forceClear)

        assertThat(tmMemoryPage.clickOptions(forceClear).canDelete(forceClear))
                .describedAs("The item can be deleted")
                .isTrue()

        tmMemoryPage = tmMemoryPage.clickDeleteTmAndAccept(forceClear)

        assertThat(tmMemoryPage.listedTranslationMemorys)
                .describedAs("The item is deleted")
                .doesNotContain(forceClear)
    }

}
