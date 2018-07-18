package org.zanata.service.impl

import org.fedorahosted.openprops.Properties
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.zanata.common.DocumentType
import org.zanata.common.DocumentType.GETTEXT
import org.zanata.common.DocumentType.PROPERTIES
import org.zanata.common.DocumentType.PROPERTIES_UTF8
import org.zanata.common.ProjectType
import org.zanata.model.HDocument
import org.zanata.model.HLocale
import org.zanata.model.HSimpleComment
import org.zanata.model.HTextFlow
import org.zanata.model.po.HPoHeader
import org.zanata.model.po.HPoTargetHeader
import org.zanata.rest.service.PoUtility.headerToProperties
import org.zanata.rest.service.PoUtility.propertiesToHeader
import javax.enterprise.context.ApplicationScoped

internal const val ATTRIBUTION_KEY = "X-Zanata-MT-Attribution"

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */

@ApplicationScoped
class AttributionService {
    companion object {
        val log: Logger = LoggerFactory.getLogger(AttributionService::class.java)
    }

    fun inferDocumentType(doc: HDocument): DocumentType? {
        return when (doc.projectIteration.effectiveProjectType) {
            ProjectType.Gettext, ProjectType.Podir -> GETTEXT
            ProjectType.Properties -> PROPERTIES
            ProjectType.Utf8Properties -> PROPERTIES_UTF8
            ProjectType.Xml -> DocumentType.XML
            ProjectType.Xliff -> DocumentType.XLIFF
            ProjectType.File -> {
                // null rawDoc is assumed to be gettext
                val rawDocument = doc.rawDocument ?: return GETTEXT
                rawDocument.type
            }
            // A very old project with no effective project type
            null -> null
        }
    }

    fun supportsAttribution(doc: HDocument): Boolean =
            when (inferDocumentType(doc)) {
                GETTEXT,
                PROPERTIES,
                PROPERTIES_UTF8 -> true
                else -> false
            }

    fun addAttribution(doc: HDocument, locale: HLocale, backendId: String) {
        if (doc.textFlows.isEmpty()) return
        val attributionMessage = getAttributionMessage(backendId)

        val docType = inferDocumentType(doc)
        when (docType) {
            GETTEXT -> {
                val poTargetHeader = doc.poTargetHeaders.getOrPut(locale) {
                    HPoTargetHeader().apply {
                        targetLanguage = locale
                        document = doc
                    }
                }
                ensureAttributionForGettext(poTargetHeader, attributionMessage)
            }
            PROPERTIES,
            PROPERTIES_UTF8 -> ensureAttributionForProperties(doc.textFlows[0], attributionMessage)
            null -> throw RuntimeException("null DocumentType for $doc")
            else -> throw RuntimeException("unexpected DocumentType for $doc")
        }
    }

    internal fun ensureAttributionForGettext(header: HPoTargetHeader, attributionMessage: String) {
        val props = if (header.entries == null) {
            Properties()
        } else {
            headerToProperties(header.entries)
        }
        props.setProperty(ATTRIBUTION_KEY, attributionMessage)
        header.entries = propertiesToHeader(props)
    }

    internal fun ensureAttributionForProperties(textFlow: HTextFlow, attributionMessage: String) {
        val prefix = "$ATTRIBUTION_KEY:"
        val attributionLine = "$prefix $attributionMessage"
        if (textFlow.comment != null) {
            val oldLines = textFlow.comment.comment.lines()
            textFlow.comment.comment = ensureLine(oldLines, prefix, attributionLine)
        } else {
            textFlow.comment = HSimpleComment(attributionLine)
        }
    }

    private fun ensureLine(lines: List<String>, prefix: String, attributionLine: String): String {
        val newLines: List<String> =
                if (lines.find { it.startsWith(prefix) } != null) {
                    lines.map { line ->
                        if (line.startsWith(prefix))
                            attributionLine
                        else line
                    }
                } else lines + attributionLine
        return newLines.joinToString("\n")
    }

    fun getAttributionMessage(backendId: String): String {
        return when (backendId) {
            "GOOGLE" -> "Translated by Google"
            "DEV" -> "Pseudo-translated by MT (DEV)"
            else -> {
                log.warn("Unexpected MT backendId: {}", backendId)
                "Translated by MT backendId: $backendId"
            }
        }
    }
}
