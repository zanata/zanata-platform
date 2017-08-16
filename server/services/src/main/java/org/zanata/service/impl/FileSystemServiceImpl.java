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
package org.zanata.service.impl;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Properties;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import org.zanata.service.FileSystemService;

/**
 * Default implementation of the File System Service, offering various file
 * system related operations.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Named("fileSystemServiceImpl")
@RequestScoped
public class FileSystemServiceImpl implements FileSystemService {
    private static final long serialVersionUID = 829630909431691575L;
    private static final File STAGING_DIR = new File(
            System.getProperty("java.io.tmpdir"));

    private static final String DOWNLOAD_FILE_PREFIX = "znt-dw";

    private static final String DOWNLOAD_FILE_DESCRIPTOR_SUFFIX = ".xml";

    /**
     * @see org.zanata.service.FileSystemService#createDownloadDescriptorFile(java.io.File,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public String createDownloadDescriptorFile(File physicalFile,
            String downloadFileName, String generatingUser) throws IOException {
        String descriptorFileName = physicalFile.getName();
        if (descriptorFileName.indexOf('.') > 0) {
            descriptorFileName =
                    descriptorFileName.substring(0,
                            descriptorFileName.lastIndexOf('.'));
        }

        File descriptorFile =
                new File(STAGING_DIR, descriptorFileName
                        + DOWNLOAD_FILE_DESCRIPTOR_SUFFIX);
        Properties descriptorProps = new Properties();
        descriptorProps.put(
                DownloadDescriptorProperties.DownloadFileName.toString(),
                downloadFileName);
        descriptorProps.put(
                DownloadDescriptorProperties.PhysicalFileName.toString(),
                physicalFile.getName());
        descriptorProps.put(
                DownloadDescriptorProperties.OriginatingUserId.toString(),
                generatingUser);
        FileOutputStream outputStream =
                new FileOutputStream(descriptorFile);
        try {
            descriptorProps
                    .storeToXML(outputStream, "Zanata Download Descriptor");
        } finally {
            outputStream.close();
        }
        // Generate the download Id based on the File name

        return descriptorFileName;
    }

    @Override
    public boolean deleteDownloadDescriptorFile(String downloadId) {
        File descriptorFile =
                new File(STAGING_DIR, downloadId
                        + DOWNLOAD_FILE_DESCRIPTOR_SUFFIX);

        return descriptorFile.exists() && descriptorFile.delete();
    }

    /**
     * @see org.zanata.service.FileSystemService#createDownloadStagingFile(java.lang.String)
     */
    @Override
    public File createDownloadStagingFile(String fileExtension)
            throws IOException {
        return File.createTempFile(DOWNLOAD_FILE_PREFIX, "." + fileExtension,
                STAGING_DIR);
    }

    /**
     * @see org.zanata.service.FileSystemService#getAllExpiredDownloadFiles()
     */
    @Override
    public File[] getAllExpiredDownloadFiles() {
        // All files generated more than one day ago will be removed
        final Calendar removalThreshold = Calendar.getInstance();
        removalThreshold.add(Calendar.DATE, -1);

        return STAGING_DIR.listFiles(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.getName().startsWith(DOWNLOAD_FILE_PREFIX)
                        && f.lastModified() < removalThreshold
                                .getTimeInMillis();
            }
        });
    }

    /**
     * @see org.zanata.service.FileSystemService#findDownloadDescriptorProperties(java.lang.String)
     */
    @Override
    public Properties findDownloadDescriptorProperties(String downloadId)
            throws IOException {
        final File f =
                new File(STAGING_DIR, downloadId
                        + DOWNLOAD_FILE_DESCRIPTOR_SUFFIX);

        if (f.exists()) {
            // Read the properties from the descriptor file
            Properties descriptorProps = new Properties();
            FileInputStream inputStream = new FileInputStream(f);
            try {
                descriptorProps.loadFromXML(inputStream);
            } finally {
                inputStream.close();
            }
            return descriptorProps;
        } else {
            return null;
        }
    }

    /**
     * @see org.zanata.service.FileSystemService#findDownloadFile(java.lang.String)
     */
    @Override
    public File findDownloadFile(String downloadId) throws IOException {
        // Read the properties from the descriptor file
        Properties descriptorProps =
                this.findDownloadDescriptorProperties(downloadId);

        if (descriptorProps == null) {
            return null;
        }

        // Read The file (should be in the same directory as the descriptor
        // file)
        File toDownload =
                new File(
                        STAGING_DIR,
                        descriptorProps
                                .getProperty(DownloadDescriptorProperties.PhysicalFileName
                                        .toString()));

        if (toDownload.exists()) {
            return toDownload;
        } else {
            return null;
        }
    }

}
