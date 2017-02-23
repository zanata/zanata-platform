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
package org.zanata.feature.testharness;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.zanata.feature.account.*;
import org.zanata.feature.administration.*;
import org.zanata.feature.clientserver.ClientServerSuite;
import org.zanata.feature.concurrentedit.ConcurrentEditTestSuite;
import org.zanata.feature.dashboard.DashboardTestSuite;
import org.zanata.feature.document.DocumentTestSuite;
import org.zanata.feature.editor.EditorTestSuite;
import org.zanata.feature.endtoend.EndToEndTestSuite;
import org.zanata.feature.glossary.GlossaryTestSuite;
import org.zanata.feature.language.LanguageTestSuite;
import org.zanata.feature.misc.MiscTestSuite;
import org.zanata.feature.project.ProjectTestSuite;
import org.zanata.feature.projectversion.ProjectVersionTestSuite;
import org.zanata.feature.rest.RestTestSuite;
import org.zanata.feature.search.SearchTestSuite;
import org.zanata.feature.security.SecurityTestSuite;
import org.zanata.feature.versionGroup.VersionGroupTestSuite;

/**
 * The Zanata feature test list and suite interface.<br/>
 *
 * Lists the available test cases and can be extended for the purpose of
 * running categorised test collections.
 * @author Damian Jansen
 * <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 * @see "http://junit.org/javadoc/4.9/org/junit/experimental/categories/Categories.html"
 * @see "https://github.com/zanata/zanata-server/wiki/WebDriver-Automated-Tests"
 * @see "https://github.com/zanata/zanata-server/wiki/Writing-WebDriver-Tests"
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({

        /*
         * End to End tests
         */
        EndToEndTestSuite.class,

        /*
         * Account
         * The user account and management of, such as registration and
         * password changing
         */
        AccountTestSuite.class,

        /*
         * Administration
         * The actions of an administrator, such as editing users and
         * translation memory sets
         */
        AdministrationTestSuite.class,

        /* Client-Server
         * Usage of the mvn client
         */
        ClientServerSuite.class,

        /*
         * Concurrent Edit
         * Multiple user access to an editor instance
         */
        ConcurrentEditTestSuite.class,
        /*
         * Dashboard
         * User dashboard testing
         */
        DashboardTestSuite.class,

        /*
         * Document
         * Source document upload and management
         */
        DocumentTestSuite.class,

        /*
         * Editor
         * Translation editor general features
         */
        EditorTestSuite.class,

        /*
         * Glossary
         * Glossary management features
         */
        GlossaryTestSuite.class,

        /*
         * Language
         * Participation in an management of language teams
         */
        LanguageTestSuite.class,

        /*
         * Miscellaneous
         * Tests that don't fit into the other packages
         */
        MiscTestSuite.class,

        /*
         * Project
         * Creation and management of Projects
         */
        ProjectTestSuite.class,

        /*
         * Project Version
         * Creation and management of Project Versions
         */
        ProjectVersionTestSuite.class,

        /*
         * Rest
         * Test for UI and Rest interaction
         */
        RestTestSuite.class,

        /*
         * Search
         * Search bar functionality
         */
        SearchTestSuite.class,

        /*
         * Security
         * Login/logout and access rights
         */
        SecurityTestSuite.class,

        /*
         * Version Groups
         * Creation and management of Version Groups
         */
        VersionGroupTestSuite.class
})
public class TestPlan {

    /**
     * Interface for the execution of the Basic Acceptance Tests (BAT) category.
     *
     * Tests in this category exercise features only so far as to demonstrate
     * that the feature works, and perhaps have a single handled negative case.
     * BAT suites should not exceed an agreed interval, e.g. approximately
     * 10 minutes, in order to maintain a positive GitHub workflow.
     * Tests in this category provide a "review readiness" status for Github
     * Pull Requests, and must pass for said request to be accepted and merged.
     */
    public interface BasicAcceptanceTest {}

    /**
     * Interface for the execution of the Detailed Tests category.
     *
     * Tests that fall under this category exercise features more so than the
     * Basic Acceptance Tests (BAT), and form part of the Release Candidate
     * acceptance criteria.
     */
    public interface DetailedTest {}

    /**
     * Interface for the execution of the Unstable Test category.
     *
     * Tests in this category may encounter execution problems, for various
     * reasons such as:<br/>
     * - Relying on external interfaces (e.g. Google, Fedora, OpenShift)
     * - Timeout/performance problems
     */
    public interface UnstableTest {}

}
