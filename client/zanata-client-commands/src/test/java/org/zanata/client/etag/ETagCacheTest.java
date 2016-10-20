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
package org.zanata.client.etag;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@RunWith(JUnit4.class)
public class ETagCacheTest {

    @Test
    public void readCache() {
        InputStream is =
                this.getClass()
                        .getResourceAsStream("/etagcache/etag-cache.xml");
        ETagCache cache = ETagCacheReaderWriter.readCache(is);

        assertThat(cache.getCacheEntries().size(), is(3));
        for (int i = 0; i < cache.getCacheEntries().size(); i++) {
            ETagCacheEntry cacheEntry = cache.getCacheEntries().get(i);
            assertThat(cacheEntry.getLanguage(), is("en"));
            assertThat(cacheEntry.getLocalFileMD5(), is("1234"));
            assertThat(cacheEntry.getLocalFileName(), is("xyz_abc_" + (i + 1)
                    + ".properties"));
            assertThat(cacheEntry.getLocalFileTime(), is("1234"));
            assertThat(cacheEntry.getServerETag(), is("ABCDE"));
        }
    }

    @Test
    public void writeCache() throws Exception {
        File cacheFile = File.createTempFile("etag-cache", "xml");
        cacheFile.deleteOnExit();
        FileOutputStream os = new FileOutputStream(cacheFile);

        ETagCache cache = new ETagCache();
        cache.addEntry(new ETagCacheEntry("myfile.txt", "en-US", "1234.5678",
                "ABCDEFG", "ABCD1234"));

        ETagCacheReaderWriter.writeCache(cache, os);

        // Now read the cache and make sure it's the same
        ETagCache readCache =
                ETagCacheReaderWriter.readCache(new FileInputStream(cacheFile));

        assertThat(readCache.getCacheEntries().size(), is(cache
                .getCacheEntries().size()));
        for (int i = 0; i < cache.getCacheEntries().size(); i++) {
            ETagCacheEntry cacheEntry = cache.getCacheEntries().get(i);
            ETagCacheEntry readEntry = readCache.getCacheEntries().get(i);
            assertThat(cacheEntry.getLanguage(), is(readEntry.getLanguage()));
            assertThat(cacheEntry.getLocalFileMD5(),
                    is(readEntry.getLocalFileMD5()));
            assertThat(cacheEntry.getLocalFileName(),
                    is(readEntry.getLocalFileName()));
            assertThat(cacheEntry.getLocalFileTime(),
                    is(readEntry.getLocalFileTime()));
            assertThat(cacheEntry.getServerETag(),
                    is(readEntry.getServerETag()));
        }
    }

    @Test
    public void findEntry() throws Exception {
        InputStream is =
                this.getClass()
                        .getResourceAsStream("/etagcache/etag-cache.xml");
        ETagCache cache = ETagCacheReaderWriter.readCache(is);

        assertThat(cache.getCacheEntries().size(), is(3));

        assertThat(cache.findEntry("xyz_abc_1.properties", "en"),
                notNullValue());
    }

}
