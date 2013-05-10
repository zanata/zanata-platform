package org.zanata.service;

import java.io.IOException;

import org.zanata.exception.ValidationException;
import org.zanata.webtrans.client.resources.ValidationMessages;
import org.zanata.webtrans.server.locale.Gwti18nReader;
import org.zanata.webtrans.shared.validation.ValidationFactory;

public final class ValidationFactoryProvider
{
   private static ValidationFactory validationFactory;

   public static ValidationFactory getFactoryInstance()
   {
      if (validationFactory == null)
      {
         try
         {
            ValidationMessages valMessages = Gwti18nReader.create(ValidationMessages.class);
            validationFactory = new ValidationFactory(valMessages);
         }
         catch (IOException e)
         {
            throw new ValidationException("Unable to load validation messages");
         }
      }
      return validationFactory;
   }
}
