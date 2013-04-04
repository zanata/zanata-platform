/**
 * 
 */
package org.zanata.service;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.zanata.common.LocaleId;
import org.zanata.model.HDocument;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;
import org.zanata.webtrans.shared.model.ValidationAction;
import org.zanata.webtrans.shared.model.ValidationId;


/**
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 */
public interface ValidationService
{
   /**
    * Return all ValidationActions with enabled=true on those which are defined
    * to the project
    * 
    * @param projectSlug
    * @return
    * @throws IOException
    */
   Collection<ValidationAction> getValidationAction(String projectSlug);
   

   /**
    * Return all ValidationActions on those which are customized to the version
    * 
    * @param projectSlug
    * @param versionSlug
    * @return
    */
   Collection<ValidationAction> getValidationAction(String projectSlug, String versionSlug);

   /**
    * Return all ValidationActions with enabled=true on those which are
    * customized to the version
    * 
    * @param HProjectIteration
    * @return
    * @throws IOException
    */
   Collection<ValidationAction> getValidationObject(HProjectIteration version);

   /**
    * Run validation check on HTextFlow and HTextFlowTarget with specific locale
    * from HDocument against validations rules
    * 
    * Returns count of transUnit that has error
    * 
    * @param hDocs
    * @param validations
    * @param localeId
    */
   boolean runValidations(HDocument hDoc, List<ValidationId> validationIds, LocaleId localeId);

   /**
    * Filter list of text flow with those only contains validation error
    * 
    * @param textFlows
    * @param id
    * @param maxSize
    * @throws IOException
    */
   List<HTextFlow> filterHasErrorTexFlow(List<HTextFlow> textFlows, List<ValidationId> validationIds, LocaleId localeId, int startIndex, int maxSize);
}
