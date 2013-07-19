/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.tmx;

import java.io.InputStream;
import java.util.Calendar;

import javax.xml.stream.XMLStreamException;

import org.dbunit.operation.DatabaseOperation;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.dao.TransMemoryDAO;
import org.zanata.model.tm.TMMetadataType;
import org.zanata.model.tm.TMTranslationUnit;
import org.zanata.model.tm.TransMemory;
import org.zanata.seam.SeamAutowire;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class TMXParserTest extends ZanataDbunitJpaTest
{
   private SeamAutowire seam = SeamAutowire.instance();

   @Override
   protected void prepareDBUnitOperations()
   {
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/ClearAllTables.dbunit.xml", DatabaseOperation.DELETE_ALL));
      afterTestOperations.add(new DataSetOperation("org/zanata/test/model/ClearAllTables.dbunit.xml", DatabaseOperation.DELETE_ALL));
   }

   @BeforeMethod
   public void initializeSeam()
   {
      seam.reset()
          .ignoreNonResolvable()
          .use("entityManager", getEm())
          .use("session", getSession());
   }

   @Test
   public void parseTMX() throws Exception
   {
      // Create a TM
      TransMemory tm = parseAndSaveTMFromFile("/tmx/default-valid-tm.tmx");

      // Make sure everything is stored properly
      getEm().flush();
      getEm().refresh(tm);
      assertThat(tm.getTranslationUnits().size(), is(4));

      // Dates were modified to match the TM header in the file
      Calendar cal = Calendar.getInstance();
      cal.setTime(tm.getCreationDate());
      assertThat(cal.get(Calendar.YEAR), is(2013));
      assertThat(cal.get(Calendar.MONTH), is(4));
      assertThat(cal.get(Calendar.DATE), is(10));

      assertThat(tm.getSourceLanguage(), equalTo("en"));

      // TM metadata
      assertThat(tm.getMetadata().size(), greaterThan(0));
      assertThat(tm.getMetadata().get(TMMetadataType.TMX14), notNullValue());

      // Translation Units
      for(TMTranslationUnit tu : tm.getTranslationUnits())
      {
         assertThat(tu.getTransUnitVariants().size(), greaterThan(0));
      }
   }

   private TransMemory parseAndSaveTMFromFile(String file) throws XMLStreamException
   {
      TransMemoryDAO transMemoryDAO = seam.autowire(TransMemoryDAO.class);
      TransMemory tm = new TransMemory();
      tm.setSlug("new-tm");
      tm.setDescription("New test tm");
      transMemoryDAO.makePersistent(tm);

      return parseAndSaveTMFromFile(tm, file);
   }

   private TransMemory parseAndSaveTMFromFile(TransMemory tm, String file) throws XMLStreamException
   {
      TMXParser parser = seam.autowire(TMXParser.class);
      InputStream is = getClass().getResourceAsStream(file);

      parser.parseAndSaveTMX(is, tm);
      return tm;
   }

   @Test(expectedExceptions = RuntimeException.class)
   public void undiscernibleSourceLang() throws Exception
   {
      // Create a TM
      parseAndSaveTMFromFile("/tmx/invalid-tmx-no-discernible-srclang.xml");
   }

   @Test
   public void mergeSameTM() throws Exception
   {
      // Initial load
      TransMemory tm = parseAndSaveTMFromFile("/tmx/default-valid-tm.tmx");

      // Make sure everything is stored properly
      getEm().flush();
      getEm().refresh(tm);
      assertThat(tm.getTranslationUnits().size(), is(4));

      // Second load (should yield the same result)
      parseAndSaveTMFromFile(tm, "/tmx/default-valid-tm.tmx");

      getEm().flush();
      getEm().refresh(tm);
      assertThat(tm.getTranslationUnits().size(), is(4));
   }

   @Test
   public void mergeComplimentaryTM() throws Exception
   {
      // Initial load
      TransMemory tm = parseAndSaveTMFromFile("/tmx/default-valid-tm.tmx");

      // Make sure everything is stored properly
      getEm().flush();
      getEm().refresh(tm);
      assertThat(tm.getTranslationUnits().size(), is(4));

      // Second load (should add all new tuids)
      parseAndSaveTMFromFile(tm, "/tmx/valid-tm-with-tuids.tmx");

      getEm().flush();
      getEm().refresh(tm);
      assertThat(tm.getTranslationUnits().size(), is(8));
   }
}
