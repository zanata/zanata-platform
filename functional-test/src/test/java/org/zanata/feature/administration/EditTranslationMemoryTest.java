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

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.openqa.selenium.Alert;
import org.zanata.feature.DetailedTest;
import org.zanata.page.administration.TranslationMemoryEditPage;
import org.zanata.page.administration.TranslationMemoryPage;
import org.zanata.util.ResetDatabaseRule;
import org.zanata.util.TestFileGenerator;
import org.zanata.workflow.BasicWorkFlow;
import org.zanata.workflow.LoginWorkFlow;
import org.zanata.workflow.TranslationMemoryWorkFlow;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
public class EditTranslationMemoryTest {
    @ClassRule
    public static ResetDatabaseRule resetDatabaseRule = new ResetDatabaseRule();
    TestFileGenerator testFileGenerator = new TestFileGenerator();

    @Before
    public void before() {
        assertThat("Admin is logged in",
                new LoginWorkFlow().signIn("admin", "admin").loggedInAs(),
                Matchers.equalTo("admin"));
    }

    @Test
    public void createNewTranslationMemory() {
        String newTMId = "newtmtest";
        String tmDescription = "A new test TM";

        TranslationMemoryPage translationMemoryPage =
                new TranslationMemoryWorkFlow().createTranslationMemory(
                        newTMId, tmDescription);

        assertThat("The success message is displayed",
                translationMemoryPage.getNotificationMessage(),
                Matchers.equalTo("Successfully created"));

        assertThat("The new Translation Memory is listed",
                translationMemoryPage.getListedTranslationMemorys(),
                Matchers.hasItem(newTMId));

        assertThat("The description is displayed correctly",
                translationMemoryPage.getDescription(newTMId),
                Matchers.equalTo(tmDescription));
    }

    @Test
    public void abortCreate() {
        String abortName = "aborttmtest";
        String abortDescription = "abort tm description";

        TranslationMemoryPage translationMemoryPage =
                new BasicWorkFlow().goToHome().goToAdministration()
                        .goToTranslationMemoryPage().clickCreateNew()
                        .enterMemoryID(abortName)
                        .enterMemoryDescription(abortDescription).cancelTM();

        assertThat("The Translation Memory was not created",
                translationMemoryPage.getListedTranslationMemorys(),
                Matchers.not(Matchers.hasItem(abortName)));
    }

    @Test
    public void translationMemoryIdsAreUnique() {
        String nonUniqueTMId = "doubletmtest";
        String nameError = "This Id is not available";

        TranslationMemoryPage translationMemoryPage =
                new TranslationMemoryWorkFlow()
                        .createTranslationMemory(nonUniqueTMId);

        assertThat("The new Translation Memory is listed",
                translationMemoryPage.getListedTranslationMemorys(),
                Matchers.hasItem(nonUniqueTMId));

        TranslationMemoryEditPage translationMemoryEditPage =
                translationMemoryPage.clickCreateNew()
                        .enterMemoryID(nonUniqueTMId)
                        .enterMemoryDescription("Meh");

        assertThat("The Id Is Not Available error is displayed",
                translationMemoryEditPage.waitForErrors(),
                Matchers.hasItem(nameError));

        translationMemoryEditPage =
                translationMemoryEditPage.clickSaveAndExpectFailure();

        translationMemoryEditPage.assertNoCriticalErrors(); // RHBZ-1010771

        assertThat("The Id Is Not Available error is displayed",
                translationMemoryEditPage.waitForErrors(),
                Matchers.hasItem(nameError));
    }

    @Test
    public void importTranslationMemory() {
        String importTMId = "importmtest";
        File importFile =
                testFileGenerator
                        .generateTestFileWithContent(
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

        assertThat("The Translation Memory has one entry",
                translationMemoryPage.getNumberOfEntries(importTMId),
                Matchers.equalTo("1"));

    }

    @Test
    public void rejectEmptyTranslation() {
        String rejectTMId = "rejectemptytmtest";

        TranslationMemoryPage translationMemoryPage =
                new TranslationMemoryWorkFlow().createTranslationMemory(
                        rejectTMId).clickImport(rejectTMId);
        Alert uploadError = translationMemoryPage.expectFailedUpload();

        assertThat("Error is displayed", uploadError.getText(),
                Matchers.startsWith("There was an error uploading the file"));

        translationMemoryPage = translationMemoryPage.dismissError();

        assertThat("No change is recorded",
                translationMemoryPage.getNumberOfEntries(rejectTMId),
                Matchers.equalTo("0"));
    }

    @Test
    public void deleteTranslationMemory() {
        String deleteTMId = "deletetmtest";

        TranslationMemoryPage translationMemoryPage =
                new TranslationMemoryWorkFlow()
                        .createTranslationMemory(deleteTMId);

        assertThat("The new Translation Memory is listed",
                translationMemoryPage.getListedTranslationMemorys(),
                Matchers.hasItem(deleteTMId));

        translationMemoryPage =
                translationMemoryPage.clickDeleteTmAndAccept(deleteTMId);

        assertThat("The new Translation Memory is no longer listed",
                translationMemoryPage.getListedTranslationMemorys(),
                Matchers.not(Matchers.hasItem(deleteTMId)));
    }

    @Test
    public void dontDeleteTranslationMemory() {
        String dontDeleteTMId = "dontdeletetmtest";

        TranslationMemoryPage translationMemoryPage =
                new TranslationMemoryWorkFlow()
                        .createTranslationMemory(dontDeleteTMId);

        assertThat("The new Translation Memory is listed",
                translationMemoryPage.getListedTranslationMemorys(),
                Matchers.hasItem(dontDeleteTMId));

        translationMemoryPage =
                translationMemoryPage.clickDeleteTmAndCancel(dontDeleteTMId);

        assertThat("The new Translation Memory is still listed",
                translationMemoryPage.getListedTranslationMemorys(),
                Matchers.hasItem(dontDeleteTMId));
    }

    @Test
    public void clearTranslationMemory() {
        String clearTMId = "cleartmtest";
        File importFile =
                testFileGenerator
                        .generateTestFileWithContent(
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

        assertThat("The TM has one item",
                translationMemoryPage.getNumberOfEntries(clearTMId),
                Matchers.equalTo("1"));

        translationMemoryPage =
                translationMemoryPage.clickClearTMAndAccept(clearTMId);

        assertThat("The translation memory entries is empty",
                translationMemoryPage.waitForExpectedNumberOfEntries(clearTMId,
                        "0"), Matchers.equalTo("0"));
    }

    @Test
    public void dontClearTranslationMemory() {
        String clearTMId = "dontcleartmtest";
        File importFile =
                testFileGenerator
                        .generateTestFileWithContent(
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

        assertThat("The TM has one item",
                translationMemoryPage.getNumberOfEntries(clearTMId),
                Matchers.equalTo("1"));

        translationMemoryPage =
                translationMemoryPage.clickClearTMAndCancel(clearTMId);

        assertThat("The translation memory entries count is the same",
                translationMemoryPage.getNumberOfEntries(clearTMId),
                Matchers.equalTo("1"));
    }

    @Test
    public void mustClearBeforeDelete() {
        String forceClear = "forcecleartodelete";
        File importFile =
                testFileGenerator
                        .generateTestFileWithContent(
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

        assertThat("The TM has one item",
                translationMemoryPage.getNumberOfEntries(forceClear),
                Matchers.equalTo("1"));

        assertThat("The item cannot yet be deleted",
                !translationMemoryPage.canDelete(forceClear));

        translationMemoryPage =
                translationMemoryPage.clickClearTMAndAccept(forceClear);
        translationMemoryPage.waitForExpectedNumberOfEntries(forceClear, "0");

        assertThat("The item can be deleted",
                translationMemoryPage.canDelete(forceClear));

        translationMemoryPage =
                translationMemoryPage.clickDeleteTmAndAccept(forceClear);

        assertThat("The item is deleted",
                translationMemoryPage.getListedTranslationMemorys(),
                Matchers.not(Matchers.hasItem(forceClear)));
    }

}
