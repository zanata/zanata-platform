/*
 * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.ui.faces;

import static javax.faces.application.FacesMessage.SEVERITY_INFO;
import static javax.faces.application.FacesMessage.Severity;
import static org.jboss.seam.annotations.Install.APPLICATION;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.core.Interpolator;
import org.zanata.i18n.Messages;
import org.zanata.util.ServiceLocator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import java.util.Iterator;
import java.util.List;

/**
 * Utility to allow for easy handling of JSF messages. Serves as a replacement
 * for the old Seam 2 {@link org.jboss.seam.faces.FacesMessages} class.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Scope(ScopeType.CONVERSATION)
@Name("jsfMessages")
@Install(precedence = APPLICATION,
        classDependencies = "javax.faces.context.FacesContext")
@AutoCreate
@BypassInterceptors
public class FacesMessages {

    /**
     * Calculate the JSF client ID from the provided widget ID
     */
    private String getClientId(String id) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        return getClientId(facesContext.getViewRoot(), id, facesContext);
    }

    private static String getClientId(UIComponent component, String id,
            FacesContext facesContext) {
        String componentId = component.getId();
        if (componentId != null && componentId.equals(id)) {
            return component.getClientId(facesContext);
        } else {
            Iterator iter = component.getFacetsAndChildren();
            while (iter.hasNext()) {
                UIComponent child = (UIComponent) iter.next();
                String clientId = getClientId(child, id, facesContext);
                if (clientId != null)
                    return clientId;
            }
            return null;
        }
    }

    /**
     * Add a status message, looking up the message in the resource bundle using
     * the provided key. If the message is found, it is used, otherwise, the
     * defaultMessageTemplate will be used.
     * <p/>
     * The message will be added to the widget specified by the ID. The
     * algorithm used determine which widget the id refers to is determined by
     * the view layer implementation in use.
     * <p/>
     * You can also specify the severity, and parameters to be interpolated
     */
    void addToControl(String id, Severity severity, String key,
            String messageTemplate, final Object... params) {
        String clientId = getClientId(id);

        // NB This needs to change when migrating out of Seam
        String interpolatedMessage =
                Interpolator.instance().interpolate(messageTemplate, params);

        FacesContext.getCurrentInstance().addMessage(clientId,
                new FacesMessage(severity, interpolatedMessage, null));
    }

    public void addToControl(String id, String messageTemplate,
            final Object... params) {
        addToControl(id, SEVERITY_INFO, null, messageTemplate, params);
    }

    /**
     * Adds a global message with the default severity (info).
     *
     * @param messageTemplate
     *            The message template string (not a key).
     * @param params
     *            The parameters to be interpolated into the template.
     */
    public void addGlobal(String messageTemplate, final Object... params) {
        addToControl(null, SEVERITY_INFO, null, messageTemplate, params);
    }

    /**
     * Adds a global message with the default severity (info).
     *
     * @param severity
     *            Message severity
     * @param messageTemplate
     *            The message template string (not a key).
     * @param params
     *            The parameters to be interpolated into the template.
     */
    public void addGlobal(Severity severity, String messageTemplate,
            final Object... params) {
        addToControl(null, severity, null, messageTemplate, params);
    }

    /**
     * Adds a global message from the configured resource bundle.
     * @param severity Message severity.
     * @param key Resource bundle message key.
     * @param params The parameters to be interpolated into the message.
     */
    public void addFromResourceBundle(Severity severity, String key,
            final Object... params) {
        Messages messages =
                ServiceLocator.instance().getInstance(Messages.class);
        String formatedMssg = messages.format(key, params);
        addGlobal(severity, formatedMssg, params);
    }

    /**
     * Clears all messages from the faces context.
     */
    public void clear() {
        Iterator<FacesMessage> it =
                FacesContext.getCurrentInstance().getMessages();
        while (it.hasNext()) {
            it.remove();
            it.next();
        }
    }

    /**
     * Returns a list of messages for the specific component Id that is given,
     * and which reside in the current Faces context.
     */
    public List<FacesMessage> getMessagesList(String componentId) {
        return FacesContext.getCurrentInstance().getMessageList(componentId);
    }

    /**
     * Returns a list of global messages in the current Faces context. Global
     * messages are those not associated with a specific component id.
     * (This method is useful when retrieving global messages from a jsf page,
     * as null is not well handled in EL)
     */
    public List<FacesMessage> getGlobalMessagesList() {
        return FacesContext.getCurrentInstance().getMessageList(null);
    }
}
