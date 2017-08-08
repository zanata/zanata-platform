/*
 * Copyright 2014, Red Hat, Inc. and individual contributors
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
package org.zanata.email;

import javax.mail.internet.InternetAddress;

import com.google.common.base.Optional;
import javaslang.collection.Map;
import org.zanata.i18n.Messages;

/**
 * Strategy class for EmailBuilder to customise the content and recipients
 * of an email.
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public abstract class EmailStrategy {

    /**
     * For absent, use the default From address configured by the server
     * @return
     */
    public Optional<InternetAddress> getFromAddress() {
        return Optional.absent();
    }

    public Optional<InternetAddress[]> getReplyToAddress(){
        return Optional.absent();
    }

    public abstract String getSubject(Messages msgs);

    /**
     * The classpath resource name of the Velocity template which
     * will provide the complete email.
     * @return the resource name
     */
    public String getTemplateResourceName() {
        return "org/zanata/email/templates/template_email.vm";
    }

    /**
     * The classpath resource name of the Velocity template which
     * will provide the body of the email.
     * @return the resource name
     */
    public abstract String getBodyResourceName();

    /**
     * A map of variable name to value for the context variables needed
     * by this strategy's template.
     * @return the context variables
     * @param genericContext
     * @param toAddresses
     */
    public Map<String, Object> makeContext(
            Map<String, Object> genericContext,
            InternetAddress[] toAddresses) {
        return genericContext.put("body", getBodyResourceName());
    }

}
