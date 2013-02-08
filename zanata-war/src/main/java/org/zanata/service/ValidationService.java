/**
 * 
 */
package org.zanata.service;

import java.util.List;

import org.zanata.webtrans.shared.model.ValidationObject;


/**
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 */
public interface ValidationService
{
   /**
    * Return all ValidationObjects in groups (exclusive) with enabled=true on those which are
    * defined to the project
    * 
    * @param projectSlug
    * @return
    */
   List<ValidationObject> getValidationObject(String projectSlug);
   

   /**
    * Return all ValidationObjects with enabled=true on those which are
    * customized to the version
    * 
    * @param projectSlug
    * @param versionSlug
    * @return
    */
   List<ValidationObject> getValidationObject(String projectSlug, String versionSlug);
}
