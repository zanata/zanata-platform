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
package org.zanata.config;

import org.dbunit.operation.DatabaseOperation;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.seam.SeamAutowire;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

/**
 * Tests for the Database backed config store.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class DatabaseBackedConfigTest extends ZanataDbunitJpaTest
{
   private DatabaseBackedConfig databaseBackedConfig;

   @BeforeMethod
   public void prepare()
   {
      databaseBackedConfig = SeamAutowire.instance()
                                         .reset()
                                         .use("session", getSession())
                                         .autowire(DatabaseBackedConfig.class);
   }

   @Override
   protected void prepareDBUnitOperations()
   {
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/ApplicationConfigurationData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));

      afterTestOperations.add(new DataSetOperation("org/zanata/test/model/ClearAllTables.dbunit.xml", DatabaseOperation.DELETE_ALL));
   }

   @Test
   public void getHomeContent()
   {
      assertThat(databaseBackedConfig.getHomeContent(), equalTo("This is the home content"));
   }

   @Test
   public void getNonExistentValue() throws Exception
   {
      assertThat(databaseBackedConfig.getConfigValue("I.dont.exist"), nullValue());
   }

   @Test
   public void reset()
   {
      String original = databaseBackedConfig.getHelpContent();
      databaseBackedConfig.reset();
      assertThat(databaseBackedConfig.getHelpContent(), equalTo(original));
   }
}
