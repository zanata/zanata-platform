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
package org.zanata.util;

import org.subethamail.wiser.WiserMessage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Damian Jansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class EmailQuery {

    private static Pattern activationLink = Pattern
            .compile("<(http://.+/activate/.+)>");
    private static Pattern validateLink = Pattern
            .compile("<(http://.+/validate_email/.+)>");

    public static boolean hasActivationLink(WiserMessage emailMessage) {
        Matcher matcher = activationLink.matcher(HasEmailRule.getEmailContent(emailMessage));
        return matcher.find();
    }

    public static String getActivationLink(WiserMessage emailMessage) {
        Matcher matcher = activationLink.matcher(HasEmailRule.getEmailContent(emailMessage));
        assert matcher.find();
        return matcher.group(1);
    }

    public static boolean hasEmailVerificationLink(WiserMessage emailMessage) {
        Matcher matcher = validateLink.matcher(HasEmailRule.getEmailContent(emailMessage));
        return matcher.find();
    }

    public static String getEmailVerificationLink(WiserMessage emailMessage) {
        Matcher matcher = validateLink.matcher(HasEmailRule.getEmailContent(emailMessage));
        assert matcher.find();
        return matcher.group(1);
    }
}
