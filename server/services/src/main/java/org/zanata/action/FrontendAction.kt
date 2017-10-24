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
package org.zanata.action

import org.apache.deltaspike.core.api.common.DeltaSpike
import org.apache.deltaspike.core.api.lifecycle.Initialized
import org.codehaus.jackson.annotate.JsonIgnoreProperties
import org.codehaus.jackson.annotate.JsonProperty
import org.codehaus.jackson.map.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.io.Serializable
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.event.Observes
import javax.inject.Inject
import javax.inject.Named
import javax.servlet.ServletContext

@ApplicationScoped
@Named("frontendAction")
class FrontendAction @Inject constructor(@DeltaSpike private val servletContext: ServletContext) : Serializable {
    private val manifest: FrontendManifest = readManifest()
    val frontendJs: String
        get() = "${servletContext.contextPath}/${manifest.frontendJs}"
    val frontendCss: String
        get() = "${servletContext.contextPath}/${manifest.frontendCss}"
    val legacyJs: String
        get() = "${servletContext.contextPath}/${manifest.legacyJs}"
    val legacyCss: String
        get() = "${servletContext.contextPath}/${manifest.legacyCss}"
    /**
     * runtime.js is the webpack runtime. It has to be loaded before any other javascript modules.
     */
    val runtime: String
        get() = "${servletContext.contextPath}/${manifest.runtime}"
    val editorJs: String
        get() = "${servletContext.contextPath}/${manifest.editorJs}"
    val editorCss: String
        get() = "${servletContext.contextPath}/${manifest.editorCss}"

    fun init(@Observes @Initialized context: ServletContext) {
        // just to make sure our manifest file can be read
        log.info("zanata frontend manifest: {}", manifest)
    }

    companion object {
        val MANIFEST_PATH: String = "META-INF/resources/manifest.json"
        private val log: Logger = LoggerFactory.getLogger(FrontendAction::class.java);
        private fun readManifest(): FrontendManifest {
            val manifestResource: InputStream? = Thread.currentThread().contextClassLoader.getResourceAsStream(MANIFEST_PATH)
            return manifestResource?.use {
                ObjectMapper().readValue(manifestResource, FrontendManifest::class.java)
            } ?: throw IllegalStateException("can not load manifest.json from $MANIFEST_PATH. Did you forget to build and include zanata frontend?")
        }
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class FrontendManifest(
        @JsonProperty("editor.css") val editorCss: String,
        @JsonProperty("editor.js") val editorJs: String,
        @JsonProperty("frontend.css") val frontendCss: String,
        @JsonProperty("frontend.js") val frontendJs: String,
        // TODO pahuang legacy.css is not used?
        @JsonProperty("frontend.legacy.css") val legacyCss: String,
        @JsonProperty("frontend.legacy.js") val legacyJs: String,
        // this js module is referenced in zanata-frontend/src/frontend/app/editor/index.js as npm module 'intl-polyfill'
        // Not sure if the module cache name will impact the use of it.
        // TODO pahuang test this in browser that don't support Intl (e.g. opera mini, IE 10)
        @JsonProperty("intl-polyfill.js") val intlPolyFillJs: String,
        @JsonProperty("runtime.js") val runtime: String
)
