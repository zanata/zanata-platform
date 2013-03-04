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
package org.zanata.service;

import java.util.Collection;
import java.util.List;

import org.zanata.util.ZanataMessages;
import org.zanata.webtrans.client.resources.ValidationMessages;
import org.zanata.webtrans.shared.validation.ValidationMessageResolver;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 */
public class ValidationMessageResolverImpl implements ValidationMessageResolver
{
   private final ZanataMessages messages;

   public ValidationMessageResolverImpl(ZanataMessages messages)
   {
      this.messages = messages;
   }

   @Override
   public String leadingNewlineMissing()
   {
      return messages.leadingNewlineMissing();
   }

   @Override
   public String leadingNewlineAdded()
   {
      return messages.leadingNewlineAdded();
   }

   @Override
   public String trailingNewlineMissing()
   {
      return messages.trailingNewlineMissing();
   }

   @Override
   public String trailingNewlineAdded()
   {
      return messages.trailingNewlineAdded();
   }

   @Override
   public String targetHasFewerTabs(int sourceTabs, int targetTabs)
   {
      return messages.targetHasFewerTabs(sourceTabs, targetTabs);
   }

   @Override
   public String targetHasMoreTabs(int sourceTabs, int targetTabs)
   {
      return messages.targetHasMoreTabs(sourceTabs, targetTabs);
   }

   @Override
   public String linesAdded(int expected, int actual)
   {
      return messages.linesAdded(expected, actual);
   }

   @Override
   public String linesRemoved(int expected, int actual)
   {
      return messages.linesRemoved(expected, actual);
   }

   @Override
   public String varPositionOutOfRange(String var)
   {
      return messages.varPositionOutOfRange(var);
   }

   @Override
   public String mixVarFormats()
   {
      return messages.mixVarFormats();
   }

   @Override
   public String varPositionDuplicated(Collection<String> vars)
   {
      return messages.varPositionDuplicated(vars);
   }

   @Override
   public String differentVarCount(List<String> vars)
   {
      return messages.differentVarCount(vars);
   }

   @Override
   public String differentApostropheCount()
   {
      return messages.differentApostropheCount();
   }

   @Override
   public String quotedCharsAdded()
   {
      return messages.quotedCharsAdded();
   }

   @Override
   public String varsMissing(List<String> vars)
   {
      return messages.varsMissing(vars);
   }

   @Override
   public String varsMissingQuoted(List<String> vars)
   {
      return messages.varsMissingQuoted(vars);
   }

   @Override
   public String varsAdded(List<String> vars)
   {
      return messages.varsAdded(vars);
   }

   @Override
   public String varsAddedQuoted(List<String> vars)
   {
      return messages.varsAddedQuoted(vars);
   }

   @Override
   public String tagsAdded(List<String> tags)
   {
      return messages.tagsAdded(tags);
   }

   @Override
   public String tagsMissing(List<String> tags)
   {
      return messages.tagsMissing(tags);
   }

   @Override
   public String tagsWrongOrder(List<String> tags)
   {
      return messages.tagsWrongOrder(tags);
   }

   @Override
   public String invalidXMLEntity(String entity)
   {
      return messages.invalidXMLEntity(entity);
   }

}
