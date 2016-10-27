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

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Contains information about a particular entry in the ETag cache.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class ETagCacheEntry {
    private String localFileName;

    private String language;

    private String localFileTime;

    private String localFileMD5;

    private String serverETag;

    public ETagCacheEntry() {
    }

    public ETagCacheEntry(String localFileName, String language,
            String localFileTime, String localFileMD5, String serverETag) {
        this.localFileName = localFileName;
        this.language = language;
        this.localFileTime = localFileTime;
        this.localFileMD5 = localFileMD5;
        this.serverETag = serverETag;
    }

    @XmlAttribute
    public String getLocalFileName() {
        return localFileName;
    }

    public void setLocalFileName(String localFileName) {
        this.localFileName = localFileName;
    }

    @XmlAttribute(name = "lang")
    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    @XmlAttribute
    public String getLocalFileTime() {
        return localFileTime;
    }

    public void setLocalFileTime(String localFileTime) {
        this.localFileTime = localFileTime;
    }

    @XmlAttribute
    public String getLocalFileMD5() {
        return localFileMD5;
    }

    public void setLocalFileMD5(String localFileMD5) {
        this.localFileMD5 = localFileMD5;
    }

    @XmlAttribute
    public String getServerETag() {
        return serverETag;
    }

    public void setServerETag(String serverETag) {
        this.serverETag = serverETag;
    }
}
