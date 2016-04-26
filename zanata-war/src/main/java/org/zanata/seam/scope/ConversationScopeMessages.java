/*
 *
 *  * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
 *  * @author tags. See the copyright.txt file in the distribution for a full
 *  * listing of individual contributors.
 *  *
 *  * This is free software; you can redistribute it and/or modify it under the
 *  * terms of the GNU Lesser General Public License as published by the Free
 *  * Software Foundation; either version 2.1 of the License, or (at your option)
 *  * any later version.
 *  *
 *  * This software is distributed in the hope that it will be useful, but WITHOUT
 *  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 *  * details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public License
 *  * along with this software; if not, write to the Free Software Foundation,
 *  * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 *  * site: http://www.fsf.org.
 */

package org.zanata.seam.scope;

import java.io.Serializable;
import java.util.List;
import javax.faces.application.FacesMessage;

import javax.inject.Inject;
import javax.inject.Named;
import com.google.common.collect.Lists;
import org.apache.deltaspike.core.api.scope.GroupedConversation;
import org.apache.deltaspike.core.api.scope.GroupedConversationScoped;
import org.zanata.ui.faces.FacesMessages;
import org.zanata.util.ServiceLocator;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * @deprecated use FacesMessages
 */
@Deprecated
@Named("conversationScopeMessages")
public class ConversationScopeMessages implements Serializable {

    @Inject
    private FacesMessages jsfMessages;

    public void setMessage(FacesMessage.Severity severity, String message) {
        FacesMessage facesMessage = new FacesMessage(severity, message, null);
        jsfMessages.addGlobal(facesMessage);
    }

    public void addMessages(List<FacesMessage> messages) {
        messages.forEach(msg -> jsfMessages.addGlobal(msg));
    }

    public static ConversationScopeMessages instance() {
        return ServiceLocator.instance().getInstance(
                ConversationScopeMessages.class);
    }
}
