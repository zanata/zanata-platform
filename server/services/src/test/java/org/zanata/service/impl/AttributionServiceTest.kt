package org.zanata.service.impl

import org.assertj.core.api.Assertions.assertThat
import org.hibernate.Session
import org.jglue.cdiunit.InRequestScope
import org.junit.Test
import org.junit.runner.RunWith
import org.zanata.ZanataJpaTest
import org.zanata.common.LocaleId
import org.zanata.model.HDocument
import org.zanata.model.HLocale
import org.zanata.model.HSimpleComment
import org.zanata.model.HTextFlow
import org.zanata.model.po.HPoTargetHeader
import org.zanata.rest.dto.extensions.gettext.HeaderEntry
import org.zanata.rest.dto.extensions.gettext.PoTargetHeader
import org.zanata.rest.dto.resource.TranslationsResource
import org.zanata.rest.service.ResourceUtils
import org.zanata.test.CdiUnitRunner
import javax.enterprise.inject.Produces
import javax.inject.Inject
import javax.persistence.EntityManager

/**
 * @author Sean Flanigan [sflaniga@redhat.com](mailto:sflaniga@redhat.com)
 */
@RunWith(CdiUnitRunner::class)
class AttributionServiceTest: ZanataJpaTest() {

    private companion object {
        const val ATTRIB_GOOGLE = "$ATTRIBUTION_KEY: Translated by Google"
        const val TRANS_BY_DEV = "Pseudo-translated by MT (DEV)"
        const val ATTRIB_DEV = "$ATTRIBUTION_KEY: $TRANS_BY_DEV"
    }

    @Produces
    fun produceSession(): Session = session

    @Produces
    fun produceEM(): EntityManager = em

    // an existing entry from the database
    private val headerEntries = "PO-Revision-Date=2017-04-05 10\\:04+1000\nLast-Translator=Automatically generated\nLanguage-Team=None\n"

    @Inject
    private lateinit var resUtils: ResourceUtils

    private val attributions = AttributionService()
    private val hPoTargetHeader = HPoTargetHeader()
    private val localeDE = HLocale(LocaleId.DE)
    private val hDocument = HDocument().apply {
        poTargetHeaders[localeDE] = hPoTargetHeader
    }
    private val translationsResource = TranslationsResource()

    @Test
    @InRequestScope
    fun `add new attribution for gettext`() {
        hPoTargetHeader.entries = headerEntries
        attributions.ensureAttributionForGettext(hPoTargetHeader, attributions.getAttributionMessage("DEV"))

        resUtils.transferToTranslationsResource(translationsResource, hDocument, localeDE, setOf(PoTargetHeader.ID), listOf(), false)

        val header = translationsResource.extensions.findByType(PoTargetHeader::class.java)
        assertThat(header.entries)
                .contains(HeaderEntry(ATTRIBUTION_KEY, TRANS_BY_DEV))
                .extracting("key")
                .containsOnlyOnce(ATTRIBUTION_KEY)
    }

    @Test
    @InRequestScope
    fun `replace existing attribution for gettext`() {
        hPoTargetHeader.entries = headerEntries + ATTRIB_GOOGLE + "\n"
        attributions.ensureAttributionForGettext(hPoTargetHeader, attributions.getAttributionMessage("DEV"))

        resUtils.transferToTranslationsResource(translationsResource, hDocument, localeDE, setOf(PoTargetHeader.ID), listOf(), false)

        val header = translationsResource.extensions.findByType(PoTargetHeader::class.java)
        assertThat(header.entries)
                .contains(HeaderEntry(ATTRIBUTION_KEY, TRANS_BY_DEV))
                .extracting("key")
                .containsOnlyOnce(ATTRIBUTION_KEY)
    }


    @Test
    @InRequestScope
    fun `add new comment with attribution for properties`() {
        val hTextFlow = HTextFlow()
        attributions.ensureAttributionForProperties(hTextFlow, attributions.getAttributionMessage("DEV"))
        assertThat(hTextFlow.comment.comment)
                .contains(ATTRIB_DEV)
    }

    @Test
    @InRequestScope
    fun `add new attribution for properties`() {
        val hTextFlow = HTextFlow().apply {
            comment = HSimpleComment("an ordinary comment")
        }
        attributions.ensureAttributionForProperties(hTextFlow, attributions.getAttributionMessage("DEV"))
        assertThat(hTextFlow.comment.comment)
                .contains(ATTRIB_DEV)
                .contains("an ordinary comment")
    }

    @Test
    @InRequestScope
    fun `replace existing attribution for properties`() {
        val hTextFlow = HTextFlow().apply {
            comment = HSimpleComment("first comment\n$ATTRIB_GOOGLE\nanother ordinary comment")
        }
        attributions.ensureAttributionForProperties(hTextFlow, attributions.getAttributionMessage("DEV"))
        assertThat(hTextFlow.comment.comment)
                .isEqualTo("first comment\n$ATTRIB_DEV\nanother ordinary comment")
    }
}
