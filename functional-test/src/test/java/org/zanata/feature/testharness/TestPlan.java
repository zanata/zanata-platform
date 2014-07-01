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
import org.zanata.feature.clientserver.*;
import org.zanata.feature.concurrentedit.*;
import org.zanata.feature.dashboard.DashboardTest;
import org.zanata.feature.document.HTMLDocumentTypeTest;
import org.zanata.feature.document.UploadTest;
import org.zanata.feature.editor.*;
import org.zanata.feature.infrastructure.RetryRuleTest;
import org.zanata.feature.language.AddLanguageTest;
import org.zanata.feature.language.JoinLanguageTeamTest;
import org.zanata.feature.misc.*;
import org.zanata.feature.project.*;
import org.zanata.feature.projectversion.*;
import org.zanata.feature.search.ProjectSearchTest;
import org.zanata.feature.security.SecurityFullTest;
import org.zanata.feature.versionGroup.*;

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
         * Account
         * The user account and management of, such as registration and
         * password changing
         */
        ChangePasswordTest.class,
        InactiveUserLoginTest.class,
        InvalidEmailAddressTest.class,
        ProfileTest.class,
        RegisterTest.class,
        UsernameValidationTest.class,
        ValidEmailAddressTest.class,

        /*
         * Administration
         * The actions of an administrator, such as editing users and
         * translation memory sets
         */
        EditHomePageTest.class,
        EditTranslationMemoryTest.class,
        EditTranslationMemoryTest.class,
        ManageSearchTest.class,
        ManageUsersTest.class,

        /* Client-Server
         * Usage of the mvn client
         */
        GettextPluralSupportTest.class,
        ProjectMaintainerTest.class,
        PropertiesRoundTripTest.class,

        /*
         * Concurrent Edit
         * Multiple user access to an editor instance
         */
        ConcurrentAccessTest.class,
        ConcurrentEditTest.class,

        /*
         * Dashboard
         * User dashboard testing
         */
        DashboardTest.class,

        /*
         * Document
         * Source document upload and management
         */
        HTMLDocumentTypeTest.class,
        UploadTest.class,

        /*
         * Editor
         * Translation editor general features
         */
        TranslateHTMLTest.class,
        TranslateIdmlTest.class,
        TranslateOdsTest.class,
        TranslateOpenOfficeTest.class,
        TranslateTextTest.class,

        /*
         * Infrastructure
         * Test infrastructure validation
         */
        RetryRuleTest.class,

        /*
         * Language
         * Participation in an management of language teams
         */
        AddLanguageTest.class,
        JoinLanguageTeamTest.class,

        /*
         * Miscellaneous
         * Tests that don't fit into the other packages
         */
        ContactAdminTest.class,
        ObsoleteTextTest.class,
        RateLimitRestAndUITest.class,

        /*
         * Project
         * Creation and management of Projects
         */
        CreateProjectTest.class,
        EditPermissionsTest.class,
        EditProjectAboutTest.class,
        EditProjectGeneralTest.class,
        EditProjectLanguagesTest.class,
        EditProjectValidationsTest.class,

        /*
         * Project Version
         * Creation and management of Project Versions
         */
        CreateProjectVersionTest.class,
        EditVersionLanguagesTest.class,
        EditVersionValidationsTest.class,
        VersionFilteringTest.class,

        /*
         * Search
         * Search bar functionality
         */
        ProjectSearchTest.class,

        /*
         * Security
         * Login/logout and access rights
         */
        SecurityFullTest.class,

        /*
         * Version Groups
         * Creation and management of Version Groups
         */
        VersionGroupTest.class,
        VersionGroupIDValidationTest.class,
        VersionGroupUrlTest.class
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
