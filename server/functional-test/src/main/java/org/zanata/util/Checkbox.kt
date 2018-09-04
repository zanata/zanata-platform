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

import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.UnexpectedTagNameException

/**
 * @author Patrick Huang [pahuang@redhat.com](mailto:pahuang@redhat.com)
 */
class Checkbox private constructor(private val chkElement: WebElement) {

    init {
        val tagName = chkElement.tagName
        if (null == tagName || "input" != tagName.toLowerCase()) {
            throw UnexpectedTagNameException("input", tagName)
        }

        val type = chkElement.getAttribute("type")
        if (type == null || "checkbox" != type.toLowerCase()) {
            throw IllegalArgumentException("element is not a checkbox")
        }
    }

    fun checked(): Boolean {
        return chkElement.isSelected
    }

    fun check() {
        if (!checked()) {
            chkElement.click()
        }
    }

    fun uncheck() {
        if (checked()) {
            chkElement.click()
        }
    }

    companion object {

        fun of(webElement: WebElement): Checkbox {
            return Checkbox(webElement)
        }
    }
}
