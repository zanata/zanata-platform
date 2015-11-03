/*
 * Copyright 2012, Red Hat, Inc. and individual contributors
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
package org.zanata.rest;

import java.io.InputStream;
import java.io.Serializable;

import javax.ws.rs.FormParam;

import org.jboss.resteasy.annotations.providers.multipart.PartType;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
public class GlossaryFileUploadForm implements Serializable {
    private static final long serialVersionUID = 1L;

    @FormParam("file")
    @PartType("application/octet-stream")
    private transient InputStream fileStream;

    @FormParam("srcLocale")
    @PartType("text/plain")
    private String srcLocale;

    @FormParam("transLocale")
    @PartType("text/plain")
    private String transLocale;

    @FormParam("fileName")
    @PartType("text/plain")
    private String fileName;


    public InputStream getFileStream() {
        return fileStream;
    }

    public void setFileStream(InputStream fileStream) {
        this.fileStream = fileStream;
    }

    public String getSrcLocale() {
        return srcLocale;
    }

    public void setSrcLocale(String srcLocale) {
        this.srcLocale = srcLocale;
    }

    public String getTransLocale() {
        return transLocale;
    }

    public void setTransLocale(String transLocale) {
        this.transLocale = transLocale;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
