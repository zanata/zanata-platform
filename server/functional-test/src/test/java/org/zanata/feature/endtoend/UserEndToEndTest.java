/*
 * Copyright 2017, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.feature.endtoend;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;
import org.subethamail.wiser.WiserMessage;
import org.zanata.feature.testharness.TestPlan;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.page.AbstractPage;
import org.zanata.page.BasePage;
import org.zanata.page.WebDriverFactory;
import org.zanata.page.account.RegisterPage;
import org.zanata.page.account.SignInPage;
import org.zanata.page.dashboard.DashboardBasePage;
import org.zanata.page.projects.CreateProjectPage;
import org.zanata.page.projects.ProjectPeoplePage;
import org.zanata.page.projects.ProjectVersionsPage;
import org.zanata.page.projects.projectsettings.ProjectLanguagesTab;
import org.zanata.page.projectversion.CreateVersionPage;
import org.zanata.page.projectversion.VersionLanguagesPage;
import org.zanata.page.projectversion.versionsettings.VersionDocumentsTab;
import org.zanata.page.utility.Error404Page;
import org.zanata.page.utility.HomePage;
import org.zanata.page.webtrans.EditorPage;
import org.zanata.util.EmailQuery;
import org.zanata.util.HasEmailRule;
import org.zanata.util.TestFileGenerator;
import org.zanata.workflow.BasicWorkFlow;

import java.io.File;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.zanata.util.EmailQuery.LinkType.ACTIVATE;

/**
 * This aim of this test is to provide a method of testing as many
 * components as possible in a short period of time. Individual tests for
 * UI components via WebDriver are very time expensive.
 *
 * @author Damian Jansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(TestPlan.BasicAcceptanceTest.class)
public class UserEndToEndTest extends ZanataTestCase {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Rule
    public final HasEmailRule hasEmailRule = new HasEmailRule();

    TestFileGenerator testFileGenerator = new TestFileGenerator();

    private final String USERNAME = "Leeloominaï Lekatariba Lamina-TchaïEkbat De Sebat";
    private final String USERUSERNAME = "leeloo";
    private final String USEREMAIL = "leeloo@example.com";
    private final String PASSWORD = "4me2test";

    private final String PROJECTID = "plavalaguna";
    private final String PROJECTNAME = "Phloston Paradise";
    private final String PROJECTDESCRIPTION = "The stones are in me";

    private final String ADDEDLOCALE = "en-US";

    private String dswid;
    private File testFile;
    private File pdfFile;

    @Before
    public void before() {
        testFile = testFileGenerator.generateTestFileWithContent(
                "mydocument", ".txt",
                "Line One\n\nLine Two\n\nLine Three\n");
        pdfFile = testFileGenerator.generateTestFileWithContent(
                "mydocument", "pdf", "");
    }

    /*
     * This end-to-end tracks the story of a non-user joining and
     * setting up a project for translation, while making a few mistakes
     * along the way.  They aim to set a language, add someone to their
     * project and finish with a client accessible product.
     */
    @Test
    public void userEndToEndTest() throws Exception {

        // Went to Zanata via a bad link
        Error404Page error404Page = goToBadLink();

        // (Impl) Store the dswid for later steps
        dswid = getDswid(error404Page);

        // Attempts to register, does not succeed
        RegisterPage registerPage = failToRegister(error404Page);
        registerSuccessfully(registerPage);

        // get email wrong?

        // Copies link to browser but messes it up
        checkEmailAndFailToActivate();

        // Realises error, fixes the id
        SignInPage signInPage = checkEmailAndActivate();

        // Enters the password in wrong
        signInPage = failToSignIn(signInPage);

        // Forgot password?

        // Signs in successfully
        DashboardBasePage dashboardBasePage = signInSuccessfully(signInPage);

        // Tries to create a project but gets it wrong
        CreateProjectPage createProjectPage = failToCreateAProject(dashboardBasePage);

        // Successfully creates a project
        ProjectVersionsPage projectVersionsPage = successfullyCreateAProject(createProjectPage);

        // Adds a language to their project
        addLanguageToProject(projectVersionsPage);

        // Adds a translator to the project
        ProjectPeoplePage projectPeoplePage = addTranslatorToProject(projectVersionsPage);

        // Tries to add a version, but gets it wrong
        CreateVersionPage createVersionPage = failToCreateVersion(projectPeoplePage);

        // Gets the version right this time
        VersionLanguagesPage versionLanguagesPage = successfullyCreateVersion(createVersionPage);

        // Realises the version name is wrong
        versionLanguagesPage = renameVersion(versionLanguagesPage);

        // Tries to upload an unsupported document type
        VersionDocumentsTab versionDocumentsTab = failToUploadDocuments(versionLanguagesPage);

        // Uploads a text file instead
        versionDocumentsTab = successfullyUploadDocument(versionDocumentsTab);

        // Navigates to the editor to begin translating
        EditorPage editorPage = goToEditor(versionDocumentsTab);

        // Successfully translates their document
        editorPage = translateDocument(editorPage);

        // Downloads the translated po
        downloadTranslatedDocuments(editorPage);
    }

    private Error404Page goToBadLink() {
        return new BasicWorkFlow()
                .goToUrl(WebDriverFactory.INSTANCE.getHostUrl().concat("nosuchpage"),
                        Error404Page.class);
    }

    private RegisterPage failToRegister(Error404Page error404Page) {
        RegisterPage registerPage = error404Page.goToHomePage().goToRegistration();
        registerPage.enterEmail("notagoodemail.com")
                .enterName("a")
                .enterUserName("a")
                .enterPassword("a")
                .registerFailure();

        List<String> errors = registerPage.getErrors(4);

        assertThat(errors).contains(
                RegisterPage.USERDISPLAYNAME_LENGTH_ERROR,
                RegisterPage.MALFORMED_EMAIL_ERROR,
                RegisterPage.PASSWORD_LENGTH_ERROR);
        // May be one or the other
        assertThat(errors.contains(RegisterPage.USERNAME_VALIDATION_ERROR)
                || errors.contains(RegisterPage.USERNAME_LENGTH_ERROR)).isTrue();
        return registerPage;
    }

    private SignInPage registerSuccessfully(RegisterPage registerPage) {
        SignInPage signInPage = registerPage.clearFields()
                .enterName(USERNAME)
                .enterUserName(USERUSERNAME)
                .enterEmail(USEREMAIL)
                .enterPassword(PASSWORD)
                .register();

        assertThat(signInPage.getNotificationMessage())
                .isEqualTo(HomePage.SIGNUP_SUCCESS_MESSAGE)
                .as("Sign up is successful");

        return signInPage;
    }

    private BasePage checkEmailAndFailToActivate() {
        List<WiserMessage> messages = hasEmailRule.getMessages();
        assertThat(messages).as("one email message").hasSize(1);
        WiserMessage message = messages.get(0);
        String link = EmailQuery.getLink(message, ACTIVATE);
        boolean exceptionFound = false;
        BasePage basePage = null;
        try {
            basePage = new BasicWorkFlow().goToUrl(link.concat("blah?" + dswid), BasePage.class);
        } catch (RuntimeException rte) {
            exceptionFound = true;
        }
        assertThat(exceptionFound).as("The invalid activation ID was handled").isTrue();
        return basePage;
    }

    private SignInPage checkEmailAndActivate() {
        WiserMessage message = hasEmailRule.getMessages().get(0);
        String link = EmailQuery.getLink(message, ACTIVATE);

        SignInPage signInPage = new BasicWorkFlow().goToUrl(link.concat("?" + dswid), SignInPage.class);

        assertThat(signInPage.getNotificationMessage())
                .as("Activation was successful")
                .isEqualTo(SignInPage.ACTIVATION_SUCCESS);
        return signInPage;
    }

    private SignInPage failToSignIn(SignInPage signInPage) {
        signInPage = signInPage.enterUsername("incorrect")
                .enterPassword("incorrect")
                .clickSignInExpectError();
        assertThat(signInPage.getErrors()).contains(SignInPage.LOGIN_FAILED_ERROR);
        return signInPage;
    }

    private DashboardBasePage signInSuccessfully(SignInPage signInPage) {
        DashboardBasePage dashboardBasePage = signInPage.enterUsername(USERUSERNAME)
                .enterPassword(PASSWORD)
                .clickSignIn();
        assertThat(dashboardBasePage.getNotificationMessage())
                .contains("Welcome, ".concat(USERNAME).concat("!"));
        return dashboardBasePage;
    }

    private CreateProjectPage failToCreateAProject(DashboardBasePage dashboardBasePage) {
        CreateProjectPage createProjectPage = dashboardBasePage
                .gotoProjectsTab()
                .clickOnCreateProjectLink()
                .enterProjectId("")
                .enterProjectName("")
                .enterDescription("")
                .pressCreateProjectAndExpectFailure();
        assertThat(createProjectPage.getErrors())
                .containsExactly("value is required", "value is required");
        return createProjectPage;
    }

    private ProjectVersionsPage successfullyCreateAProject(CreateProjectPage createProjectPage) {
        ProjectVersionsPage projectVersionsPage = createProjectPage
            .enterProjectId(PROJECTID)
            .enterProjectName(PROJECTNAME)
            .enterDescription(PROJECTDESCRIPTION)
            .pressCreateProject();
        assertThat(projectVersionsPage.getNumberOfDisplayedVersions()).isEqualTo(0);
        return projectVersionsPage;
    }

    private ProjectLanguagesTab addLanguageToProject(ProjectVersionsPage projectVersionsPage) {
        ProjectLanguagesTab projectLanguagesTab = projectVersionsPage
                .gotoSettingsTab()
                .gotoSettingsLanguagesTab()
                .addLanguage(ADDEDLOCALE);
        assertThat(projectLanguagesTab.getEnabledLocaleList()).contains(ADDEDLOCALE);
        return projectLanguagesTab;
    }

    private ProjectPeoplePage addTranslatorToProject(ProjectVersionsPage projectVersionsPage) {
        ProjectPeoplePage projectPeoplePage = projectVersionsPage
                .gotoPeopleTab()
                .clickAddSomeone()
                .enterAddSomeoneUsername("translator")
                .selectUserFromAddList("translator")
                .clickTranslatorCheckboxFor("English (United States)")
                .clickAddPerson();
        assertThat(projectPeoplePage.getPeople().contains("translator|Translator"));
        return projectPeoplePage;
    }

    private CreateVersionPage failToCreateVersion(ProjectPeoplePage projectPeoplePage) {
        CreateVersionPage createVersionPage = projectPeoplePage
                .gotoVersionsTab()
                .clickCreateVersionLink()
                .inputVersionId("")
                .saveExpectingError();
        assertThat(createVersionPage.getErrors()).containsExactly("value is required");
        return createVersionPage;
    }

    private VersionLanguagesPage successfullyCreateVersion(CreateVersionPage createVersionPage) {
        VersionLanguagesPage versionLanguagesPage = createVersionPage
                .inputVersionId("mister")
                .saveVersion();
        assertThat(versionLanguagesPage.getProjectVersionName()).isEqualTo("mister");
        return versionLanguagesPage;
    }

    private VersionLanguagesPage renameVersion(VersionLanguagesPage versionLanguagesPage) {
        versionLanguagesPage = versionLanguagesPage
                .gotoSettingsTab()
                .gotoSettingsGeneral()
                .enterVersionID("master")
                .updateVersion();
        assertThat(versionLanguagesPage.getVersionID()).isEqualTo("master");
        return versionLanguagesPage;
    }

    private VersionDocumentsTab failToUploadDocuments(VersionLanguagesPage versionLanguagesPage) {
        VersionDocumentsTab versionDocumentsTab = versionLanguagesPage
                .gotoSettingsTab()
                .gotoSettingsDocumentsTab()
                .pressUploadFileButton()
                .enterFilePath(pdfFile.getAbsolutePath());
        assertThat(versionDocumentsTab.canSubmitDocument()).isFalse();
        return versionDocumentsTab;
    }

    private VersionDocumentsTab successfullyUploadDocument(VersionDocumentsTab versionDocumentsTab) {
        versionDocumentsTab = versionDocumentsTab
                .clickRemoveOn(pdfFile.getName())
                .enterFilePath(testFile.getAbsolutePath())
                .submitUpload()
                .clickUploadDone();
        assertThat(versionDocumentsTab.getSourceDocumentsList()).contains(testFile.getName());
        return versionDocumentsTab;
    }

    private EditorPage goToEditor(VersionDocumentsTab versionDocumentsTab) {
        EditorPage editorPage = versionDocumentsTab
                .gotoLanguageTab()
                .clickLocale(ADDEDLOCALE)
                .clickDocument(testFile.getName());
        assertThat(editorPage.getMessageSourceAtRowIndex(0))
                .contains("Line One");
        return editorPage;
    }

    private EditorPage translateDocument(EditorPage editorPage) {
        editorPage = editorPage.translateTargetAtRowIndex(0, "Lyn Wun")
                .approveTranslationAtRow(0)
                .translateTargetAtRowIndex(1, "Lyn To")
                .translateTargetAtRowIndex(2, "Lyn Thre")
                .saveAsFuzzyAtRow(2);
        editorPage.reload();
        editorPage = new EditorPage(editorPage.getDriver());
        assertThat(editorPage.getBasicTranslationTargetAtRowIndex(0))
                .contains("Lyn Wun");
        return editorPage;
    }

    private VersionLanguagesPage downloadTranslatedDocuments(EditorPage editorPage) {
        // Make sure we don't verify the source
        assertThat(testFile.delete());
        VersionLanguagesPage versionLanguagesPage = editorPage
                .clickVersionBreadcrumb("master")
                .clickLocale("en-US")
                .clickDownloadTranslatedFile(testFile.getName(), "txt");
        assertThat(new File("/tmp/" + testFile.getName()).exists());
        return versionLanguagesPage;
    }

    private String getDswid(AbstractPage page) {
        String currentUrl = page.getDriver().getCurrentUrl();
        return currentUrl.substring(currentUrl.indexOf("dswid="));
    }
}
