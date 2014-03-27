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

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import com.google.common.collect.Lists;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Name("conversationScopeMessages")
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class ConversationScopeMessages implements Serializable {

    private List<FacesMessage> messages = Lists.newArrayList();

    @Begin(join = true)
    public void setMessage(FacesMessage.Severity severity, String message) {
        FacesMessage facesMessage = new FacesMessage(severity, message, null);
        setMessages(Lists.newArrayList(facesMessage));
    }

    @Begin(join = true)
    public void setMessages(List<FacesMessage> messages) {
        this.messages = messages;
    }

    public List<FacesMessage> getMessages() {
        return messages;
    }

    public void clearMessages() {
        messages.clear();
    }

    public boolean hasMessages() {
        return !messages.isEmpty();
    }

    public static ConversationScopeMessages instance() {
        return (ConversationScopeMessages) Component
                .getInstance(ConversationScopeMessages.class);
    }
}
