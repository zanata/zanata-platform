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
import org.junit.Test;
import org.testng.annotations.BeforeTest;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

/**
 * Unit tests for the {@link AutowireContexts} class.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class AutowireContextsTest
{
   @BeforeTest
   public void newSessionBeforeTest()
   {
      AutowireContexts.getInstance().newSession();
   }

   @Test
   public void putComponentInRequestScope()
   {
      String val = "A string!";
      String name = "test";
      AutowireContexts.getInstance().putValue(name, AutowireContexts.ContextType.Request, val);

      // Make sure it's there
      assertThat((String) AutowireContexts.getInstance().getValue(name), is(val));
   }

   @Test
   public void putComponentInTwoScopes()
   {
      String requestVal = "A Request String";
      String sessionVal = "A Session String";
      String name = "comp";

      AutowireContexts.getInstance().putValue(name, AutowireContexts.ContextType.Request, requestVal);
      AutowireContexts.getInstance().putValue(name, AutowireContexts.ContextType.Session, sessionVal);

      // The request one must be returned
      assertThat((String) AutowireContexts.getInstance().getValue(name), is(requestVal));
      // New Request
      AutowireContexts.getInstance().newRequest();
      // Now the session one is returned
      assertThat((String) AutowireContexts.getInstance().getValue(name), is(sessionVal));
   }

   @Test
   public void noComponent()
   {
      assertThat((String) AutowireContexts.getInstance().getValue("not-set"), nullValue());
   }
}
