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

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void createNewTranslationMemory() {
        String newTMId = "newtmtest";
        String tmDescription = "A new test TM";

        TranslationMemoryPage translationMemoryPage =
                new TranslationMemoryWorkFlow().createTranslationMemory(
                        newTMId, tmDescription);

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

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void abortCreate() {
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

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void translationMemoryIdsAreUnique() {
        String nonUniqueTMId = "doubletmtest";
        String nameError = "This Id is not available";

        TranslationMemoryPage translationMemoryPage =
                new TranslationMemoryWorkFlow()
                        .createTranslationMemory(nonUniqueTMId);

        assertThat(translationMemoryPage.getListedTranslationMemorys())
                .contains(nonUniqueTMId)
                .as("The new Translation Memory is listed");

        TranslationMemoryEditPage translationMemoryEditPage =
                translationMemoryPage.clickCreateNew()
                        .enterMemoryID(nonUniqueTMId)
                        .enterMemoryDescription("Meh");

        assertThat(translationMemoryEditPage.waitForErrors())
                .contains(nameError)
                .as("The Id Is Not Available error is displayed");

        translationMemoryEditPage =
                translationMemoryEditPage.clickSaveAndExpectFailure();

        translationMemoryEditPage.assertNoCriticalErrors(); // RHBZ-1010771

        assertThat(translationMemoryEditPage.waitForErrors())
                .contains(nameError)
                .as("The Id Is Not Available error is displayed");
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void importTranslationMemory() {
        String importTMId = "importmtest";
        File importFile = testFileGenerator.generateTestFileWithContent(
                "importtmtest",
                ".tmx",
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                        + "<!DOCTYPE tmx SYSTEM \"http://www.lisa.org/tmx/tmx14.dtd\">\n"
                        + "<tmx version=\"1.4\">\n"
                        + "  <header creationtool=\"Zanata TranslationsTMXExportStrategy\" creationtoolversion=\"unknown\" segtype=\"block\" o-tmf=\"unknown\" adminlang=\"en\" srclang=\"*all*\" datatype=\"unknown\"/>\n"
                        + "  <body>\n"
                        + "<tu srclang=\"en-US\" tuid=\"about-fedora:master:About_Fedora:d033787962c24b1dc3e00316c86e578c\"><tuv xml:lang=\"en-US\"><seg>Fedora is an open, innovative, forward looking operating system and platform, based on Linux, that is always free for anyone to use, modify and distribute, now and forever. It is developed by a large community of people who strive to provide and maintain the very best in free, open source software and standards. Fedora is part of the Fedora Project, sponsored by Red Hat, Inc.</seg></tuv><tuv xml:lang=\"pl\"><seg>This is a TM Import Test</seg></tuv></tu>\n"
                        + "  </body>\n" + "</tmx>");

        TranslationMemoryPage translationMemoryPage =
                new TranslationMemoryWorkFlow()
                        .createTranslationMemory(importTMId)
                        .clickImport(importTMId)
                        .enterImportFileName(importFile.getAbsolutePath())
                        .clickUploadButtonAndAcknowledge();

        assertThat(translationMemoryPage.getNumberOfEntries(importTMId))
                .isEqualTo("1")
                .as("The Translation Memory has one entry");

    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void rejectEmptyTranslation() {
        String rejectTMId = "rejectemptytmtest";

        TranslationMemoryPage translationMemoryPage =
                new TranslationMemoryWorkFlow().createTranslationMemory(
                        rejectTMId).clickImport(rejectTMId);
        Alert uploadError = translationMemoryPage.expectFailedUpload();

        assertThat(uploadError.getText())
                .startsWith("There was an error uploading the file")
                .as("Error is displayed");

        translationMemoryPage = translationMemoryPage.dismissError();

        assertThat(translationMemoryPage.getNumberOfEntries(rejectTMId))
                .isEqualTo("0")
                .as("No change is recorded");
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void deleteTranslationMemory() {
        String deleteTMId = "deletetmtest";

        TranslationMemoryPage translationMemoryPage =
                new TranslationMemoryWorkFlow()
                        .createTranslationMemory(deleteTMId);

        assertThat(translationMemoryPage.getListedTranslationMemorys())
                .contains(deleteTMId)
                .as("The new Translation Memory is listed");

        translationMemoryPage =
                translationMemoryPage.clickDeleteTmAndAccept(deleteTMId);

        assertThat(translationMemoryPage.getListedTranslationMemorys())
                .doesNotContain(deleteTMId)
                .as("The new Translation Memory is no longer listed");
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void dontDeleteTranslationMemory() {
        String dontDeleteTMId = "dontdeletetmtest";

        TranslationMemoryPage translationMemoryPage =
                new TranslationMemoryWorkFlow()
                        .createTranslationMemory(dontDeleteTMId);

        assertThat(translationMemoryPage.getListedTranslationMemorys())
                .contains(dontDeleteTMId)
                .as("The new Translation Memory is listed");

        translationMemoryPage =
                translationMemoryPage.clickDeleteTmAndCancel(dontDeleteTMId);

        assertThat(translationMemoryPage.getListedTranslationMemorys())
                .contains(dontDeleteTMId)
                .as("The new Translation Memory is still listed");
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void clearTranslationMemory() {
        String clearTMId = "cleartmtest";
        File importFile = testFileGenerator.generateTestFileWithContent(
                "cleartmtest",
                ".tmx",
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<!DOCTYPE tmx SYSTEM \"http://www.lisa.org/tmx/tmx14.dtd\">\n"
                + "<tmx version=\"1.4\">\n"
                + "  <header creationtool=\"Zanata TranslationsTMXExportStrategy\" creationtoolversion=\"unknown\" segtype=\"block\" o-tmf=\"unknown\" adminlang=\"en\" srclang=\"*all*\" datatype=\"unknown\"/>\n"
                + "  <body>\n"
                + "<tu srclang=\"en-US\" tuid=\"about-fedora:master:About_Fedora:d033787962c24b1dc3e00316c86e578c\"><tuv xml:lang=\"en-US\"><seg>Fedora is an open, innovative, forward looking operating system and platform, based on Linux, that is always free for anyone to use, modify and distribute, now and forever. It is developed by a large community of people who strive to provide and maintain the very best in free, open source software and standards. Fedora is part of the Fedora Project, sponsored by Red Hat, Inc.</seg></tuv><tuv xml:lang=\"pl\"><seg>This is a TM Import Test</seg></tuv></tu>\n"
                + "  </body>\n" + "</tmx>");

        TranslationMemoryPage translationMemoryPage =
                new TranslationMemoryWorkFlow()
                        .createTranslationMemory(clearTMId)
                        .clickImport(clearTMId)
                        .enterImportFileName(importFile.getAbsolutePath())
                        .clickUploadButtonAndAcknowledge();

        assertThat(translationMemoryPage.getNumberOfEntries(clearTMId))
                .isEqualTo("1")
                .as("The TM has one item");

        translationMemoryPage =
                translationMemoryPage.clickClearTMAndAccept(clearTMId);

        assertThat(translationMemoryPage
                .waitForExpectedNumberOfEntries(clearTMId, "0"))
                .isEqualTo("0")
                .as("The translation memory entries is empty");
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void dontClearTranslationMemory() {
        String clearTMId = "dontcleartmtest";
        File importFile = testFileGenerator.generateTestFileWithContent(
                "cleartmtest",
                ".tmx",
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<!DOCTYPE tmx SYSTEM \"http://www.lisa.org/tmx/tmx14.dtd\">\n"
                + "<tmx version=\"1.4\">\n"
                + "  <header creationtool=\"Zanata TranslationsTMXExportStrategy\" creationtoolversion=\"unknown\" segtype=\"block\" o-tmf=\"unknown\" adminlang=\"en\" srclang=\"*all*\" datatype=\"unknown\"/>\n"
                + "  <body>\n"
                + "<tu srclang=\"en-US\" tuid=\"about-fedora:master:About_Fedora:d033787962c24b1dc3e00316c86e578c\"><tuv xml:lang=\"en-US\"><seg>Fedora is an open, innovative, forward looking operating system and platform, based on Linux, that is always free for anyone to use, modify and distribute, now and forever. It is developed by a large community of people who strive to provide and maintain the very best in free, open source software and standards. Fedora is part of the Fedora Project, sponsored by Red Hat, Inc.</seg></tuv><tuv xml:lang=\"pl\"><seg>This is a TM Import Test</seg></tuv></tu>\n"
                + "  </body>\n" + "</tmx>");

        TranslationMemoryPage translationMemoryPage =
                new TranslationMemoryWorkFlow()
                        .createTranslationMemory(clearTMId)
                        .clickImport(clearTMId)
                        .enterImportFileName(importFile.getAbsolutePath())
                        .clickUploadButtonAndAcknowledge();

        assertThat(translationMemoryPage.getNumberOfEntries(clearTMId))
                .isEqualTo("1")
                .as("The TM has one item");

        translationMemoryPage =
                translationMemoryPage.clickClearTMAndCancel(clearTMId);

        assertThat(translationMemoryPage.getNumberOfEntries(clearTMId))
                .isEqualTo("1")
                .as("The translation memory entries count is the same");
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void mustClearBeforeDelete() {
        String forceClear = "forcecleartodelete";
        File importFile = testFileGenerator.generateTestFileWithContent(
                "cleartmtest",
                ".tmx",
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<!DOCTYPE tmx SYSTEM \"http://www.lisa.org/tmx/tmx14.dtd\">\n"
                + "<tmx version=\"1.4\">\n"
                + "  <header creationtool=\"Zanata TranslationsTMXExportStrategy\" creationtoolversion=\"unknown\" segtype=\"block\" o-tmf=\"unknown\" adminlang=\"en\" srclang=\"*all*\" datatype=\"unknown\"/>\n"
                + "  <body>\n"
                + "<tu srclang=\"en-US\" tuid=\"about-fedora:master:About_Fedora:d033787962c24b1dc3e00316c86e578c\"><tuv xml:lang=\"en-US\"><seg>Fedora is an open, innovative, forward looking operating system and platform, based on Linux, that is always free for anyone to use, modify and distribute, now and forever. It is developed by a large community of people who strive to provide and maintain the very best in free, open source software and standards. Fedora is part of the Fedora Project, sponsored by Red Hat, Inc.</seg></tuv><tuv xml:lang=\"pl\"><seg>This is a TM Import Test</seg></tuv></tu>\n"
                + "  </body>\n" + "</tmx>");

        TranslationMemoryPage translationMemoryPage =
                new TranslationMemoryWorkFlow()
                        .createTranslationMemory(forceClear)
                        .clickImport(forceClear)
                        .enterImportFileName(importFile.getAbsolutePath())
                        .clickUploadButtonAndAcknowledge();

        assertThat(translationMemoryPage.getNumberOfEntries(forceClear))
                .isEqualTo("1")
                .as("The TM has one item");

        assertThat(translationMemoryPage.canDelete(forceClear))
                .isFalse()
                .as("The item cannot yet be deleted");

        translationMemoryPage =
                translationMemoryPage.clickClearTMAndAccept(forceClear);
        translationMemoryPage.waitForExpectedNumberOfEntries(forceClear, "0");

        assertThat(translationMemoryPage.canDelete(forceClear))
                .isTrue()
                .as("The item can be deleted");

        translationMemoryPage =
                translationMemoryPage.clickDeleteTmAndAccept(forceClear);

        assertThat(translationMemoryPage.getListedTranslationMemorys())
                .doesNotContain(forceClear)
                .as("The item is deleted");
    }

}
