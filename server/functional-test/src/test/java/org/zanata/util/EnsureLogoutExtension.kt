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
package org.zanata.util

import org.junit.jupiter.api.extension.*
import org.zanata.page.utility.HomePage
import org.zanata.workflow.BasicWorkFlow

/**
 * A test rule that will ensure tests have a clean browser session before and
 * after.
 *
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 * @author Patrick Huang [pahuang@redhat.com](mailto:pahuang@redhat.com)
 */
class EnsureLogoutExtension : BeforeEachCallback, AfterTestExecutionCallback {

    override fun beforeEach(context: ExtensionContext) {
        logoutIfLoggedIn()
    }

    override fun afterTestExecution(context: ExtensionContext) {
        logoutIfLoggedIn()
    }

    private fun logoutIfLoggedIn(): HomePage {
        val homePage = BasicWorkFlow().goToHome()
        if (homePage.hasLoggedIn()) {
            homePage.logout()
        }
        return homePage
    }
}
