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
package org.zanata.page;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.zanata.page.account.ProfilePage;
import org.zanata.page.account.RegisterPage;
import org.zanata.page.account.SignInPage;
import org.zanata.page.administration.AdministrationPage;
import org.zanata.page.dashboard.DashboardBasePage;
import org.zanata.page.groups.VersionGroupsPage;
import org.zanata.page.languages.LanguagesPage;
import org.zanata.page.projects.ProjectVersionsPage;
import org.zanata.page.projects.ProjectsPage;
import org.zanata.page.search.SearchPage;
import org.zanata.page.utility.ContactAdminFormPage;
import org.zanata.page.utility.HomePage;
import org.zanata.util.WebElementUtil;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import lombok.extern.slf4j.Slf4j;
import org.zanata.workflow.BasicWorkFlow;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A Base Page is an extension of the Core Page, providing the navigation bar
 * and sidebar links common to most pages outside of the editor.
 *
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Slf4j
public class BasePage extends CorePage {

    private final By NavMenuBy = By.id("nav-main");
    private By projectsLink = By.id("projects_link");
    private By groupsLink = By.id("version-groups_link");
    private By languagesLink = By.id("languages_link");
    private By glossaryLink = By.id("glossary_link");
    private By userAvatar = By.id("user--avatar");
    private static final By BY_SIGN_IN = By.id("signin_link");
    private static final By BY_SIGN_OUT = By.id("banner_form:right_menu_sign_out_link");
    private static final By BY_DASHBOARD_LINK = By.id("banner_form:dashboard");
    private static final By BY_ADMINISTRATION_LINK = By.id("banner_form:administration");
    private By searchInput = By.id("projectAutocomplete-autocomplete__input");
    private By registrationLink = By.id("register_link_internal_auth");
    private static final By contactAdminLink = By.linkText("Contact admin");

    public BasePage(final WebDriver driver) {
        super(driver);
    }

    public DashboardBasePage goToMyDashboard() {
        log.info("Click Dashboard menu link");
        clickElement(userAvatar);
        clickLinkAfterAnimation(BY_DASHBOARD_LINK);
        return new DashboardBasePage(getDriver());
    }

    public ProjectsPage goToProjects() {
        log.info("Click Projects");
        clickNavMenuItem(existingElement(projectsLink));
        return new ProjectsPage(getDriver());
    }

    private void clickNavMenuItem(final WebElement menuItem) {
        scrollToTop();
        slightPause();
        if (!menuItem.isDisplayed()) {
            // screen is too small the menu become dropdown
            clickElement(readyElement(existingElement(By.id("nav-main")), By.tagName("a")));
        }
        clickElement(menuItem);
    }

    public VersionGroupsPage goToGroups() {
        log.info("Click Groups");
        clickNavMenuItem(existingElement(groupsLink));
        return new VersionGroupsPage(getDriver());
    }

    public LanguagesPage goToLanguages() {
        log.info("Click Languages");
        clickNavMenuItem(existingElement(languagesLink));
        return new LanguagesPage(getDriver());
    }

    public AdministrationPage goToAdministration() {
        log.info("Click Administration menu link");
        clickElement(userAvatar);
        clickLinkAfterAnimation(BY_ADMINISTRATION_LINK);
        return new AdministrationPage(getDriver());
    }

    public RegisterPage goToRegistration() {
        log.info("Click Sign Up");
        Preconditions.checkArgument(!hasLoggedIn(),
                "User has logged in! You should sign out or delete cookie " +
                        "first in your test.");

        clickElement(registrationLink);
        return new RegisterPage(getDriver());
    }

    public SignInPage clickSignInLink() {
        log.info("Click Log In");
        clickElement(BY_SIGN_IN);
        return new SignInPage(getDriver());
    }

    public boolean hasLoggedIn() {
        log.info("Query user is logged in");
        waitForPageSilence();
        List<WebElement> avatar = getDriver().findElements(userAvatar);
        return avatar.size() > 0;
    }

    public String loggedInAs() {
        log.info("Query logged in user name");
        return existingElement(userAvatar).getAttribute("data-original-title");
    }

    public HomePage logout() {
        log.info("Click Log Out");
        clickElement(userAvatar);
        clickLinkAfterAnimation(BY_SIGN_OUT);
        //Handle RHBZ1197955
        if (getDriver().findElements(By.className("error-div")).size() > 0) {
            log.info("RHBZ1197955 encountered, go to homepage");
            new BasicWorkFlow().goToHome();
        }
        return new HomePage(getDriver());
    }

    public List<String> getBreadcrumbLinks() {
        List<WebElement> breadcrumbs =
                getDriver().findElement(By.id("breadcrumbs_panel"))
                        .findElements(By.className("breadcrumbs_link"));
        return WebElementUtil.elementsToText(breadcrumbs);
    }

    public String getLastBreadCrumbText() {
        WebElement breadcrumb =
                getDriver().findElement(By.id("breadcrumbs_panel"))
                        .findElement(By.className("breadcrumbs_display"));
        return breadcrumb.getText();
    }

    public <P> P clickBreadcrumb(final String link, Class<P> pageClass) {
        List<WebElement> breadcrumbs =
                getDriver().findElement(By.id("breadcrumbs_panel"))
                        .findElements(By.className("breadcrumbs_link"));
        Predicate<WebElement> predicate = new Predicate<WebElement>() {
            @Override
            public boolean apply(WebElement input) {
                return input.getText().equals(link);
            }
        };
        Optional<WebElement> breadcrumbLink =
                Iterables.tryFind(breadcrumbs, predicate);
        if (breadcrumbLink.isPresent()) {
            breadcrumbLink.get().click();
            return PageFactory.initElements(getDriver(), pageClass);
        }
        throw new RuntimeException("can not find " + link + " in breadcrumb: "
                + WebElementUtil.elementsToText(breadcrumbs));
    }

    public <P> P goToPage(String navLinkText, Class<P> pageClass) {
        readyElement(existingElement(NavMenuBy),
                By.linkText(navLinkText)).click();
        return PageFactory.initElements(getDriver(), pageClass);
    }

    /**
     * This is a workaround for
     * https://code.google.com/p/selenium/issues/detail?id=2766 Elemenet not
     * clickable at point due to the change coordinate of element in page.
     *
     * @param locator
     */
    public void clickLinkAfterAnimation(By locator) {
        clickLinkAfterAnimation(existingElement(locator));
    }

    public void clickLinkAfterAnimation(WebElement element) {
        getExecutor().executeScript("arguments[0].click();", element);
    }

    public String getHelpURL() {
        log.info("Query Help URL");
        WebElement help_link = existingElement(By.id("help_link"));
        return help_link.getAttribute("href");
    }

    public ContactAdminFormPage clickContactAdmin() {
        log.info("Click Contact Admin button");
        clickElement(contactAdminLink);
        return new ContactAdminFormPage(getDriver());
    }

    public BasePage enterSearch(String searchText) {
        log.info("Enter Project/Person search {}", searchText);
        WebElementUtil.searchAutocomplete(getDriver(), "projectAutocomplete",
                searchText);
        return new BasePage(getDriver());
    }

    public SearchPage submitSearch() {
        log.info("Press Enter on Zanata search");
        existingElement(searchInput).sendKeys(Keys.ENTER);
        return new SearchPage(getDriver());
    }

    public BasePage expectSearchListContains(final String expected) {
        waitForPageSilence();
        String msg = "Project search list contains " + expected;
        waitForAMoment().withMessage("Waiting for search contains").until(
                new Predicate<WebDriver>() {
                    @Override
                    public boolean apply(WebDriver input) {
                        return getZanataSearchAutocompleteItems()
                                .contains(expected);
                    }
                }
        );
        assertThat(getZanataSearchAutocompleteItems()).as(msg).contains(
                expected);
        return new BasePage(getDriver());
    }

    public List<String> getZanataSearchAutocompleteItems() {
        log.info("Query Project/Person search results list");
        return WebElementUtil.getSearchAutocompleteItems(getDriver(),
                "general-search-form", "projectAutocomplete");
    }

    public ProfilePage clickUserSearchEntry(final String searchEntry) {
        log.info("Click Person search result {}", searchEntry);
        clickSearchEntry(searchEntry);
        return new ProfilePage(getDriver());
    }

    public ProjectVersionsPage clickProjectSearchEntry(String searchEntry) {
        log.info("Click Projects search result {}", searchEntry);
        clickSearchEntry(searchEntry);
        return new ProjectVersionsPage(getDriver());
    }

    private void clickSearchEntry(final String searchEntry) {
        String msg = "search result " + searchEntry;
        WebElement searchItem =
                waitForAMoment().withMessage(msg).until(
                        new Function<WebDriver, WebElement>() {
                            @Override
                            public WebElement apply(WebDriver driver) {
                                List<WebElement> items =
                                        WebElementUtil
                                                .getSearchAutocompleteResults(
                                                        driver,
                                                        "general-search-form",
                                                        "projectAutocomplete");

                                for (WebElement item : items) {
                                    if (item.getText().equals(searchEntry)) {
                                        return item;
                                    }
                                }
                                return null;
                            }
                        });
        clickElement(searchItem);
    }

    public void clickWhenTabEnabled(final WebElement tab) {
        waitForPageSilence();
        clickElement(tab);
    }

    /**
     * Check if the page has the home button, expecting a valid base page
     * @return boolean is valid
     */
    public boolean isPageValid() {
        return (getDriver().findElements(By.id("home"))).size() > 0;
    }

}
