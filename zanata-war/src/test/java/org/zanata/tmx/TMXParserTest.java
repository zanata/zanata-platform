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

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.dao.TransMemoryDAO;
import org.zanata.model.tm.TMTranslationUnit;
import org.zanata.model.tm.TransMemory;
import org.zanata.seam.SeamAutowire;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

/**
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class TMXParserTest extends ZanataDbunitJpaTest
{
   private SeamAutowire seam = SeamAutowire.instance();

   @Override
   protected void prepareDBUnitOperations()
   {
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
      TransMemoryDAO transMemoryDAO = seam.autowire(TransMemoryDAO.class);
      TransMemory tm = new TransMemory();
      tm.setSlug("new-tm");
      tm.setDescription("New test tm");
      transMemoryDAO.makePersistent(tm);

      TMXParser parser = seam.autowire(TMXParser.class);
      InputStream is = getClass().getResourceAsStream("/tmx/fedora-readme-burning-isos.tmx");

      parser.parseAndSaveTMX(is, tm);

      // Make sure everything is stored properly
      getEm().refresh(tm);
      assertThat(tm.getTranslationUnits().size(), is(168));

      for(TMTranslationUnit tu : tm.getTranslationUnits())
      {
         assertThat(tu.getTransUnitVariants().size(), greaterThan(0));
      }
   }
}
