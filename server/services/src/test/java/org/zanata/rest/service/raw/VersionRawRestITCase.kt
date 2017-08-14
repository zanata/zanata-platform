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
import org.atteo.classindex.ClassIndex
import org.dbunit.operation.DatabaseOperation
import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.arquillian.container.test.api.OperateOnDeployment
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget
import org.jboss.shrinkwrap.api.Archive
import org.jboss.shrinkwrap.api.ShrinkWrap
import org.jboss.shrinkwrap.api.spec.WebArchive
import org.jboss.shrinkwrap.resolver.api.maven.Maven
import org.junit.Test
import org.junit.runner.RunWith
import org.zanata.RestTest
import org.zanata.arquillian.ArquillianUtil.addClassesWithDependencies
import org.zanata.arquillian.LifecycleArquillian
import org.zanata.provider.DBUnitProvider
import org.zanata.rest.JaxRSApplication
import org.zanata.rest.MediaTypes
import org.zanata.rest.ResourceRequest
import org.zanata.rest.dto.VersionInfo
import org.zanata.rest.service.VersionService
import org.zanata.util.RawRestTestUtils.assertJaxbUnmarshal
import org.zanata.util.RawRestTestUtils.assertJsonUnmarshal
import javax.ws.rs.client.Invocation
import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.Response

@RunWith(LifecycleArquillian::class)
class VersionRawRestITCase : RestTest() {

    companion object {
        @Deployment(name = "VersionService", testable = false)
        @JvmStatic
        fun createDeployment(): Archive<*> {
            val libs = Maven.resolver()
                    .loadPomFromFile("pom.xml")
                    .resolve("com.google.guava:guava")
                    .withoutTransitivity()
                    .asFile()
            val war = ShrinkWrap
                    .create(WebArchive::class.java, "VersionService.war")
                    .addClassesWithDependencies(VersionService::class.java)
                    .addClasses(JaxRSApplication::class.java, ClassIndex::class.java)
                    .addAsLibraries(*libs)
//                    .addAsResource(
//                            EmptyAsset.INSTANCE,
//                            "beans.xml")
//                    .addAsWebInfResource(
//                            EmptyAsset.INSTANCE,
//                            "beans.xml")
//            war.content.forEach { path, _ -> println(path) }
            return war
        }
    }

    override fun prepareDBUnitOperations() {
        addBeforeTestOperation(DBUnitProvider.DataSetOperation(
                "org/zanata/test/model/ApplicationConfigurationData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT))

        addAfterTestOperation(DBUnitProvider.DataSetOperation(
                "org/zanata/test/model/ClearAllTables.dbunit.xml",
                DatabaseOperation.DELETE_ALL))
    }

    @Test
    @OperateOnDeployment("VersionService")
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
    @OperateOnDeployment("VersionService")
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
