/*
 * Copyright 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.zanata.webtrans.server.rpc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.service.TranslationService;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.shared.rpc.ReplaceText;
import org.zanata.webtrans.shared.rpc.UpdateTransUnitResult;
import com.google.common.base.Strings;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

@Name("webtrans.gwt.ReplaceTextHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(ReplaceText.class)
public class ReplaceTextHandler extends AbstractActionHandler<ReplaceText, UpdateTransUnitResult>
{
   private static final Logger LOGGER = LoggerFactory.getLogger(ReplaceTextHandler.class);

   @In(value = "webtrans.gwt.UpdateTransUnitHandler", create = true)
   UpdateTransUnitHandler updateTransUnitHandler;

   @Override
   public UpdateTransUnitResult execute(ReplaceText action, ExecutionContext context) throws ActionException
   {
      //TODO in an optimal world we should do security check before making all the effort. Wait for SecurityService implementation
      String searchText = action.getSearchText();
      String replaceText = action.getReplaceText();
      if (Strings.isNullOrEmpty(searchText) || Strings.isNullOrEmpty(replaceText))
      {
         throw new ActionException("search or replace text is empty");
      }

      int flags = action.isCaseSensitive() ? Pattern.UNICODE_CASE : Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
      Pattern pattern = Pattern.compile(action.getSearchText(), flags);

      LOGGER.debug("transUnit {} before replace [{}]", action.getTransUnitId(), action.getContents());
      for (int i = 0, contentsSize = action.getContents().size(); i < contentsSize; i++)
      {
         String content = action.getContents().get(i);
         Matcher matcher = pattern.matcher(content);
         String newContent = matcher.replaceAll(replaceText);
         action.getContents().set(i, newContent);
      }

      LOGGER.debug("transUnit {} after replace [{}]", action.getTransUnitId(), action.getContents());

      return updateTransUnitHandler.execute(action, context);
   }

   @Override
   public void rollback(ReplaceText action, UpdateTransUnitResult result, ExecutionContext context) throws ActionException
   {
      throw new ActionException("not supported");
   }
}
