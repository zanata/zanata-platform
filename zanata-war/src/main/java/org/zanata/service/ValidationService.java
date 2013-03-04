/**
 * 
 */
package org.zanata.service;

import java.util.Collection;

import org.zanata.webtrans.shared.model.ValidationAction;


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
}
