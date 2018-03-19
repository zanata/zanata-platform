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
    val commonmarkJS = scriptName(BOWER, "commonmark", "dist/commonmark.min.js")
    val googleCajaHtmlSanitizerJS = scriptName(BOWER, "google-caja", "html-sanitizer-minified.js")
    val blueimpJavaScriptTemplatesJS = scriptName(BOWER, "blueimp-tmpl", "js/tmpl.min.js")
    val crossroadsJS = scriptName(CLASSIC, "crossroads.js", "crossroads.min.js")
    val signalsJS = scriptName(CLASSIC, "js-signals", "signals.min.js")
    // TODO wait for https://github.com/zanata/zanata-platform/pull/747
    // then change webjars.diff to webjars.diffJS in Application.xhtml and enable this:
//    val diffJS = scriptName(NPM, "diff", "dist/diff.min.js")

    /** Gets the script name for jQueryTyping's JS file */
    // Normally this would be a Kotlin property, but we have to name the
    // method getjQueryTyping (lower case 'j') if we want to use it in EL
    // as webjars.jQueryTyping (javabean rules)
    // Ref: http://futuretask.blogspot.com/2005/01/java-tip-6-dont-capitalize-first-two.html
    fun getjQueryTypingJS() = scriptName(BOWER, JQ_TYPING_LIB, jqTypingJS)

    /**
     * Returns the URL for the specified webjar resource (whose name has been
     * returned by one of the other WebJars functions). If the resource is not
     * found, an exception is thrown.
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

// jquery-typing uses the version number in the paths inside the package,
// so we have to handle it specially:
private const val JQ_TYPING_LIB = "github-com-ccakes-jquery-typing"
private val jqTypingVer: String = getJarVersion(BOWER, JQ_TYPING_LIB)
private val jqTypingJS = "plugin/jquery.typing-$jqTypingVer.min.js"

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
