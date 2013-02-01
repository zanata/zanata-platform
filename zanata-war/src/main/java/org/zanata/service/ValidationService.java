/**
 * 
 */
package org.zanata.service;

import java.util.List;
import java.util.Map;

import org.zanata.webtrans.shared.model.ValidationActionInfo;
import org.zanata.webtrans.shared.model.ValidationId;

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
   
   List<ValidationActionInfo> getEnabledValidations(String projectSlug);
   
   List<ValidationActionInfo> getEnabledValidations(String projectSlug, String versionSlug);
}
