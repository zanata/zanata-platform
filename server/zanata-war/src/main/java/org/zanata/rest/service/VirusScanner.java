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
import java.io.OutputStream;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteStreamHandler;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import org.zanata.exception.VirusDetectedException;
import com.google.common.base.Stopwatch;

/**
 * <code>VirusScanner</code> scans files using ClamAV's <code>clamdscan</code>
 * command if available, or a different scanner if configured.
 * <p>
 * By default, <code>VirusScanner</code> looks for <code>clamdscan</code> on the
 * system path, but this can be overridden with the system property
 * <code>virusScanner</code>, either with a full path such as
 * <code>/usr/bin/clamdscan</code>, or the name of another scanner entirely.
 * <code>clamdscan</code> depends on the <code>clamd</code> service, so this
 * class will throw a <code>RuntimeException</code> if <code>clamdscan</code> is
 * found but <code>clamd</code> is not running.
 * <p>
 * If the system property has been set, a failure to launch the scanner will
 * cause an exception. (If it has not been set, an error will be logged but that
 * is all.)
 *
 * @author Sean Flanigan
 *         <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@Named("virusScanner")
@RequestScoped
public class VirusScanner {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(VirusScanner.class);

    private static final boolean DISABLED;
    private static final boolean SCANNER_SET;
    private static final String SCANNER_NAME;
    private static final boolean USING_CLAM;
    // clamdscan-specific options:
    private static final String[] CLAMDSCAN_ARGS = {
            // just makes the error messages less verbose
            "--no-summary",
            // This ensures that clamd can scan the file regardless of
            // permissions or security context (since clamdscan actually
            // accesses the file, not clamd):
            "--stream" };
    static {
        // If the system property is empty or null, we try to use
        // clamdscan, but we don't throw an exception if we can't find it.
        // clamscan would work too, but takes ~15 seconds
        String scannerProperty = System.getProperty("virusScanner");
        DISABLED = "DISABLED".equals(scannerProperty);
        if (DISABLED) {
            log.warn("virus scanning disabled");
            SCANNER_SET = true;
            SCANNER_NAME = scannerProperty;
            USING_CLAM = false;
        } else {
            if (scannerProperty == null || scannerProperty.isEmpty()) {
                SCANNER_NAME = "clamdscan";
                SCANNER_SET = false;
                log.info(
                        "defaulting to clamdscan (system property \'virusScanner\' is null or empty)");
                log.warn(
                        "failure to run scanner will be logged but otherwise ignored");
            } else {
                SCANNER_NAME = scannerProperty;
                SCANNER_SET = true;
                log.info(
                        "property \'virusScanner\' found: failure to run scanner will be treated as an error");
            }
            USING_CLAM = SCANNER_NAME.toLowerCase().contains("clamdscan");
            if (USING_CLAM) {
                log.info("scanning with command \'{}\', using arguments: {}",
                        SCANNER_NAME, CLAMDSCAN_ARGS);
            } else {
                log.info("scanning with command \'{}\'", SCANNER_NAME);
            }
        }
    }

    public static boolean isDisabled() {
        return DISABLED;
    }

    public static boolean isScannerSet() {
        return SCANNER_SET;
    }

    /**
     * Scans the specified file by calling out to ClamAV, unless disabled by
     * system property.
     * <p>
     * Note 1: the file will be made world readable, so that clamd can access
     * it.
     * <p>
     * Note 2: the caller is responsible for deleting the file.
     *
     * @param file
     *            file to be scanned (probably a temp file)
     * @param documentName
     *            human-friendly name for the file
     * @throws VirusDetectedException
     *             if the scanner detects a virus
     * @throws RuntimeException
     *             if something else goes wrong (eg can't execute virus scanner)
     */
    public void scan(File file, String documentName)
            throws VirusDetectedException {
        if (DISABLED) {
            log.debug("file not scanned: {}", documentName);
        } else {
            doScan(file, documentName);
        }
    }

    private void doScan(File file, String documentName) {
        Stopwatch stop = Stopwatch.createStarted();
        CommandLine cmdLine = buildCommandLine(file);
        ByteArrayOutputStream scannerOutput = new ByteArrayOutputStream();
        Executor executor = buildExecutor(scannerOutput);
        try {
            int exitValue = executor.execute(cmdLine);
            log.debug("{} to scan file: \'{}\'", stop, documentName);
            handleResult(exitValue, documentName, scannerOutput);
        } catch (IOException e) {
            // perhaps the antivirus executable was not found...
            // we omit the stack exception, because it tends to be uninteresting
            // in this case
            String msg = "error executing " + SCANNER_NAME
                    + ", unable to scan file \'" + documentName
                    + "\' for viruses: " + e.getMessage();
            if (SCANNER_SET) {
                throw new RuntimeException(msg);
            }
            log.error(msg);
        }
    }

    private void handleResult(int exitValue, String documentName,
            Object output) {
        // The following return codes are taken from the clamdscan manpage.
        // If another scanner is used, we may not use the ideal
        // exception when something goes wrong, but as long as zero
        // still means "no virus" it should be okay.
        final int noVirusFound = 0;
        final int virusFound = 1;
        final int someErrorOccurred = 2;
        switch (exitValue) {
        case noVirusFound:
            log.info("{} says file \'{}\' is clean: {}", SCANNER_NAME,
                    documentName, output);
            return;

        case virusFound:
            throw new VirusDetectedException(
                    SCANNER_NAME + " detected virus: " + output);

        case someErrorOccurred:

        default:
            // This can happen if clamdscan is found, but the clamd service is
            // not running.
            String msg = SCANNER_NAME + " returned error scanning file \'"
                    + documentName + "\': " + output + (USING_CLAM
                            ? "\nPlease ensure clamd service is running." : "");
            throw new RuntimeException(msg);

        }
    }

    private CommandLine buildCommandLine(File file) {
        CommandLine cmdLine = new CommandLine(SCANNER_NAME);
        if (USING_CLAM) {
            cmdLine.addArguments(CLAMDSCAN_ARGS, false);
        }
        cmdLine.addArgument(file.getPath(), false);
        return cmdLine;
    }

    /**
     * Builds an Executor which will output to the specified OutputStream.
     * <p>
     * The Executor will be configured to return exit values as int, rather than
     * throwing ExecuteException.
     *
     * @param output
     * @return a configured Executor
     */
    private Executor buildExecutor(OutputStream output) {
        DefaultExecutor executor = new DefaultExecutor();
        ExecuteWatchdog watchdog = new ExecuteWatchdog(60000);
        executor.setWatchdog(watchdog);
        ExecuteStreamHandler psh = new PumpStreamHandler(output);
        executor.setStreamHandler(psh);
        // We want to handle all exit values directly (not as ExecuteException).
        executor.setExitValues(null);
        return executor;
    }
}
