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
import lombok.extern.slf4j.Slf4j;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

    private final ScriptEngine engine = newEngine();

    private ThreadLocal<Bindings> threadBindings =
            ThreadLocal.withInitial(this::initBindings);

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
            bindings.put("src", commonMark);
            return (String) engine.eval("mdRender(src)", bindings);
        } catch (ScriptException e) {
            throw Throwables.propagate(e);
        }
    }

    private Bindings initBindings() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                getScriptAsStream(), StandardCharsets.UTF_8))) {
            Bindings bindings = engine.createBindings();
            engine.eval("window = this;", bindings);
            engine.eval(reader, bindings);
            // Create a javascript function 'mdRender' which takes CommonMark
            // as a string and returns a rendered HTML string:
            String initScript =
                    "var reader = new commonmark.Parser();" +
                            "var writer = new commonmark.HtmlRenderer();" +
                            "function mdRender(src) {" +
                            "  return writer.render(reader.parse(src));" +
                            "};";
            engine.eval(initScript, bindings);
            return bindings;
        } catch (IOException | ScriptException e) {
            throw Throwables.propagate(e);
        }
    }

    private static InputStream getScriptAsStream() {
        InputStream stream =
                CommonMarkRenderer.class.getClassLoader().getResourceAsStream(
                        RESOURCE_NAME);
        if (stream == null) {
            throw new RuntimeException("Script "+ RESOURCE_NAME + " not found");
        }
        return stream;
    }

    private static ScriptEngine newEngine() {
        ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        return scriptEngineManager.getEngineByName("JavaScript");
    }

}
