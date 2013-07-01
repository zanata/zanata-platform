/*
 * Copyright 2013, Red Hat, Inc. and individual contributors
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
package org.zanata.rest.service;

import java.io.File;
import java.io.IOException;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.reporters.Files;
import org.zanata.exception.VirusDetectedException;

/**
 * @author Sean Flanigan, <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@Test(groups = { "unit-tests" })
public class VirusScannerTest
{
   private VirusScanner virusScanner = new VirusScanner();

   @BeforeClass
   private void checkDisabled()
   {
      if (virusScanner.isDisabled())
      {
         throw new SkipException("virusScanner is DISABLED");
      }
   }

   public void virusScanSafeFile() throws IOException
   {
      String data = "This is a simple test file, which doesn't contain a virus.\n";
      File file = File.createTempFile("data", ".tmp");
      try
      {
         Files.writeFile(data, file);
         virusScanner.scan(file, "safe");
      }
      finally
      {
         file.delete();
      }
   }

   public void virusScanEicarFile() throws IOException
   {
      String eicar = generateFakeVirus();
      File file = File.createTempFile("eicar", ".tmp");
      try
      {
         Files.writeFile(eicar, file);
         virusScanner.scan(file, "eicar");
         virusNotFound("failed to detect eicar test signature");
      }
      catch (VirusDetectedException e)
      {
         // expected (unless virusScanner is DISABLED or blank)
      }
      finally
      {
         file.delete();
      }
   }

   private String generateFakeVirus()
   {
      // See http://www.eicar.org/86-0-Intended-use.html
      // We use ROT13 so that virus scanners won't think this source file
      // is "infected" with EICAR.
      String eicar = rot13("K5B!C%@NC[4\\CMK54(C^)7PP)7}$RVPNE-FGNAQNEQ-NAGVIVEHF-GRFG-SVYR!$U+U*\n");
      return eicar;
   }

   private void virusNotFound(String msg)
   {
      if (virusScanner.isScannerSet())
      {
         Assert.fail(msg);
      }
      else
      {
         throw new SkipException("virusScanner is blank and clamdscan not found");
      }
   }

   private static String rot13(String s)
   {
      StringBuilder sb = new StringBuilder(s.length());
      for (int i = 0; i < s.length(); i++)
      {
         char c = s.charAt(i);
         char r;
         if ((c >= 'a' && c <= 'm') || (c >= 'A' && c <= 'M'))
         {
            r = (char) (c + 13);
         }
         else if ((c >= 'n' && c <= 'z') || (c >= 'N' && c <= 'Z'))
         {
            r = (char) (c - 13);
         }
         else
         {
            r = c;
         }
         sb.append(r);
      }
      return sb.toString();
   }
}
