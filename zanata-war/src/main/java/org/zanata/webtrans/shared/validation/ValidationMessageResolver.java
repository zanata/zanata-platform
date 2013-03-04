/*
 * Copyright 2012, Red Hat, Inc. and individual contributors
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
package org.zanata.webtrans.shared.validation;

import java.util.Collection;
import java.util.List;

/**
 * Message interface for ValidationAction class in both server and client
 * 
 * Implementation:
 * client side : org.zanata.webtrans.client.service.ValidationMessageResolverImpl
 * server side : org.zanata.service.ValidationMessageResolverImpl
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 */
public interface ValidationMessageResolver
{
   // Newline validator
   String leadingNewlineMissing();

   String leadingNewlineAdded();

   String trailingNewlineMissing();

   String trailingNewlineAdded();

   // Tab validator
   String targetHasFewerTabs(int sourceTabs, int targetTabs);

   String targetHasMoreTabs(int sourceTabs, int targetTabs);

   String linesAdded(int expected, int actual);

   String linesRemoved(int expected, int actual);

   // Printf variables validator
   String varPositionOutOfRange(String var);

   String mixVarFormats();

   String varPositionDuplicated(Collection<String> vars);

   // Java variables validator
   String differentVarCount(List<String> vars);

   String differentApostropheCount();

   String quotedCharsAdded();

   // Shared variables validator messages
   String varsMissing(List<String> vars);

   String varsMissingQuoted(List<String> vars);

   String varsAdded(List<String> vars);

   String varsAddedQuoted(List<String> vars);

   // XHM/HTML tag validator
   String tagsAdded(List<String> tags);

   String tagsMissing(List<String> tags);

   String tagsWrongOrder(List<String> tags);

   // XML Entity validator
   String invalidXMLEntity(String entity);
}
