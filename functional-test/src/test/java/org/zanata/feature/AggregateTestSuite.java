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
package org.zanata.feature;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.zanata.feature.account.AccountTestSuite;
import org.zanata.feature.administration.AdministrationTestSuite;
import org.zanata.feature.clientserver.ClientServerTestSuite;
import org.zanata.feature.concurrentedit.ConcurrentEditTestSuite;
import org.zanata.feature.dashboard.DashboardTestSuite;
import org.zanata.feature.document.DocumentTestSuite;
import org.zanata.feature.editor.EditorTestSuite;
import org.zanata.feature.glossary.GlossaryTestSuite;
import org.zanata.feature.googleopenid.GoogleOpenIDTestSuite;
import org.zanata.feature.infrastructure.InfrastructureTestSuite;
import org.zanata.feature.language.LanguageTestSuite;
import org.zanata.feature.misc.MiscTestSuite;
import org.zanata.feature.project.ProjectTestSuite;
import org.zanata.feature.projectversion.ProjectVersionTestSuite;
import org.zanata.feature.search.SearchTestSuite;
import org.zanata.feature.security.SecurityTestSuite;
import org.zanata.feature.startNewProject.CreateSampleProjectTestSuite;
import org.zanata.feature.versionGroup.VersionGroupTestSuite;

/**
 * The top level of the feature test suite hierarchy. Lists the available test
 * suites and can be extended for the purpose of running categorised test
 * collections.
 * @author Damian Jansen <a
 *      href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        AccountTestSuite.class,
        AdministrationTestSuite.class,
        ClientServerTestSuite.class,
        ConcurrentEditTestSuite.class,
        DashboardTestSuite.class,
        DocumentTestSuite.class,
        EditorTestSuite.class,
        GlossaryTestSuite.class,
        GoogleOpenIDTestSuite.class,
        InfrastructureTestSuite.class,
        LanguageTestSuite.class,
        MiscTestSuite.class,
        ProjectTestSuite.class,
        ProjectVersionTestSuite.class,
        SearchTestSuite.class,
        SecurityTestSuite.class,
        VersionGroupTestSuite.class,
        CreateSampleProjectTestSuite.class })
public class AggregateTestSuite {
}
