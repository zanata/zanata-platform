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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Tests for {@link HttpHeaders}.
 * From https://github.com/jenkinsci/google-storage-plugin/blob/google-storage-plugin-0.10/src/test/java/com/google/jenkins/plugins/storage/HttpHeadersTest.java
 */
public class HttpHeadersTest {

  @Test
  public void testGetContentDisposition_ascii() {
    assertEquals(
        "attachment; filename=\"myapp.war\"; filename*=UTF-8''myapp.war",
        HttpHeaders.getContentDisposition("myapp.war", false));

    assertEquals(
        "attachment; filename=\"contains space.txt\"; "
            + "filename*=UTF-8''contains%20space.txt",
        HttpHeaders.getContentDisposition("contains space.txt", false));
  }

  @Test
  public void testGetContentDisposition_asciiInline() {
    assertEquals(
        "inline; filename=\"build-log.txt\"; filename*=UTF-8''build-log.txt",
        HttpHeaders.getContentDisposition("build-log.txt", true));
  }

  @Test
  public void testGetContentDisposition_unicodeBmp() {
    assertEquals(
        "attachment; filename=\"snowman _.txt\"; "
            + "filename*=UTF-8''snowman%20%E2%98%83.txt",
        HttpHeaders.getContentDisposition("snowman ☃.txt", false));
  }

  @Test
  public void testGetContentDisposition_unicodeNonBmp() {
    assertEquals(
        "attachment; filename=\"_.zip\"; filename*=UTF-8''%F0%9D%92%9E.zip",
        HttpHeaders.getContentDisposition("𝒞.zip", false));
  }

  @Test
  public void testGetContentDisposition_rfc2616Escapes() {
    assertEquals(
        "attachment; filename=\"-\\\\-\\\"-\"; filename*=UTF-8''-%5C-%22-",
        HttpHeaders.getContentDisposition("-\\-\"-", false));
  }

  @Test
  public void testGetContentDisposition_rfc5987IdentitySymbols() {
    assertEquals(
        "attachment; filename=\"!#$&+-.^_`|~\"; filename*=UTF-8''!#$&+-.^_`|~",
        HttpHeaders.getContentDisposition("!#$&+-.^_`|~", false));
  }

  @Test
  public void testGetContentDisposition_rfc5987PercentEncodedSymbols() {
    assertEquals(
        "attachment; filename=\"@%*()=[]{}\\\\:;\\\"'<>,/?\"; "
            + "filename*=UTF-8''%40%25%2A%28%29%3D%5B%5D%7B%7D%5C%3A%3B%22%27"
            + "%3C%3E%2C%2F%3F",
        HttpHeaders.getContentDisposition("@%*()=[]{}\\:;\"'<>,/?", false));
  }
}
