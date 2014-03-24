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
import java.util.Map;

import javax.faces.application.FacesMessage;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Name("flashScopeMessage")
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class FlashScopeMessage implements Serializable {

    public final static String MESSAGES_KEY = "messages";

    private Map<String, List<FacesMessage>> attributes = Maps.newHashMap();

    @Begin(join = true)
    public void putMessage(FacesMessage.Severity severity, String message) {
        FacesMessage facesMessage = new FacesMessage(severity, message, null);
        putMessages(Lists.newArrayList(facesMessage));
    }

    @Begin(join = true)
    public void putMessages(List<FacesMessage> messages) {
        attributes.put(MESSAGES_KEY, messages);
    }

    public List<FacesMessage> getMessages() {
        return attributes.get(MESSAGES_KEY);
    }

    public void clearMessages() {
        attributes.remove(MESSAGES_KEY);
    }

    public boolean hasMessages() {
        return attributes.containsKey(MESSAGES_KEY);
    }

    public static FlashScopeMessage instance() {
        return (FlashScopeMessage) Component
                .getInstance(FlashScopeMessage.class);
    }
}
