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
package org.zanata.rest.service;

import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.InRequestScope;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.zanata.common.ProjectType;
import org.zanata.dao.ProjectDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.test.CdiUnitRunner;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@RunWith(CdiUnitRunner.class)
@InRequestScope
public class LegacyFileMapperTest {
//    private String projectSlug = "projectSlug";
//    private String iterSlug = "iterSlug";

    @Produces @Mock
    ProjectDAO projectDAO;

    @Produces @Mock
    ProjectIterationDAO projectIterationDAO;

    @Inject
    LegacyFileMapper mapper;

    @Before
    public void before() {
//        when(projectDAO.)
    }

    @Test
    public void serverPropertiesWithPropertiesHint() {
        String serverDocId = mapper.getServerDocId(ProjectType.Properties, "docId.properties", ProjectType.Properties);
        assertThat(serverDocId).isEqualTo("docId");
    }

    @Test
    public void serverNullWithPropertiesHint() {
        String serverDocId = mapper.getServerDocId(null, "docId.properties", ProjectType.Properties);
        assertThat(serverDocId).isEqualTo("docId");
    }

    @Test
    public void serverFileWithAnyHint() {
        String serverDocId = mapper.getServerDocId(ProjectType.File, "docId.properties", ProjectType.Properties);
        assertThat(serverDocId).isEqualTo("docId.properties");
    }

    @Test(expected = WebApplicationException.class)
    public void serverNullWithNullHint() {
        mapper.getServerDocId(null, "docId.properties", null);
    }

    // FIXME
//    @Test
//    public void serverXliffWithPropertiesHint() {
//        String serverDocId = mapper.getServerDocId(ProjectType.Xliff, "docId.properties", ProjectType.Properties);
//        assertThat(serverDocId).isEqualTo("docId");
//    }

}
