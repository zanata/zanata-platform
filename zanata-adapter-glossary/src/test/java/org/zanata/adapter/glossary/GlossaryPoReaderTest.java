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
import java.util.List;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.LocaleId;
import org.zanata.rest.dto.Glossary;

/**
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 **/
@Test(groups = "unit-tests")
public class GlossaryPoReaderTest
{
   IMocksControl control = EasyMock.createControl();

   private final File sourceFile = new File("src/test/resources/glossary/fuel_hi.po"); // 578
                                                                                       // glossary
                                                                                       // entries
   private final File sourceFile2 = new File("src/test/resources/glossary/compendium-zh_TW.po"); // 2645
                                                                                                 // glossary
                                                                                                 // entries

   private final int sourceSize1 = 578;
   private final int sourceSize2 = 2645;

   private final int BATCH_SIZE = 50;

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
   public void extractGlossaryTest() throws IOException
   {
      GlossaryPoReader reader = new GlossaryPoReader(LocaleId.EN_US, new LocaleId("hi"), BATCH_SIZE, false);

      List<Glossary> glossaries = reader.extractGlossary(sourceFile);
      Assert.assertEquals(Math.ceil(sourceSize1 / BATCH_SIZE), glossaries.size(), BATCH_SIZE);
      Assert.assertEquals(BATCH_SIZE, glossaries.get(0).getGlossaryEntries().size());
      Assert.assertEquals(BATCH_SIZE, glossaries.get(1).getGlossaryEntries().size());

      Assert.assertEquals(sourceSize1 % BATCH_SIZE, glossaries.get(glossaries.size() - 1).getGlossaryEntries().size());

   }

   @Test
   public void glossaryBatchTest() throws IOException
   {
      GlossaryPoReader reader = new GlossaryPoReader(LocaleId.EN_US, new LocaleId("zh-Hants"), BATCH_SIZE, false);
      List<Glossary> glossaries = reader.extractGlossary(sourceFile2);
      Assert.assertEquals(Math.ceil(sourceSize2 / BATCH_SIZE), glossaries.size(), BATCH_SIZE);
      Assert.assertEquals(BATCH_SIZE, glossaries.get(0).getGlossaryEntries().size());
      Assert.assertEquals(BATCH_SIZE, glossaries.get(1).getGlossaryEntries().size());

      Assert.assertEquals(sourceSize2 % BATCH_SIZE, glossaries.get(glossaries.size() - 1).getGlossaryEntries().size());

   }
}
