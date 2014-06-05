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
package org.zanata.common.io;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;

import org.junit.Test;

/**
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class ProgressAwareInputStreamTest {

    String testDir = "src/test/resources/";

    @Test
    public void fileSize() throws Exception {
        // Given:
        int lastPercent = 100;
        ProgressAwareInputStream stream =
                new ProgressAwareInputStream(new File(testDir,
                        "org/zanata/common/util/ElementBuilderTest.xml"));
        stream.setOnProgressListener(new ProgressAwareInputStream.OnProgressListener() {
            @Override
            public void onProgress(int percentage) {
                System.out.println("Updated percentage: " + percentage);
            }
        });

        // When:
        byte[] buffer = new byte[5];
        while (stream.read(buffer) > 0) {
            // Reading every 5 bytes
        }

        // Then:
        assertThat(lastPercent, is(100));
    }
}
