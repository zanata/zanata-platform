package org.zanata.feature.manual

import com.google.common.collect.ImmutableList
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.zanata.common.LocaleId
import org.zanata.common.ProjectType
import org.zanata.feature.testharness.ZanataTestCase
import org.zanata.rest.dto.resource.Resource
import org.zanata.rest.dto.resource.TextFlow
import org.zanata.rest.dto.resource.TextFlowTarget
import org.zanata.rest.dto.resource.TranslationsResource
import org.zanata.util.SampleDataResourceClient
import org.zanata.util.ZanataRestCaller

import org.zanata.util.RandomStringUtils.randomAlphabetic
import org.zanata.util.ZanataRestCaller.buildSourceResource
import org.zanata.util.ZanataRestCaller.buildTranslationResource
import org.zanata.util.ZanataRestCaller.buildTextFlow
import org.zanata.util.ZanataRestCaller.buildTextFlowTarget

/**
 * This is a manual test that will help tuning/troubleshooting copyTrans. This
 * will just set up a project ovirt-reports-history and push up a few version's
 * translation.
 *
 * @author Patrick Huang [pahuang@redhat.com](mailto:pahuang@redhat.com)
 */

@Tag("Manual")
class CopyTransTuningTest : ZanataTestCase() {

    private val translatedLocales = ImmutableList.builder<LocaleId>().add(LocaleId("ja"))
            .add(LocaleId("de")).add(LocaleId("es"))
            .add(LocaleId("zh")).build()
    private lateinit var resources: Array<Resource>
    private lateinit var translations: Array<Pair>

    @BeforeEach
    fun setUp() {
        // use defaults
        val pluralForms: String? = null
        for (locale in translatedLocales) {
            SampleDataResourceClient.addLanguage(locale.id, pluralForms)
        }
        zanataRestCaller = ZanataRestCaller()
        val projectType = ProjectType.Utf8Properties.name.toLowerCase()
        zanataRestCaller.createProjectAndVersion(PROJECT_SLUG, "master", projectType)
        zanataRestCaller.createProjectAndVersion(PROJECT_SLUG, "3.3", projectType)
        zanataRestCaller.createProjectAndVersion(PROJECT_SLUG, "3.4", projectType)
        zanataRestCaller.createProjectAndVersion(PROJECT_SLUG, "3.5", projectType)
        val numOfTextFlows = 2000
        val message1 = buildSourceResource("message1",
                *generateTextFlows(numOfTextFlows))

        resources = arrayOf(message1)/* , message2, message3, message4 */
        val translation1 = buildTranslationResource(
                *generateTextFlowTargets(numOfTextFlows))
        translations = arrayOf(Pair.of(message1, translation1))
        // , Pair.of(message2, translation2),
        // Pair.of(message3, translation3),
        // Pair.of(message4, translation4) };
    }

    private fun pushSource(iterationSlug: String) {
        for (resource in resources) {
            zanataRestCaller.postSourceDocResource(PROJECT_SLUG, iterationSlug,
                    resource, false)
        }
    }

    private fun pushTargets(iterationSlug: String) {
        for (localeId in translatedLocales) {
            for (pair in translations) {
                log.info("pushing translation - version:{}, id:{}, locale:{}",
                        iterationSlug, pair.source.name, localeId)
                zanataRestCaller.postTargetDocResource(PROJECT_SLUG, iterationSlug,
                        pair.source.name, localeId, pair.target, "import")
            }
        }
    }

    @Test
    fun pushPreviousVersions() {
        pushSource("master")

        // pushSource("3.3");
        // pushSource("3.4");
        pushSource("3.5")
        pushTargets("master")
        // pushTargets("3.3");
        // pushTargets("3.4");
        /*
         *
         * List<String> locales = Lists.transform(translatedLocales,
         * Functions.toStringFunction()); String[] localesArray =
         * locales.toArray(new String[locales.size()]);
         * ContainerTranslationStatistics statistics =
         * zanataRestCaller.getStatistics(PROJECT_SLUG, "master", localesArray);
         *
         * List<TranslationStatistics> statsList = statistics.getStats(); for
         * (TranslationStatistics translationStatistics : statsList) {
         * log.info("statistics for locale: {}",
         * translationStatistics.getLocale());
         * Assertions.assertThat(translationStatistics.getPercentTranslated())
         * .isEqualTo(100); }
         */
    }

    class Pair private constructor(val source: Resource, val target: TranslationsResource) {
        companion object {

            fun of(source: Resource,
                   target: TranslationsResource): Pair {
                return Pair(source, target)
            }
        }
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(CopyTransTuningTest::class.java)

        private val PROJECT_SLUG = "ovirt-reports-history"

        private fun generateTextFlows(numOfTextFlows: Int): Array<TextFlow?> {
            val textFlows = arrayOfNulls<TextFlow>(numOfTextFlows)
            for (i in textFlows.indices) {
                textFlows[i] = buildTextFlow("res$i",
                        randomAlphabetic(10))
            }
            return textFlows
        }

        private fun generateTextFlowTargets(numOfTargets: Int): Array<TextFlowTarget?> {
            val targets = arrayOfNulls<TextFlowTarget>(numOfTargets)
            for (i in targets.indices) {
                targets[i] = buildTextFlowTarget("res$i", "translation no. $i")
            }
            return targets
        }
    }
}
