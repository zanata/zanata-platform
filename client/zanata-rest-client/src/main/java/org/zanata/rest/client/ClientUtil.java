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

package org.zanata.rest.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;


/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class ClientUtil {

    }

    public static String calculateFileMD5(File srcFile) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            InputStream fileStream = new FileInputStream(srcFile);
            try {
                fileStream = new DigestInputStream(fileStream, md);
                byte[] buffer = new byte[256];
                //noinspection StatementWithEmptyBody
                while (fileStream.read(buffer) > 0) {
                    // just keep digesting the input
                }
            } finally {
                fileStream.close();
            }
            @SuppressWarnings("UnnecessaryLocalVariable")
            String md5hash = new String(Hex.encodeHex(md.digest()));
            return md5hash;
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new RuntimeException(e);
        }
    public static String getBaseURL(String movedTo) {
        try {
            URL url = new URI(movedTo).toURL();
            int pathIndex = movedTo.lastIndexOf(url.getPath());
            return movedTo.substring(0, pathIndex) + "/";
        } catch (MalformedURLException | URISyntaxException e) {
            return movedTo;
        }
    }

    /**
     * Extract filename from response header.
     *
     * e.g. Content-Disposition=[attachment; filename="RPM.po"]
     *
     * @param headers
     * @return
     */
    public static String getFileNameFromHeader(MultivaluedMap<String, String> headers) {
        final String CONTENT_DISPOSITION_HEADER = "Content-Disposition";
        final String FILENAME_PATTERN = "filename=\"(.*?)\"$";

        String contentDisposition = headers.getFirst(CONTENT_DISPOSITION_HEADER);
        if (StringUtils.isEmpty(contentDisposition)) {
            return null;
        }
        Pattern p = Pattern.compile(FILENAME_PATTERN);
        Matcher m = p.matcher(contentDisposition);
        return m.find() ? m.group(1) : null;
    }

}
