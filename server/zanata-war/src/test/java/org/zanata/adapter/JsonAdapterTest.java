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

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.zanata.common.LocaleId;
import org.zanata.rest.dto.resource.Resource;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
// TODO test writeTranslatedFile
public class JsonAdapterTest {

    private JsonAdapter adapter;
    private File testFile;

    @Before
    public void setup() {
        adapter = new JsonAdapter();
        testFile = new File("src/test/resources/org/zanata/adapter/basicjson.json");
        assert testFile.exists();
    }

//    @Feature(summary = "The user can translate JavaScript Object Notation files",
//            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 0)
    @Test
    public void parseJSON() {
        Resource resource =
                adapter.parseDocumentFile(testFile.toURI(), LocaleId.EN,
                        Optional.absent());
//        System.out.println(DTOUtil.toXML(resource));
        assertThat(resource.getTextFlows()).hasSize(5);
        assertThat(resource.getTextFlows().get(0).getContents()).isEqualTo(
                ImmutableList.of("Line One"));
        // TODO test IDs
    }

}
