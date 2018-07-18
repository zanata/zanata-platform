/*
 * Copyright 2016, Red Hat, Inc. and individual contributors as indicated by the
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

import org.junit.jupiter.api.extension.AfterTestExecutionCallback
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback
import org.zanata.util.SampleDataResourceClient.allowAnonymousUser
import org.junit.jupiter.api.extension.ExtensionContext

/**
 * This will allow anonymous user to access Zanata read only resources.
 * @author Patrick Huang [pahuang@redhat.com](mailto:pahuang@redhat.com)
 */
class AllowAnonymousExtension : BeforeTestExecutionCallback, AfterTestExecutionCallback {


    override fun beforeTestExecution(context: ExtensionContext) {
        SampleDataResourceClient.allowAnonymousUser(true)
    }

    override fun afterTestExecution(context: ExtensionContext) {
        try {
            SampleDataResourceClient.allowAnonymousUser(false)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

    }
}
