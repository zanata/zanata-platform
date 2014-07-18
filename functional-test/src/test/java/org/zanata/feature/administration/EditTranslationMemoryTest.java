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
package org.zanata.feature.administration;

import java.io.File;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.openqa.selenium.Alert;
import org.zanata.feature.Feature;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.page.administration.TranslationMemoryEditPage;
import org.zanata.page.administration.TranslationMemoryPage;
import org.zanata.util.AddUsersRule;
import org.zanata.util.TestFileGenerator;
import org.zanata.workflow.BasicWorkFlow;
import org.zanata.workflow.LoginWorkFlow;
import org.zanata.workflow.TranslationMemoryWorkFlow;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
public class EditTranslationMemoryTest extends ZanataTestCase {

    @Rule
    public AddUsersRule addUsersRule = new AddUsersRule();

    private TestFileGenerator testFileGenerator = new TestFileGenerator();

    @Before
    public void before() {
        assertThat(new LoginWorkFlow().signIn("admin", "admin").loggedInAs())
                .isEqualTo("admin")
                .as("Admin is logged in");
    }

    @Feature(summary = "The administrator can create a new translation " +
            "memory entry",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 0)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void createNewTranslationMemory() throws Exception {
        String newTMId = "newtmtest";
        String tmDescription = "A new test TM";

        TranslationMemoryPage translationMemoryPage =
                new TranslationMemoryWorkFlow()
                        .createTranslationMemory(newTMId, tmDescription);

        assertThat(translationMemoryPage
                .expectNotification("Successfully created"))
                .isTrue()
                .as("The success message is displayed");

        assertThat(translationMemoryPage.getListedTranslationMemorys())
                .contains(newTMId)
                .as("The new Translation Memory is listed");

        assertThat(translationMemoryPage.getDescription(newTMId))
                .isEqualTo(tmDescription)
                .as("The description is displayed correctly");
    }

    @Feature(summary = "The administrator can cancel creating a new " +
            "translation memory entry",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 0)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void abortCreate() throws Exception {
        String abortName = "aborttmtest";
        String abortDescription = "abort tm description";

        TranslationMemoryPage translationMemoryPage = new BasicWorkFlow()
                .goToHome()
                .goToAdministration()
                .goToTranslationMemoryPage()
                .clickCreateNew()
                .enterMemoryID(abortName)
                .enterMemoryDescription(abortDescription)
                .cancelTM();

        assertThat(translationMemoryPage.getListedTranslationMemorys())
                .doesNotContain(abortName)
                .as("The Translation Memory was not created");
    }

    @Feature(summary = "The administrator must use a unique identifier to " +
            "create a new translation memory entry",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 0)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void translationMemoryIdsAreUnique() throws Exception {
        String nonUniqueTMId = "doubletmtest";

        TranslationMemoryPage tmMemoryPage = new TranslationMemoryWorkFlow()
                .createTranslationMemory(nonUniqueTMId);

        assertThat(tmMemoryPage.getListedTranslationMemorys())
                .contains(nonUniqueTMId)
                .as("The new Translation Memory is listed");

        TranslationMemoryEditPage translationMemoryEditPage = tmMemoryPage
                .clickCreateNew()
                .enterMemoryID(nonUniqueTMId)
                .enterMemoryDescription("Meh")
                .clickSaveAndExpectFailure();

        assertThat(translationMemoryEditPage.waitForErrors())
                .contains(TranslationMemoryPage.ID_UNAVAILABLE)
                .as("The Id Is Not Available error is displayed");

        translationMemoryEditPage = translationMemoryEditPage
                .clickSaveAndExpectFailure();

        translationMemoryEditPage.assertNoCriticalErrors(); // RHBZ-1010771

        assertThat(translationMemoryEditPage.waitForErrors())
                .contains(TranslationMemoryPage.ID_UNAVAILABLE)
                .as("The Id Is Not Available error is displayed");
    }

    @Feature(summary = "The administrator can import data from a tmx data " +
            "file into a translation memory entry",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 0)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void importTranslationMemory() throws Exception {
        String importTMId = "importmtest";
        File importFile = testFileGenerator.openTestFile("test-tmx.xml");

        TranslationMemoryPage tmMemoryPage = new TranslationMemoryWorkFlow()
                .createTranslationMemory(importTMId)
                .clickImport(importTMId)
                .enterImportFileName(importFile.getAbsolutePath())
                .clickUploadButtonAndAcknowledge();

        assertThat(tmMemoryPage.getNumberOfEntries(importTMId)).isEqualTo("1")
                .as("The Translation Memory has one entry");
    }

    @Feature(summary = "The system rejects empty TMX data files",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 0)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void rejectEmptyTranslation() throws Exception {
        String rejectTMId = "rejectemptytmtest";

        TranslationMemoryPage tmMemoryPage = new TranslationMemoryWorkFlow()
                .createTranslationMemory(rejectTMId)
                .clickImport(rejectTMId);
        Alert uploadError = tmMemoryPage.expectFailedUpload();

        assertThat(uploadError.getText()
                .startsWith(TranslationMemoryPage.UPLOAD_ERROR)).isTrue()
                .as("Error is displayed");

        tmMemoryPage = tmMemoryPage.dismissError();

        assertThat(tmMemoryPage.getNumberOfEntries(rejectTMId)).isEqualTo("0")
                .as("No change is recorded");
    }

    @Feature(summary = "The administrator can delete a translation memory entry",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 0)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void deleteTranslationMemory() throws Exception {
        String deleteTMId = "deletetmtest";

        TranslationMemoryPage tmMemoryPage = new TranslationMemoryWorkFlow()
                .createTranslationMemory(deleteTMId);

        assertThat(tmMemoryPage.getListedTranslationMemorys())
                .contains(deleteTMId)
                .as("The new Translation Memory is listed");

        tmMemoryPage = tmMemoryPage.clickDeleteTmAndAccept(deleteTMId);

        assertThat(tmMemoryPage.getListedTranslationMemorys())
                .doesNotContain(deleteTMId)
                .as("The new Translation Memory is no longer listed");
    }

    @Feature(summary = "The administrator can cancel the delete of a " +
            "translation memory entry",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 0)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void dontDeleteTranslationMemory() throws Exception {
        String dontDeleteTMId = "dontdeletetmtest";

        TranslationMemoryPage tmMemoryPage = new TranslationMemoryWorkFlow()
                .createTranslationMemory(dontDeleteTMId);

        assertThat(tmMemoryPage.getListedTranslationMemorys())
                .contains(dontDeleteTMId)
                .as("The new Translation Memory is listed");

        tmMemoryPage = tmMemoryPage.clickDeleteTmAndCancel(dontDeleteTMId);

        assertThat(tmMemoryPage.getListedTranslationMemorys())
                .contains(dontDeleteTMId)
                .as("The new Translation Memory is still listed");
    }

    @Feature(summary = "The administrator can clear the content of a " +
            "translation memory entry",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 0)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void clearTranslationMemory() throws Exception {
        String clearTMId = "cleartmtest";
        File importFile = testFileGenerator.openTestFile("test-tmx.xml");

        TranslationMemoryPage tmMemoryPage = new TranslationMemoryWorkFlow()
                .createTranslationMemory(clearTMId)
                .clickImport(clearTMId)
                .enterImportFileName(importFile.getAbsolutePath())
                .clickUploadButtonAndAcknowledge();

        assertThat(tmMemoryPage.getNumberOfEntries(clearTMId)).isEqualTo("1")
                .as("The TM has one item");

        tmMemoryPage = tmMemoryPage.clickClearTMAndAccept(clearTMId);

        assertThat(tmMemoryPage.waitForExpectedNumberOfEntries(clearTMId, "0"))
                .isEqualTo("0")
                .as("The translation memory entries is empty");
    }

    @Feature(summary = "The administrator can cancel clearing the content " +
            "of a translation memory entry",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 0)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void dontClearTranslationMemory() throws Exception {
        String clearTMId = "dontcleartmtest";
        File importFile = testFileGenerator.openTestFile("test-tmx.xml");

        TranslationMemoryPage tmMemoryPage = new TranslationMemoryWorkFlow()
                .createTranslationMemory(clearTMId)
                .clickImport(clearTMId)
                .enterImportFileName(importFile.getAbsolutePath())
                .clickUploadButtonAndAcknowledge();

        assertThat(tmMemoryPage.getNumberOfEntries(clearTMId)).isEqualTo("1")
                .as("The TM has one item");

        tmMemoryPage = tmMemoryPage.clickClearTMAndCancel(clearTMId);

        assertThat(tmMemoryPage.getNumberOfEntries(clearTMId)).isEqualTo("1")
                .as("The translation memory entries count is the same");
    }

    @Feature(summary = "The administrator must clear a translation memory " +
            "entry before it can be deleted",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 0)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void mustClearBeforeDelete() throws Exception {
        String forceClear = "forcecleartodelete";
        File importFile = testFileGenerator.openTestFile("test-tmx.xml");

        TranslationMemoryPage tmMemoryPage = new TranslationMemoryWorkFlow()
                .createTranslationMemory(forceClear)
                .clickImport(forceClear)
                .enterImportFileName(importFile.getAbsolutePath())
                .clickUploadButtonAndAcknowledge();

        assertThat(tmMemoryPage.getNumberOfEntries(forceClear)).isEqualTo("1")
                .as("The TM has one item");
        assertThat(tmMemoryPage.canDelete(forceClear)).isFalse()
                .as("The item cannot yet be deleted");

        tmMemoryPage = tmMemoryPage.clickClearTMAndAccept(forceClear);
        tmMemoryPage.waitForExpectedNumberOfEntries(forceClear, "0");

        assertThat(tmMemoryPage.canDelete(forceClear)).isTrue()
                .as("The item can be deleted");

        tmMemoryPage = tmMemoryPage.clickDeleteTmAndAccept(forceClear);

        assertThat(tmMemoryPage.getListedTranslationMemorys())
                .doesNotContain(forceClear)
                .as("The item is deleted");
    }

}
