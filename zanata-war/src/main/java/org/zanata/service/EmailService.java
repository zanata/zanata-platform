/*
 * Copyright 2012, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.service;

import java.util.List;
import java.util.Set;

import org.zanata.model.HPerson;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public interface EmailService
{

   /**
    * sends emails to configured admin emails for server, or admin users if no
    * server emails are configured.
    *
    *
    * @param emailTemplate
    * @param fromName
    * @param fromLoginName
    * @param replyEmail
    * @param subject
    * @param message
    * @return
    */
   String sendToAdminEmails(String emailTemplate, String fromName, String fromLoginName, String replyEmail, String subject, String message);

   /**
    * sends emails to version group maintainers -> admin -> admin users
    *
    * @param emailTemplate
    * @param maintainers
    * @param fromName
    * @param fromLoginName
    * @param replyEmail
    * @param subject
    * @param message
    * @return
    */
   String sendToVersionGroupMaintainer(String emailTemplate, Set<HPerson> maintainers, String fromName, String fromLoginName, String replyEmail, String subject, String message);

   /**
    *  sends emails to language coordinators -> admin -> admin users
    *
    * @param emailTemplate
    * @param coordinators
    * @param fromName
    * @param fromLoginName
    * @param replyEmail
    * @param subject
    * @param message
    * @param language
    * @return
    */
   String sendToLanguageCoordinators(String emailTemplate, List<HPerson> coordinators, String fromName, String fromLoginName, String replyEmail, String subject, String message, String language);
}
