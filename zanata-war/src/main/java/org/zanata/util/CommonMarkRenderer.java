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

import com.google.common.base.Throwables;
import com.google.common.io.Resources;
import jdk.nashorn.api.scripting.JSObject;
import lombok.extern.slf4j.Slf4j;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@AutoCreate
@Name("commonMarkRenderer")
@Scope(ScopeType.APPLICATION)
@Slf4j
public class CommonMarkRenderer {

    private static final String VER = Dependencies.getVersion(
            "org.webjars.bower:commonmark:jar");
    private static final String SCRIPT_NAME = "commonmark/" +
            VER + "/dist/commonmark.min.js";
    private static final String VER_SANITIZER = Dependencies.getVersion(
            "org.webjars.bower:google-caja:jar");
    private static final String SCRIPT_NAME_SANITIZER = "google-caja/" +
            VER_SANITIZER + "/html-sanitizer-minified.js";
    private static final String RESOURCE_NAME = "META-INF/resources/webjars/" +
            SCRIPT_NAME;
    private static final ScriptEngine engine =
            new ScriptEngineManager().getEngineByName("Nashorn");
    private static final CompiledScript compiledScript = compileScript();
    private static final ThreadLocal<Bindings> threadBindings =
            ThreadLocal.withInitial(engine::createBindings);

    static {
        log.info("Using commonmark.js version {}", VER);
        log.info("Using Google Caja version {}", VER_SANITIZER);
    }

    /**
     * Return the name of the implementation script for JSF h:outputScript.
     *
     * You can use it like this:
     * <pre>
     * {@code <h:outputScript target="body" library="webjars"
     *     name="${commonMarkRenderer.outputScriptName}"/>}
     * </pre>
     * @return
     */
    public String getOutputScriptName() {
        return SCRIPT_NAME;
    }

    /**
     * Return the name of the sanitizer script for JSF h:outputScript.
     *
     * You can use it like this:
     * <pre>
     * {@code <h:outputScript target="body" library="webjars"
     *     name="${commonMarkRenderer.outputScriptNameSanitizer}"/>}
     * </pre>
     * @return
     */
    public String getOutputScriptNameSanitizer() {
        return SCRIPT_NAME_SANITIZER;
    }

    /**
     * Render CommonMark text to HTML and sanitise it.
     * @param commonMark
     * @return
     */
    public String renderToHtmlSafe(String commonMark) {
        String unsafeHtml = renderToHtmlUnsafe(commonMark);
        return HtmlUtil.SANITIZER.sanitize(unsafeHtml);
    }

    public String renderToHtmlUnsafe(String commonMark) {
        try {
            Bindings bindings = threadBindings.get();
            JSObject boundScript = (JSObject) compiledScript.eval(bindings);
            return (String) boundScript.call(compiledScript, commonMark);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private static CompiledScript compileScript() {
        try {
            // Create a javascript function 'mdRender' which takes CommonMark
            // as a string and returns a rendered HTML string:
            String commonMarkScript = Resources.toString(getScriptResource(),
                    StandardCharsets.UTF_8);
            String initScript = "" +
                    "if (!mdRender) {" +
                    "  window = this;" +
                    commonMarkScript +
                    "  var reader = new commonmark.Parser();" +
                    "  var writer = new commonmark.HtmlRenderer();" +
                    "  function mdRender(src) {" +
                    "    return writer.render(reader.parse(src));" +
                    "  };" +
                    "}" +
                    "mdRender";
            return ((Compilable) engine).compile(initScript);
        } catch (ScriptException | IOException e) {
            throw Throwables.propagate(e);
        }
    }

    private static URL getScriptResource() {
        return Resources.getResource(CommonMarkRenderer.class, "/" + RESOURCE_NAME);
    }

}
