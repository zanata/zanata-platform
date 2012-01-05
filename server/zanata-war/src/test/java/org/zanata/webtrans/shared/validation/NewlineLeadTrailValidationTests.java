/*
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.webtrans.shared.validation;

import org.junit.Before;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.zanata.webtrans.shared.validation.action.NewlineLeadTrailValidation;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
public class NewlineLeadTrailValidationTests
{
   private NewlineLeadTrailValidation newlineLeadTrailValidation;

   @Before
   public void init()
   {
      newlineLeadTrailValidation = null;
   }

   @Test
   public void NewlineLeadTrailTestWithMissingLead()
   {
      newlineLeadTrailValidation = new NewlineLeadTrailValidation("Newline lead/trail", "Newline lead/trail validation");

      String source = "\nTesting string with leading new line";
      String target = "Testing string with leading new line\n";
      newlineLeadTrailValidation.validate(source, target);
      Assert.assertTrue(newlineLeadTrailValidation.hasError());
      Assert.assertEquals(newlineLeadTrailValidation.getError().size(), 1);
   }

   @Test
   public void NewlineLeadTrailTestWithMissingTrail()
   {
      newlineLeadTrailValidation = new NewlineLeadTrailValidation("Newline lead/trail", "Newline lead/trail validation");

      String source = "Testing string with leading new line\n";
      String target = "\nTesting string with leading new line";
      newlineLeadTrailValidation.validate(source, target);
      Assert.assertTrue(newlineLeadTrailValidation.hasError());
      Assert.assertEquals(newlineLeadTrailValidation.getError().size(), 1);
   }

   @Test
   public void NewlineLeadTrailTestWithMissingBoth()
   {
      newlineLeadTrailValidation = new NewlineLeadTrailValidation("Newline lead/trail", "Newline lead/trail validation");

      String source = "\nTesting string with leading new line\n";
      String target = "Testing string with leading new line";
      newlineLeadTrailValidation.validate(source, target);
      Assert.assertTrue(newlineLeadTrailValidation.hasError());
      Assert.assertEquals(newlineLeadTrailValidation.getError().size(), 1);
   }

   @Test
   public void NewlineLeadTrailTestMatch()
   {
      newlineLeadTrailValidation = new NewlineLeadTrailValidation("Newline lead/trail", "Newline lead/trail validation");

      String source = "\nTesting string with leading new line\n";
      String target = "\nTesting string with leading new line\n";
      newlineLeadTrailValidation.validate(source, target);
      Assert.assertFalse(newlineLeadTrailValidation.hasError());
      Assert.assertEquals(newlineLeadTrailValidation.getError().size(), 0);
   }
}


 