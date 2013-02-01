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
import org.zanata.webtrans.shared.model.ValidationActionInfo;
import org.zanata.webtrans.shared.model.ValidationId;
import org.zanata.webtrans.shared.validation.action.HtmlXmlTagValidation;
import org.zanata.webtrans.shared.validation.action.JavaVariablesValidation;
import org.zanata.webtrans.shared.validation.action.NewlineLeadTrailValidation;
import org.zanata.webtrans.shared.validation.action.PrintfVariablesValidation;
import org.zanata.webtrans.shared.validation.action.PrintfXSIExtensionValidation;
import org.zanata.webtrans.shared.validation.action.TabValidation;
import org.zanata.webtrans.shared.validation.action.XmlEntityValidation;

/**
 * @author aeng
 * 
 */
public final class ValidationFactory
{
   public static List<ValidationActionInfo> getAllValidationIds()
   {
      List<ValidationActionInfo> validationIds = new ArrayList<ValidationActionInfo>();

      validationIds.add(new ValidationActionInfo(ValidationId.HTML_XML, null, false));
      validationIds.add(new ValidationActionInfo(ValidationId.NEW_LINE, null, false));
      validationIds.add(new ValidationActionInfo(ValidationId.TAB, null, false));
      validationIds.add(new ValidationActionInfo(ValidationId.JAVA_VARIABLES, null, false));
      validationIds.add(new ValidationActionInfo(ValidationId.XML_ENTITY, null, false));
      validationIds.add(new ValidationActionInfo(ValidationId.PRINTF_VARIABLES, null, false));
      validationIds.add(new ValidationActionInfo(ValidationId.PRINTF_XSI_EXTENSION, null, false));

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
}
