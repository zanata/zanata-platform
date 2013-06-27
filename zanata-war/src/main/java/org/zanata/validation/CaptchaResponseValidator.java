/*
 * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Clone of {@link org.jboss.seam.captcha.CaptchaResponseValidator} that
 * correctly wraps the message template specifier.
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class CaptchaResponseValidator implements ConstraintValidator<CaptchaResponse, String>
{
   private static final String WRAPPED_MESSAGE_TEMPLATE_IDENTIFIER = "{org.jboss.seam.captcha.error}";

   public void initialize(CaptchaResponse constraintAnnotation)
   {
   }

   public boolean isValid(String response, ConstraintValidatorContext context)
   {
      boolean isCorrectResponse = isCorrect(response);
      if (!isCorrectResponse)
      {
         addConstraintViolationIn(context);
      }
      return isCorrectResponse;
   }

   private boolean isCorrect(String response)
   {
      return Captcha.instance().validateResponse(response);
   }

   private void addConstraintViolationIn(ConstraintValidatorContext context)
   {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate(WRAPPED_MESSAGE_TEMPLATE_IDENTIFIER).addConstraintViolation();
   }
}
