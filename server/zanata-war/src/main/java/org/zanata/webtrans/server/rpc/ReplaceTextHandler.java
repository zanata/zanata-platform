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

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.shared.rpc.ReplaceText;
import org.zanata.webtrans.shared.rpc.ReplaceTextResult;
import org.zanata.webtrans.shared.rpc.UpdateTransUnit;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

@Name("webtrans.gwt.ReplaceTextHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(ReplaceText.class)
public class ReplaceTextHandler extends AbstractActionHandler<ReplaceText, ReplaceTextResult>
{
   private static final String ACTION_MODIFY_TRANSLATION = "modify-translation";

   @Override
   public ReplaceTextResult execute(ReplaceText action, ExecutionContext context) throws ActionException
   {
      //TODO implement
      throw new UnsupportedOperationException("Implement me!");
      //return null;
   }

   @Override
   public void rollback(ReplaceText action, ReplaceTextResult result, ExecutionContext context) throws ActionException
   {
      //TODO implement
      throw new UnsupportedOperationException("Implement me!");
      //
   }
}
