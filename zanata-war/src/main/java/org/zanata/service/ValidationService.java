/**
 * 
 */
package org.zanata.service;

import java.util.List;

import org.zanata.webtrans.shared.model.ValidationRule;

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
   
   List<ValidationRule> getValidationRules();
}
