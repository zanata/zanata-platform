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
package org.zanata.feature.endtoend

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.zanata.feature.testharness.ZanataTestCase
import org.zanata.page.AbstractPage
import org.zanata.page.BasePage
import org.zanata.page.WebDriverFactory
import org.zanata.page.account.RegisterPage
import org.zanata.page.account.SignInPage
import org.zanata.page.dashboard.DashboardBasePage
import org.zanata.page.projects.CreateProjectPage
import org.zanata.page.projects.ProjectPeoplePage
import org.zanata.page.projects.ProjectVersionsPage
import org.zanata.page.projects.projectsettings.ProjectLanguagesTab
import org.zanata.page.projectversion.CreateVersionPage
import org.zanata.page.projectversion.VersionLanguagesPage
import org.zanata.page.projectversion.versionsettings.VersionDocumentsTab
import org.zanata.page.utility.Error404Page
import org.zanata.page.utility.HomePage
import org.zanata.page.webtrans.EditorPage
import org.zanata.util.EmailQuery
import org.zanata.util.TestFileGenerator
import org.zanata.workflow.BasicWorkFlow

import java.io.File

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.extension.ExtendWith
import org.zanata.feature.testharness.BasicAcceptanceTest
import org.zanata.util.EmailQuery.LinkType.ACTIVATE
import org.zanata.util.HasEmailExtension

/**
 * This aim of this test is to provide a method of testing as many
 * components as possible in a short period of time. Individual tests for
 * UI components via WebDriver are very time expensive.
 *
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
@BasicAcceptanceTest
@ExtendWith(HasEmailExtension::class)
class UserEndToEndTest : ZanataTestCase() {

    private var testFileGenerator = TestFileGenerator()

    private val userName = "Leeloominaï Lekatariba Lamina-TchaïEkbat De Sebat"
    private val userUserName = "leeloo"
    private val userEmail = "leeloo@example.com"
    private val userPassword = "4me2test"

    private val projectId = "plavalaguna"
    private val projectName = "Phloston Paradise"
    private val projectDescription = "The stones are in me"

    private val addedLocale = "en-US"

    private lateinit var dswid: String
    private lateinit var testFile: File
    private lateinit var pdfFile: File

    @BeforeEach
    fun before() {
        testFile = testFileGenerator.generateTestFileWithContent(
                "mydocument", ".txt",
                "Line One\n\nLine Two\n\nLine Three\n")
        pdfFile = testFileGenerator.generateTestFileWithContent(
                "mydocument", "pdf", "")
    }

    /*
     * This end-to-end tracks the story of a non-user joining and
     * setting up a project for translation, while making a few mistakes
     * along the way.  They aim to set a language, add someone to their
     * project and finish with a client accessible product.
     */
    @Test
    @Throws(Exception::class)
    fun userEndToEndTest() {

        // Went to Zanata via a bad link
        val error404Page = goToBadLink()

        // (Impl) Store the dswid for later steps
        dswid = getDswid(error404Page)

        // Attempts to register, does not succeed
        val registerPage = failToRegister(error404Page)
        registerSuccessfully(registerPage)

        // get email wrong?

        // Copies link to browser but messes it up
        checkEmailAndFailToActivate()

        // Realises error, fixes the id
        var signInPage = checkEmailAndActivate()

        // Enters the password in wrong
        signInPage = failToSignIn(signInPage)

        // Forgot password?

        // Signs in successfully
        val dashboardBasePage = signInSuccessfully(signInPage)

        // Tries to create a project but gets it wrong
        val createProjectPage = failToCreateAProject(dashboardBasePage)

        // Successfully creates a project
        val projectVersionsPage = successfullyCreateAProject(createProjectPage)

        // Adds a language to their project
        addLanguageToProject(projectVersionsPage)

        // Adds a translator to the project
        val projectPeoplePage = addTranslatorToProject(projectVersionsPage)

        // Tries to add a version, but gets it wrong
        val createVersionPage = failToCreateVersion(projectPeoplePage)

        // Gets the version right this time
        var versionLanguagesPage = successfullyCreateVersion(createVersionPage)

        // Realises the version name is wrong
        versionLanguagesPage = renameVersion(versionLanguagesPage)

        // Tries to upload an unsupported document type
        var versionDocumentsTab = failToUploadDocuments(versionLanguagesPage)

        // Uploads a text file instead
        versionDocumentsTab = successfullyUploadDocument(versionDocumentsTab)

        // Navigates to the editor to begin translating
        var editorPage = goToEditor(versionDocumentsTab)

        // Successfully translates their document
        editorPage = translateDocument(editorPage)

        // Downloads the translated po
        downloadTranslatedDocuments(editorPage)
    }

    private fun goToBadLink(): Error404Page {
        return BasicWorkFlow()
                .goToUrl(WebDriverFactory.INSTANCE.hostUrl + "nosuchpage",
                        Error404Page::class.java)
    }

    private fun failToRegister(error404Page: Error404Page): RegisterPage {
        val registerPage = error404Page.goToHomePage().goToRegistration()
        registerPage.enterEmail("notagoodemail.com")
                .enterName("a")
                .enterUserName("a")
                .enterPassword("a")
                .registerFailure()

        val errors = registerPage.getErrors(4)

        assertThat(errors)
                .describedAs("The registration page contains multiple errors")
                .contains(
                        RegisterPage.USER_DISPLAY_NAME_LENGTH_ERROR,
                        RegisterPage.MALFORMED_EMAIL_ERROR,
                        RegisterPage.PASSWORD_LENGTH_ERROR)
        // May be one or the other
        assertThat(errors.contains(RegisterPage.USERNAME_VALIDATION_ERROR) || errors.contains(RegisterPage.USERNAME_LENGTH_ERROR))
                .describedAs("The register page contains a username validation error")
                .isTrue()
        return registerPage
    }

    private fun registerSuccessfully(registerPage: RegisterPage): SignInPage {
        val signInPage = registerPage.clearFields()
                .enterName(userName)
                .enterUserName(userUserName)
                .enterEmail(userEmail)
                .enterPassword(userPassword)
                .register()

        assertThat(signInPage.notificationMessage)
                .describedAs("Sign up is successful")
                .isEqualTo(HomePage.SIGNUP_SUCCESS_MESSAGE)

        return signInPage
    }

    private fun checkEmailAndFailToActivate() {
        val messages = hasEmailExtension.messages
        assertThat(messages).describedAs("one email message").hasSize(1)
        val message = messages[0]
        val link = EmailQuery.getLink(message, ACTIVATE)
        try {
            BasicWorkFlow().goToUrl("""${link}blah?$dswid""", BasePage::class.java)
            fail("A Runtime Exception should be thrown")
        } catch (rte: RuntimeException) {
            // Good
        }
    }

    private fun checkEmailAndActivate(): SignInPage {
        val message = hasEmailExtension.messages[0]
        val link = EmailQuery.getLink(message, ACTIVATE)

        val signInPage = BasicWorkFlow().goToUrl("""$link?$dswid""", SignInPage::class.java)

        assertThat(signInPage.notificationMessage)
                .describedAs("Activation was successful")
                .isEqualTo(SignInPage.ACTIVATION_SUCCESS)
        return signInPage
    }

    private fun failToSignIn(signInPage: SignInPage): SignInPage {
        val loginPage = signInPage.enterUsername("incorrect")
                .enterPassword("incorrect")
                .clickSignInExpectError()
        assertThat(loginPage.errors)
                .describedAs("The page shows a login failed message")
                .contains(SignInPage.LOGIN_FAILED_ERROR)
        return loginPage
    }

    private fun signInSuccessfully(signInPage: SignInPage): DashboardBasePage {
        val dashboardBasePage = signInPage.enterUsername(userUserName)
                .enterPassword(userPassword)
                .clickSignIn()
        assertThat(dashboardBasePage.notificationMessage)
                .describedAs("The page shows a welcome message")
                .contains("Welcome, $userName!")
        return dashboardBasePage
    }

    private fun failToCreateAProject(dashboardBasePage: DashboardBasePage): CreateProjectPage {
        val createProjectPage = dashboardBasePage
                .gotoProjectsTab()
                .clickOnCreateProjectLink()
                .enterProjectId("")
                .enterProjectName("")
                .enterDescription("")
                .pressCreateProjectAndExpectFailure()
        assertThat(createProjectPage.errors)
                .describedAs("A value is required error is shown")
                .containsExactly("value is required", "value is required")
        return createProjectPage
    }

    private fun successfullyCreateAProject(createProjectPage: CreateProjectPage): ProjectVersionsPage {
        val projectVersionsPage = createProjectPage
                .enterProjectId(projectId)
                .enterProjectName(projectName)
                .enterDescription(projectDescription)
                .pressCreateProject()
        assertThat(projectVersionsPage.numberOfDisplayedVersions)
                .describedAs("A project is created, with no versions")
                .isEqualTo(0)
        return projectVersionsPage
    }

    private fun addLanguageToProject(projectVersionsPage: ProjectVersionsPage): ProjectLanguagesTab {
        val projectLanguagesTab = projectVersionsPage
                .gotoSettingsTab()
                .gotoSettingsLanguagesTab()
                .addLanguage(addedLocale)
        assertThat(projectLanguagesTab.enabledLocaleList)
                .describedAs("The locale was added to the project")
                .contains(addedLocale)
        return projectLanguagesTab
    }

    private fun addTranslatorToProject(projectVersionsPage: ProjectVersionsPage): ProjectPeoplePage {
        val projectPeoplePage = projectVersionsPage
                .gotoPeopleTab()
                .clickAddSomeone()
                .enterAddSomeoneUsername("translator")
                .selectUserFromAddList("translator")
                .clickTranslatorCheckboxFor("English (United States)")
                .clickAddPerson()
        assertThat(projectPeoplePage.people)
                .describedAs("The user was added to the project")
                .contains("translator|English (United States) Translator;")
        return projectPeoplePage
    }

    private fun failToCreateVersion(projectPeoplePage: ProjectPeoplePage): CreateVersionPage {
        val createVersionPage = projectPeoplePage
                .gotoVersionsTab()
                .clickCreateVersionLink()
                .inputVersionId("")
                .saveExpectingError()
        assertThat(createVersionPage.errors)
                .describedAs("A value is required error is shown")
                .containsExactly("value is required")
        return createVersionPage
    }

    private fun successfullyCreateVersion(createVersionPage: CreateVersionPage): VersionLanguagesPage {
        val versionLanguagesPage = createVersionPage
                .inputVersionId("mister")
                .saveVersion()
        assertThat(versionLanguagesPage.projectVersionName)
                .describedAs("The new version is displayed")
                .isEqualTo("mister")
        return versionLanguagesPage
    }

    private fun renameVersion(versionLanguagesPage: VersionLanguagesPage): VersionLanguagesPage {
        val languagesPage = versionLanguagesPage
                .gotoSettingsTab()
                .gotoSettingsGeneral()
                .enterVersionID("master")
                .updateVersion()
        assertThat(languagesPage.versionID)
                .describedAs("The version name was updated to master")
                .isEqualTo("master")
        return languagesPage
    }

    private fun failToUploadDocuments(versionLanguagesPage: VersionLanguagesPage): VersionDocumentsTab {
        val versionDocumentsTab = versionLanguagesPage
                .gotoSettingsTab()
                .gotoSettingsDocumentsTab()
                .pressUploadFileButton()
                .enterFilePath(pdfFile.absolutePath)
        assertThat(versionDocumentsTab.canSubmitDocument())
                .describedAs("The user cannot submit the upload form")
                .isFalse()
        return versionDocumentsTab
    }

    private fun successfullyUploadDocument(versionDocumentsTab: VersionDocumentsTab): VersionDocumentsTab {
        val documentsTab = versionDocumentsTab
                .clickRemoveOn(pdfFile.name)
                .enterFilePath(testFile.absolutePath)
                .submitUpload()
                .clickUploadDone()
        assertThat(documentsTab.sourceDocumentsList)
                .describedAs("The source documents list contains the document")
                .contains(testFile.name)
        return documentsTab
    }

    private fun goToEditor(versionDocumentsTab: VersionDocumentsTab): EditorPage {
        val editorPage = versionDocumentsTab
                .gotoLanguageTab()
                .clickLocale(addedLocale)
                .clickDocument(testFile.name)
        assertThat(editorPage.getMessageSourceAtRowIndex(0))
                .describedAs("The first textflow entry is displayed")
                .contains("Line One")
        return editorPage
    }

    private fun translateDocument(editorPage: EditorPage): EditorPage {
        var editor = editorPage.translateTargetAtRowIndex(0, "Lyn Wun")
                .approveTranslationAtRow(0)
                .translateTargetAtRowIndex(1, "Lyn To")
                .translateTargetAtRowIndex(2, "Lyn Thre")
                .saveAsFuzzyAtRow(2)
        editor.reload()
        editor = EditorPage(editor.driver)
        assertThat(editor.getBasicTranslationTargetAtRowIndex(0))
                .describedAs("The first translation was successfully saved")
                .contains("Lyn Wun")
        return editor
    }

    private fun downloadTranslatedDocuments(editorPage: EditorPage): VersionLanguagesPage {
        // Make sure we don't verify the source
        assertThat(testFile.delete()).isTrue()
        val versionLanguagesPage = editorPage
                .clickVersionBreadcrumb("master")
                .clickLocale("en-US")
                .clickDownloadTranslatedFile(testFile.name, "txt")
        assertThat(File("/tmp/" + testFile.name).exists())
                .describedAs("The file was successfully downloaded")
                .isTrue()
        return versionLanguagesPage
    }

    private fun getDswid(page: AbstractPage): String {
        val currentUrl = page.driver.currentUrl
        return currentUrl.substring(currentUrl.indexOf("dswid="))
    }
}
