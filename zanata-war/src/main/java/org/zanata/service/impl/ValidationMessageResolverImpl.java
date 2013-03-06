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
package org.zanata.service.impl;

import java.util.Collection;
import java.util.List;

import org.zanata.webtrans.client.resources.ValidationMessages;
import org.zanata.webtrans.shared.validation.ValidationMessageResolver;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 */
public class ValidationMessageResolverImpl implements ValidationMessageResolver
{
   private ValidationMessages valMessages;

   public ValidationMessageResolverImpl(ValidationMessages valMessages)
   {
      this.valMessages = valMessages;
   }
   
   @Override
   public String leadingNewlineMissing()
   {
      return valMessages.leadingNewlineMissing();
   }

   @Override
   public String leadingNewlineAdded()
   {
      return valMessages.leadingNewlineAdded();
   }

   @Override
   public String trailingNewlineMissing()
   {
      return valMessages.trailingNewlineMissing();
   }

   @Override
   public String trailingNewlineAdded()
   {
      return valMessages.trailingNewlineAdded();
   }

   @Override
   public String targetHasFewerTabs(int sourceTabs, int targetTabs)
   {
      return valMessages.targetHasFewerTabs(sourceTabs, targetTabs);
   }

   @Override
   public String targetHasMoreTabs(int sourceTabs, int targetTabs)
   {
      return valMessages.targetHasMoreTabs(sourceTabs, targetTabs);
   }

   @Override
   public String linesAdded(int expected, int actual)
   {
      return valMessages.linesAdded(expected, actual);
   }

   @Override
   public String linesRemoved(int expected, int actual)
   {
      return valMessages.linesRemoved(expected, actual);
   }

   @Override
   public String varPositionOutOfRange(String var)
   {
      return valMessages.varPositionOutOfRange(var);
   }

   @Override
   public String mixVarFormats()
   {
      return valMessages.mixVarFormats();
   }

   @Override
   public String varPositionDuplicated(Collection<String> vars)
   {
      return valMessages.varPositionDuplicated(vars);
   }

   @Override
   public String differentVarCount(List<String> vars)
   {
      return valMessages.differentVarCount(vars);
   }

   @Override
   public String differentApostropheCount()
   {
      return valMessages.differentApostropheCount();
   }

   @Override
   public String quotedCharsAdded()
   {
      return valMessages.quotedCharsAdded();
   }

   @Override
   public String varsMissing(List<String> vars)
   {
      return valMessages.varsMissing(vars);
   }

   @Override
   public String varsMissingQuoted(List<String> vars)
   {
      return valMessages.varsMissingQuoted(vars);
   }

   @Override
   public String varsAdded(List<String> vars)
   {
      return valMessages.varsAdded(vars);
   }

   @Override
   public String varsAddedQuoted(List<String> vars)
   {
      return valMessages.varsAddedQuoted(vars);
   }

   @Override
   public String tagsAdded(List<String> tags)
   {
      return valMessages.tagsAdded(tags);
   }

   @Override
   public String tagsMissing(List<String> tags)
   {
      return valMessages.tagsMissing(tags);
   }

   @Override
   public String tagsWrongOrder(List<String> tags)
   {
      return valMessages.tagsWrongOrder(tags);
   }

   @Override
   public String invalidXMLEntity(String entity)
   {
      return valMessages.invalidXMLEntity(entity);
   }

}
