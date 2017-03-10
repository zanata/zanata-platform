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

import org.junit.Rule;
import org.junit.Test;
import org.zanata.util.SampleProjectRule;

/**
 * This is a class for experiment some things i.e. populate cargo instance with
 * some example users and languages so that a manual test can be performed.
 * Under normal circumstances it will have no active tests in it.
 */
public class ExperimentTest {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(ExperimentTest.class);

    @Rule
    public SampleProjectRule sampleProjectRule = new SampleProjectRule();

    @Test
    public void test() {
        // we need at least a test to apply the rule
    }
}
