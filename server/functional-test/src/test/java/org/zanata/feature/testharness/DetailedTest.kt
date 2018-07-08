/*
 * Copyright 2018, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.feature.testharness

import org.junit.jupiter.api.Tag
import kotlin.annotation.Retention

/**
 * Interface for the execution of the Detailed Tests category.
 *
 * Tests that fall under this category exercise features more so than the
 * Basic Acceptance Tests (BAT), and form part of the Release Candidate
 * acceptance criteria.
 *
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE,
        AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER,
        AnnotationTarget.PROPERTY_SETTER)
@Retention
@Tag("DetailedTest")
annotation class DetailedTest
