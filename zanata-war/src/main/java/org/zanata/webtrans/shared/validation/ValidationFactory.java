/**
 * 
 */
package org.zanata.webtrans.shared.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zanata.webtrans.client.resources.ValidationMessages;
import org.zanata.webtrans.shared.model.ValidationAction;
import org.zanata.webtrans.shared.model.ValidationInfo;
import org.zanata.webtrans.shared.model.ValidationId;
import org.zanata.webtrans.shared.model.ValidationObject;
import org.zanata.webtrans.shared.validation.action.HtmlXmlTagValidation;
import org.zanata.webtrans.shared.validation.action.JavaVariablesValidation;
import org.zanata.webtrans.shared.validation.action.NewlineLeadTrailValidation;
import org.zanata.webtrans.shared.validation.action.PrintfVariablesValidation;
import org.zanata.webtrans.shared.validation.action.PrintfXSIExtensionValidation;
import org.zanata.webtrans.shared.validation.action.TabValidation;
import org.zanata.webtrans.shared.validation.action.XmlEntityValidation;


/**
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 */
public final class ValidationFactory
{
   public static List<ValidationInfo> getAllValidationIds(boolean enabled)
   {
      List<ValidationInfo> validationIds = new ArrayList<ValidationInfo>();

      validationIds.add(new ValidationInfo(ValidationId.HTML_XML, null, enabled));
      validationIds.add(new ValidationInfo(ValidationId.NEW_LINE, null, enabled));
      validationIds.add(new ValidationInfo(ValidationId.TAB, null, enabled));
      validationIds.add(new ValidationInfo(ValidationId.JAVA_VARIABLES, null, enabled));
      validationIds.add(new ValidationInfo(ValidationId.XML_ENTITY, null, enabled));
      validationIds.add(new ValidationInfo(ValidationId.PRINTF_VARIABLES, null, enabled));
      validationIds.add(new ValidationInfo(ValidationId.PRINTF_XSI_EXTENSION, null, false));

      return validationIds;
   }
   
   
   public static Map<ValidationId, ValidationAction> getAllValidationActions(ValidationMessages messages)
   {
      HashMap<ValidationId, ValidationAction> validationList = new HashMap<ValidationId, ValidationAction>();

      validationList.put(ValidationId.HTML_XML, new HtmlXmlTagValidation(ValidationId.HTML_XML, messages));
      validationList.put(ValidationId.NEW_LINE, new NewlineLeadTrailValidation(ValidationId.NEW_LINE, messages));
      validationList.put(ValidationId.TAB, new TabValidation(ValidationId.TAB, messages));
      
      validationList.put(ValidationId.JAVA_VARIABLES, new JavaVariablesValidation(ValidationId.JAVA_VARIABLES, messages));
      validationList.put(ValidationId.XML_ENTITY, new XmlEntityValidation(ValidationId.XML_ENTITY, messages));
      
      PrintfVariablesValidation printfVariablesValidation = new PrintfVariablesValidation(ValidationId.PRINTF_VARIABLES, messages);
      PrintfXSIExtensionValidation positionalPrintfValidation = new PrintfXSIExtensionValidation(ValidationId.PRINTF_XSI_EXTENSION, messages);
      
      printfVariablesValidation.mutuallyExclusive(positionalPrintfValidation);
      positionalPrintfValidation.mutuallyExclusive(printfVariablesValidation);
      
      validationList.put(ValidationId.PRINTF_VARIABLES, printfVariablesValidation);
      validationList.put(ValidationId.PRINTF_XSI_EXTENSION, positionalPrintfValidation);

      return validationList;
   }

   public static Map<ValidationId, ValidationObject> getAllValidationObject()
   {
      HashMap<ValidationId, ValidationObject> validationList = new HashMap<ValidationId, ValidationObject>();

      validationList.putAll(getAllValidationActions(null));

      return validationList;
   }
}
