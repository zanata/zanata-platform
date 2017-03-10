/*
 * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.util;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import static com.google.common.base.Throwables.getRootCause;

/**
 * @author Sean Flanigan
 *         <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public class TestLogger extends RunListener {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(TestLogger.class);

    @Override
    public void testStarted(Description description) throws Exception {
        log.info("Test starting: {}", description);
    }

    @Override
    public void testFinished(Description description) throws Exception {
        log.info("Test finished: {}", description);
    }

    @Override
    public void testIgnored(Description description) throws Exception {
        log.error("Test IGNORED: {}", description);
    }

    @Override
    public void testFailure(Failure failure) throws Exception {
        Throwable e = failure.getException();
        log.error("Test FAILED: " + failure, getRootCause(e));
    }

    @Override
    public void testAssumptionFailure(Failure failure) {
        log.error("Test FAILED ASSUMPTION: " + failure, failure.getException());
    }
}
