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
package org.zanata.ui.ajax.commandbutton;

import org.richfaces.renderkit.RenderKitUtils;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.FacesRenderer;
import java.io.IOException;

import static org.richfaces.renderkit.RenderKitUtils.attributes;
import static org.richfaces.renderkit.RenderKitUtils.renderPassThroughAttributes;
import static org.richfaces.renderkit.RenderKitUtils.shouldRenderAttribute;

/**
 * Command button renderer for html faces.
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@FacesRenderer(componentFamily = CommandButton.COMPONENT_FAMILY,
        rendererType = CommandButtonRenderer.RENDERER_TYPE)
// TODO why does this class copy so much code from parent class?
// TODO can we give it a better name?
public class CommandButtonRenderer extends
        org.richfaces.renderkit.html.CommandButtonRenderer {

    public static final String RENDERER_TYPE =
            "org.zanata.CommandButtonRenderer";

    private static final RenderKitUtils.Attributes PASS_THROUGH_ATTRIBUTES2 =
            attributes()
                    .generic("accept", "accept")

                    .generic("accesskey", "accesskey")

                    .generic("align", "align")

                    .generic("alt", "alt")

                    .bool("checked", "checked")

                    .generic("class", "styleClass")

                    .generic("dir", "dir")

                    .bool("disabled", "disabled")

                    .generic("lang", "lang")

                    .generic("maxlength", "maxlength")

                    .generic("onblur", "onblur")

                    .generic("onchange", "onchange")

                    .generic("ondblclick", "ondblclick", "dblclick")

                    .generic("onfocus", "onfocus")

                    .generic("onkeydown", "onkeydown", "keydown")

                    .generic("onkeypress", "onkeypress", "keypress")

                    .generic("onkeyup", "onkeyup", "keyup")

                    .generic("onmousedown", "onmousedown", "mousedown")

                    .generic("onmousemove", "onmousemove", "mousemove")

                    .generic("onmouseout", "onmouseout", "mouseout")

                    .generic("onmouseover", "onmouseover", "mouseover")

                    .generic("onmouseup", "onmouseup", "mouseup")

                    .generic("onselect", "onselect")

                    .bool("readonly", "readonly")

                    .generic("role", "role")

                    .generic("size", "size")

                    .uri("src", "src")

                    .generic("style", "style")

                    .generic("tabindex", "tabindex")

                    .generic("title", "title")

                    .uri("usemap", "usemap")

    ;

    @Override
    public void doEncodeEnd(ResponseWriter responseWriter,
            FacesContext facesContext, UIComponent component)
            throws IOException {
        String clientId = component.getClientId(facesContext);
        responseWriter.startElement("button", component);
        {
            String value = clientId;
            if (null != value &&
                    value.length() > 0) {
                responseWriter.writeAttribute("id", value, null);
            }

        }

        {
            String value = clientId;
            if (null != value &&
                    value.length() > 0) {
                responseWriter.writeAttribute("name", value, null);
            }

        }

        {
            String value = this.getOnClick(facesContext, component);
            if (null != value &&
                    value.length() > 0) {
                responseWriter.writeAttribute("onclick", value, null);
            }

        }

        {
            Object value = component.getAttributes().get("value");
            if (null != value &&
                    shouldRenderAttribute(value)) {
                responseWriter.writeAttribute("value", value, null);
            }

        }

        renderPassThroughAttributes(facesContext, component,
                PASS_THROUGH_ATTRIBUTES2);

        renderChildren(facesContext, component);
        responseWriter.endElement("button");
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }
}
