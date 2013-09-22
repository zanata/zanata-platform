package org.zanata.action;

import java.io.*;
import java.util.*;

import lombok.*;

import org.jboss.seam.*;
import org.jboss.seam.annotations.*;
import org.zanata.service.*;
import org.zanata.webtrans.shared.model.*;
import org.zanata.webtrans.shared.validation.*;

import com.google.common.collect.*;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Name("validationOptionsAction")
@Scope(ScopeType.PAGE)
public class ValidationOptionsAction implements Serializable
{
   @In
   private ValidationService validationServiceImpl;

   @Setter
   @Getter
   private String versionSlug;

   @Setter
   @Getter
   private String projectSlug;

   private Map<ValidationId, ValidationAction> availableValidations = Maps.newHashMap();

   public List<ValidationAction> getValidationList()
   {
      if (availableValidations.isEmpty())
      {
         Collection<ValidationAction> validationList = validationServiceImpl.getValidationActions(projectSlug,
                 versionSlug);

         for (ValidationAction validationAction : validationList)
         {
            availableValidations.put(validationAction.getId(), validationAction);
         }
      }
      List<ValidationAction> sortedList = new ArrayList<ValidationAction>(availableValidations.values());
      Collections.sort(sortedList, ValidationFactory.ValidationActionComparator);
      return sortedList;
   }

   /**
    * If this action is enabled(Warning or Error), then it's exclusive validation will be turn off
    * @param selectedValidationAction
    */
   public void ensureMutualExclusivity(ValidationAction selectedValidationAction)
   {
      if (selectedValidationAction.getState() != ValidationAction.State.Off)
      {
         for (ValidationAction exclusiveValAction : selectedValidationAction.getExclusiveValidations())
         {
            if (availableValidations.containsKey(exclusiveValAction.getId()))
            {
               availableValidations.get(exclusiveValAction.getId()).setState(ValidationAction.State.Off);
            }
         }
      }
   }

   public List<ValidationAction.State> getValidationStates()
   {
      return Arrays.asList(ValidationAction.State.values());
   }

   @Out(required = false)
   public Collection<ValidationAction> getCustomizedValidations()
   {
      return availableValidations.values();
   }
}
