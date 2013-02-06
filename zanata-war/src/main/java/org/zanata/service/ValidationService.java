/**
 * 
 */
package org.zanata.service;

import java.util.Comparator;
import java.util.Map;

import org.zanata.webtrans.shared.model.ValidationId;
import org.zanata.webtrans.shared.model.ValidationObject;


/**
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 */
public interface ValidationService
{
   /**
    * Return all ValidationObjects with enabled=true on those which are
    * customized to the project
    * 
    * @param projectSlug
    * @return
    */
   Map<ValidationId, ValidationObject> getValidationObject(String projectSlug);
   

   /**
    * Return all ValidationObjects with enabled=true on those which are
    * customized to the version
    * 
    * @param projectSlug
    * @param versionSlug
    * @return
    */
   Map<ValidationId, ValidationObject> getValidationObject(String projectSlug, String versionSlug);

   /**
    * Return comparator
    * 
    * @return
    */
   Comparator<ValidationObject> getObjectComparator();
}
