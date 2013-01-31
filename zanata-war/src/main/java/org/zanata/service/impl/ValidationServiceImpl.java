/**
 * 
 */
package org.zanata.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.service.ValidationService;
import org.zanata.util.ZanataMessages;
import org.zanata.webtrans.shared.model.ValidationId;
import org.zanata.webtrans.shared.model.ValidationRule;

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

   private List<ValidationRule> validationRules;
   
   public void init()
   {
      validationRules = new ArrayList<ValidationRule>();
      
      ValidationRule htmlxmlValidation = new ValidationRule(ValidationId.HTML_XML, zanataMessages.getMessage("jsf.validation.htmlXmlValidator.desc"), true);
      ValidationRule newlineLeadTrailValidation = new ValidationRule(ValidationId.NEW_LINE, zanataMessages.getMessage("jsf.validation.newlineValidator.desc"), true);
      ValidationRule tabValidation = new ValidationRule(ValidationId.TAB, zanataMessages.getMessage("jsf.validation.tabValidator.desc"), true);
      ValidationRule javaVariablesValidation = new ValidationRule(ValidationId.JAVA_VARIABLES, zanataMessages.getMessage("jsf.validation.javaVariablesValidator.desc"), true);
      ValidationRule xmlEntityValidation = new ValidationRule(ValidationId.XML_ENTITY, zanataMessages.getMessage("jsf.validation.xmlEntityValidator.desc"), true);
      ValidationRule printfVariablesValidation = new ValidationRule(ValidationId.PRINTF_VARIABLES, zanataMessages.getMessage("jsf.validation.printfVariablesValidator.desc"), true);
      ValidationRule positionalPrintfValidation = new ValidationRule(ValidationId.PRINTF_XSI_EXTENSION, zanataMessages.getMessage("jsf.validation.printfXSIExtensionValidation.desc"), false);

      printfVariablesValidation.mutuallyExclusive(positionalPrintfValidation);
      positionalPrintfValidation.mutuallyExclusive(printfVariablesValidation);
      
      validationRules.add(htmlxmlValidation);
      validationRules.add(newlineLeadTrailValidation);
      validationRules.add(tabValidation);
      validationRules.add(printfVariablesValidation);
      validationRules.add(positionalPrintfValidation);
      validationRules.add(javaVariablesValidation);
      validationRules.add(xmlEntityValidation);
   }
   
   @Override
   public List<ValidationRule> getValidationRules()
   {
      init();
      return validationRules; 
   }
}
