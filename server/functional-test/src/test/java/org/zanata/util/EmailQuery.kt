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
package org.zanata.util

import org.subethamail.wiser.WiserMessage
import java.util.regex.Pattern

/**
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
object EmailQuery {
    private val log = org.slf4j.LoggerFactory.getLogger(EmailQuery::class.java)

    enum class LinkType {
        ACTIVATE, VALIDATE_EMAIL, PASSWORD_RESET
    }

    private fun getLinkRegex(type: LinkType): Pattern {
        return Pattern.compile("<(http://.+/" + type.name.toLowerCase() + "/.+)>")
    }

    fun hasLink(emailMessage: WiserMessage, linkType: LinkType): Boolean {
        log.info("Query {} has a {} link", emailMessage, linkType.name)
        val linkPattern = getLinkRegex(linkType)
        val matcher = linkPattern.matcher(HasEmailExtension.getEmailContent(emailMessage))
        return matcher.find()
    }

    fun getLink(emailMessage: WiserMessage, linkType: LinkType): String {
        log.info("Get {} link from email {}", linkType.name, emailMessage)
        val linkPattern = getLinkRegex(linkType)
        val matcher = linkPattern.matcher(HasEmailExtension.getEmailContent(emailMessage))
        assert(matcher.find())
        return matcher.group(1)
    }
}
