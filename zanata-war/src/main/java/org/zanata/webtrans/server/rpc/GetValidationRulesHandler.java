/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.webtrans.server.rpc;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.ValidationService;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.shared.rpc.GetValidationRulesAction;
import org.zanata.webtrans.shared.rpc.GetValidationRulesResult;

/**
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 */
@Name("webtrans.gwt.GetValidationRulesHandler")
@ActionHandlerFor(GetValidationRulesAction.class)
public class GetValidationRulesHandler extends AbstractActionHandler<GetValidationRulesAction, GetValidationRulesResult>
{
   @In
   private ZanataIdentity identity;

   @In
   private ValidationService validationServiceImpl;


   @Override
   public GetValidationRulesResult execute(GetValidationRulesAction action, ExecutionContext context) throws ActionException
   {
      return new GetValidationRulesResult(validationServiceImpl.getValidationRules());
   }

   @Override
   public void rollback(GetValidationRulesAction action, GetValidationRulesResult result, ExecutionContext context) throws ActionException
   {
   }
}
