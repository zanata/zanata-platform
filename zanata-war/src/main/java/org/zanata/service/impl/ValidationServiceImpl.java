/**
 * 
 */
package org.zanata.service.impl;

import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.service.ValidationService;
import org.zanata.util.ZanataMessages;
import org.zanata.webtrans.shared.model.ValidationId;
import org.zanata.webtrans.shared.model.ValidationInfo;
import org.zanata.webtrans.shared.model.ValidationObject;
import org.zanata.webtrans.shared.validation.ValidationFactory;

/**
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 */
@Name("validationServiceImpl")
@AutoCreate
@Scope(ScopeType.STATELESS)
public class ValidationServiceImpl implements ValidationService
{
   @In
   private ZanataMessages zanataMessages;
   
   private static final String DESC_KEY = ".desc";

   private List<ValidationInfo> execute()
   {
      List<ValidationInfo> validationIds = ValidationFactory.getAllValidationIds(false);

      // List<ValidationId> enabledValidations = new ArrayList<ValidationId>();
      
      //user DAO to get list of enabled validations
      //loop through all validationIds list and set enabled

      for (ValidationInfo actionInfo : validationIds)
      {
         actionInfo.setDescription(zanataMessages.getMessage(actionInfo.getId().getMessagePrefix() + DESC_KEY));
         actionInfo.setEnabled(true);
         if (actionInfo.getId().equals(ValidationId.PRINTF_XSI_EXTENSION))
         {
            actionInfo.setEnabled(false);
         }
         // if(enabledValidations.contains(entry.getKey()))
         // {
         // entry.setValue(true);
         // }
      }
      return validationIds;
   }
   
   @Override
   public List<ValidationInfo> getValidationInfo(String projectSlug, String versionSlug)
   {
      return execute();
   }

   @Override
   public List<ValidationInfo> getValidationInfo(String projectSlug)
   {
      return execute();
   }

   @Override
   public List<ValidationObject> getValidations(String projectSlug)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public List<ValidationObject> getValidations(String projectSlug, String versionSlug)
   {
      // TODO Auto-generated method stub
      return null;
   }
}
