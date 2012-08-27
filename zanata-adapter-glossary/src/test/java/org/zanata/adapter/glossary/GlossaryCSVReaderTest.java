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
package org.zanata.adapter.glossary;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
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
   GlossaryCSVReader reader;

   IMocksControl control = EasyMock.createControl();

   @BeforeMethod
   void beforeMethod()
   {
      control.reset();
   }

   <T> T createMock(String name, Class<T> toMock)
   {
      T mock = control.createMock(name, toMock);
      return mock;
   }

   @Test
   public void extractGlossaryTest1() throws IOException
   {
      List<String> commentHeaders = new ArrayList<String>();
      commentHeaders.add("pos");
      commentHeaders.add("description");
      
      GlossaryCSVReader reader = new GlossaryCSVReader(300, commentHeaders);
      
      File sourceFile = new File("src/test/resources/glossary/translate1.csv");
      
      List<Glossary> glossaries = reader.extractGlossary(sourceFile);
      // System.out.println(glossary);
      Assert.assertEquals(1, glossaries.size());

      Assert.assertEquals(2, glossaries.get(0).getGlossaryEntries().size());

      for (GlossaryEntry entry : glossaries.get(0).getGlossaryEntries())
      {
         Assert.assertEquals(3, entry.getGlossaryTerms().size());
      }

   }
   
   @Test
   public void extractGlossaryTest2() throws IOException
   {
      List<String> commentHeaders = new ArrayList<String>();
      commentHeaders.add("description1");
      commentHeaders.add("description2");
      commentHeaders.add("description3"); // this will be ignored
      
      GlossaryCSVReader reader = new GlossaryCSVReader(300, commentHeaders);
      
      
      File sourceFile = new File("src/test/resources/glossary/translate2.csv");
      
      List<Glossary> glossaries = reader.extractGlossary(sourceFile);
      // System.out.println(glossary);
      Assert.assertEquals(1, glossaries.size());

      Assert.assertEquals(2, glossaries.get(0).getGlossaryEntries().size());

      for (GlossaryEntry entry : glossaries.get(0).getGlossaryEntries())
      {
         Assert.assertEquals(3, entry.getGlossaryTerms().size());
      }

   }
}


 