/*
 * Copyright 2015, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.page.account;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.zanata.page.BasePage;
import org.zanata.page.utility.HomePage;

/**
 * @author Damian Jansen
 *         <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class InactiveAccountPage extends BasePage {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(InactiveAccountPage.class);

    public InactiveAccountPage(WebDriver driver) {
        super(driver);
    }

    public HomePage clickResendActivationEmail() {
        log.info("Click resend activation email");
        clickElement(By.id("resendEmail"));
        return new HomePage(getDriver());
    }

    public InactiveAccountPage enterNewEmail(String email) {
        enterText(
                readyElement(
                        By.id("inactiveAccountForm:email:input:emailInput")),
                email);
        return new InactiveAccountPage(getDriver());
    }

    public HomePage clickUpdateEmail() {
        clickElement(By.id("inactiveAccountForm:email:input:updateEmail"));
        return new HomePage(getDriver());
    }
}
