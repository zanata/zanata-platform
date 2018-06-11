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
package org.zanata.feature.misc;

import org.junit.Test;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.page.utility.Error404Page;
import org.zanata.workflow.BasicWorkFlow;

import static org.assertj.core.api.Assertions.assertThat;
/**
 * @author Damian Jansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class PageNotFoundTest extends ZanataTestCase {

    @Test
    public void pageNotFound() {
        Error404Page error404Page = new BasicWorkFlow()
                .goToPage("notAPage", Error404Page.class);

        assertThat(error404Page.isItA404())
                .isTrue()
                .as("Standard page shows a 404 error");

        error404Page = new BasicWorkFlow()
                .goToPage("projects/view/NotAProject", Error404Page.class);

        assertThat(error404Page.isItA404())
                .isTrue()
                .as("Seam 'entity not found' page shows a 404 error");
    }
}
