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
package net.openl10n.flies.util;

import net.openl10n.flies.util.Base64UrlSafe;

import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = { "unit-tests" })
public class Base64UrlSafeTest
{
   @Test
   public void test()
   {
      String var = "hding;helen.ding.uq@gmail.com";
      String enVar = Base64UrlSafe.encode(var);
      System.out.println("encode:" + enVar);
      String deVar = Base64UrlSafe.decode(enVar);
      System.out.println("decode:" + deVar);
      Assert.assertEquals(var, deVar);
   }

}
