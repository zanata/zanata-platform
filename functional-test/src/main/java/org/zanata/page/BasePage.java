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
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.zanata.page.account.MyAccountPage;
import org.zanata.page.account.RegisterPage;
import org.zanata.page.account.SignInPage;
import org.zanata.page.administration.AdministrationPage;
import org.zanata.page.groups.VersionGroupsPage;
import org.zanata.page.projects.ProjectsPage;
import org.zanata.page.utility.HomePage;
import org.zanata.util.WebElementUtil;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import lombok.extern.slf4j.Slf4j;

/**
 * A Base Page is an extension of the Core Page, providing the navigation bar
 * and sidebar links common to most pages outside of the editor.
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

    @FindBy(id = "user--avatar")
    private WebElement userAvatar;

    private static final By BY_SIGN_IN = By.id("signin_link");
    private static final By BY_SIGN_OUT = By.id("right_menu_sign_out_link");
    private static final By BY_PROFILE_LINK = By.id("profile");
    private static final By BY_ADMINISTRATION_LINK = By.id("administration");

    public BasePage(final WebDriver driver) {
        super(driver);
    }

    public MyAccountPage goToMyProfile() {
        userAvatar.click();

        clickLinkAfterAnimation(BY_PROFILE_LINK);

        return new MyAccountPage(getDriver());
    }

    public ProjectsPage goToProjects() {
        clickNavMenuItem(projectsLink);
        return new ProjectsPage(getDriver());
    }

    private void clickNavMenuItem(final WebElement menuItem) {
        if (!menuItem.isDisplayed()) {
            // screen is too small the menu become dropdown
            getDriver().findElement(By.id("nav-main"))
                    .findElement(By.tagName("a"))
                    .click();
        }
        waitForTenSec().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return menuItem.isDisplayed();
            }
        });
        menuItem.click();
    }

    public VersionGroupsPage goToGroups() {
        clickNavMenuItem(groupsLink);
        return new VersionGroupsPage(getDriver());
    }

    public AdministrationPage goToAdministration() {
        userAvatar.click();

        clickLinkAfterAnimation(BY_ADMINISTRATION_LINK);

        return new AdministrationPage(getDriver());
    }

    public RegisterPage goToRegistration() {
        Preconditions
                .checkArgument(!hasLoggedIn(),
                        "User has logged in! You should sign out or delete cookie first in your test.");

        WebElement registerLink =
                getDriver().findElement(By.id("register_link_internal_auth"));
        registerLink.click();
        return new RegisterPage(getDriver());
    }

    public SignInPage clickSignInLink() {
        WebElement signInLink = getDriver().findElement(BY_SIGN_IN);
        signInLink.click();
        return new SignInPage(getDriver());
    }

    public boolean hasLoggedIn() {
        List<WebElement> signInLink = getDriver().findElements(BY_SIGN_IN);
        return signInLink.size() == 0;
    }

    public String loggedInAs() {
        return userAvatar.getAttribute("data-original-title");
    }

    public HomePage logout() {
        userAvatar.click();

        clickLinkAfterAnimation(BY_SIGN_OUT);
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
        JavascriptExecutor executor = (JavascriptExecutor) getDriver();
        executor.executeScript("arguments[0].click();", getDriver()
                .findElement(locator));
    }
}
