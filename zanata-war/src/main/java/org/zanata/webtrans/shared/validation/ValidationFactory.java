/**
 * 
 */
package org.zanata.webtrans.shared.validation;

import com.google.common.collect.Maps;
import org.zanata.webtrans.client.resources.ValidationMessages;
import org.zanata.webtrans.shared.model.ValidationAction;
import org.zanata.webtrans.shared.model.ValidationAction.State;
import org.zanata.webtrans.shared.model.ValidationId;
import org.zanata.webtrans.shared.validation.action.HtmlXmlTagValidation;
import org.zanata.webtrans.shared.validation.action.JavaVariablesValidation;
import org.zanata.webtrans.shared.validation.action.NewlineLeadTrailValidation;
import org.zanata.webtrans.shared.validation.action.PrintfVariablesValidation;
import org.zanata.webtrans.shared.validation.action.PrintfXSIExtensionValidation;
import org.zanata.webtrans.shared.validation.action.TabValidation;
import org.zanata.webtrans.shared.validation.action.XmlEntityValidation;

import java.util.Comparator;
import java.util.Map;

/**
 * Validation Factory - provides list of available validation rules to run on server or client.
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public final class ValidationFactory
{
   private final ValidationMessages validationMessages;

   private final Map<ValidationId, ValidationAction> referenceMap;

   public static final Comparator<ValidationAction> ValidationActionComparator = new Comparator<ValidationAction>()
   {
      @Override
      public int compare(ValidationAction o1, ValidationAction o2)
      {
         return o1.getId().getDisplayName().compareTo(o2.getId().getDisplayName());
      }
   };

   public ValidationFactory(ValidationMessages validationMessages)
   {
      this.validationMessages = validationMessages;
      referenceMap = generateActions();
   }

   /**
    * Generate all Validation Actions with default states(Warning)
    * 
    * @return Map<ValidationId, ValidationAction>
    */
   public Map<ValidationId, ValidationAction> getAllValidationActions()
   {
      return generateActions();
   }

   public ValidationAction getValidationAction(ValidationId id)
   {
      return referenceMap.get(id);
   }

   private Map<ValidationId, ValidationAction> generateActions()
   {
      Map<ValidationId, ValidationAction> validationMap = Maps.newHashMap();

      validationMap.put(ValidationId.HTML_XML, new HtmlXmlTagValidation(ValidationId.HTML_XML, validationMessages));
      validationMap.put(ValidationId.JAVA_VARIABLES, new JavaVariablesValidation(ValidationId.JAVA_VARIABLES,
            validationMessages));
      validationMap.put(ValidationId.NEW_LINE, new NewlineLeadTrailValidation(ValidationId.NEW_LINE,
            validationMessages));

      PrintfVariablesValidation printfVariablesValidation = new PrintfVariablesValidation(
            ValidationId.PRINTF_VARIABLES, validationMessages);
      PrintfXSIExtensionValidation positionalPrintfValidation = new PrintfXSIExtensionValidation(
            ValidationId.PRINTF_XSI_EXTENSION, validationMessages);
      positionalPrintfValidation.setState(State.Off);

      printfVariablesValidation.mutuallyExclusive(positionalPrintfValidation);
      positionalPrintfValidation.mutuallyExclusive(printfVariablesValidation);

      validationMap.put(ValidationId.PRINTF_VARIABLES, printfVariablesValidation);
      validationMap.put(ValidationId.PRINTF_XSI_EXTENSION, positionalPrintfValidation);
      validationMap.put(ValidationId.TAB, new TabValidation(ValidationId.TAB, validationMessages));
      validationMap.put(ValidationId.XML_ENTITY, new XmlEntityValidation(ValidationId.XML_ENTITY, validationMessages));

      return validationMap;
   }
}
