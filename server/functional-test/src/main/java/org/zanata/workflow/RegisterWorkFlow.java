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
package org.zanata.workflow;

import org.zanata.page.account.EditProfilePage;
import org.zanata.page.account.SignInPage;
import org.zanata.page.googleaccount.GoogleAccountPage;
import org.zanata.page.utility.HomePage;

/**
 * @author Damian Jansen
 *         <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class RegisterWorkFlow extends AbstractWebWorkFlow {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(RegisterWorkFlow.class);

    public SignInPage registerInternal(String name, String username,
            String password, String email) {
        log.info("Register as {}:{}, ({}:{})", username, password, name, email);
        return new BasicWorkFlow().goToHome().goToRegistration().enterName(name)
                .enterUserName(username).enterPassword(password)
                .enterEmail(email).register();
    }

    public HomePage registerGoogleOpenID(String name, String username,
            String password, String email) {
        GoogleAccountPage googleAccountPage = new BasicWorkFlow().goToHome()
                .clickSignInLink().selectGoogleOpenID();
        /*
         * There is the chance that Google presents the old page. It seems to be
         * random. Just enter the email in this case. Otherwise, If Google has
         * remembered us, skip entering the email. If Google has remembered
         * someone else, change the user.
         */
        if (googleAccountPage.isTheOldGoogleSite()) {
            googleAccountPage = googleAccountPage.enterGoogleEmail(email);
        } else if (googleAccountPage.hasRememberedAuthentication()) {
            if (!googleAccountPage.rememberedUser().equals(email)) {
                googleAccountPage =
                        googleAccountPage.removeSavedAuthentication();
            }
        } else {
            googleAccountPage = googleAccountPage.enterGoogleEmail(email);
        }
        EditProfilePage editProfilePage =
                googleAccountPage.enterGooglePassword(password).clickSignIn()
                        .acceptPermissions();
        return editProfilePage.enterName(name).enterUserName(username)
                .enterEmail(email).clickSave();
    }
}
