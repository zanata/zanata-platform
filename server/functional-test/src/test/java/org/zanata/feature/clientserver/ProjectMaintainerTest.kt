/*
 * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.feature.clientserver

import com.google.common.base.Joiner
import com.google.common.collect.ImmutableMap
import com.google.common.collect.Lists
import com.google.common.io.Files
import org.junit.jupiter.api.Test
import org.zanata.common.LocaleId
import org.zanata.feature.Trace
import org.zanata.feature.testharness.DetailedTest
import org.zanata.feature.testharness.ZanataTestCase
import org.zanata.page.projectversion.VersionLanguagesPage
import org.zanata.util.Constants
import org.zanata.util.PropertiesHolder
import org.zanata.util.ZanataRestCaller
import org.zanata.workflow.BasicWorkFlow
import org.zanata.workflow.ClientWorkFlow
import org.zanata.workflow.LoginWorkFlow

import java.io.File
import java.io.FilenameFilter
import java.io.IOException

import org.assertj.core.api.Assertions.assertThat
import org.zanata.util.MavenHome.mvn
import org.zanata.util.TestFileGenerator.generateZanataXml
import org.zanata.util.TestFileGenerator.makePropertiesFile
import org.zanata.util.ZanataRestCaller.buildTextFlowTarget
import org.zanata.util.ZanataRestCaller.buildTranslationResource
import org.zanata.workflow.BasicWorkFlow.PROJECT_VERSION_TEMPLATE

/**
 * @author Patrick Huang [pahuang@redhat.com](mailto:pahuang@redhat.com)
 */
@DetailedTest
class ProjectMaintainerTest : ZanataTestCase() {

    private val client = ClientWorkFlow()
    private val projectRootPath = client.getProjectRootPath("plural")
    private val translatorConfig = ClientWorkFlow
            .getUserConfigPath("translator")
    private val propFilter = FilenameFilter { _, name -> name.endsWith(".properties") }

    @Trace(summary = "A non-maintainer user may not push to a project")
    @Test
    fun nonProjectMaintainerCanNotPush() {
        // admin creates the project
        val adminRestCaller = ZanataRestCaller()
        adminRestCaller.createProjectAndVersion("plurals", "master", "podir")

        // translator tries to push
        val output = client.callWithTimeout(projectRootPath,
                "${mvn()} -e -B $MAVEN_PLUGIN:push -Dzanata.userConfig=$translatorConfig")

        val joinedOutput = Joiner.on("\n").skipNulls().join(output)
        assertThat(joinedOutput).contains("Authorization check failed")
    }

    @Trace(summary = "The system will run CopyTrans when a push occurs")
    @Test
    fun pushTransAndCopyTransTest() {
        // translator creates the project and become maintainer
        val restCaller = ZanataRestCaller("translator", PropertiesHolder
                .getProperty(Constants.zanataTranslatorKey.value()))
        restCaller.createProjectAndVersion("plurals", "master", "podir")
        val output = client.callWithTimeout(projectRootPath,
                "${mvn()} -e -B $MAVEN_PLUGIN:push -Dzanata.copyTrans=false " +
                        "-Dzanata.userConfig=$translatorConfig")

        assertThat(client.isPushSuccessful(output)).isTrue()

        LoginWorkFlow().signIn("admin", "admin")
        val versionPage = BasicWorkFlow().goToPage(String.format(
                PROJECT_VERSION_TEMPLATE, "plurals", "master"),
                VersionLanguagesPage::class.java)
        assertThat(versionPage.getStatisticsForLocale("pl"))
                .contains("0.0%")

        // push trans
        client.callWithTimeout(
                projectRootPath,
                "${mvn()} -e -B $MAVEN_PLUGIN:push -Dzanata.pushType=trans " +
                        "-Dzanata.copyTrans=false " +
                        "-Dzanata.userConfig=$translatorConfig")

        versionPage.reload()
        assertThat(versionPage.getStatisticsForLocale("pl")).contains("6.0%")

        // create new version
        restCaller.createProjectAndVersion("plurals", "beta", "podir")
        val updatedZanataXml = File(Files.createTempDir(), "zanata.xml")
        generateZanataXml(updatedZanataXml, "plurals", "beta", "podir",
                Lists.newArrayList("pl"))
        // push source and run copyTrans
        client.callWithTimeout(
                projectRootPath,
                "${mvn()} -e -B $MAVEN_PLUGIN:push -Dzanata.pushType=source " +
                        "-Dzanata.copyTrans=true " +
                        "-Dzanata.userConfig=$translatorConfig " +
                        "-Dzanata.projectConfig=${updatedZanataXml.absolutePath}")

        val betaVersionPage = BasicWorkFlow().goToPage(String.format(
                PROJECT_VERSION_TEMPLATE, "plurals", "beta"),
                VersionLanguagesPage::class.java)

        assertThat(betaVersionPage.getStatisticsForLocale("pl")).contains("6.0%")
    }

    @Trace(summary = "A maintainer user may pull translations from a project")
    @Test
    @Throws(IOException::class)
    fun projectMaintainerPullTest() {
        val restCaller = ZanataRestCaller("translator",PropertiesHolder
                .getProperty(Constants.zanataTranslatorKey.value()))
        val workDir = Files.createTempDir()
        val projectSlug = "pull-test"
        val iterationSlug = "master"
        val projectType = "properties"
        restCaller.createProjectAndVersion(projectSlug, iterationSlug,
                projectType)
        // generate a properties source
        makePropertiesFile(File(workDir, "prop1.properties"), ImmutableMap
                .builder<String, String>().put("hello", "hello world").build())
        makePropertiesFile(File(workDir, "prop2.properties"), ImmutableMap
                .builder<String, String>().put("greeting", "hey buddy")
                .build())

        // copy a pom file

        generateZanataXml(File(workDir, "zanata.xml"), projectSlug,
                iterationSlug, projectType, Lists.newArrayList("pl"))
        client.callWithTimeout(workDir,
                "${mvn()} -e -B $MAVEN_PLUGIN:push -Dzanata.userConfig=$translatorConfig")

        // only message1 has translation
        val translationsResource = buildTranslationResource(
                buildTextFlowTarget("hello", "translated"))
        restCaller.postTargetDocResource(projectSlug, iterationSlug, "prop1",
                LocaleId("pl"), translationsResource, "auto")

        // dryRun creates nothing
        val transDir = Files.createTempDir()
        client.callWithTimeout(workDir,
                "${mvn()} -e -B $MAVEN_PLUGIN:pull -DdryRun " +
                        "-Dzanata.userConfig=$translatorConfig " +
                        "-Dzanata.transDir=$transDir")
        assertThat(transDir.listFiles(propFilter)).isEmpty()

        // create skeletons is false will only pull translated files
        client.callWithTimeout(workDir,
                "${mvn()} -e -B $MAVEN_PLUGIN:pull " +
                        "-Dzanata.createSkeletons=false " +
                        "-Dzanata.userConfig=$translatorConfig" +
                        " -Dzanata.transDir=${transDir.absolutePath}")

        assertThat(transDir.listFiles(propFilter)).contains(File(
                transDir, "prop1_pl.properties"))

        // pull both
        client.callWithTimeout(workDir,
                "${mvn()} -e -B $MAVEN_PLUGIN:pull -Dzanata.pullType=both " +
                        "-Dzanata.userConfig=$translatorConfig " +
                        "-Dzanata.transDir=${transDir.absolutePath}")

        assertThat(transDir.listFiles(propFilter)).contains(File(transDir,
                "prop1_pl.properties"))
        // @formatter:off
        assertThat(workDir.listFiles(propFilter)).contains(
                File(workDir, "prop1.properties"),
                File(workDir, "prop2.properties"))
        // @formatter:on
    }

    companion object {

        /**
         * This is workaround for https://zanata.atlassian.net/browse/ZNTA-1011
         * TODO: remove this and replace with shared pom.xml file in all zanata-maven-plugin
         */
        const val MAVEN_PLUGIN = "org.zanata:zanata-maven-plugin:4.5.0"
    }

}
