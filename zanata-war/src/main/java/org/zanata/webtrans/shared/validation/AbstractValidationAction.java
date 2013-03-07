/*
 * Copyright 2011, Red Hat, Inc. and individual contributors
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

import java.util.ArrayList;
import java.util.List;

import org.zanata.webtrans.client.resources.ValidationMessages;
import org.zanata.webtrans.shared.model.ValidationAction;
import org.zanata.webtrans.shared.model.ValidationInfo;
import org.zanata.webtrans.shared.validation.action.JavaVariablesValidation;
import org.zanata.webtrans.shared.validation.action.NewlineLeadTrailValidation;
import org.zanata.webtrans.shared.validation.action.PrintfVariablesValidation;
import org.zanata.webtrans.shared.validation.action.PrintfXSIExtensionValidation;
import org.zanata.webtrans.shared.validation.action.TabValidation;
import org.zanata.webtrans.shared.validation.action.XmlEntityValidation;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 * @see HtmlXMlTagValidation
 * @see JavaVariablesValidation
 * @see NewlineLeadTrailValidation
 * @see PrintfVariablesValidation
 * @see PrintfXSIExtensionValidation
 * @see TabValidation
 * @see XmlEntityValidation
 **/
public abstract class AbstractValidationAction implements ValidationAction
{
   private ValidationInfo validationInfo;

   private ArrayList<String> errorList = new ArrayList<String>();
   private ArrayList<ValidationAction> exclusiveValidations = new ArrayList<ValidationAction>();

   private ValidationMessages validationMessages;

   public AbstractValidationAction(ValidationInfo validationInfo, ValidationMessages validationMessages)
   {
      this.validationInfo = validationInfo;
      this.validationMessages = validationMessages;
   }

   @Override
   public void validate(String source, String target)
   {
      if (!Strings.isNullOrEmpty(target) && !Strings.isNullOrEmpty(source))
      {
         doValidate(source, target);
      }
   }

   protected abstract void doValidate(String source, String target);


   @Override
   public List<ValidationAction> getExclusiveValidations()
   {
      return exclusiveValidations;
   }
   
   @Override
   public void mutuallyExclusive(ValidationAction... exclusiveValidations)
   {
      this.exclusiveValidations = Lists.newArrayList(exclusiveValidations);
   }

   @Override
   public boolean hasError()
   {
      return !errorList.isEmpty();
   }

   @Override
   public List<String> getError()
   {
      return errorList;
   }

   @Override
   public void clearErrorMessage()
   {
      errorList.clear();
   }

   protected void addError(String error)
   {
      errorList.add(error);
   }
   
   protected ValidationMessages getMessages()
   {
      return validationMessages;
   }
   
   @Override
   public void setValidationInfo(ValidationInfo validationInfo)
   {
      this.validationInfo = validationInfo;
   }

   @Override
   public ValidationInfo getValidationInfo()
   {
      return validationInfo;
   }

}


 