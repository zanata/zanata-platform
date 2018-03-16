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
import org.apache.commons.io.IOUtils;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import java.io.IOException;
import java.net.URL;

/**
 * @author Sean Flanigan
 *         <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@ApplicationScoped
public class CommonMarkRenderer {

    // Share ScriptEngine and CompiledScript across threads, but not Bindings
    // See http://stackoverflow.com/a/30159424/14379
    private ScriptEngine engine;
    private CompiledScript compiledFunctions;
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
            new ScriptEngineManager().getEngineByName("js");
        compiledFunctions = compileFunctions((Compilable) engine,
                engine.getBindings(ScriptContext.GLOBAL_SCOPE));
//        threadBindings = ThreadLocal.withInitial(() -> engine.createBindings());
        threadBindings = ThreadLocal.withInitial(SimpleBindings::new);
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

    private CompiledScript compileFunctions(Compilable engine,
            Bindings globalBindings) {
        try {
            // Create a javascript function 'mdRender' which takes CommonMark
            // as a string and returns a rendered HTML string:
            String commonMarkScript =
                    IOUtils.toString(getScriptResource(), Charsets.UTF_8);
            String functionsScript = commonMarkScript +
                    "var reader = new commonmark.Parser();" +
                    "var writer = new commonmark.HtmlRenderer();" +
                    "var parsed = reader.parse(commonMarkText);" +
                    "writer.render(parsed);";
            // libraries like commonmark.js assume the presence of 'window'
            //noinspection CollectionAddedToSelf
            globalBindings.put("window", globalBindings);
            return engine.compile(functionsScript);
        } catch (ScriptException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String renderToHtmlUnsafe(String commonMark) {
        // uncomment this if you want to try sharing the scope between threads (just for testing!)
//        Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
        Bindings bindings = threadBindings.get();
        bindings.put("commonMarkText", commonMark);
        try {
            return (String) compiledFunctions.eval(bindings);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            bindings.remove("commonMarkText");
        }
    }

    private URL getScriptResource() {
        String resourceName = "/META-INF/resources/webjars/" + getOutputScriptName();
        URL url = CommonMarkRenderer.class.getResource(resourceName);
        if (url == null) {
            throw new IllegalArgumentException(
                    "resource " + resourceName + " relative to " +
                            CommonMarkRenderer.class.getName() + " not found.");
        }
        return url;
    }
}
