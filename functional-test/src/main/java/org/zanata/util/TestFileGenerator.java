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
package org.zanata.util;


 import org.apache.commons.io.FileUtils;

 import java.io.*;

 /**
  * Create and manipulate basic text files for testing.
  *
  * @author Damian Jansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
  */
public class TestFileGenerator
{
   // Length is maximum filename length - 4 (.xxx) - 19 (for tmp file randomness)
   private static String longFileName = "lRRDXddgEnKzT2Wpu3VfT3Zs4pYuPXaqorA1CAtGcaZq6xydHdOghbsy"
      +"Pu5GnbbmknPNRZ0vc7IEaiPm59CBQ9NkIH1if9Y4uHHYgjWJT8Yhs5qibcEZDNAZwLmDNHaRJhQr2Y1z3VslMFGGS"
      +"P25eqzU1lDjejCsd26wRhT1UOkbhRRlm0ybGk8lTQgHEqT9sno1Veuw8A0StLGDfHAmCDFcUzAz9HMeuMUn9nFW";

   public TestFileGenerator()
   {
   }

   /**
    * Return a string with near maximum filename length
    * @return String
    */
   public String longFileName()
   {
      return longFileName;
   }

   /**
    * Create a test file in temporary storage with content.
    * Note that the file will contain random characters from the temporary file create process.
    *
    * @param fileName Prefix of file eg. "myTest"
    * @param suffix Suffix of file, eg. ".txt"
    * @param content Contents of the file, eg. "This is a test file"
    * @return File
    */
   public File generateTestFileWithContent(String fileName, String suffix, String content)
   {
      File tempFile = generateTestFile(fileName, suffix);
      setTestFileContent(tempFile, content);
      return tempFile;
   }

   private File generateTestFile(String fileName, String suffix)
   {
      File testFile;
      try
      {
         testFile = File.createTempFile(fileName, suffix);
      } catch(IOException ioException)
      {
         throw new RuntimeException("Unable to create temporary file "+fileName);
      }
      testFile.deleteOnExit();
      return testFile;
   }

   private void setTestFileContent(File testFile, String testContent)
   {
      try
      {
         FileWriter fileWriter = new FileWriter(testFile);
         fileWriter.write(testContent);
         fileWriter.flush();
         fileWriter.close();
      }
      catch(IOException ioException)
      {
         throw new RuntimeException("Could not open file for writing "+testFile.getName());
      }
   }

   /**
    * Change the size of a file to fileSize. The target file will be truncated or extended as
    * necessary.
    *
    * @param tempFile File to alter
    * @param fileSize Intended file size of resulting file
    */
   public void forceFileSize(File tempFile, long fileSize)
   {
      try
      {
         RandomAccessFile randomAccessFile = new RandomAccessFile(tempFile, "rw");
         randomAccessFile.setLength(fileSize);
      } catch(IOException e)
      {
         throw new RuntimeException("Unable to set the test file length");
      }
   }

   /**
    * Get the contents of the given file.
    *
    * @param testFile File to read contents from
    * @return String
    */
   public String getTestFileContent(File testFile)
   {
      String fileContents;
      try
      {
         fileContents = FileUtils.readFileToString(testFile);
      }
      catch(IOException ioException)
      {
         throw new RuntimeException("Could not read from test file.");
      }
      return fileContents;
   }

   /**
    * Gives the name of the first listed file in a directory. Intended for validating testing
    * upload of files to an empty directory.
    * Will throw an exception if no files are found.
    *
    * @param directory Storage directory of desired file.
    * @return String
    */
   public String getFirstFileNameInDirectory(String directory)
   {
      try
      {
         return new File(directory).list()[0];
      }
      catch(ArrayIndexOutOfBoundsException arrayException)
      {
         throw new RuntimeException("Expected files in dir "+directory+" but none found.");
      }
   }
}
