/**
 * 
 */
package org.zanata.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.service.ValidationService;
import org.zanata.util.ZanataMessages;
import org.zanata.webtrans.shared.model.ValidationActionInfo;
import org.zanata.webtrans.shared.model.ValidationId;
import org.zanata.webtrans.shared.validation.ValidationFactory;

/**
 * @author aeng
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

   private List<ValidationActionInfo> execute()
   {
      List<ValidationActionInfo> validationIds = ValidationFactory.getAllValidationIds();
//      List<ValidationId> enabledValidations = new ArrayList<ValidationId>();
      
      //user DAO to get list of enabled validations
      //loop through all validationIds list and set enabled
      for(ValidationActionInfo actionInfo: validationIds)
      {
         actionInfo.setDescription(zanataMessages.getMessage(actionInfo.getId().getMessagePrefix() + DESC_KEY));
         actionInfo.setEnabled(true);
//         if(enabledValidations.contains(entry.getKey()))
//         {
//            entry.setValue(true);
//         }
      }
      return validationIds;
   }
   
   @Override
   public List<ValidationActionInfo> getEnabledValidations(String projectSlug, String versionSlug)
   {
      return execute();
   }

   @Override
   public List<ValidationActionInfo> getEnabledValidations(String projectSlug)
   {
      return execute();
   }
}
