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

import javax.faces.component.FacesComponent;
import javax.faces.component.html.HtmlInputText;

/**
 * This is a text input with the same properties as h:inputText but allows for
 * extra properties like placeholder. This is not needed when/if using JSF 2.2
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@FacesComponent(TextInput.COMPONENT_TYPE)
public class TextInput extends HtmlInputText {

    public static final String COMPONENT_FAMILY = "org.zanata";
    public static final String COMPONENT_TYPE = "org.zanata.TextInput";

    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }

    @Override
    public String getRendererType() {
        return TextInputRenderer.RENDERER_TYPE;
    }
}
