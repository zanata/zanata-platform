/*
 * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
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

package org.zanata.model;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class HTextFlowTest {
    @Test
    public void testSetContents() throws Exception {
        HTextFlow textFlow = new HTextFlow();

        textFlow.setContents("a", "b");
        assertThat(textFlow.getContents()).contains("a", "b");

        textFlow.setContents("a");
        assertThat(textFlow.getContents()).contains("a");
        // check that content1 is nulled out (after having been non-null
        // earlier)
        assertThat(textFlow.getContent1()).isNull();

        // set original value
        textFlow.setContents("a", "b");
        assertThat(textFlow.getContents()).contains("a", "b");

        // set same value
        textFlow.setContents("a", "b");
        assertThat(textFlow.getContents()).contains("a", "b");
    }
}
