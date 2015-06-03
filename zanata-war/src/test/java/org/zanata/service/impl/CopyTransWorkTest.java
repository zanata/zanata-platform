/*
 * Copyright 2014, Red Hat, Inc. and individual contributors
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
package org.zanata.service.impl;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.zanata.common.ContentState;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zanata.common.ContentState.Approved;
import static org.zanata.common.ContentState.NeedReview;
import static org.zanata.common.ContentState.New;
import static org.zanata.common.ContentState.Translated;
import static org.zanata.model.HCopyTransOptions.ConditionRuleAction.DOWNGRADE_TO_FUZZY;
import static org.zanata.model.HCopyTransOptions.ConditionRuleAction.IGNORE;
import static org.zanata.model.HCopyTransOptions.ConditionRuleAction.REJECT;
import static org.zanata.service.impl.CopyTransWork.MatchRulePair;

/**
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public class CopyTransWorkTest {
    private final List<ContentState> validTranslatedStates = ImmutableList.of(
            Translated, Approved);

    @Test
    public void basicDetermineContentState() {
        // An empty rule list should not change the state
        for (ContentState state : validTranslatedStates) {
            assertThat(CopyTransWork.determineContentStateFromRuleList(
                    Lists.<MatchRulePair> newArrayList(),
                    true, state), is(state));
            assertThat(CopyTransWork.determineContentStateFromRuleList(
                    Lists.<MatchRulePair> newArrayList(),
                    false, state), is(Translated));
        }
    }

    @Test
    public void contentStateWithIgnoreRule() {
        // If the rule is IGNORE, the state should not change no matter what the
        // result is
        for (ContentState state : validTranslatedStates) {
            assertThat(CopyTransWork.determineContentStateFromRuleList(
                    Lists.newArrayList(new MatchRulePair(
                            Suppliers.ofInstance(true), IGNORE)), true, state
                    ), is(state));
            assertThat(CopyTransWork.determineContentStateFromRuleList(
                    Lists.newArrayList(new MatchRulePair(
                            Suppliers.ofInstance(false), IGNORE)), true, state
                    ), is(state));
            assertThat(CopyTransWork.determineContentStateFromRuleList(
                    Lists.newArrayList(new MatchRulePair(
                            Suppliers.ofInstance(true), IGNORE)), false, state
                    ), is(Translated));
            assertThat(CopyTransWork.determineContentStateFromRuleList(
                    Lists.newArrayList(new MatchRulePair(
                            Suppliers.ofInstance(false), IGNORE)), false, state
                    ), is(Translated));
        }
    }

    @Test
    public void contentStateWithRejectRule() {
        // If the rule is Reject, then the match should be rejected only when
        // the evaluation fails
        for (ContentState state : validTranslatedStates) {
            assertThat(CopyTransWork.determineContentStateFromRuleList(
                    Lists.newArrayList(new MatchRulePair(
                            Suppliers.ofInstance(true), REJECT)), true, state
                    ), is(state));
            assertThat(CopyTransWork.determineContentStateFromRuleList(
                    Lists.newArrayList(new MatchRulePair(
                            Suppliers.ofInstance(false), REJECT)), true, state
                    ), is(New));
            assertThat(CopyTransWork.determineContentStateFromRuleList(
                    Lists.newArrayList(new MatchRulePair(
                            Suppliers.ofInstance(true), REJECT)), false, state
                    ), is(Translated));
            assertThat(CopyTransWork.determineContentStateFromRuleList(
                    Lists.newArrayList(new MatchRulePair(
                            Suppliers.ofInstance(false), REJECT)), false, state
                    ), is(New));
        }
    }

    @Test
    public void contentStateWithDowngradeRule() {
        // If the rule is downgrade, then the match should be downgraded when
        // the evaluation fails
        for (ContentState state : validTranslatedStates) {
            assertThat(CopyTransWork.determineContentStateFromRuleList(
                    Lists.newArrayList(new MatchRulePair(
                            Suppliers.ofInstance(true), DOWNGRADE_TO_FUZZY)),
                    true, state
                    ), is(state));
            assertThat(CopyTransWork.determineContentStateFromRuleList(
                    Lists.newArrayList(
                            new MatchRulePair(
                                    Suppliers.ofInstance(false),
                                    DOWNGRADE_TO_FUZZY)), true,
                    state
                    ),
                    is(NeedReview));
            assertThat(CopyTransWork.determineContentStateFromRuleList(
                    Lists.newArrayList(
                            new MatchRulePair(
                                    Suppliers.ofInstance(true),
                                    DOWNGRADE_TO_FUZZY)), false,
                    state
                    ),
                    is(Translated));
            assertThat(CopyTransWork.determineContentStateFromRuleList(
                    Lists.newArrayList(
                            new MatchRulePair(
                                    Suppliers.ofInstance(false),
                                    DOWNGRADE_TO_FUZZY)), false,
                    state
                    ),
                    is(NeedReview));
        }
    }

    @Test
    public void failedRejectionRule() {
        // A single rejection should reject the whole translation no matter what
        // the other rules say
        assertThat(CopyTransWork.determineContentStateFromRuleList(Lists
                .newArrayList(
                        new MatchRulePair(Suppliers.ofInstance(false),
                                DOWNGRADE_TO_FUZZY),
                        new MatchRulePair(Suppliers.ofInstance(false),
                                REJECT)
                ),
                true, Translated
                ), is(New));
        assertThat(CopyTransWork.determineContentStateFromRuleList(Lists
                .newArrayList(new MatchRulePair(Suppliers.ofInstance(true),
                        IGNORE), new MatchRulePair(Suppliers.ofInstance(false),
                        REJECT)), false, Translated), is(New));

        assertThat(CopyTransWork.determineContentStateFromRuleList(Lists
                .newArrayList(new MatchRulePair(Suppliers.ofInstance(false),
                        REJECT), new MatchRulePair(Suppliers.ofInstance(false),
                        DOWNGRADE_TO_FUZZY)), true, Translated), is(New));
        assertThat(CopyTransWork.determineContentStateFromRuleList(Lists
                .newArrayList(new MatchRulePair(Suppliers.ofInstance(false),
                        REJECT), new MatchRulePair(Suppliers.ofInstance(true),
                        IGNORE)), false, Translated), is(New));
    }

    @Test
    public void failedDowngradeRule() {
        // A failed Downgrade rule should cause the content state to be fuzzy in
        // all cases, except if a rejection is encountered
        assertThat(CopyTransWork.determineContentStateFromRuleList(Lists
                .newArrayList(
                        new MatchRulePair(Suppliers.ofInstance(false),
                                DOWNGRADE_TO_FUZZY),
                        new MatchRulePair(Suppliers.ofInstance(true),
                                REJECT)
                ),
                true, Translated
                ), is(NeedReview));
        assertThat(CopyTransWork.determineContentStateFromRuleList(Lists
                .newArrayList(
                        new MatchRulePair(Suppliers.ofInstance(false),
                                DOWNGRADE_TO_FUZZY),
                        new MatchRulePair(Suppliers.ofInstance(true),
                                REJECT)
                ),
                false, Translated
                ), is(NeedReview));
        assertThat(CopyTransWork.determineContentStateFromRuleList(Lists
                .newArrayList(
                        new MatchRulePair(Suppliers.ofInstance(false),
                                DOWNGRADE_TO_FUZZY),
                        new MatchRulePair(Suppliers.ofInstance(false),
                                IGNORE)
                ),
                true, Approved
                ), is(NeedReview));
        assertThat(CopyTransWork.determineContentStateFromRuleList(Lists
                .newArrayList(
                        new MatchRulePair(Suppliers.ofInstance(false),
                                DOWNGRADE_TO_FUZZY),
                        new MatchRulePair(Suppliers.ofInstance(false),
                                IGNORE)
                ),
                true, Approved
                ), is(NeedReview));
    }

    @Test
    public void determineContentStateFromRuleListBasics() {
        // Tests the expected content state when approval is/is not required,
        // and NO rules are evaluated
        assertThat(CopyTransWork.determineContentStateFromRuleList(
                Lists.<MatchRulePair> newArrayList(),
                true, Translated), is(Translated));
        assertThat(CopyTransWork.determineContentStateFromRuleList(
                Lists.<MatchRulePair> newArrayList(),
                false, Translated), is(Translated));
        assertThat(CopyTransWork.determineContentStateFromRuleList(
                Lists.<MatchRulePair> newArrayList(),
                true, Approved), is(Approved));
        assertThat(CopyTransWork.determineContentStateFromRuleList(
                Lists.<MatchRulePair> newArrayList(),
                false, Approved), is(Translated));
    }

}
