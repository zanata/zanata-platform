/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata;

import org.hamcrest.Matchers;
import org.junit.ClassRule;
import org.junit.Test;
import org.zanata.util.ResetDatabaseRule;

import static org.hamcrest.MatcherAssert.assertThat;

public class ExperimentTest {
    @ClassRule
    public static ResetDatabaseRule rule = new ResetDatabaseRule(
            ResetDatabaseRule.Config.NoResetAfter,
            ResetDatabaseRule.Config.WithData);

    // public static ResetDatabaseRule rule = new
    // ResetDatabaseRule(ResetDatabaseRule.Config.NoResetAfter);

    @Test
    public void canFindText() {
        String linkText = "master\n   Documents: 0";
        System.out.println(linkText);
        String stripNewLine = linkText.replaceAll("\\n", " ");
        System.out.println(stripNewLine);
        boolean matches = stripNewLine.matches("master\\s+Documents.+");
        assertThat(matches, Matchers.equalTo(true));
    }

    // @Test
    public void canDoCampbell() {
        // #. Tag: para
        // #, no-c-format
        // msgid "Describes Fedora, the Fedora Project, and how you can help."
        // msgstr ""
        String tag = "#. Tag: para";
        String msgCtx = "#, no-c-format";
        String msgId = "msgid \"This is string number %d. \"";
        String msgStr = "msgstr \"%s %d\"";
        String potMsgStr = "msgstr \"\"";

        for (int i = 55; i > 0; i--) {
            System.out.println(tag);
            System.out.println(msgCtx);
            System.out.printf(msgId, i);
            System.out.println();
            System.out.printf(potMsgStr);
            // System.out.printf(msgStr, "translated string number", i);
            System.out.println();
            System.out.println();
        }
    }
}
