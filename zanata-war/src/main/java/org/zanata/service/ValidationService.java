/**
 * 
 */
package org.zanata.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.zanata.webtrans.shared.model.DocValidationResultInfo;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.ValidationAction;
import org.zanata.webtrans.shared.model.ValidationId;
import org.zanata.model.HDocument;
import org.zanata.model.HProjectIteration;


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
    */
   Collection<ValidationAction> getValidationObject(HProjectIteration version);


   Map<DocumentId, List<DocValidationResultInfo>> runValidations(List<HDocument> hDocs, List<ValidationId> validationIds, Long localeId);
}
