/*
 * Copyright 2010, Red Hat, Inc. and individual contributors
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
package org.zanata.rest.service.raw

import org.assertj.core.api.KotlinAssertions.assertThat
import org.dbunit.operation.DatabaseOperation
import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.arquillian.container.test.api.RunAsClient
import org.jboss.arquillian.junit.Arquillian
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget
import org.jboss.shrinkwrap.api.ShrinkWrap
import org.jboss.shrinkwrap.api.spec.WebArchive
import org.junit.Test
import org.junit.runner.RunWith
import org.zanata.RestTest
import org.zanata.arquillian.ArquillianUtil.addClassesWithDependencies
import org.zanata.provider.DBUnitProvider
import org.zanata.rest.MediaTypes
import org.zanata.rest.ResourceRequest
import org.zanata.rest.dto.VersionInfo
import org.zanata.rest.service.VersionService
import org.zanata.util.RawRestTestUtils.assertJaxbUnmarshal
import org.zanata.util.RawRestTestUtils.assertJsonUnmarshal
import javax.ws.rs.client.Invocation
import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.Response
import org.zanata.arquillian.ArquillianUtil.addWebInfXml
import org.zanata.arquillian.ArquillianUtil.addPersistenceConfig
import org.zanata.model.HApplicationConfiguration
import org.zanata.rest.service.raw.ArquillianRest.beansXmlForRest
import org.zanata.rest.service.raw.ArquillianRest.classesWithDependenciesForRest
import org.zanata.rest.service.raw.ArquillianRest.libsForRest
import org.zanata.rest.service.raw.ArquillianRest.jbossDeploymentStructureForRest
import org.zanata.seam.security.CurrentUserImpl

// TODO we don't want to add LifecycleArquillian to the deployment
@RunWith(Arquillian::class)
class VersionRawRestITCase : RestTest() {

    companion object {
        @Deployment(name = "VersionService", testable = false)
        @JvmStatic
        fun createDeployment(): WebArchive {
            val classes = listOf(
                    VersionRawRestITCase::class.java,
                    VersionService::class.java,
                    HApplicationConfiguration::class.java,
                    CurrentUserImpl::class.java
            )
            val war = ShrinkWrap
                    .create(WebArchive::class.java, "VersionService.war")
                    .addAsLibraries(*libsForRest(listOf("org.jboss.shrinkwrap:shrinkwrap-api")))
                    .addWebInfXml(jbossDeploymentStructureForRest())
                    .addPersistenceConfig()
                    .addClassesWithDependencies(classes + classesWithDependenciesForRest())
                    .addWebInfXml(beansXmlForRest(ServerPathAlt::class.java, OAuthAlt::class.java, AnonAccessAlt::class.java))
                    .addAsResource("org/zanata/test/model/ApplicationConfigurationData.dbunit.xml")
//            war.content.forEach { path, _ -> println(path) }
            return war
        }
    }

    override fun getDataSetToClear(): String? {
        return "org/zanata/test/model/ApplicationConfigurationData.dbunit.xml"
    }

    override fun prepareDBUnitOperations() {
        addBeforeTestOperation(DBUnitProvider.DataSetOperation(
                "org/zanata/test/model/ApplicationConfigurationData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT))
    }

    @Test
    @RunAsClient
    fun getJson() {
        object : ResourceRequest(getRestEndpointUrl("/version"), "GET") {
            override fun prepareRequest(webTarget: ResteasyWebTarget): Invocation.Builder {
                return webTarget.request().header(HttpHeaders.ACCEPT,
                        MediaTypes.APPLICATION_ZANATA_VERSION_JSON)
            }

            override fun onResponse(response: Response) {
                assertThat(response.status).isEqualTo(200) // Ok
                val entityString = response.readEntity(String::class.java)
                assertJsonUnmarshal(entityString, VersionInfo::class.java)
            }
        }.run()
    }

    @Test
    @RunAsClient
    fun getXml() {
        object : ResourceRequest(getRestEndpointUrl("/version"), "GET") {
            override fun prepareRequest(webTarget: ResteasyWebTarget): Invocation.Builder {
                return webTarget.request().header(HttpHeaders.ACCEPT,
                        MediaTypes.APPLICATION_ZANATA_VERSION_XML)
            }

            override fun onResponse(response: Response) {
                assertThat(response.status).isEqualTo(200) // Ok
                val entityString = response.readEntity(String::class.java)
                assertJaxbUnmarshal(entityString, VersionInfo::class.java)
            }
        }.run()
    }

}
