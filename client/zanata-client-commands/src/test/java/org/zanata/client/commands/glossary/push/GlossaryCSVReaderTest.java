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
package org.zanata.client.commands.glossary.push;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.zanata.client.commands.glossary.push.GlossaryCSVReader;
import org.zanata.client.commands.glossary.push.GlossaryPushOptions;
import org.zanata.client.config.LocaleList;
import org.zanata.client.config.LocaleMapping;
import org.zanata.rest.dto.Glossary;
import org.zanata.rest.dto.GlossaryEntry;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
@Test(groups = "unit-tests")
public class GlossaryCSVReaderTest
{
   LocaleList locales = new LocaleList();

   GlossaryCSVReader reader = new GlossaryCSVReader();

   IMocksControl control = EasyMock.createControl();
   GlossaryPushOptions mockPushOption;

   @BeforeMethod
   void beforeMethod()
   {
      control.reset();
   }

   @BeforeTest
   public void prepare()
   {
      locales.add(new LocaleMapping("hi"));
   }

   <T> T createMock(String name, Class<T> toMock)
   {
      T mock = control.createMock(name, toMock);
      return mock;
   }

   @Test
   public void extractGlossaryTest1() throws IOException
   {
      File sourceFile = new File("src/test/resources/glossary/translate1.csv");
      List<String> commentHeaders = new ArrayList<String>();
      commentHeaders.add("pos");
      commentHeaders.add("description");

      mockPushOption = createMock("mockPushGlossaryOption", GlossaryPushOptions.class);
      EasyMock.expect(mockPushOption.getSourceLang()).andReturn("en-US").anyTimes();
      EasyMock.expect(mockPushOption.getTransLang()).andReturn("hi").anyTimes();
      EasyMock.expect(mockPushOption.getLocales()).andReturn(locales).anyTimes();
      EasyMock.expect(mockPushOption.getTreatSourceCommentsAsTarget()).andReturn(false).anyTimes();
      EasyMock.expect(mockPushOption.getCommentCols()).andReturn(commentHeaders).anyTimes();

      reader.setOpts(mockPushOption);
      EasyMock.replay(mockPushOption);

      Glossary glossary = reader.extractGlossary(sourceFile);
      // System.out.println(glossary);
      Assert.assertNotNull(glossary);
      Assert.assertEquals(2, glossary.getGlossaryEntries().size());

      for (GlossaryEntry entry : glossary.getGlossaryEntries())
      {
         Assert.assertEquals(3, entry.getGlossaryTerms().size());
      }

   }
   
   @Test
   public void extractGlossaryTest2() throws IOException
   {
      File sourceFile = new File("src/test/resources/glossary/translate2.csv");
      List<String> commentHeaders = new ArrayList<String>();
      commentHeaders.add("description1");
      commentHeaders.add("description2");
      commentHeaders.add("description3"); // this will be ignored

      mockPushOption = createMock("mockPushGlossaryOption", GlossaryPushOptions.class);
      EasyMock.expect(mockPushOption.getSourceLang()).andReturn("en-US").anyTimes();
      EasyMock.expect(mockPushOption.getTransLang()).andReturn("hi").anyTimes();
      EasyMock.expect(mockPushOption.getLocales()).andReturn(locales).anyTimes();
      EasyMock.expect(mockPushOption.getTreatSourceCommentsAsTarget()).andReturn(false).anyTimes();
      EasyMock.expect(mockPushOption.getCommentCols()).andReturn(commentHeaders).anyTimes();

      reader.setOpts(mockPushOption);
      EasyMock.replay(mockPushOption);

      Glossary glossary = reader.extractGlossary(sourceFile);
      // System.out.println(glossary);
      Assert.assertNotNull(glossary);
      Assert.assertEquals(2, glossary.getGlossaryEntries().size());

      for (GlossaryEntry entry : glossary.getGlossaryEntries())
      {
         Assert.assertEquals(3, entry.getGlossaryTerms().size());
      }

   }
}


 