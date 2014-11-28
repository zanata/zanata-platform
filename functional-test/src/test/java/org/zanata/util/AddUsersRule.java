/*
 * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
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

import org.junit.rules.ExternalResource;
import org.zanata.common.LocaleId;
import com.google.common.base.Throwables;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;

import static org.zanata.util.SampleDataResourceClient.deleteExceptEssentialData;
import static org.zanata.util.SampleDataResourceClient.makeSampleLanguages;
import static org.zanata.util.SampleDataResourceClient.makeSampleUsers;
import static org.zanata.util.SampleDataResourceClient.userJoinsLanguageTeam;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
public class AddUsersRule extends ExternalResource {

    @Override
    protected void before() throws Exception {
        deleteExceptEssentialData();
        makeSampleUsers();
        makeSampleLanguages();
        userJoinsLanguageTeam("translator", "fr,hi,pl");
    }

    @Override
    protected void after() {
        try {
            deleteExceptEssentialData();
        }
        catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
