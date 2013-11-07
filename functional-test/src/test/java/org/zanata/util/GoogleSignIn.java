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
package org.zanata.util;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Slf4j
public class GoogleSignIn {
    /**
     * Initiate a basic connection and confirm a HTTP_OK (200) response
     *
     * @return boolean - response was/not successful
     */
    public static boolean googleIsReachable() {
        try {
            URL url = new URL("http://www.google.com");
            HttpURLConnection httpURLConnection =
                    (HttpURLConnection) url.openConnection();
            httpURLConnection.connect();
            return HttpURLConnection.HTTP_OK == httpURLConnection
                    .getResponseCode();
        } catch (IOException ioe) {
            log.warn("Unable to initiate Google connection test: "
                    + ioe.getMessage());
            return false;
        }
    }

    /**
     * Query the environment for a GOOGLEID variable and return the stated
     * password that corresponds to the indicated username. Expects the variable
     * to be in the form: GOOGLEID=username1:password1;username2:password2;...
     *
     * @param username
     *            Username of username:password pair query
     * @return password for indicated username, or empty string for a
     *         query/match failure
     */
    public static String getSignIn(String username) {
        String googlePass;
        String empty = "";
        googlePass = System.getenv("GOOGLEID");
        if (googlePass == null || googlePass.isEmpty()) {
            return empty;
        }

        for (String signIn : googlePass.split(";")) {
            String[] usernamePasswordPair = signIn.split(":");
            if (usernamePasswordPair.length > 0
                    && usernamePasswordPair[0].equals(username)) {
                return usernamePasswordPair[1];
            }
        }
        log.warn("Cannot find user/password combination for " + username);
        return empty;
    }
}
