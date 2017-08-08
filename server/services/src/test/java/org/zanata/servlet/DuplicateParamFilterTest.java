/*
 * Copyright 2016, Red Hat, Inc. and individual contributors
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
package org.zanata.servlet;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.zanata.servlet.DuplicateParamFilter.FilteredRequest.deduplicate;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public class DuplicateParamFilterTest {
    @Test
    public void testDeduplicateEmpty() {
        String[] vals = new String[0];
        assertThat(deduplicate(vals)).isEmpty();
    }

    @Test
    public void testDeduplicateOneString() {
        String[] vals = new String[] {"Testing"};
        assertThat(deduplicate(vals)).containsExactly(vals);
    }

    @Test
    public void testDeduplicateTwoIdentical() {
        String[] vals = new String[] {"Testing", "Testing"};
        assertThat(deduplicate(vals)).containsExactly("Testing");
    }

    @Test
    public void testDeduplicateMixed() {
        String[] vals = new String[] {"a", "b", "a", "c", "c", "", "", "d"};
        assertThat(deduplicate(vals)).containsExactly("a", "b", "c", "", "d");
    }

}
