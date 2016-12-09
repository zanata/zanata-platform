/*
 * Copyright 2013 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zanata.util.web;

import com.google.common.base.Charsets;

/**
 * Utility class for building HTTP header values.
 * From https://github.com/jenkinsci/google-storage-plugin/blob/google-storage-plugin-0.10/src/main/java/com/google/jenkins/plugins/storage/HttpHeaders.java
 */
public class HttpHeaders {

  /**
   * Returns an RFC 6266 Content-Disposition header for the given filename.
   */
  public static String getContentDisposition(String filename,
      boolean showInline) {
    return String.format(
        "%s; filename=%s; filename*=%s",
        showInline ? "inline" : "attachment",
        getRfc2616QuotedString(filename),
        getRfc5987ExtValue(filename));
  }

  /**
   * Returns an RFC 2616 quoted-string encoding of the given text. Code points
   * &lt;= U+255 will be encoded, others will be zapped to underscore.
   */
  private static String getRfc2616QuotedString(String text) {
    StringBuilder builder = new StringBuilder("\"");
    for (int i = 0; i < text.length(); ) {
      int codePoint = text.codePointAt(i);
      i += Character.charCount(codePoint);
      if (codePoint == '"') {
        builder.append("\\\"");
      } else if (codePoint == '\\') {
        builder.append("\\\\");
      } else if (codePoint <= 255) {
        // Assumes the remote side will interpret as ISO-8859-1.
        builder.appendCodePoint(codePoint);
      } else {
        builder.append('_');
      }
    }
    return builder.append('"').toString();
  }

  /**
   * Returns an RFC 5987 ext-value encoding of the given text, with charset
   * UTF-8 and no language tag.
   */
  private static String getRfc5987ExtValue(String text) {
    StringBuilder builder = new StringBuilder("UTF-8''");
    for (int i = 0; i < text.length(); ) {
      int codePoint = text.codePointAt(i);
      int len = Character.charCount(codePoint);
      if ((codePoint >= '0' && codePoint <= '9')
          || (codePoint >= 'A' && codePoint <= 'Z')
          || (codePoint >= 'a' && codePoint <= 'z')
          || (RFC_5987_ATTR_CHARS.indexOf(codePoint) != -1)) {
        builder.appendCodePoint(codePoint);
      } else {
        for (byte b : text.substring(i, i + len).getBytes(Charsets.UTF_8)) {
          builder.append(String.format("%%%02X", ((int) b) & 0xff));
        }
      }
      i += len;
    }
    return builder.toString();
  }

  private static final String RFC_5987_ATTR_CHARS = "!#$&+-.^_`|~";

  private HttpHeaders() {}
}
