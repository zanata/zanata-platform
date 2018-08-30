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

import static javax.faces.application.FacesMessage.SEVERITY_ERROR;
import static javax.faces.application.FacesMessage.SEVERITY_INFO;
import static javax.faces.application.FacesMessage.Severity;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.deltaspike.core.spi.scope.window.WindowContext;
import org.zanata.i18n.Messages;
/* TODO [CDI] check this: migrated from ScopeType.CONVERSATION */

/**
 * Utility to allow for easy handling of JSF messages. Serves as a replacement
 * for the old Seam 2 org.jboss.seam.faces.FacesMessages class.
 *
 * @author Carlos Munoz
 *         <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@org.apache.deltaspike.core.api.scope.WindowScoped
@Named("jsfMessages")
public class FacesMessages implements Serializable {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(FacesMessages.class);
    private static final long serialVersionUID = -5066321533305820364L;

    private final List<FacesMessage> globalMessages = new ArrayList<>();
    private final Map<String, List<FacesMessage>> keyedMessages =
            new HashMap<>();
    @Inject
    private WindowContext windowContext;
    @Inject
    private Messages msgs;
    @SuppressFBWarnings("SE_BAD_FIELD")
    @Inject
    private FacesContext facesContext;

    @PostConstruct
    void postConstruct() {
        log.debug("{}: postConstruct", this);
    }

    @PreDestroy
    void preDestroy() {
        log.debug("{}: preDestroy", this);
    }

    /**
     * Called to transfer messages from FacesMessages to JSF
     */
    public void beforeRenderResponse() {
        log.debug("{}: beforeRenderResponse", this);
        for (FacesMessage message : globalMessages) {
            facesContext.addMessage(null, message);
        }
        for (Map.Entry<String, List<FacesMessage>> entry : keyedMessages
                .entrySet()) {
            for (FacesMessage message : entry.getValue()) {
                String clientId = getClientId(entry.getKey());
                facesContext.addMessage(clientId, message);
            }
        }
        clear();
    }

    /**
     * Calculate the JSF client ID from the provided widget ID
     */
    private @Nullable String getClientId(String id) {
        // we search from backwards, so for a component tree A->B->C, we search
        // id from C then B then A for a match of id. If we found
        // C.getId().equals(id), we will use C.getClientId()
        Stack<UIComponent> uiComponentStack = new Stack<>();
        addComponentToStack(uiComponentStack, facesContext.getViewRoot());
        while (!uiComponentStack.empty()) {
            UIComponent pop = uiComponentStack.pop();
            if (pop.getId() != null && id.equals(pop.getId())) {
                return pop.getClientId();
            }
        }
        return null;
    }

    private void addComponentToStack(Stack<UIComponent> uiComponentStack,
            UIComponent component) {
        uiComponentStack.push(component);
        Iterator<UIComponent> iter = component.getFacetsAndChildren();
        Queue<UIComponent> children = new LinkedList<>();
        while (iter.hasNext()) {
            UIComponent next = iter.next();
            uiComponentStack.push(next);
            children.add(next);
        }
        for (UIComponent child : children) {
            addComponentToStack(uiComponentStack, child);
        }
    }

    /**
     * Add a status message.
     * <p/>
     * The message will be added to the widget specified by the ID. The
     * algorithm used determine which widget the id refers to is determined by
     * the view layer implementation in use.
     * <p/>
     * You can also specify the severity, and parameters to be interpolated
     */
    private void addToControl(String id, Severity severity, String message) {
        log.debug("{}: addToControl(id={}, template={})", this, id,
                message);
        FacesMessage jsfMssg =
                new FacesMessage(severity, message, null);
        if (id == null) {
            if (shouldLogAsWarning(severity)) {
                log.warn("Global message to user (wid: {}): {})",
                        windowContext.getCurrentWindowId(),
                        message);
            } else {
                log.debug("Global message to user (wid: {}): {})",
                        windowContext.getCurrentWindowId(),
                        message);
            }
            // Global message
            globalMessages.add(jsfMssg);
        } else {
            log.debug("Message to user (wid: {} id: {}): {})",
                    windowContext.getCurrentWindowId(), id, message);
            // Control specific message
            if (keyedMessages.containsKey(id)) {
                keyedMessages.get(id).add(jsfMssg);
            } else {
                List<FacesMessage> list = new ArrayList<>();
                list.add(jsfMssg);
                keyedMessages.put(id, list);
            }
        }
    }

    private boolean shouldLogAsWarning(Severity severity) {
        // log error and fatal messages (to user) as warning (to log)
        return severity.compareTo(SEVERITY_ERROR) >= 0;
    }

    public void addToControl(String id, String message) {
        addToControl(id, SEVERITY_INFO, message);
    }

    /**
     * Adds a global message with the default severity (info).
     *
     * @param message
     *            The message string (not a key).
     */
    public void addGlobal(String message) {
        addGlobal(SEVERITY_INFO, message);
    }

    /**
     * Adds a global message with the default severity (info).
     *
     * @param severity
     *            Message severity
     * @param message
     *            The message string (not a key).
     */
    public void addGlobal(Severity severity, String message) {
        addToControl(null, severity, message);
    }

    public void addGlobal(FacesMessage msg) {
        if (shouldLogAsWarning(msg.getSeverity())) {
            log.warn("Global FacesMessage to user (wid: {}): {})",
                    windowContext.getCurrentWindowId(), msg.getSummary());
        }
        globalMessages.add(msg);
    }

    /**
     * Adds a global message from the configured resource bundle.
     *
     * @param severity
     *            Message severity.
     * @param key
     *            Resource bundle message key.
     * @param params
     *            The parameters to be interpolated into the message.
     */
    public void addFromResourceBundle(Severity severity, String key,
            final Object... params) {
        String formatedMssg = msgs.formatWithAnyArgs(key, params);
        addGlobal(severity, formatedMssg);
    }

    /**
     * Clears all messages from the faces context.
     */
    public void clear() {
        keyedMessages.clear();
        globalMessages.clear();
    }

    /**
     * Returns a list of messages for the specific component Id that is given,
     * and which reside in the current Faces context.
     */
    public List<FacesMessage> getMessagesList(String componentId) {
        return facesContext.getMessageList(componentId);
    }

    /**
     * Returns a list of global messages in the current Faces context. Global
     * messages are those not associated with a specific component id. (This
     * method is useful when retrieving global messages from a jsf page, as null
     * is not well handled in EL)
     */
    public List<FacesMessage> getGlobalMessagesList() {
        return facesContext.getMessageList(null);
    }
}
