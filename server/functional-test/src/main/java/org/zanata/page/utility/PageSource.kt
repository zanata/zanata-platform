/*
 * Copyright 2017, Red Hat, Inc. and individual contributors
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
package org.zanata.page.utility

import org.openqa.selenium.WebDriver
import org.zanata.util.ShortString.shorten

/**
 * Summarises the HTML for a page.
 * @param pageSource HTML to be summarised
 * @return a summary of the HTML
 * @author Sean Flanigan [sflaniga@redhat.com](mailto:sflaniga@redhat.com)
 */
fun shortenPageSource(pageSource: String): String {
    // Chrome's dinosaur game puts lots of binary data into the DOM
    if (pageSource.contains("data:image/png;base64") && pageSource.contains("ERR_INTERNET_DISCONNECTED")) {
        return "ERR_INTERNET_DISCONNECTED"
    }
    return shorten(pageSource, 2000)
}

/**
 * Extension function to summarise the HTML for WebDriver's current page.
 * @return a summary of the HTML
 * @author Sean Flanigan [sflaniga@redhat.com](mailto:sflaniga@redhat.com)
 */
fun WebDriver.shortenPageSource(): String {
    return shortenPageSource(pageSource)
}
