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

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import static org.zanata.util.SampleDataResourceClient.deleteExceptEssentialData;
import static org.zanata.util.SampleDataResourceClient.makeSampleLanguages;
import static org.zanata.util.SampleDataResourceClient.makeSampleProject;
import static org.zanata.util.SampleDataResourceClient.makeSampleUsers;
import static org.zanata.util.SampleDataResourceClient.setRateLimit;
import static org.zanata.util.SampleDataResourceClient.userJoinsLanguageTeam;

/**
 * Create sample data for tests
 *
 * @see org.junit.jupiter.api.extension.Extension
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 * @author Damian Jansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class SampleDataExtension implements BeforeEachCallback {
    public static final int CONCURRENT_RATE_LIMIT = 20;
    public static final int ACTIVE_RATE_LIMIT = 10;

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        deleteExceptEssentialData();
        makeSampleUsers();
        makeSampleLanguages();
        String locales = "fr,hi,pl";
        userJoinsLanguageTeam("translator", locales);
        userJoinsLanguageTeam("glossarist", locales);
        makeSampleProject();
        setRateLimit(ACTIVE_RATE_LIMIT, CONCURRENT_RATE_LIMIT);
    }
}
