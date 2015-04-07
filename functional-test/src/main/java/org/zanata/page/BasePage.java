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
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.zanata.page.account.ProfilePage;
import org.zanata.page.account.RegisterPage;
import org.zanata.page.account.SignInPage;
import org.zanata.page.administration.AdministrationPage;
import org.zanata.page.dashboard.DashboardBasePage;
import org.zanata.page.glossary.GlossaryPage;
import org.zanata.page.groups.VersionGroupsPage;
import org.zanata.page.languages.LanguagesPage;
import org.zanata.page.projects.ProjectVersionsPage;
import org.zanata.page.projects.ProjectsPage;
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

    @FindBy(id = "projects_link")
    private WebElement projectsLink;

    @FindBy(id = "version-groups_link")
    private WebElement groupsLink;

    @FindBy(id = "languages_link")
    private WebElement languagesLink;

    private static final By BY_SIGN_IN = By.id("signin_link");
    private static final By BY_SIGN_OUT = By.id("right_menu_sign_out_link");
    private static final By BY_DASHBOARD_LINK = By.id("dashboard");
    private static final By BY_ADMINISTRATION_LINK = By.id("administration");
    private static final By contactAdminLink = By.linkText("Contact admin");
    private static final By BY_USER_AVATAR = By.id("user--avatar");

    public BasePage(final WebDriver driver) {
        super(driver);
    }

    public DashboardBasePage goToMyDashboard() {
        log.info("Click Dashboard menu link");
        waitForWebElement(BY_USER_AVATAR).click();
        clickLinkAfterAnimation(BY_DASHBOARD_LINK);
        return new DashboardBasePage(getDriver());
    }

    public ProjectsPage goToProjects() {
        log.info("Click Projects");
        clickNavMenuItem(getDriver().findElement(By.id("projects_link")));
        return new ProjectsPage(getDriver());
    }

    private void clickNavMenuItem(final WebElement menuItem) {
        scrollToTop();
        slightPause();
        if (!menuItem.isDisplayed()) {
            // screen is too small the menu become dropdown
            getDriver().findElement(By.id("nav-main"))
                    .findElement(By.tagName("a")).click();
        }
        waitForAMoment().withMessage("displayed: " + menuItem).until(
                new Predicate<WebDriver>() {
                    @Override
                    public boolean apply(WebDriver input) {
                        return menuItem.isDisplayed();
                    }
                });
        // The notifications can sometimes get in the way
        waitForAMoment().withMessage("clickable: " + menuItem).until(
                ExpectedConditions.elementToBeClickable(menuItem));
        menuItem.click();
    }

    public VersionGroupsPage goToGroups() {
        log.info("Click Groups");
        clickNavMenuItem(groupsLink);
        return new VersionGroupsPage(getDriver());
    }

    public LanguagesPage goToLanguages() {
        log.info("Click Languages");
        clickNavMenuItem(getDriver().findElement(By.id("languages_link")));
        return new LanguagesPage(getDriver());
    }

    public GlossaryPage goToGlossary() {
        log.info("Click Glossary");
        // Dynamically find the link, as it is not present for every user
        clickNavMenuItem(getDriver().findElement(By.id("glossary_link")));
        return new GlossaryPage(getDriver());
    }

    public AdministrationPage goToAdministration() {
        log.info("Click Administration menu link");
        waitForWebElement(BY_USER_AVATAR).click();
        clickLinkAfterAnimation(BY_ADMINISTRATION_LINK);
        return new AdministrationPage(getDriver());
    }

    public RegisterPage goToRegistration() {
        log.info("Click Sign Up");
        Preconditions
                .checkArgument(!hasLoggedIn(),
                        "User has logged in! You should sign out or delete cookie first in your test.");
        WebElement registerLink =
                getDriver().findElement(By.id("register_link_internal_auth"));
        registerLink.click();
        return new RegisterPage(getDriver());
    }

    public SignInPage clickSignInLink() {
        log.info("Click Log In");
        waitForPageSilence();
        WebElement signInLink = getDriver().findElement(BY_SIGN_IN);
        signInLink.click();
        return new SignInPage(getDriver());
    }

    public boolean hasLoggedIn() {
        log.info("Query user is logged in");
        waitForPageSilence();
        List<WebElement> avatar = getDriver().findElements(BY_USER_AVATAR);
        return avatar.size() > 0;
    }

    public String loggedInAs() {
        log.info("Query logged in user name");
        waitForPageSilence();
        return waitForWebElement(BY_USER_AVATAR)
                .getAttribute("data-original-title");
    }

    public HomePage logout() {
        log.info("Click Log Out");
        scrollIntoView(waitForWebElement(BY_USER_AVATAR));
        waitForWebElement(BY_USER_AVATAR).click();
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
        getDriver().findElement(NavMenuBy)
                .findElement(By.linkText(navLinkText)).click();
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
        getExecutor().executeScript("arguments[0].click();", getDriver()
                .findElement(locator));
    }

    public void clickLinkAfterAnimation(WebElement element) {
        getExecutor().executeScript("arguments[0].click();", element);
    }

    public String getHelpURL() {
        log.info("Query Help URL");
        WebElement help_link = getDriver().findElement(By.id("help_link"));
        return help_link.getAttribute("href");
    }

    public ContactAdminFormPage clickContactAdmin() {
        log.info("Click Contact Admin button");
        waitForWebElement(contactAdminLink).click();
        return new ContactAdminFormPage(getDriver());
    }

    public BasePage enterSearch(String searchText) {
        log.info("Enter Project/Person search {}", searchText);
        WebElementUtil.searchAutocomplete(getDriver(), "projectAutocomplete",
                searchText);
        return new BasePage(getDriver());
    }

    public ProjectsPage submitSearch() {
        log.info("Press Enter on Project/Person search");
        getDriver().findElement(
                By.id("projectAutocomplete-autocomplete__input")).sendKeys(
                Keys.ENTER);
        return new ProjectsPage(getDriver());
    }

    public BasePage waitForSearchListContains(final String expected) {
        String msg = "Project/Person search list contains " + expected;
        logWaiting(msg);
        waitForAMoment().withMessage(msg).until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return getZanataSearchAutocompleteItems().contains(expected);
            }
        });
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
        searchItem.click();
    }

    public void clickWhenTabEnabled(final WebElement tab) {
        String msg = "Clickable tab: " + tab;
        waitForAMoment().withMessage(msg).until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                waitForPageSilence();
                boolean clicked = false;
                try {
                    scrollIntoView(tab);
                    if (tab.isDisplayed() && tab.isEnabled()) {
                        tab.click();
                        clicked = true;
                    }
                } catch (WebDriverException wde) {
                    return false;
                }
                return clicked;
            }
        });
    }

    public String getHtmlSource(WebElement webElement) {
        return (String) getExecutor().executeScript(
                "return arguments[0].innerHTML;", webElement);
    }


    public boolean isValid() {
        return (getDriver().findElements(By.id("home"))).size() > 0;
    }

    public void clickElement(By findby) {
        scrollIntoView(waitForWebElement(findby));
        waitForWebElement(findby).click();
    }
}
