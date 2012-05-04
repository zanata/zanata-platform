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
package org.zanata.seam;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.jboss.seam.Component;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.dao.ProjectDAO;

/**
 * Tests for the {@link SeamAutowire} component.
 * Also useful as a template for other Autowire tests.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class SeamAutowireTest extends ZanataDbunitJpaTest
{
   @Override
   protected void prepareDBUnitOperations()
   {
   }

   @BeforeTest
   public void resetSeamAutowire()
   {
      SeamAutowire.instance().reset();
   }

   @Test
   public void autowireSession()
   {
      ProjectDAO dao = SeamAutowire.instance().ignoreNonResolvable().use("session", getSession()).autowire(ProjectDAO.class);

      int t = dao.getTotalProjectCount();
      System.out.println("Total Projects: " + t);
   }

   @Test
   public void autowireProvided()
   {
      ProjectDAO dao = new ProjectDAO();
      dao = SeamAutowire.instance().ignoreNonResolvable().use("session", getSession()).autowire(dao);

      int t = dao.getTotalProjectCount();
      System.out.println("Total Projects: " + t);
   }

   @Test
   public void testComponentInvocation()
   {
      SeamAutowire.instance().use("component", "This is the component!");

      String val = (String)Component.getInstance("component");

      MatcherAssert.assertThat(val, Matchers.is("This is the component!"));
   }
}
