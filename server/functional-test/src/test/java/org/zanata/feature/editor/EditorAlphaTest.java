/*
 * Copyright 2017, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.feature.editor;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.testharness.TestPlan;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.page.editor.ReactEditorPage;
import org.zanata.workflow.BasicWorkFlow;
import org.zanata.workflow.LoginWorkFlow;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Damian Jansen
 *         <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(TestPlan.BasicAcceptanceTest.class)
public class EditorAlphaTest extends ZanataTestCase {

    // Test that the alpha button works
    @Test(timeout = MAX_SHORT_TEST_DURATION)
    public void showAlphaEditor() {
        new LoginWorkFlow()
                .signIn("admin", "admin");
        ReactEditorPage reactEditorPage = new BasicWorkFlow()
                .goToEditor("about-fedora", "master", "fr", "About_Fedora")
                .pressAlphaEditorButton()
                .switchToEditorWindow();

        assertThat(reactEditorPage.isReactEditor()).isTrue();

        // Wait for the phrases to load
        reactEditorPage.expectNumberOfTargets(1);
        assertThat(reactEditorPage.getTransunitTargets().size()).isEqualTo(1);
    }
}
