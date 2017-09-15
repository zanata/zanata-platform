/*
 * Copyright 2015, Red Hat, Inc. and individual contributors as indicated by the
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

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class HmacUtil {

    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    /**
     * Generate SHA with given key and valueToDigest.
     * All values will using UTF-8 encoding.
     *
     * @param key - key for generate sha
     * @param valueToDigest - value for generate sha
     * @throws IllegalArgumentException
     */
    public static String hmacSha1(final String key, final String valueToDigest)
            throws IllegalArgumentException {
        return hmacSha1(StringUtils.getBytesUtf8(key),
                StringUtils.getBytesUtf8(valueToDigest));
    }

    public static String hmacSha1(final byte[] key, final byte[] valueToDigest)
            throws IllegalArgumentException {

        // Compute the hmac on input data bytes
        byte[] rawHmac=  getHmacSha1(key).doFinal(valueToDigest);
        return Base64.encodeBase64String(rawHmac);
    }

    // Get an hmac_sha1 key from the raw key bytes
    private static Mac getHmacSha1(final byte[] key) {
        return getInitializedMac(HMAC_SHA1_ALGORITHM, key);
    }

    // Get an hmac_sha1 Mac instance and initialize with the signing key
    private static Mac
            getInitializedMac(final String algorithm, final byte[] key) {

        if (key == null) {
            throw new IllegalArgumentException("Null key");
        }

        try {
            final SecretKeySpec keySpec = new SecretKeySpec(key, algorithm);
            final Mac mac = Mac.getInstance(algorithm);
            mac.init(keySpec);
            return mac;
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        } catch (final InvalidKeyException e) {
            throw new IllegalArgumentException(e);
        }
    }
}

