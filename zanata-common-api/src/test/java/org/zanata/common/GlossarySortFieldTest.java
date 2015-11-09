/*
 * Copyright 2015, Red Hat, Inc. and individual contributors
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

package org.zanata.common;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

/**
 * @author Alex Eng <a href="aeng@redhat.com">aeng@redhat.com</a>
 */
public class GlossarySortFieldTest {

    @Test
    public void getByFieldTest() {
        String field = "src_content";
        GlossarySortField sortField = GlossarySortField.getByField(field);
        assertThat(sortField, notNullValue());
    }

    @Test
    public void getByFieldTest2() {
        String field = "-src_content";
        GlossarySortField sortField = GlossarySortField.getByField(field);
        assertThat(sortField, notNullValue());
        assertThat(sortField.isAscending(), is(false));
    }

    @Test
    public void getByFieldTest3() {
        String field = "notSupportField";
        GlossarySortField sortField = GlossarySortField.getByField(field);
        assertThat(sortField, nullValue());
    }
}
