/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zanata.ui;

import java.io.IOException;
import java.util.Iterator;

import javax.faces.application.FacesMessage;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

/**
 *
 * @see UIInputContainer
 */
@FacesComponent(ZanataUIInputContainer.COMPONENT_TYPE)
public class ZanataUIInputContainer extends UIInputContainer {
    /**
     * The standard component type for this component.
     */
    public static final String COMPONENT_TYPE = "org.zanata.faces.InputContainer";

    @Override
    public void encodeBegin(final FacesContext context) throws IOException {
        getAttributes().put(getInvalidAttributeName(), false);
        super.encodeBegin(context);

        InputContainerElements elements = super.scan(getFacet(UIComponent.COMPOSITE_FACET_NAME), null, context);

        for (EditableValueHolder input : elements.getInputs()) {
            // if we use FacesMessages.addToControl(id, message) directly in
            // code, e.g. testing whether some value is unique in database, we
            // need to set invalid to true in order to display the message. We
            // also need to add the message to input client id because that's
            // what's being set as for for h:message (see
            // org.zanata.ui.UIInputContainer.InputContainerElements.wire).
            Iterator<FacesMessage> messagesForInput = context.getMessages(
                    ((UIComponent) input).getClientId(context));
            if (messagesForInput.hasNext()) {
                getAttributes().put(getInvalidAttributeName(), true);
            }
        }
    }
}
