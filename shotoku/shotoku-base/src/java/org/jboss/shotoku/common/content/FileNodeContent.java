/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.shotoku.common.content;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.jboss.shotoku.exceptions.RepositoryException;
import org.jboss.shotoku.tools.Tools;

/**
 * A subclass of NodeContent, which reads content from a given file, if the
 * content is unchanged (so on first content set, this will start behaving
 * like an ordinary NodeContent).
 * @author Adam Warski (adamw@aster.pl)
 */
public class FileNodeContent extends NodeContent {
    private File file;

    public FileNodeContent(File file) {
        this.file = file;
    }

    @Override
    public byte[] asByteArray() {
        if (getChanged()) {
            return super.asByteArray();
        } else {
            try {
                return Tools.getFileBytes(file).array();
            } catch (IOException e) {
                throw new RepositoryException(e);
            }
        }
    }

    @Override
    public InputStream asInputStream() {
        if (getChanged()) {
            return super.asInputStream();
        } else {
            try {
                return new FileInputStream(file);
            } catch (IOException e) {
                throw new RepositoryException(e);
            }
        }
    }

    @Override
    public String asString() {
        if (getChanged()) {
            return super.asString();
        } else {
            try {
                return Tools.getFileString(file);
            } catch (IOException e) {
                throw new RepositoryException(e);
            }
        }
    }

    @Override
    public long getLength() {
        if (getChanged()) {
            return super.getLength();
        } else {
            return file.length();
        }
    }
}
