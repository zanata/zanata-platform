/*
 * Copyright 2016, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.util

import java.util.function.Function

import org.openqa.selenium.WebDriver
import org.openqa.selenium.support.ui.WebDriverWait
import org.zanata.page.WebDriverFactory

/**
 * Modifies WebDriverWait to check JavaScript logs and errors before each
 * invocation of the test function (typically every 500ms).
 * @author Sean Flanigan [sflaniga@redhat.com](mailto:sflaniga@redhat.com)
 */
class WebDriverLogWait(private val factory: WebDriverFactory,
                       timeOutInSeconds: Long) : WebDriverWait(factory.getDriver(), timeOutInSeconds) {

    override fun <V> until(isTruthy: Function<in WebDriver, V>): V {
        return super.until { driver ->
            factory.logLogs()
            isTruthy.apply(driver)
        }
    }
}
