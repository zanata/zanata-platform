/**
 * 
 */
package org.zanata.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.service.ValidationService;
import org.zanata.util.ZanataMessages;
import org.zanata.webtrans.shared.validation.ValidationObject;
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
@Name("validationService")
@AutoCreate
@Scope(ScopeType.STATELESS)
public class ValidationServiceImpl implements ValidationService
{
   @In
   private ZanataMessages messages;

   private final Map<String, ValidationObject> validationList;
   
   public ValidationServiceImpl()
   {
      validationList = new HashMap<String, ValidationObject>();
      
      HtmlXmlTagValidation htmlxmlValidation = new HtmlXmlTagValidation(messages);
      NewlineLeadTrailValidation newlineLeadTrailValidation = new NewlineLeadTrailValidation(messages);
      TabValidation tabValidation = new TabValidation(messages);
      JavaVariablesValidation javaVariablesValidation = new JavaVariablesValidation(messages);
      XmlEntityValidation xmlEntityValidation = new XmlEntityValidation(messages);
      PrintfVariablesValidation printfVariablesValidation = new PrintfVariablesValidation(messages);
      PrintfXSIExtensionValidation positionalPrintfValidation = new PrintfXSIExtensionValidation(messages);
      printfVariablesValidation.mutuallyExclusive(positionalPrintfValidation);
      positionalPrintfValidation.mutuallyExclusive(printfVariablesValidation);
      
      validationList.put(htmlxmlValidation.getId(), htmlxmlValidation);
      validationList.put(newlineLeadTrailValidation.getId(), newlineLeadTrailValidation);
      validationList.put(tabValidation.getId(), tabValidation);
      validationList.put(printfVariablesValidation.getId(), printfVariablesValidation);
      validationList.put(positionalPrintfValidation.getId(), positionalPrintfValidation);
      validationList.put(javaVariablesValidation.getId(), javaVariablesValidation);
      validationList.put(xmlEntityValidation.getId(), xmlEntityValidation);
   }
   
   @Override
   public Map<String, ValidationObject> getValidationList()
   {
      return validationList; 
   }
}
