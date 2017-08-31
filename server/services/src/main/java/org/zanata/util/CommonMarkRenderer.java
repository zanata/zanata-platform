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

import com.google.common.base.Charsets;
import jdk.nashorn.api.scripting.JSObject;
import org.apache.commons.io.IOUtils;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;

/**
 * @author Sean Flanigan
 *         <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@ApplicationScoped
public class CommonMarkRenderer implements Serializable {
    private static final long serialVersionUID = 1L;

    // Share ScriptEngine and CompiledScript across threads, but not Bindings
    // See http://stackoverflow.com/a/30159424/14379
    private ScriptEngine engine;
    private CompiledScript functions;
    private ThreadLocal<Bindings> threadBindings;

    private final WebJars webjars;

    @Inject
    public CommonMarkRenderer(WebJars webjars) {
        this.webjars = webjars;
    }

    public CommonMarkRenderer() {
        this(null);
    }

    @PostConstruct
    public void postConstruct() {
        engine =
            new ScriptEngineManager().getEngineByName("Nashorn");
        functions = compileFunctions();
        threadBindings =
                ThreadLocal.withInitial(() -> {
                    Bindings bindings = engine.createBindings();
                    // libraries like commonmark.js assume the presence of 'window'
                    //noinspection CollectionAddedToSelf
                    bindings.put("window", bindings);
                    try {
                        functions.eval(bindings);
                        return bindings;
                    } catch (ScriptException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private String getOutputScriptName() {
        return webjars.getCommonmarkJS();
    }

    /**
     * Render CommonMark text to HTML and sanitise it.
     */
    public String renderToHtmlSafe(String commonMark) {
        String unsafeHtml = renderToHtmlUnsafe(commonMark);
        return HtmlUtil.SANITIZER.sanitize(unsafeHtml);
    }

    public String renderToHtmlUnsafe(String commonMark) {
        try {
            Bindings bindings = threadBindings.get();
            JSObject mdRender = (JSObject) bindings.get("mdRender");
            return (String) mdRender.call(bindings, commonMark);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private CompiledScript compileFunctions() {
        try {
            // Create a javascript function 'mdRender' which takes CommonMark
            // as a string and returns a rendered HTML string:
            String commonMarkScript =
                    IOUtils.toString(getScriptResource(), Charsets.UTF_8);
            String functionsScript = commonMarkScript
                    + "var reader = new commonmark.Parser();var writer = new commonmark.HtmlRenderer();function mdRender(src) {  return writer.render(reader.parse(src));};";
            return ((Compilable) engine).compile(functionsScript);
        } catch (ScriptException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private URL getScriptResource() {
        String resourceName = "/META-INF/resources/webjars/" + getOutputScriptName();
        URL url = getClass().getResource(resourceName);
        if (url == null) {
            throw new IllegalArgumentException(
                    "resource " + resourceName + " relative to " +
                            CommonMarkRenderer.class.getName() + " not found.");
        }
        return url;
    }
}
