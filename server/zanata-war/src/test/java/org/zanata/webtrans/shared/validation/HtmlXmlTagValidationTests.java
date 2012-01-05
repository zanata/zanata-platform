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
import org.zanata.webtrans.shared.validation.action.HtmlXmlTagValidation;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
public class HtmlXmlTagValidationTests
{
   private HtmlXmlTagValidation htmlXmlTagValidation;

   @Before
   public void init()
   {
      htmlXmlTagValidation = null;
   }

   @Test
   public void HTMLTagTestTagMissing()
   {
      htmlXmlTagValidation = new HtmlXmlTagValidation("HTML/XML tag", "Matching HTML/XML tag validation");

      String source = "<html><title>HTML TAG Test</title><table><tr><td>column 1 row 1</td><td>column 2 row 1</td></tr></table></html>";
      String target = "<html><title>HTML TAG Test</title><table><tr><td>column 1 row 1</td></tr></table></html>";
      htmlXmlTagValidation.validate(source, target);
      Assert.assertTrue(htmlXmlTagValidation.hasError());
      Assert.assertEquals(htmlXmlTagValidation.getError().size(), 1);
   }

   @Test
   public void HTMLTagTestTagMissing2()
   {
      htmlXmlTagValidation = new HtmlXmlTagValidation("HTML/XML tag", "Matching HTML/XML tag validation");

      String source = "<html><title>HTML TAG Test</title><table><tr><td>column 1 row 1</td><td>column 2 row 1</td></tr></table></html>";
      String target = "<html><title>HTML TAG Test</title><table><tr></tr></table></html>";
      htmlXmlTagValidation.validate(source, target);
      Assert.assertTrue(htmlXmlTagValidation.hasError());
      Assert.assertEquals(htmlXmlTagValidation.getError().size(), 1);
   }

   @Test
   public void HTMLTagTestMatching()
   {
      htmlXmlTagValidation = new HtmlXmlTagValidation("HTML/XML tag", "Matching HTML/XML tag validation");

      String source = "<html><title>HTML TAG Test</title><table><tr><td>column 1 row 1</td><td>column 2 row 1</td></tr></table></html>";
      String target = "<html><title>HTML TAG Test</title><table><tr><td>column 1 row 1</td><td>column 2 row 1</td></tr></table></html>";
      htmlXmlTagValidation.validate(source, target);
      Assert.assertFalse(htmlXmlTagValidation.hasError());
      Assert.assertEquals(htmlXmlTagValidation.getError().size(), 0);
   }

   @Test
   public void XMLTagTestTagMissing()
   {
      htmlXmlTagValidation = new HtmlXmlTagValidation("HTML/XML tag", "Matching HTML/XML tag validation");

      String source = "<group><users><user>1</user><user>2</user></users></group>";
      String target = "<group><users></users></group>";
      htmlXmlTagValidation.validate(source, target);
      Assert.assertTrue(htmlXmlTagValidation.hasError());
      Assert.assertEquals(htmlXmlTagValidation.getError().size(), 1);
   }

   @Test
   public void XMLTagTestTagMissing2()
   {
      htmlXmlTagValidation = new HtmlXmlTagValidation("HTML/XML tag", "Matching HTML/XML tag validation");

      String source = "<group><users><user>1</user></users></group>";
      String target = "<group><users></users></group>";
      htmlXmlTagValidation.validate(source, target);
      Assert.assertTrue(htmlXmlTagValidation.hasError());
      Assert.assertEquals(htmlXmlTagValidation.getError().size(), 1);
   }

   @Test
   public void XMLTagTestMatching()
   {
      htmlXmlTagValidation = new HtmlXmlTagValidation("HTML/XML tag", "Matching HTML/XML tag validation");

      String source = "<group><users><user>1</user></users></group>";
      String target = "<group><users><user>1</user></users></group>";
      htmlXmlTagValidation.validate(source, target);
      Assert.assertFalse(htmlXmlTagValidation.hasError());
      Assert.assertEquals(htmlXmlTagValidation.getError().size(), 0);
   }
}


 