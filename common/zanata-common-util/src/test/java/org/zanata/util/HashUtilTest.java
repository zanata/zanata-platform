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
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Hex;
import org.junit.Test;
import org.zanata.common.io.DigestWriter;

import com.google.common.base.Charsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

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

        assertThat(digestWriterHash).isEqualTo(hashUtilHash);
    }

    @Test
    public void singleSourceHash() {
        String testSingleContent = "test single content";
        String testSingleContentHash = "18ee57a0cd282965a4e6a5e772352dba";
        String hashUtilHash = HashUtil.sourceHash(testSingleContent);
        assertThat(hashUtilHash).isEqualTo(testSingleContentHash);
    }

    @Test
    public void singleSourceHashNullIsInvalid() {
        String nullString = null;
        try {
            HashUtil.sourceHash(nullString);
            fail("Should have had a NullPointer|IllegalArgument exception");
        } catch (NullPointerException | IllegalArgumentException e) {
            // Pass - can have one or the other?
        }
    }

    @Test
    public void singleSourceHashNullIfEmpty() {
        assertThat(HashUtil.sourceHash("")).isNull();
    }

    @Test
    public void sourceHashHashesJoinedStrings() {
        List<String> contents = new ArrayList<>();
        contents.add("content 1");
        contents.add("content 2");
        contents.add("content 3");

        String pipeJoined = "content 1|content 2|content 3";
        String hashOfPipeJoined = HashUtil.sourceHash(pipeJoined);

        assertThat(HashUtil.sourceHash(contents)).isEqualTo(hashOfPipeJoined);
    }

    @Test
    public void sourceHashNullIfEmpty() {
        List<String> emptyContents = new ArrayList<>();
        assertThat(HashUtil.sourceHash(emptyContents)).isNull();
    }

    @Test
    public void sourceHashWrapsEmptyInPipes() {
        List<String> contents = new ArrayList<>();
        contents.add("content 1");
        contents.add("");
        contents.add("");

        String pipeJoined = "content 1||";
        String hashOfPipeJoined = HashUtil.sourceHash(pipeJoined);

        assertThat(HashUtil.sourceHash(contents)).isEqualTo(hashOfPipeJoined);
    }

    @Test
    public void sourceHashTreatsNullAsEmpty() {
        List<String> contents = new ArrayList<>();
        contents.add("content 1");
        contents.add(null);
        contents.add("content 3");

        String pipeJoined = "content 1||content 3";
        String hashOfPipeJoined = HashUtil.sourceHash(pipeJoined);

        assertThat(HashUtil.sourceHash(contents)).isEqualTo(hashOfPipeJoined);
    }

    @Test
    public void sourceHashHandlesLotsOfContents() {
        List<String> contents = new ArrayList<>();
        contents.add("content 1");
        contents.add("content 2");
        contents.add("content 3");
        contents.add("content 4");
        contents.add("content 5");
        contents.add("content 6");
        contents.add("content 7");
        contents.add("content 8");
        contents.add("content 9");
        contents.add("content 10");

        String pipeJoined = "content 1|content 2|content 3|content 4|" +
                "content 5|content 6|content 7|content 8|content 9|content 10";
        String hashOfPipeJoined = HashUtil.sourceHash(pipeJoined);

        assertThat(HashUtil.sourceHash(contents)).isEqualTo(hashOfPipeJoined);
    }

}
