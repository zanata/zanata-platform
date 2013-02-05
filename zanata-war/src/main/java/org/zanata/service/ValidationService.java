/**
 * 
 */
package org.zanata.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.zanata.webtrans.shared.model.ValidationId;
import org.zanata.webtrans.shared.model.ValidationInfo;
import org.zanata.webtrans.shared.model.ValidationObject;


/**
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 */
public interface ValidationService
{
   /**
    * a list contains all validation rules.
    * 
    * @see org.zanata.webtrans.client.service.ValidationService
    * @param valMessages Validation messages
    * @return a map contains all validation objects.
    */
   
   Map<ValidationId, ValidationObject> getValidationObject(String projectSlug);
   
   List<ValidationInfo> getValidationInfo(String projectSlug, String versionSlug);

   Set<String> convertCustomizedValidations(Map<String, String> customizedValidations);
}
