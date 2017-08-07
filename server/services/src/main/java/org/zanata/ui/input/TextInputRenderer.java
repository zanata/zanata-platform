/*
 * Copyright 2015, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.ui.input;

import com.sun.faces.renderkit.html_basic.TextRenderer;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.context.ResponseWriterWrapper;
import javax.faces.render.FacesRenderer;
import java.io.IOException;

/**
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@FacesRenderer(componentFamily = TextInput.COMPONENT_FAMILY,
        rendererType = TextInputRenderer.RENDERER_TYPE)
public class TextInputRenderer extends TextRenderer {

    public static final String RENDERER_TYPE =
            "org.zanata.TextInputRenderer";

    /**
     * Extra attributes that should be written directly as attributes on the
     * generated HTML input tag.
     *
     * A list of attributes for a HTML input tag can be found at
     * https://developer.mozilla.org/en/docs/Web/HTML/Element/Input
     */
    private enum ExtraAttributes {
        placeholder,
        // Triggered by any change in value, see
        // https://developer.mozilla.org/en-US/docs/Web/Events/input
        oninput
    }

    @Override
    protected void getEndTextToRender(FacesContext context,
            UIComponent component, String currentValue) throws IOException {
        final ResponseWriter originalResponseWriter = context.getResponseWriter();
        context.setResponseWriter(new ResponseWriterWrapper() {
            @Override
            public ResponseWriter getWrapped() {
                return originalResponseWriter;
            }

            @Override
            public void startElement(String name, UIComponent component)
                    throws IOException {
                super.startElement(name, component);
                if("input".equals(name)) {
                    // Add the extra attributes
                    for(ExtraAttributes att : ExtraAttributes.values()) {
                        Object value = component.getAttributes().get(att.name());
                        if(value != null) {
                            super.writeAttribute(att.name(), value, null);
                        }
                    }
                }
            }
        });
        super.getEndTextToRender(context, component, currentValue);
        // Restore the original response writer
        context.setResponseWriter(originalResponseWriter);
    }
}
