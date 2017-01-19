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
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.zanata.page.account.RegisterPage;
import org.zanata.page.account.SignInPage;
import org.zanata.page.administration.AdministrationPage;
import org.zanata.page.dashboard.DashboardBasePage;
import org.zanata.page.explore.ExplorePage;
import org.zanata.page.languages.LanguagesPage;
import org.zanata.page.utility.HomePage;
import com.google.common.base.Preconditions;
import org.zanata.workflow.BasicWorkFlow;

/**
 * A Base Page is an extension of the Core Page, providing the navigation bar
 * and sidebar links common to most pages outside of the editor.
 *
 * @author Damian Jansen
 *         <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class BasePage extends CorePage {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(BasePage.class);
    private static final By NAV = By.id("nav");
    private static final By languagesLink = By.id("nav_language");
    private static final By exploreLink = By.id("nav_search");
    private static final By glossaryLink = By.id("nav_glossary");
    private static final By BY_SIGN_IN = By.id("nav_login");
    private static final By BY_SIGN_OUT = By.id("nav_logout");
    private static final By BY_PROFILE = By.id("nav_profile");
    private static final By BY_DASHBOARD_LINK = By.id("nav_dashboard");
    private static final By BY_ADMINISTRATION_LINK = By.id("nav_admin");
    private static final By registrationLink = By.id("nav_sign_up");

    public BasePage(final WebDriver driver) {
        super(driver);
    }

    public DashboardBasePage goToMyDashboard() {
        log.info("Click Dashboard menu link");
        clickLinkAfterAnimation(BY_DASHBOARD_LINK);
        return new DashboardBasePage(getDriver());
    }

    public ExplorePage gotoExplore() {
        log.info("Click Explore menu link");
        clickLinkAfterAnimation(exploreLink);
        return new ExplorePage(getDriver());
    }

    private void clickNavMenuItem(final WebElement menuItem) {
        slightPause();
        clickElement(menuItem);
    }

    public LanguagesPage goToLanguages() {
        log.info("Click Languages");
        clickNavMenuItem(existingElement(languagesLink));
        return new LanguagesPage(getDriver());
    }

    public AdministrationPage goToAdministration() {
        log.info("Click Administration menu link");
        clickLinkAfterAnimation(BY_ADMINISTRATION_LINK);
        return new AdministrationPage(getDriver());
    }

    public RegisterPage goToRegistration() {
        log.info("Click Sign Up");
        Preconditions.checkArgument(!hasLoggedIn(),
                "User has logged in! You should sign out or delete cookie first in your test.");
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
        List<WebElement> logoutLink = getDriver().findElements(BY_SIGN_OUT);
        return logoutLink.size() > 0;
    }

    public String loggedInAs() {
        log.info("Query logged in user name");
        return existingElement(NAV).getAttribute("name");
    }

    public HomePage logout() {
        log.info("Click Log Out");
        clickLinkAfterAnimation(BY_SIGN_OUT);
        // Handle RHBZ1197955
        if (getDriver().findElements(By.className("error-div")).size() > 0) {
            log.info("RHBZ1197955 encountered, go to homepage");
            new BasicWorkFlow().goToHome();
        }
        return new HomePage(getDriver());
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

    public void clickWhenTabEnabled(final WebElement tab) {
        waitForPageSilence();
        clickElement(tab);
    }

    /**
     * Check if the page has the home button, expecting a valid base page
     *
     * @return boolean is valid
     */
    public boolean isPageValid() {
        return (getDriver().findElements(By.id("nav"))).size() > 0;
    }

    public void closeNotification() {
        getDriver().findElement(By.id("btn-notification-close")).click();
    }
}
