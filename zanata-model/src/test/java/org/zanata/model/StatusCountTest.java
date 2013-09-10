/*
 * Copyright 2010, Red Hat, Inc. and individual contributors
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
package org.zanata.model;

import org.testng.annotations.Test;
import org.zanata.common.ContentState;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class StatusCountTest
{
   @Test
   public void lombokWorks() {
      StatusCount statusCount = new StatusCount(ContentState.New, 1L);

      StatusCount other = new StatusCount(ContentState.New, 1L);

      assertThat(statusCount.canEqual(other), equalTo(true));
      assertThat(statusCount.equals(other), equalTo(true));
      assertThat(statusCount.hashCode(), equalTo(other.hashCode()));
      System.out.println(statusCount.hashCode());
      assertThat(statusCount.toString(), equalTo("StatusCount(status=New, count=1)"));
   }
}
