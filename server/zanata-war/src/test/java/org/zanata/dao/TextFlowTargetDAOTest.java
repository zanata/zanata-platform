/*
 * Copyright 2010, Red Hat, Inc. and individual contributors
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
package org.zanata.dao;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

import org.dbunit.operation.DatabaseOperation;
import org.easymock.EasyMock;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.common.ContentState;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;

@Test(groups = { "jpa-tests" })
public class TextFlowTargetDAOTest extends ZanataDbunitJpaTest
{

   private TextFlowTargetDAO textFlowTargetDAO;
   
   
   @Override
   protected void prepareDBUnitOperations()
   {
      //      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/ClearAllTables.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/ProjectsData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/TextFlowTestData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/LocalesData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));      
   }
   
   @BeforeMethod(firstTimeOnly = true)
   public void setup()
   {
      textFlowTargetDAO = new TextFlowTargetDAO((Session) getEm().getDelegate());
   }
   
   // FIXME broken test
   @Test(enabled = false)
   public void findLatestEquivalentTranslation() throws Exception 
   {
      HDocument doc = (HDocument) getSession().get(HDocument.class, 1L);
      HLocale hLocale = (HLocale) getSession().get(HLocale.class, 1L);
      
      ScrollableResults results = this.textFlowTargetDAO.findLatestEquivalentTranslations(doc, hLocale);
      
      int rows = 0;
      
      // TODO Have more comprehensive results
      while(results.next())
      {
         final HTextFlowTarget oldTFT = (HTextFlowTarget)results.get(0);
         final HTextFlow textFlow = (HTextFlow)results.get(1);
         
         // make sure that each result is valid
         assertThat(oldTFT.getTextFlow(), equalTo(textFlow));
         assertThat(oldTFT.getLocale().getId(), equalTo(hLocale.getId()));
         assertThat(textFlow.getDocument().getDocId(), is("/my/path/document.txt"));
         assertThat(oldTFT.getState(), is(ContentState.Approved));
         
         rows++;
      }
      
      // make sure there where results
      assertThat(rows, greaterThan(0));
   }
   
}
