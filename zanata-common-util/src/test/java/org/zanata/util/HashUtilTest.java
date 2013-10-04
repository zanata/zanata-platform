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
package org.zanata.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.security.MessageDigest;

import org.apache.commons.codec.binary.Hex;
import org.testng.annotations.Test;
import org.zanata.common.io.DigestWriter;

import com.google.common.base.Charsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class HashUtilTest {
    @Test
    public void fileUtilHashSameAsDigestWriter() throws Exception {
        File tmpFile = File.createTempFile("Test", "-tmp");
        tmpFile.deleteOnExit();

        MessageDigest md5 = MessageDigest.getInstance("MD5");
        Writer fw =
                new OutputStreamWriter(new FileOutputStream(tmpFile),
                        Charsets.UTF_8);
        DigestWriter dw = new DigestWriter(fw, md5);

        dw.write("This is a new Temporary file");
        dw.flush();
        dw.close();

        String digestWriterHash = new String(Hex.encodeHex(md5.digest()));
        String hashUtilHash = HashUtil.getMD5Checksum(tmpFile);

        assertThat(digestWriterHash, equalTo(hashUtilHash));
    }
}
