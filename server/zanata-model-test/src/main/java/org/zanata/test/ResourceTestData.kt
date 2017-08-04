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
package org.zanata.test

import org.zanata.common.ContentType
import org.zanata.common.LocaleId
import org.zanata.common.ResourceType
import org.zanata.rest.dto.extensions.comment.SimpleComment
import org.zanata.rest.dto.extensions.gettext.HeaderEntry
import org.zanata.rest.dto.extensions.gettext.PoHeader
import org.zanata.rest.dto.extensions.gettext.PotEntryHeader
import org.zanata.rest.dto.resource.Resource
import org.zanata.rest.dto.resource.ResourceMeta
import org.zanata.rest.dto.resource.TextFlow

/**
 * @author Sean Flanigan [sflaniga@redhat.com](mailto:sflaniga@redhat.com)
 */
object ResourceTestData {

    @JvmStatic
    val testDocMetaWithPoHeader: ResourceMeta
        get() = testDocMeta.apply {
            getExtensions(true).add(
                    PoHeader("comment", HeaderEntry("ref", "test ref")))
        }

    @JvmStatic
    val testDocMeta: ResourceMeta
        get() = ResourceMeta().apply {
            contentType = ContentType.TextPlain
            name = "test1"
            lang = LocaleId("en-US")
            type = ResourceType.FILE
            getExtensions(true)
        }

    @JvmStatic
    val testDocWithTextFlow: Resource
        get() = Resource("test1").apply {
            contentType = ContentType.TextPlain
            lang = LocaleId.EN_US
            type = ResourceType.FILE
            getExtensions(true)
            textFlows.add(TextFlow("rest1", LocaleId.EN_US, "tf1").apply { getExtensions(true) })
        }

    @JvmStatic
    val testDocWith2TextFlows: Resource
        get() {
            val sr = Resource("test2")
            sr.contentType = ContentType.TextPlain
            sr.lang = LocaleId.EN_US
            sr.type = ResourceType.FILE
            sr.getExtensions(true)

            val stf = TextFlow("tf1", LocaleId.EN_US, "tf1")
            stf.getExtensions(true)
            val stf2 = TextFlow("tf2", LocaleId.EN_US, "testtf2")
            sr.textFlows.add(stf)
            sr.textFlows.add(stf2)
            return sr
        }

    @JvmStatic
    val testDocWithPoHeader: Resource
        get() = testDocWithTextFlow.apply {
            getExtensions(true).add(PoHeader("comment", HeaderEntry("h1", "v1"),
                    HeaderEntry("h2", "v2"), HeaderEntry(
                    "Content-Type", "charset=UTF-8")))
        }

    @JvmStatic
    val testDocWithPotEntryHeader: Resource
        get() {
            val sr = testDocWithTextFlow
            val stf = sr.textFlows[0]

            val potEntryHeader = PotEntryHeader()
            potEntryHeader.context = "potentrycontext"
            potEntryHeader.flags.add("")
            potEntryHeader.references.add("")
            stf.getExtensions(true).add(potEntryHeader)
            return sr
        }

    @JvmStatic
    val testDocWithTextFlowComment: Resource
        get() = testDocWithTextFlow.apply {
            textFlows[0].getExtensions(true).add(SimpleComment("textflow comment"))
        }

}
