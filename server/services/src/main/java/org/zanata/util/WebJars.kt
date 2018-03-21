/*
 * Copyright 2018, Red Hat, Inc. and individual contributors
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
package org.zanata.util

import java.net.URL
import javax.enterprise.context.ApplicationScoped
import javax.inject.Named

/**
 * If you need to update dependencies.properties without running the whole build, try this:<br></br>
 * `mvn org.codehaus.gmavenplus:gmavenplus-plugin:execute@dependency-versions -pl :services`
 * @author Sean Flanigan
 * [sflaniga@redhat.com](mailto:sflaniga@redhat.com)
 */
@Named("webjars")
@ApplicationScoped
class WebJars {

    // these strings are all used in xhtml pages with h:outputScript, either in zanata-war or gwt-editor
    val commonmarkJS by lazy {
        scriptName(BOWER, "commonmark", "dist/commonmark.min.js")
    }
    val googleCajaHtmlSanitizerJS by lazy {
        scriptName(BOWER, "google-caja", "html-sanitizer-minified.js")
    }
    val blueimpJavaScriptTemplatesJS by lazy {
        scriptName(BOWER, "blueimp-tmpl", "js/tmpl.min.js")
    }
    val crossroadsJS by lazy {
        scriptName(CLASSIC, "crossroads.js", "crossroads.min.js")
    }
    val signalsJS by lazy {
        scriptName(CLASSIC, "js-signals", "signals.min.js")
    }
    val diffJS by lazy {
        scriptName(NPM, "diff", "dist/diff.min.js")
    }

    // Note that the JavaBean property name (for JSF) is JQueryTypingJS, not jQueryTypingJS
    // Ref: http://futuretask.blogspot.com/2005/01/java-tip-6-dont-capitalize-first-two.html
    val jQueryTypingJS by lazy {
        // jquery-typing uses the version number in the paths inside the
        // package, so we have to handle it specially.
        val jqTypingLib = "github-com-ccakes-jquery-typing"
        val jqTypingVer: String = getJarVersion(BOWER, jqTypingLib)
        scriptName(BOWER, jqTypingLib, "plugin/jquery.typing-$jqTypingVer.min.js")
    }

    /**
     * Returns the URL for the specified webjar resource (whose name has been
     * returned by one of the other WebJars functions/properties). If the
     * resource is not found, an exception is thrown.
     */
    fun getResource(nameInsideJar: String): URL {
        val name = "/META-INF/resources/webjars/$nameInsideJar"
        return javaClass.getResource(name) ?: throw RuntimeException("resource not found: $name")
    }
}

// These are the Maven groupIds for the three types of webjars
private const val BOWER = "org.webjars.bower"
private const val CLASSIC = "org.webjars"
private const val NPM = "org.webjars.npm"

/*
 * Gets the Maven version of a webjar artifact.
 * Throws IllegalStateException if the info is not available.
 */
private fun getJarVersion(groupId: String, libName: String): String {
    try {
        return Dependencies.getVersion("$groupId:$libName:jar")
    } catch (e: IllegalStateException) {
        throw IllegalStateException("no entry for $groupId:$libName:jar. " +
                "Check that dependencies.properties is up to date", e)
    }
}

/*
 * Returns the name of the implementation script for JSF h:outputScript.
 *
 * You can use it like this:
 *
 * ```
 * <h:outputScript target="body" library="webjars"
 *   name="${webjars.scriptName(BOWER, 'commonmark', 'dist/commonmark.min.js'}"/>
 * ```
 */
private fun scriptName(
        groupId: String, libName: String, resourceName: String): String {
    val ver: String = getJarVersion(groupId = groupId, libName = libName)
    return "$libName/$ver/$resourceName"
}
