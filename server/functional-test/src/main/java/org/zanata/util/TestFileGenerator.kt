/*
 * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.util

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter
import java.io.RandomAccessFile
import java.net.URL
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBException
import javax.xml.bind.Marshaller
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlElementWrapper
import javax.xml.bind.annotation.XmlElements
import javax.xml.bind.annotation.XmlRootElement
import com.google.common.base.Preconditions
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.ObjectUtils
import org.fedorahosted.openprops.Properties

/**
 * Create and manipulate basic text files for testing.
 *
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
class TestFileGenerator {

    /**
     * Return a string with near maximum filename length
     *
     * @return String
     */
    @Suppress("unused")
    fun longFileName(): String {
        return longFileName
    }

    /**
     * Create a test file in temporary storage with content. Note that the file
     * will contain random characters from the temporary file creation process.
     *
     * @param fileName Prefix of file eg. "myTest"
     * @param suffix Suffix of file, eg. ".txt"
     * @param content Contents of the file, eg. "This is a test file"
     * @return File object for created file
     */
    fun generateTestFileWithContent(fileName: String, suffix: String,
                                    content: String): File {
        val tempFile = generateTestFile(fileName, suffix)
        setTestFileContent(tempFile, content)
        return tempFile
    }

    private fun generateTestFile(fileName: String, suffix: String): File {
        val testFile: File
        try {
            testFile = File.createTempFile(fileName, suffix)
        } catch (ioException: IOException) {
            throw RuntimeException(
                    "Unable to create temporary file $fileName")
        }

        testFile.deleteOnExit()
        return testFile
    }

    private fun setTestFileContent(testFile: File, testContent: String) {
        try {
            val outputStreamWriter = OutputStreamWriter(FileOutputStream(testFile),
                    Charset.forName("UTF-8").newEncoder())
            outputStreamWriter.write(testContent.replace("\n".toRegex(), System.getProperty("line.separator")))
            outputStreamWriter.flush()
            outputStreamWriter.close()
        } catch (ioException: IOException) {
            throw RuntimeException(
                    "Could not open file for writing " + testFile.name)
        }

    }

    /**
     * Change the size of a file to fileSize. The target file will be truncated
     * or extended as necessary.
     *
     * @param tempFile
     * File to alter
     * @param fileSize
     * Intended file size of resulting file
     * @throws RuntimeException
     * if the file cannot be altered
     */
    @Suppress("unused")
    fun forceFileSize(tempFile: File, fileSize: Long) {
        try {
            RandomAccessFile(tempFile, "rw").use { randomAccessFile -> randomAccessFile.setLength(fileSize) }
        } catch (e: IOException) {
            throw RuntimeException("Unable to set the test file length")
        }

    }

    /**
     * Get the contents of the given file.
     *
     * @param testFile
     * File to read contents from
     * @return contents of the target file
     * @throws RuntimeException
     * if the file cannot be read
     */
    @Suppress("unused")
    fun getTestFileContent(testFile: File): String {
        val fileContents: String
        try {
            fileContents = testFile.inputStream().readBytes().toString()
        } catch (ioException: IOException) {
            throw RuntimeException("Could not read from test file.")
        }

        return fileContents
    }

    /**
     * Gives the name of the first listed file in a directory. Intended for
     * validating testing upload of files to an empty directory.
     *
     * @param directory
     * Storage directory of desired file.
     * @return name of first listed file in target directory
     * @throws RuntimeException if no files are found
     */
    @Suppress("unused")
    fun getFirstFileNameInDirectory(directory: String): String {
        try {
            val list = ObjectUtils.firstNonNull(
                    File(directory).list(), arrayOfNulls(0))
            return list[0]
        } catch (arrayException: ArrayIndexOutOfBoundsException) {
            throw RuntimeException(
                    "Expected files in dir $directory but none found.")
        }

    }

    @XmlRootElement(namespace = ZanataXml.NS, name = "config")
    private class ZanataXml {
        @XmlElement(namespace = ZanataXml.NS)
        private var url = PropertiesHolder.getProperty(Constants.zanataInstance.value()!!)
        @XmlElement(namespace = ZanataXml.NS)
        private var project: String? = null
        @XmlElement(name = "project-version", namespace = ZanataXml.NS)
        private var projectVersion: String? = null
        @XmlElement(name = "project-type", namespace = ZanataXml.NS)
        private var projectType: String? = null
        @XmlElementWrapper(name = "locales", namespace = ZanataXml.NS)
        @XmlElements(XmlElement(name = "locale", namespace = ZanataXml.NS))
        private var locales: List<String>? = null

        @Suppress("unused")
        fun setUrl(url: String) {
            this.url = url
        }

        fun setProject(project: String) {
            this.project = project
        }

        fun setProjectVersion(projectVersion: String) {
            this.projectVersion = projectVersion
        }

        fun setProjectType(projectType: String) {
            this.projectType = projectType
        }

        fun setLocales(locales: List<String>) {
            this.locales = locales
        }

        companion object {
            internal const val NS = "http://zanata.org/namespace/config/"
        }
    }

    fun openTestFile(filename: String): File {
        val testFile: File
        val url = Thread.currentThread().contextClassLoader
                .getResource(filename)
        Preconditions.checkNotNull(url, "File %s url is null", filename)
        testFile = File(url!!.path)
        Preconditions.checkArgument(testFile.exists(), "%s not found",
                testFile)
        return testFile
    }

    companion object {
        // Length is maximum filename length - 4 (.xxx) - 19 (for tmp file
        // randomness)
        private const val longFileName = "lRRDXddgEnKzT2Wpu3VfT3Zs4pYuPXaqorA1CAtGcaZq6xydHdOghbsyPu5GnbbmknPNRZ0vc7IEaiPm59CBQ9NkIH1if9Y4uHHYgjWJT8Yhs5qibcEZDNAZwLmDNHaRJhQr2Y1z3VslMFGGSP25eqzU1lDjejCsd26wRhT1UOkbhRRlm0ybGk8lTQgHEqT9sno1Veuw8A0StLGDfHAmCDFcUzAz9HMeuMUn9nFW"

        /**
         * Generates a zanata.xml with url default to test instance.
         *
         * @param output
         * where to write it
         * @param projectSlug
         * project slug
         * @param versionSlug
         * version slug
         * @param projectType
         * project type
         * @param locales
         * locales
         */
        fun generateZanataXml(output: File, projectSlug: String,
                              versionSlug: String, projectType: String, locales: List<String>) {
            val zanataXml = ZanataXml()
            zanataXml.setProject(projectSlug)
            zanataXml.setProjectVersion(versionSlug)
            zanataXml.setProjectType(projectType)
            zanataXml.setLocales(locales)
            marshall(output, zanataXml, ZanataXml::class.java)
        }

        private fun <T> marshall(output: File, `object`: T, xmlClass: Class<T>) {
            try {
                val jaxbContext = JAXBContext.newInstance(xmlClass)
                val jaxbMarshaller = jaxbContext.createMarshaller()
                jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
                jaxbMarshaller.marshal(`object`, output)
            } catch (e: JAXBException) {
                throw RuntimeException(e)
            }

        }

        @Throws(IOException::class)
        fun makePropertiesFile(file: File,
                               entries: Map<String, String>) {
            val resource = Properties()
            for ((key, value) in entries) {
                resource.setProperty(key, value)
            }
            resource.store(OutputStreamWriter(FileOutputStream(file),
                    StandardCharsets.UTF_8), null)
        }
    }
}
