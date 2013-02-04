/**
 * 
 */
package org.zanata.service;

import java.util.List;

import org.zanata.webtrans.shared.model.ValidationInfo;
import org.zanata.webtrans.shared.model.ValidationObject;

/**
 * @author aeng
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
   
   List<ValidationInfo> getValidationInfo(String projectSlug);
   
   List<ValidationInfo> getValidationInfo(String projectSlug, String versionSlug);

   List<ValidationObject> getValidations(String projectSlug);

   List<ValidationObject> getValidations(String projectSlug, String versionSlug);
}
