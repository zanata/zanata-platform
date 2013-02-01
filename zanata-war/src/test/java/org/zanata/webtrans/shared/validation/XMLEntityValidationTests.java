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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import org.hamcrest.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.webtrans.client.resources.ValidationMessages;
import org.zanata.webtrans.shared.model.ValidationId;
import org.zanata.webtrans.shared.validation.action.XmlEntityValidation;

/**
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 **/
@Test(groups = { "unit-tests" })
public class XMLEntityValidationTests
{
   // TODO use TestMessages

   private XmlEntityValidation xmlEntityValidation;

   @Mock
   private ValidationMessages mockMessages;

   @BeforeMethod
   public void beforeMethod()
   {
      MockitoAnnotations.initMocks(this);
      xmlEntityValidation = new XmlEntityValidation(ValidationId.XML_ENTITY, mockMessages);
      xmlEntityValidation.setEnabled(true);
   }

   @Test
   public void idIsSet()
   {
      assertThat(xmlEntityValidation.getId(), Matchers.equalTo(ValidationId.XML_ENTITY));
   }

   @Test
   public void testNoEntity()
   {
      String source = "Source string without xml entity";
      String target = "Target string without xml entity";
      xmlEntityValidation.validate(source, target);

      assertThat(xmlEntityValidation.hasError(), Matchers.equalTo(false));
      assertThat(xmlEntityValidation.getError().size(), Matchers.equalTo(0));
   }

   @Test
   public void testWithCompleteEntity()
   {
      String source = "Source string";
      String target = "Target string: &mash; bla bla &test;";
      xmlEntityValidation.validate(source, target);

      assertThat(xmlEntityValidation.hasError(), Matchers.equalTo(false));
      assertThat(xmlEntityValidation.getError().size(), Matchers.equalTo(0));
   }

   @Test
   public void testWithIncompleteEntityCharRef()
   {
      when(mockMessages.invalidXMLEntity("&mash")).thenReturn("Mock invalid messages");
      when(mockMessages.invalidXMLEntity("&test")).thenReturn("Mock invalid messages");

      String source = "Source string";
      String target = "Target string: &mash bla bla &test";
      xmlEntityValidation.validate(source, target);

      assertThat(xmlEntityValidation.hasError(), Matchers.equalTo(true));
      assertThat(xmlEntityValidation.getError().size(), Matchers.equalTo(2));
   }
   
   @Test
   public void testWithIncompleteEntityDecimalRef()
   {
      when(mockMessages.invalidXMLEntity("&#1234")).thenReturn("Mock invalid messages");
      when(mockMessages.invalidXMLEntity("&#BC;")).thenReturn("Mock invalid messages");

      String source = "Source string";
      String target = "Target string: &#1234 bla bla &#BC;";
      xmlEntityValidation.validate(source, target);

      assertThat(xmlEntityValidation.hasError(), Matchers.equalTo(true));
      assertThat(xmlEntityValidation.getError().size(), Matchers.equalTo(2));
   }

   @Test
   public void testWithIncompleteEntityHexadecimalRef()
   {
      when(mockMessages.invalidXMLEntity("&#x1234")).thenReturn("Mock invalid messages");
      when(mockMessages.invalidXMLEntity("&#x09Z")).thenReturn("Mock invalid messages");

      String source = "Source string";
      String target = "Target string: &#x1234 bla bla &#x09Z";
      xmlEntityValidation.validate(source, target);

      assertThat(xmlEntityValidation.hasError(), Matchers.equalTo(true));
      assertThat(xmlEntityValidation.getError().size(), Matchers.equalTo(2));
   }

}
