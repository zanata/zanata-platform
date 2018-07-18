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
import java.io.*
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


    /**
     * Parse an origin JSON file as a Resource
     *
     * @param options ParserOptions containing the source locale and JSON file URI
     */
    @Throws(FileFormatAdapterException::class, IllegalArgumentException::class)
    override fun parseDocumentFile(options: ParserOptions): Resource {
        val document = Resource().apply {
            lang = options.locale
            contentType = ContentType.TextPlain
        }
        val resources = document.textFlows
        val flatMap = jsonFileToFlattenedMap(options.rawFile)
        for ((key, value) in flatMap) {
            if (value is String) {
                val textFlow = TextFlow(key, options.locale, value)
                textFlow.isPlural = false
                resources.add(textFlow)
            }
        }
        return document
    }

    /**
     * Parse a translated JSON raw file as a TranslationsResource
     *
     * @param options ParserOptions containing a URI to the JSON file
     */
    @Throws(FileFormatAdapterException::class, IllegalArgumentException::class)
    override fun parseTranslationFile(options: ParserOptions):
            TranslationsResource {
        val transRes = TranslationsResource()
        val translations = transRes.textFlowTargets
        val flatMap = jsonFileToFlattenedMap(options.rawFile)
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

    /**
     * Reads an original JSON file, converts it to a Map and replaces entry
     * values with translations. Writes the result to the given output.
     *
     * @param output write destination for the processed result
     * @param options given writer options, containing translations (if any)
     *     and original JSON file location
     * @param approvedOnly specify whether to include Translated translations,
     *     or only Approved
     */
    override fun writeTranslatedFile(output: OutputStream,
                                     options: WriterOptions,
                                     approvedOnly: Boolean) {
        val document = Resource()
        document.contentType = ContentType.TextPlain
        val flatMap = jsonFileToFlattenedMap(options.sourceParserOptions.rawFile)
        replaceWithTranslations(flatMap,
                options.translatedDoc.translation!!.textFlowTargets,
                approvedOnly)
        try {
            output.writer().use {
                it.write(flattenedMapToJson(flatMap))
            }
        } catch (exception: Exception) {
            when(exception) {
                is IOException -> {
                    throw FileFormatAdapterException(
                            "Cannot create the translated file")
                }
                else -> throw exception
            }
        }
    }

    /**
     * Replace entries in the flattened map with corresponding translations
     *
     * @param flatMap original JSON file as a map
     * @param transTargets list of translations to apply to the map
     * @param approvedOnly whether to include Translated state entries
     */
    private fun replaceWithTranslations(flatMap: MutableMap<String, Any>,
                                        transTargets: List<TextFlowTarget>,
                                        approvedOnly: Boolean) {
        val translations = transformToMapByResId(transTargets)
        for (key in flatMap.keys) {
            translations[key]?.let { tft ->
                if (usable(tft, approvedOnly)) {
                    flatMap[key] = tft.contents[0]
                }
            }
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

    /**
     * Determine translation is usable, based on the approved only flag
     *
     * @param target TextFlowTarget to query
     * @param approvedOnly whether to include Translated state entries
     */
    private fun usable(target: TextFlowTarget, approvedOnly: Boolean): Boolean {
        return (target.state.isApproved || !approvedOnly && target.state.isTranslated)
    }

    /**
     * Read a json file and return a Map of key value pairs
     *
     * @param rawFile URI of raw file to be read
     */
    private fun jsonFileToFlattenedMap(rawFile: URI): MutableMap<String, Any> {
        try {
            return JsonFlattener(
                    InputStreamReader(FileInputStream(File(rawFile))))
                    .flattenAsMap()
        } catch (e: IOException) {
            throw FileFormatAdapterException("Cannot open the source file")
        }
    }

    /**
     * Convert a Map to pretty JSON
     *
     * @param flatMap map to be converted to JSON
     */
    private fun flattenedMapToJson(flatMap: Map<String, Any>): String {
        return JsonUnflattener(flatMap.toString())
                .withPrintMode(PrintMode.PRETTY)
                .unflatten()
    }

}
