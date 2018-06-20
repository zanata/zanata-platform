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
package org.zanata.adapter

import com.github.wnameless.json.flattener.JsonFlattener
import com.github.wnameless.json.flattener.PrintMode
import com.github.wnameless.json.flattener.StringEscapePolicy
import com.github.wnameless.json.unflattener.JsonUnflattener
import com.google.common.collect.Maps
import org.zanata.adapter.FileFormatAdapter.ParserOptions
import org.zanata.adapter.FileFormatAdapter.WriterOptions
import org.zanata.common.ContentState
import org.zanata.common.ContentType
import org.zanata.exception.FileFormatAdapterException
import org.zanata.rest.dto.resource.Resource
import org.zanata.rest.dto.resource.TextFlow
import org.zanata.rest.dto.resource.TextFlowTarget
import org.zanata.rest.dto.resource.TranslationsResource
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.io.IOException
import java.io.OutputStream
import java.net.URI

/**
 * Adapter to handle JavaScript Object Notation (JSON) documents.
 * @see [JSON Specification](http://www.json.org/)
 *
 * @author Damian Jansen
 * [djansen@redhat.com](mailto:djansen@redhat.com)
 */
class JsonAdapter : FileFormatAdapter {

    override val rawTranslationUploadAvailable = true

    @Throws(FileFormatAdapterException::class, IllegalArgumentException::class)
    override fun parseDocumentFile(options: ParserOptions): Resource {
        val document = Resource().apply {
            lang = options.locale
            contentType = ContentType.TextPlain
        }
        val resources = document.textFlows
        val flatMap = createFlattenedMap(options.rawFile)
        for ((key, value) in flatMap) {
            if (value is String) {
                val textFlow = TextFlow(key, options.locale, value)
                textFlow.isPlural = false
                resources.add(textFlow)
            }
        }
        return document
    }

    @Throws(FileFormatAdapterException::class, IllegalArgumentException::class)
    override fun parseTranslationFile(options: ParserOptions):
            TranslationsResource {
        val transRes = TranslationsResource()
        val translations = transRes.textFlowTargets
        val flatMap = createFlattenedMap(options.rawFile)
        for ((key, value) in flatMap) {
            if (value is String) {
                val textFlowTarget = TextFlowTarget(key).apply {
                    contents = listOf(value)
                    state = ContentState.Translated
                }
                translations.add(textFlowTarget)
            }
        }
        return transRes
    }

    override fun writeTranslatedFile(output: OutputStream,
                                     options: WriterOptions,
                                     approvedOnly: Boolean) {
        val document = Resource()
        document.contentType = ContentType.TextPlain
        val translations = transformToMapByResId(
                options.translatedDoc.translation!!.textFlowTargets)
        val flatMap = createFlattenedMap(options.sourceParserOptions.rawFile)
        for (key in flatMap.keys) {
            translations[key]?.let { tft ->
                if (usable(tft, approvedOnly)) {
                    flatMap[key] = tft.contents[0]
                }
            }
        }
        try {
            output.write(JsonUnflattener(flatMap.toString())
                    .withPrintMode(PrintMode.PRETTY)
                    .unflatten().toByteArray())
        } catch (e: IOException) {
            throw FileFormatAdapterException("Cannot create the translated file")
        }

    }

    /**
     * Transform list of TextFlowTarget to map with TextFlowTarget.resId as key
     *
     * @param targets
     * @return map of TextFlowTargets by resId
     */
    private fun transformToMapByResId(
            targets: List<TextFlowTarget>): Map<String, TextFlowTarget> {
        val resIdTargetMap = Maps.newHashMap<String, TextFlowTarget>()

        for (target in targets) {
            resIdTargetMap[target.resId] = target
        }
        return resIdTargetMap
    }

    private fun usable(target: TextFlowTarget, approvedOnly: Boolean): Boolean {
        return (target.state.isApproved || !approvedOnly && target.state.isTranslated)
    }

    private fun createFlattenedMap(rawFile: URI): MutableMap<String, Any> {
        try {
            return JsonFlattener(
                    InputStreamReader(FileInputStream(File(rawFile))))
                    .flattenAsMap()
        } catch (e: IOException) {
            throw FileFormatAdapterException("Cannot open the source file")
        }
    }
}
