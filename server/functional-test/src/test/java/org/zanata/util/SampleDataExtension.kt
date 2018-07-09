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
package org.zanata.util

import org.junit.jupiter.api.extension.BeforeTestExecutionCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.zanata.util.SampleDataResourceClient.deleteExceptEssentialData
import org.zanata.util.SampleDataResourceClient.makeSampleLanguages
import org.zanata.util.SampleDataResourceClient.makeSampleProject
import org.zanata.util.SampleDataResourceClient.makeSampleUsers
import org.zanata.util.SampleDataResourceClient.setRateLimit
import org.zanata.util.SampleDataResourceClient.userJoinsLanguageTeam

/**
 * Create sample data for tests
 *
 * @see org.junit.jupiter.api.extension.Extension
 *
 * @author Patrick Huang [pahuang@redhat.com](mailto:pahuang@redhat.com)
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
class SampleDataExtension : BeforeTestExecutionCallback {

    override fun beforeTestExecution(context: ExtensionContext) {
        deleteExceptEssentialData()
        makeSampleUsers()
        makeSampleLanguages()
        val locales = "fr,hi,pl"
        userJoinsLanguageTeam("translator", locales)
        userJoinsLanguageTeam("glossarist", locales)
        makeSampleProject()
        setRateLimit(ACTIVE_RATE_LIMIT, CONCURRENT_RATE_LIMIT)
    }

    companion object {
        const val CONCURRENT_RATE_LIMIT = 20
        const val ACTIVE_RATE_LIMIT = 10
    }
}
