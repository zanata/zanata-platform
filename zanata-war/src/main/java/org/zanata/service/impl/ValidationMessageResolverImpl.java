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

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.util.ZanataMessages;
import org.zanata.webtrans.shared.validation.ValidationMessageResolver;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 */
@Name("validationMessageResolverImpl")
@AutoCreate
@Scope(ScopeType.STATELESS)
public class ValidationMessageResolverImpl implements ValidationMessageResolver
{
   @In
   private ZanataMessages messages;

   @Override
   public String leadingNewlineMissing()
   {
      return messages.getMessage("jsf.validation.htmlXmlValidator.leadingNewlineMissing");
   }

   @Override
   public String leadingNewlineAdded()
   {
      return messages.getMessage("jsf.validation.htmlXmlValidator.leadingNewlineAdded");
   }

   @Override
   public String trailingNewlineMissing()
   {
      return messages.getMessage("jsf.validation.htmlXmlValidator.trailingNewlineMissing");
   }

   @Override
   public String trailingNewlineAdded()
   {
      return messages.getMessage("jsf.validation.htmlXmlValidator.trailingNewlineAdded");
   }

   @Override
   public String targetHasFewerTabs(int sourceTabs, int targetTabs)
   {
      return messages.getMessage("jsf.validation.tabValidator.targetHasFewerTabs", sourceTabs, targetTabs);
   }

   @Override
   public String targetHasMoreTabs(int sourceTabs, int targetTabs)
   {
      return null;
      // return messages.targetHasMoreTabs(sourceTabs, targetTabs);
   }

   @Override
   public String linesAdded(int expected, int actual)
   {
      return null;
      // return messages.linesAdded(expected, actual);
   }

   @Override
   public String linesRemoved(int expected, int actual)
   {
      return null;
      // return messages.linesRemoved(expected, actual);
   }

   @Override
   public String varPositionOutOfRange(String var)
   {
      return null;
      // return messages.varPositionOutOfRange(var);
   }

   @Override
   public String mixVarFormats()
   {
      return null;
      // return messages.mixVarFormats();
   }

   @Override
   public String varPositionDuplicated(Collection<String> vars)
   {
      return null;
      // return messages.varPositionDuplicated(vars);
   }

   @Override
   public String differentVarCount(List<String> vars)
   {
      return null;
      // return messages.differentVarCount(vars);
   }

   @Override
   public String differentApostropheCount()
   {
      return null;
      // return messages.differentApostropheCount();
   }

   @Override
   public String quotedCharsAdded()
   {
      return null;
      // return messages.quotedCharsAdded();
   }

   @Override
   public String varsMissing(List<String> vars)
   {
      return null;
      // return messages.varsMissing(vars);
   }

   @Override
   public String varsMissingQuoted(List<String> vars)
   {
      return null;
      // return messages.varsMissingQuoted(vars);
   }

   @Override
   public String varsAdded(List<String> vars)
   {
      return null;
      // return messages.varsAdded(vars);
   }

   @Override
   public String varsAddedQuoted(List<String> vars)
   {
      return null;
      // return messages.varsAddedQuoted(vars);
   }

   @Override
   public String tagsAdded(List<String> tags)
   {
      return null;
      // return messages.tagsAdded(tags);
   }

   @Override
   public String tagsMissing(List<String> tags)
   {
      return null;
      // return messages.tagsMissing(tags);
   }

   @Override
   public String tagsWrongOrder(List<String> tags)
   {
      return null;
      // return messages.tagsWrongOrder(tags);
   }

   @Override
   public String invalidXMLEntity(String entity)
   {
      return null;
      // return messages.invalidXMLEntity(entity);
   }

}
