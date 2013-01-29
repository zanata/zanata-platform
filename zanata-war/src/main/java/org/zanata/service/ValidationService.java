/**
 * 
 */
package org.zanata.service;

import java.util.Map;

import org.zanata.webtrans.shared.validation.ValidationObject;

/**
 * @author aeng
 *
 */
public interface ValidationService
{
   /**
    * a map contains all validation objects.
    *
    * @see org.zanata.webtrans.client.service.ValidationService
    * @param valMessages Validation messages
    * @return a map contains all validation objects.
    */
   
   Map<String, ValidationObject> getValidationList();
}
