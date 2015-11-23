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
package org.zanata.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

/**
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class AsyncConfigTest {

    private static final String THREAD_POOL_SIZE_VAL = "15";

    private AsyncConfig asyncConfig;
    @Mock private SystemPropertyConfigStore configStore;

    @Before
    public void beforeTest() {
        initMocks(this);
        asyncConfig = new AsyncConfig(configStore);
    }

    @Test
    public void testNumberOfThreads() {
        when(configStore.get(AsyncConfig.THREAD_POOL_SIZE)).thenReturn(
                THREAD_POOL_SIZE_VAL);

        assertThat(asyncConfig.getThreadPoolSize()).isEqualTo(15);
    }

    @Test
    public void testNumberOfThreadsDefault() {
        when(configStore.get(AsyncConfig.THREAD_POOL_SIZE)).thenReturn(null);

        assertThat(asyncConfig.getThreadPoolSize()).isEqualTo(10);
    }
}
