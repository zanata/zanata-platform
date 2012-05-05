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
package org.zanata.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.LocaleId;
import org.zanata.dao.LocaleDAO;
import org.zanata.model.HLocale;


@Test(groups = { "business-tests" })
public class LocaleServiceImplTest
{
   private LocaleServiceImpl testLocaleServiceImpl;
   private LocaleDAO mockDAO;


   @BeforeMethod(firstTimeOnly = true)
   public void setup()
   {
      this.testLocaleServiceImpl = new LocaleServiceImpl();
      this.mockDAO = EasyMock.createMock(LocaleDAO.class);
      this.testLocaleServiceImpl.setLocaleDAO(mockDAO);
   }


   @Test
   public void testGetAllJavaLanguages() throws Exception
   {
      List<LocaleId> loc = this.testLocaleServiceImpl.getAllJavaLanguages();
      StringBuilder st = new StringBuilder("");
      for (LocaleId localeId : loc)
      {
         st.append(localeId.getId() + ",");
      }
      System.out.println(st);

      Assert.assertTrue(loc.contains(LocaleId.DE));
      Assert.assertTrue(loc.contains(LocaleId.EN));
      Assert.assertTrue(loc.contains(LocaleId.EN_US));
      Assert.assertTrue(loc.contains(LocaleId.ES));
      Assert.assertTrue(loc.contains(LocaleId.FR));
   }

   @Test
   public void testGetAllSupportedLanguages()
   {
      List<HLocale> lan = new ArrayList<HLocale>();
      lan.add(new HLocale(new LocaleId("as-IN")));
      lan.add(new HLocale(new LocaleId("pt-BR")));
      EasyMock.expect(mockDAO.findAll()).andReturn(lan);
      EasyMock.replay(mockDAO);
      List<HLocale> sup = this.testLocaleServiceImpl.getAllLocales();
      Assert.assertEquals(sup.size(), 2);
      String loc1 = sup.get(0).getLocaleId().getId();
      Assert.assertEquals(loc1, "as-IN");
      String loc2 = sup.get(1).getLocaleId().getId();
      Assert.assertEquals(loc2, "pt-BR");
      EasyMock.verify(mockDAO);
   }
}
