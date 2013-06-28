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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteStreamHandler;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.jboss.seam.annotations.Name;
import org.zanata.exception.VirusDetectedException;

import com.google.common.base.Stopwatch;

/**
 * <code>VirusScanner</code> scans files using ClamAV's <code>clamdscan</code> command if available.
 * <code>clamdscan</code> depends on the <code>clamd</code> service, so this class will throw an
 * exception if <code>clamdscan</code> is found but <code>clamd</code> is not running.
 * <p>
 * By default, <code>VirusScanner</code> looks for <code>clamdscan</code> on the system path, but
 * this can be overridden with the <code>virusScanner</code> system property, either
 * with a full path such as <code>/usr/bin/clamdscan</code>, or another scanner entirely.  If the
 * system property has been set, a failure to launch the scanner will
 * cause an exception.  (If it has not been set, an error will be logged
 * but that is all.)
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
@Data
@Name("virusScanner")
@Slf4j
public class VirusScanner
{
   private final String scanner;
   private final boolean disabled;
   private final boolean scannerSet;

   public VirusScanner()
   {
      // if the system property is empty or null, we try to use
      // clamdscan, but we don't throw an exception if we can't find it.
      String scannerProperty = System.getProperty("virusScanner"); // clamscan would work too, but takes ~15 seconds
      this.disabled = "DISABLED".equals(scannerProperty);
      if (scannerProperty == null || scannerProperty.isEmpty())
      {
         this.scanner = "clamdscan";
         this.scannerSet = false;
      }
      else
      {
         this.scanner = scannerProperty;
         this.scannerSet = true;
      }
   }

   /**
    * Scans the specified file by calling out to ClamAV.
    * <p>
    * The current implementation looks for clamdscan on the system path, but
    * merely logs an error if it can't be found (or if clamd is not running),
    * rather than rejecting the file.
    * <p>
    * Note that the caller is responsible for deleting the file.
    * @param file
    * @param name
    * @throws VirusDetectedException if a virus is detected
    * @throws RuntimeException if something else goes wrong (eg can't execute virus scanner)
    */
   public void scan(File file, String name) throws VirusDetectedException
   {
      if (disabled)
      {
         log.debug("file not scanned: {}", name);
         return;
      }
      Stopwatch stop = new Stopwatch().start();
      CommandLine cmdLine = new CommandLine(scanner);
      if (scanner.matches(".*clamd?scan"))
      {
         // clam-specific option; just makes the error messages less verbose
         cmdLine.addArgument("--no-summary");
      }
      cmdLine.addArgument(file.getPath());
      DefaultExecutor executor = new DefaultExecutor();
      ExecuteWatchdog watchdog = new ExecuteWatchdog(60000);
      executor.setWatchdog(watchdog);
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      ExecuteStreamHandler psh = new PumpStreamHandler(output);
      executor.setStreamHandler(psh);

      // We want to handle the following clamav exit values directly.
      // If another scanner is used, we may not use the ideal
      // exception when something goes wrong, but as long as zero
      // still means "no virus" it should be okay.
      executor.setExitValues(new int[] {0, 1, 2});
      try
      {
         int exitValue = executor.execute(cmdLine);
         log.debug("{} to scan file: '{}'", stop, name);
         switch(exitValue)
         {
         case 0:
            log.info("{} says file '{}' is clean: {}", scanner, name, output);
            return;
         case 1:
            throw new VirusDetectedException(scanner + " detected virus: " + output);
         case 2:
         default:
            // This can happen if clamdscan is found, but the clamd service is not running.
            String msg = scanner + " returned error scanning file '"+name+"': " + output + "\nPlease ensure clamd service is running.";
            throw new RuntimeException(msg);
         }
      }
      catch (IOException e)
      {
         // perhaps the antivirus executable was not found...
         // we omit the stack exception, because it tends to be uninteresting in this case
         String msg = "error executing " + scanner + ", unable to scan file '"+name+"' for viruses: " + e.getMessage();
         if (scannerSet)
         {
            throw new RuntimeException(msg);
         }
         log.error(msg);
      }
   }

}
