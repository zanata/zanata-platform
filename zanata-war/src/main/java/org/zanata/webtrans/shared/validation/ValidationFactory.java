/**
 * 
 */
package org.zanata.webtrans.shared.validation;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import org.zanata.webtrans.shared.model.ValidationAction;
import org.zanata.webtrans.shared.model.ValidationId;
import org.zanata.webtrans.shared.validation.action.HtmlXmlTagValidation;
import org.zanata.webtrans.shared.validation.action.JavaVariablesValidation;
import org.zanata.webtrans.shared.validation.action.NewlineLeadTrailValidation;
import org.zanata.webtrans.shared.validation.action.PrintfVariablesValidation;
import org.zanata.webtrans.shared.validation.action.PrintfXSIExtensionValidation;
import org.zanata.webtrans.shared.validation.action.TabValidation;
import org.zanata.webtrans.shared.validation.action.XmlEntityValidation;


/**
 * Validation Factory - provides list of available validation rules to run on server or client.
 * IMPORTANT: Run ValidationFactory.init(ValidationMessageResolver) to initialize 
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 */
public class ValidationFactory
{
   private static Map<ValidationId, ValidationAction> VALIDATION_MAP = new TreeMap<ValidationId, ValidationAction>();

   public static Comparator<ValidationId> ValidationIdComparator = new Comparator<ValidationId>()
   {
      @Override
      public int compare(ValidationId o1, ValidationId o2)
      {
         return o1.getDisplayName().compareTo(o2.getDisplayName());
      }
   };

   public ValidationFactory(ValidationMessageResolver messageResolver)
   {
      init(messageResolver);
   }

   /**
    * Generate sorted list of all Validation Actions with enabled = false
    * 
    * Used in client side (ValidationAction)
    * 
    * @param messages
    * @return Map<ValidationId, ValidationAction>
    */
   public Map<ValidationId, ValidationAction> getAllValidationActions()
   {
      return VALIDATION_MAP;
   }

   public ValidationAction getValidationAction(ValidationId id)
   {
      return VALIDATION_MAP.get(id);
   }

   private void init(ValidationMessageResolver messageResolver)
   {
      initValidationMap(messageResolver);
   }

   private void initValidationMap(ValidationMessageResolver messageResolver)
   {
      VALIDATION_MAP.clear();

      VALIDATION_MAP.put(ValidationId.HTML_XML, new HtmlXmlTagValidation(ValidationId.HTML_XML, messageResolver));
      VALIDATION_MAP.put(ValidationId.NEW_LINE, new NewlineLeadTrailValidation(ValidationId.NEW_LINE, messageResolver));
      VALIDATION_MAP.put(ValidationId.TAB, new TabValidation(ValidationId.TAB, messageResolver));

      VALIDATION_MAP.put(ValidationId.JAVA_VARIABLES, new JavaVariablesValidation(ValidationId.JAVA_VARIABLES, messageResolver));
      VALIDATION_MAP.put(ValidationId.XML_ENTITY, new XmlEntityValidation(ValidationId.XML_ENTITY, messageResolver));

      PrintfVariablesValidation printfVariablesValidation = new PrintfVariablesValidation(ValidationId.PRINTF_VARIABLES, messageResolver);
      PrintfXSIExtensionValidation positionalPrintfValidation = new PrintfXSIExtensionValidation(ValidationId.PRINTF_XSI_EXTENSION, messageResolver);

      printfVariablesValidation.mutuallyExclusive(positionalPrintfValidation);
      positionalPrintfValidation.mutuallyExclusive(printfVariablesValidation);

      VALIDATION_MAP.put(ValidationId.PRINTF_VARIABLES, printfVariablesValidation);
      VALIDATION_MAP.put(ValidationId.PRINTF_XSI_EXTENSION, positionalPrintfValidation);
   }

}
