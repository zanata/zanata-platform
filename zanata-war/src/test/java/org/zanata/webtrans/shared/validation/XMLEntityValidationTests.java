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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.webtrans.client.resources.ValidationMessages;
import org.zanata.webtrans.shared.validation.action.XmlEntityValidation;

/**
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 **/
@Test(groups = { "unit-tests" })
public class XMLEntityValidationTests
{
   // mock message strings
   private static final String MOCK_ENTITY_VALIDATOR_NAME = "test xml entity validator name";
   private static final String MOCK_ENTITY_VALIDATOR_DESCRIPTION = "test xml entity validator description";

   private XmlEntityValidation xmlEntityValidation;

   private ValidationMessages mockMessages;

   @BeforeClass
   public void mockMessages()
   {
      mockMessages = createMock(ValidationMessages.class);

      expect(mockMessages.xmlEntityValidatorName()).andReturn(MOCK_ENTITY_VALIDATOR_NAME).anyTimes();
      expect(mockMessages.xmlEntityValidatorDescription()).andReturn(MOCK_ENTITY_VALIDATOR_DESCRIPTION).anyTimes();

      replay(mockMessages);
   }

   @BeforeMethod
   public void init()
   {
      xmlEntityValidation = null;
   }

   @Test
   public void idIsSet()
   {
      xmlEntityValidation = new XmlEntityValidation(mockMessages);
      assertThat(xmlEntityValidation.getId(), is(MOCK_ENTITY_VALIDATOR_NAME));
   }

   @Test
   public void descriptionIsSet()
   {
      xmlEntityValidation = new XmlEntityValidation(mockMessages);
      assertThat(xmlEntityValidation.getDescription(), is(MOCK_ENTITY_VALIDATOR_DESCRIPTION));
   }

   @Test
   public void testNoEntity()
   {
      xmlEntityValidation = new XmlEntityValidation(mockMessages);
      String source = "Source string without xml entity";
      String target = "Target string without xml entity";
      xmlEntityValidation.validate(source, target);

      assertThat(xmlEntityValidation.hasError(), is(false));
      assertThat(xmlEntityValidation.getError().size(), is(0));
   }

   @Test
   public void testWithCompleteEntity()
   {
      xmlEntityValidation = new XmlEntityValidation(mockMessages);
      String source = "Source string";
      String target = "Target string: &mash; bla bla &test;";
      xmlEntityValidation.validate(source, target);

      assertThat(xmlEntityValidation.hasError(), is(false));
      assertThat(xmlEntityValidation.getError().size(), is(0));
   }

   @Test
   public void testWithIncompleteEntityCharRef()
   {
      mockMessages = createMock(ValidationMessages.class);

      expect(mockMessages.xmlEntityValidatorName()).andReturn(MOCK_ENTITY_VALIDATOR_NAME).anyTimes();
      expect(mockMessages.xmlEntityValidatorDescription()).andReturn(MOCK_ENTITY_VALIDATOR_DESCRIPTION).anyTimes();

      expect(mockMessages.invalidXMLEntity("&mash")).andReturn("Mock invalid messages");
      expect(mockMessages.invalidXMLEntity("&test")).andReturn("Mock invalid messages");
      replay(mockMessages);

      xmlEntityValidation = new XmlEntityValidation(mockMessages);
      String source = "Source string";
      String target = "Target string: &mash bla bla &test";
      xmlEntityValidation.validate(source, target);

      assertThat(xmlEntityValidation.hasError(), is(true));
      assertThat(xmlEntityValidation.getError().size(), is(2));
   }
   
   @Test
   public void testWithIncompleteEntityDecimalRef()
   {
      mockMessages = createMock(ValidationMessages.class);

      expect(mockMessages.xmlEntityValidatorName()).andReturn(MOCK_ENTITY_VALIDATOR_NAME).anyTimes();
      expect(mockMessages.xmlEntityValidatorDescription()).andReturn(MOCK_ENTITY_VALIDATOR_DESCRIPTION).anyTimes();

      expect(mockMessages.invalidXMLEntity("&#1234")).andReturn("Mock invalid messages");
      expect(mockMessages.invalidXMLEntity("&#BC;")).andReturn("Mock invalid messages");
      replay(mockMessages);

      xmlEntityValidation = new XmlEntityValidation(mockMessages);
      String source = "Source string";
      String target = "Target string: &#1234 bla bla &#BC;";
      xmlEntityValidation.validate(source, target);

      assertThat(xmlEntityValidation.hasError(), is(true));
      assertThat(xmlEntityValidation.getError().size(), is(2));
   }

   @Test
   public void testWithIncompleteEntityHexadecimalRef()
   {
      mockMessages = createMock(ValidationMessages.class);

      expect(mockMessages.xmlEntityValidatorName()).andReturn(MOCK_ENTITY_VALIDATOR_NAME).anyTimes();
      expect(mockMessages.xmlEntityValidatorDescription()).andReturn(MOCK_ENTITY_VALIDATOR_DESCRIPTION).anyTimes();

      expect(mockMessages.invalidXMLEntity("&#x1234")).andReturn("Mock invalid messages");
      expect(mockMessages.invalidXMLEntity("&#x09Z")).andReturn("Mock invalid messages");
      replay(mockMessages);

      xmlEntityValidation = new XmlEntityValidation(mockMessages);
      String source = "Source string";
      String target = "Target string: &#x1234 bla bla &#x09Z";
      xmlEntityValidation.validate(source, target);

      assertThat(xmlEntityValidation.hasError(), is(true));
      assertThat(xmlEntityValidation.getError().size(), is(2));
   }

   @Test
   public void testWithMissingEntity()
   {
      mockMessages = createMock(ValidationMessages.class);

      expect(mockMessages.xmlEntityValidatorName()).andReturn(MOCK_ENTITY_VALIDATOR_NAME).anyTimes();
      expect(mockMessages.xmlEntityValidatorDescription()).andReturn(MOCK_ENTITY_VALIDATOR_DESCRIPTION).anyTimes();

      ArrayList<String> missingEntities = new ArrayList<String>();
      missingEntities.add(" [&amp;] ");
      missingEntities.add(" [&RedHat;] ");
      missingEntities.add(" [&#123;] ");
      missingEntities.add(" [&#x123F;] ");
      
      expect(mockMessages.entityMissing(missingEntities)).andReturn("Mock missing messages");
      replay(mockMessages);

      xmlEntityValidation = new XmlEntityValidation(mockMessages);
      String source = "Source string &amp; and &RedHat; and &#123; and &#x123F;";
      String target = "Target string";
      xmlEntityValidation.validate(source, target);

      assertThat(xmlEntityValidation.hasError(), is(true));
      assertThat(xmlEntityValidation.getError().size(), is(1));
   }

}
