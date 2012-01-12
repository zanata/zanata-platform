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
import org.zanata.webtrans.shared.validation.action.VariablesValidation;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
public class VariablesValidationTests
{
   private VariablesValidation variablesValidation;

   @Before
   public void init()
   {
      variablesValidation = null;
   }

   @Test
   public void VariablesTestWithMismatch()
   {
      variablesValidation = new VariablesValidation("Variables check", "Variables check validation");

      String source = "Testing string with variable %var1";
      String target = "Testing string with no variables";
      variablesValidation.validate(source, target);
      Assert.assertTrue(variablesValidation.hasError());
      Assert.assertEquals(variablesValidation.getError().size(), 1);
   }

   @Test
   public void VariablesTestWithMismatch2()
   {
      variablesValidation = new VariablesValidation("Variables check", "Variables check validation");

      String source = "Testing string with variable %var1 and %var2";
      String target = "Testing string with no variables";
      variablesValidation.validate(source, target);
      Assert.assertTrue(variablesValidation.hasError());
      Assert.assertEquals(variablesValidation.getError().size(), 1);
   }

   @Test
   public void VariablesTestWithMismatch3()
   {
      variablesValidation = new VariablesValidation("Variables check", "Variables check validation");

      String source = "Testing string with variable %var1 and %var2 and %var3";
      String target = "Testing string with no variables";
      variablesValidation.validate(source, target);
      Assert.assertTrue(variablesValidation.hasError());
      Assert.assertEquals(variablesValidation.getError().size(), 1);
   }

   @Test
   public void VariablesTestWithMismatch4()
   {
      variablesValidation = new VariablesValidation("Variables check", "Variables check validation");

      String source = "Testing string with variable %var1 and %var2 and %var3";
      String target = "Testing string with variable %var1 and %var2 and %var3 and %var4";
      variablesValidation.validate(source, target);
      Assert.assertTrue(variablesValidation.hasError());
      Assert.assertEquals(variablesValidation.getError().size(), 1);
   }

   @Test
   public void VariablesTestWithMatch()
   {
      variablesValidation = new VariablesValidation("Variables check", "Variables check validation");

      String source = "Testing string with variable %var1 and %var2";
      String target = "Testing string with variable %var1 and %var2";
      variablesValidation.validate(source, target);
      Assert.assertFalse(variablesValidation.hasError());
      Assert.assertEquals(variablesValidation.getError().size(), 0);
   }
}


 