/*
 * Copyright 2015, Red Hat, Inc. and individual contributors
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
package org.zanata.util;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

/**
 * If you need to update dependencies.properties without running the whole build, try this:<br/>
 * {@code mvn org.codehaus.gmavenplus:gmavenplus-plugin:execute@dependency-versions -pl :services}
 * @author Sean Flanigan
 *         <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@Named("webjars")
@ApplicationScoped
public class WebJars implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Return the name of the implementation script for JSF h:outputScript.
     *
     * You can use it like this:
     *
     * <pre>
     * {@code <h:outputScript target="body" library="webjars"
     * name="${webjars.bower('commonmark', 'dist/commonmark.min.js'}"/>}
     * </pre>
     */
    private String bower(String libName, String resourceName) {
        return resolveLibResource("org.webjars.bower", libName, resourceName);
    }

    /**
     * Return the name of the implementation script for JSF h:outputScript.
     *
     * You can use it like this:
     *
     * <pre>
     * {@code <h:outputScript target="body" library="webjars"
     * name="${webjars.classic('commonmark', 'dist/commonmark.min.js'}"/>}
     * </pre>
     */
    private String classic(String libName, String resourceName) {
        return resolveLibResource("org.webjars", libName, resourceName);
    }

    /*
     * Return the name of the implementation script for JSF h:outputScript.
     *
     * You can use it like this:
     *
     * <pre>
     * {@code <h:outputScript target="body" library="webjars"
     * name="${webjars.npm('commonmark', 'dist/commonmark.min.js'}"/>}
     * </pre>
     */
//    private String npm(String libName, String resourceName) {
//        return resolveLibResource("org.webjars.npm", libName, resourceName);
//    }

    @Nonnull
    private String resolveLibResource(String groupId, String libName,
            String resourceName) {
        String depName = groupId + ":" + libName + ":jar";
        String ver = Dependencies.getVersion(depName);
        String basePath = libName + "/" + ver + "/";
        return basePath + resourceName;
    }

    public String getCommonmarkJS() {
        return bower("commonmark", "dist/commonmark.min.js");
    }

    public String getGoogleCajaHtmlSanitizerJS() {
        return bower("google-caja", "html-sanitizer-minified.js");
    }

    // TODO find a minified version of codemirror 3.2.1 or upgrade to 5.x
    public String getCodemirrorJS() {
        return bower("codemirror", "lib/codemirror.js");
    }

    public String getCodemirrorCSS() {
        return bower("codemirror", "lib/codemirror.css");
    }

    // TODO upgrade to google-diff-match-patch from webjars (see also Application.xhtml)
//    public String getGoogleDiffMatchPatch() {
//        // diff_match_patch.js is minified, diff_match_patch_uncompressed.js is not
//        return classic("google-diff-match-patch", "diff_match_patch.js");
//    }

    public String getBlueimpJavaScriptTemplates() {
        return bower("blueimp-tmpl", "js/tmpl.min.js");
    }

    public String jQueryFileUpload(String resourceName) {
        return bower("jquery-file-upload", resourceName);
    }

    public String getCrossroadsJS() {
        return classic("crossroads.js", "crossroads.min.js");
    }

    public String getJQueryTyping() {
        return bower("github-com-ccakes-jquery-typing", "plugin/jquery.typing-0.3.3.min.js");
    }

    public String getSignalsJS() {
        return classic("js-signals", "signals.min.js");
    }
}
