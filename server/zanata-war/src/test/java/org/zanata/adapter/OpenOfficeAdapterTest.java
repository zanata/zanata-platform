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
package org.zanata.adapter;

import static org.assertj.core.api.Assertions.assertThat;


import org.junit.Before;
import org.junit.Test;
import org.zanata.rest.dto.resource.Resource;

/**
 * @author Sean Flanigan
 *         <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
// @Feature(summary = "The user can translate OpenOffice files",
// tcmsTestPlanIds = 5316, tcmsTestCaseIds = 0)
// TODO test writeTranslatedFile
public class OpenOfficeAdapterTest extends AbstractAdapterTest<OpenOfficeAdapter> {

    @Before
    public void setup() {
        adapter = new OpenOfficeAdapter();
    }

    @Test
    public void parseODG() {
        Resource resource = parseTestFile("test-odg.odg");
        check3TextFlows(resource, true);
    }

    @Test
    public void parseODP() {
        Resource resource = parseTestFile("test-odp.odp");
        // TODO we are getting extra junk at the end
        check3TextFlows(resource, false);
    }

    @Test
    public void parseODT() {
        Resource resource = parseTestFile("test-odt.odt");
        check3TextFlows(resource, true);
    }

    // @Feature(summary = "The user can translate an OpenOffice spreadsheet
    // file",
    // tcmsTestPlanIds = 5316, tcmsTestCaseIds = 0)
    @Test
    public void parseODS() {
        Resource resource = parseTestFile("test-ods.ods");
        checkTextFlowsForODS(resource);
    }

    private void checkTextFlowsForODS(Resource resource) {
        // TODO we are getting extra junk at the end
        // assertThat(resource.getTextFlows()).hasSize(5);
        // TODO test IDs

        assertThat(resource.getTextFlows().get(0).getContents().get(0))
                .isEqualTo("TestODS")
                .as("Item 1 shows TestODS (the sheet name)");
        assertThat(resource.getTextFlows().get(1).getContents().get(0))
                .isEqualTo("First").as("Item 2 shows First (the page name)");
        assertThat(resource.getTextFlows().get(2).getContents().get(0))
                .isEqualTo("Line One").as("Item 3 shows Line One");
        assertThat(resource.getTextFlows().get(3).getContents().get(0))
                .isEqualTo("Line Two").as("Item 4 shows Line Two");
        assertThat(resource.getTextFlows().get(4).getContents().get(0))
                .isEqualTo("Line Three").as("Item 5 shows Line Three");
    }

    private void check3TextFlows(Resource resource, boolean checkSize) {
        if (checkSize) assertThat(resource.getTextFlows()).hasSize(3);
        // TODO test IDs

        assertThat(resource.getTextFlows().get(0).getContents().get(0))
                .isEqualTo("Line One").as("Item 0 shows Line One");
        assertThat(resource.getTextFlows().get(1).getContents().get(0))
                .isEqualTo("Line Two").as("Item 1 shows Line Two");
        assertThat(resource.getTextFlows().get(2).getContents().get(0))
                .isEqualTo("Line Three").as("Item 2 shows Line Three");
    }

}
