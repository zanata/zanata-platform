/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.workflow;

import com.google.common.base.Predicate;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.zanata.page.AbstractPage;
import org.zanata.page.BasePage;
import org.zanata.page.account.InactiveAccountPage;
import org.zanata.page.account.SignInPage;
import org.zanata.page.dashboard.DashboardBasePage;
import java.util.List;

public class LoginWorkFlow extends AbstractWebWorkFlow {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(LoginWorkFlow.class);

    public LoginWorkFlow() {
    }

    public <P extends AbstractPage> P signInAndGoToPage(String username,
            String password, Class<P> pageClass) {
        try {
            doSignIn(username, password);
        } catch (IllegalAccessError iae) {
            log.warn(
                    "Login failed. May due to some weird issue. Will Try again.");
            doSignIn(username, password);
        } catch (TimeoutException e) {
            log.error(
                    "timeout on login. If you are running tests manually with cargo.wait, you probably forget to create the user admin/admin.");
            throw e;
        }
        return PageFactory.initElements(driver, pageClass);
    }

    public DashboardBasePage signIn(String username, String password) {
        log.info("accessing zanata at: {}", hostUrl);
        return signInAndGoToPage(username, password, DashboardBasePage.class);
    }

    public SignInPage signInFailure(String username, String password) {
        log.info("log in as username: {}", username);
        return new BasePage(driver).clickSignInLink().enterUsername(username)
                .enterPassword(password).clickSignInExpectError();
    }

    public InactiveAccountPage signInInactive(String username,
            String password) {
        log.info("log in as username: {}", username);
        return new BasePage(driver).clickSignInLink().enterUsername(username)
                .enterPassword(password).clickSignInExpectInactive();
    }

    private void doSignIn(String username, String password) {
        log.info("log in as username: {}", username);
        BasePage basePage = new BasePage(driver);
        basePage.deleteCookiesAndRefresh();
        basePage.clickSignInLink().enterUsername(username)
                .enterPassword(password).clickSignIn().waitForAMoment()
                .withMessage("login").until((Predicate<WebDriver>) driver1 -> {
                    // only enable this if you temporarily disable implicit
                    // waits:
                    // fail-fast logic
                    // TODO can we enable this now?
                    // List<WebElement> messages =
                    // driver.findElements(By.className("message--danger"));
                    // if (messages.size() > 0 && messages.get(0)
                    // .getText().contains(" Login failed ")) {
                    // throw new IllegalAccessError("Login failed");
                    // }
                    driver1.findElement(By.id("nav_logout"));
                    return true;
                });
    }
}
