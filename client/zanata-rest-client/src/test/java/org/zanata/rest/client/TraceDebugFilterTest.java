/*
 * Copyright 2017, Red Hat, Inc. and individual contributors
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
package org.zanata.rest.client;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.slf4j.Logger;

import static org.mockito.Mockito.verify;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public class TraceDebugFilterTest {
    @Rule
    public MockitoRule rule = MockitoJUnit.rule();
    @Mock
    private Logger log;

    @Test
    public void testInfoLog() {
        TraceDebugFilter filter = new TraceDebugFilter(true, log);
        // should be logged as INFO
        filter.log("info message including {}");
        verify(log).info("info message including {}");
    }

    @Test
    public void testTraceLog() {
        TraceDebugFilter filter = new TraceDebugFilter(false, log);
        // should be logged as TRACE
        filter.log("trace message including {}");
        verify(log).trace("trace message including {}");
    }
}
