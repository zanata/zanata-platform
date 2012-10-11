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
package org.zanata.client.commands.push;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.jboss.resteasy.client.ClientResponse;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.client.commands.PushPullCommand;
import org.zanata.client.util.ConsoleUtils;
import org.zanata.rest.DocumentFileUploadForm;
import org.zanata.rest.client.IFileResource;
import org.zanata.rest.client.ISourceDocResource;
import org.zanata.rest.client.ITranslatedDocResource;
import org.zanata.rest.client.ZanataProxyFactory;
import org.zanata.rest.dto.ChunkUploadResponse;

/**
 * Command to send files directly to the server without parsing on the client.
 * 
 * @author David Mason, <a href="mailto:damason@redhat.com">damason@redhat.com</a>
 *
 */
public class RawPushCommand extends PushPullCommand<RawPushOptions>
{
   private static final Logger log = LoggerFactory.getLogger(PushCommand.class);

   protected final IFileResource fileResource;

   private List<String> fileExtensions = new ArrayList<String>();

   public RawPushCommand(RawPushOptions opts)
   {
      super(opts);
      this.fileResource = getRequestFactory().getFileResource();

      // FIXME get this list from server
      // FIXME include command-line parameter for required types
      fileExtensions.add("txt");
   }

   public RawPushCommand(RawPushOptions opts, ZanataProxyFactory factory, ISourceDocResource sourceDocResource, ITranslatedDocResource translationResources, URI uri)
   {
      super(opts, factory, sourceDocResource, translationResources, uri);
      this.fileResource = factory.getFileResource();

      // FIXME get this list from server
      // FIXME include command-line parameter for required types
      fileExtensions.add("txt");
   }

   @Override
   public void run() throws Exception
   {
      // only supporting single module for now

      File sourceDir = getOpts().getSrcDir();
      if (!sourceDir.exists())
      {
         boolean enableModules = getOpts().getEnableModules();
         // TODO remove when modules implemented
         if (enableModules)
         {
            log.warn("enableModules=true but multi-modules not yet supported for this command. Using single module push.");
            enableModules = false;
         }

         if (enableModules)
         {
            log.info("source directory '" + sourceDir + "' not found; skipping docs push for module " + getOpts().getCurrentModule());
            return;
         }
         else
         {
            throw new RuntimeException("directory '" + sourceDir + "' does not exist - check srcDir option");
         }
      }

      RawPushStrategy strat = new RawPushStrategy();

      String[] srcFiles = strat.getSrcFiles(sourceDir, getOpts().getIncludes(), getOpts().getExcludes(), fileExtensions, true);

      // TODO handle obsolete document deletion
      log.warn("Obsolete document removal is not yet implemented, no documents will be removed from the server.\n");

      if (srcFiles.length == 0)
      {
         log.info("no documents in module: {}; nothing to do", getOpts().getCurrentModule());
         return;
      }

      if (getOpts().getPushType() == PushPullType.Trans || getOpts().getPushType() == PushPullType.Both)
      {
       log.warn("pushType set to '" + getOpts().getPushType() + "', but translation push is not yet implemented." +
                " Only source documents will be pushed.\n");

//         if (getOpts().getLocaleMapList() == null)
//            throw new ConfigException("pushType set to '" + getOpts().getPushType() + "', but zanata.xml contains no <locales>");
//         log.warn("pushType set to '" + getOpts().getPushType() + "': existing translations on server may be overwritten/deleted");

//         if (getOpts().getPushType() == PushPullType.Both)
//         {
//            confirmWithUser("This will overwrite existing documents AND TRANSLATIONS on the server, and delete obsolete documents.\n");
//         }
//         else if (getOpts().getPushType() == PushPullType.Trans)
//         {
//            confirmWithUser("This will overwrite existing TRANSLATIONS on the server.\n");
//         }
      }
      else
      {
//         confirmWithUser("This will overwrite existing documents on the server, and delete obsolete documents.\n");
         confirmWithUser("This will overwrite existing documents on the server.\n");
      }

      boolean hasErrors = false;

      for (String localDocName : srcFiles)
      {
         String qualifiedDocName = qualifiedDocName(localDocName);
         boolean sourcePushed = false;
         if (getOpts().getPushType() == PushPullType.Source || getOpts().getPushType() == PushPullType.Both)
         {
            if (!getOpts().isDryRun())
            {
               sourcePushed = pushSourceDocumentToServer(sourceDir, localDocName, qualifiedDocName);
//               ClientUtility.checkResult(putResponse, uri);
               if (!sourcePushed)
               {
                  hasErrors = true;
               }
            }
            else
            {
               log.info("pushing source doc [qualifiedname={}] to server (skipped due to dry run)", qualifiedDocName);
            }
         }

         if (sourcePushed && (getOpts().getPushType() == PushPullType.Trans || getOpts().getPushType() == PushPullType.Both))
         {
            log.warn("Translated document push not yet implemented. Skipping.\n");
            // TODO push translations. See PushCommand.pushCurrentModule() and strategy.visitTranslationResources
         }
      }

      if (hasErrors)
      {
         throw new Exception("Push completed with errors, see log for details.");
      }

   }

   /**
    * 
    * @param sourceDir
    * @param localDocName
    * @param qualifiedDocName docName with added module prefix
    * @return true if the push was successful
    * @throws FileNotFoundException
    * @throws NoSuchAlgorithmException
    * @throws IOException
    */
   private boolean pushSourceDocumentToServer(File sourceDir, String localDocName, String qualifiedDocName) throws FileNotFoundException, NoSuchAlgorithmException, IOException
   {
      log.info("pushing source document [{}] to server", qualifiedDocName);

      // FIXME use real value for document
      String fileType = "txt";

      File srcFile = new File(sourceDir, localDocName);
      String md5hash = calculateFileHash(srcFile);

      if (srcFile.length() <= getOpts().getChunkSize())
      {
         // single chunk
         log.info("    transmitting file [{}] as single chunk", srcFile.getAbsolutePath());
         return pushSourceDocumentSingleChunk(qualifiedDocName, fileType, srcFile, md5hash);
      }
      else
      {
         // multiple chunks
         long remainingLength = srcFile.length();
         int totalChunks = (int) (srcFile.length() / getOpts().getChunkSize()
               + (srcFile.length() % getOpts().getChunkSize() == 0 ? 0 : 1));
         int currentChunk = 1;
         log.info("    transmitting file [{}] as {} chunks", srcFile.getAbsolutePath(), totalChunks);
         log.info("        pushing chunk {} of {}", currentChunk, totalChunks);
         ClientResponse<ChunkUploadResponse> uploadResponse = pushSourceDocumentFirstChunk(qualifiedDocName, fileType, srcFile, md5hash, getOpts().getChunkSize());
         remainingLength -= getOpts().getChunkSize();
         int pos = getOpts().getChunkSize();
         Long uploadId = uploadResponse.getEntity().getUploadId();
         checkChunkUploadStatus(uploadResponse);
         if (uploadId == null)
         {
            throw new RuntimeException("server did not return upload id");
         }

         while (remainingLength > getOpts().getChunkSize())
         {
            log.info("        pushing chunk {} of {}", ++currentChunk, totalChunks);
            uploadResponse = pushSourceDocumentSubsequentChunk(uploadId, qualifiedDocName, fileType, srcFile, md5hash, pos, getOpts().getChunkSize(), false);
            checkChunkUploadStatus(uploadResponse);
            remainingLength -= getOpts().getChunkSize();
            pos += getOpts().getChunkSize();
         }

         log.info("        pushing chunk {} of {}", ++currentChunk, totalChunks);
         uploadResponse = pushSourceDocumentSubsequentChunk(uploadId, qualifiedDocName, fileType, srcFile, md5hash, pos, getOpts().getChunkSize(), true);
         checkChunkUploadStatus(uploadResponse);

         return true;
      }
   }

   private void checkChunkUploadStatus(ClientResponse<ChunkUploadResponse> uploadResponse)
   {
      if (uploadResponse.getStatus() >= 300) {
         throw new RuntimeException("Server returned error status: " + uploadResponse.getStatus()
               + ". Error message: " + uploadResponse.getEntity().getErrorMessage());
      }
   }

   private boolean pushSourceDocumentSingleChunk(String docName, String fileType, File srcFile, String md5hash) throws FileNotFoundException
   {
      ConsoleUtils.startProgressFeedback();
      DocumentFileUploadForm uploadForm = new DocumentFileUploadForm();
      uploadForm.setFileType(fileType);
      uploadForm.setFirst(true);
      uploadForm.setLast(true);
      uploadForm.setHash(md5hash);
      InputStream fileStream = new FileInputStream(srcFile);
      uploadForm.setFileStream(fileStream);
      ClientResponse<ChunkUploadResponse> response = fileResource.uploadSourceFile(getOpts().getProj(), getOpts().getProjectVersion(), docName, uploadForm);
      ConsoleUtils.endProgressFeedback();

      log.debug("response from server: {}", response.getEntity());
      return response.getStatus() < 300;
   }

   private ClientResponse<ChunkUploadResponse> pushSourceDocumentFirstChunk(String docName, String fileType, File srcFile, String md5hash, int chunkSize) throws FileNotFoundException
   {
      ConsoleUtils.startProgressFeedback();
      DocumentFileUploadForm uploadForm = new DocumentFileUploadForm();
      uploadForm.setFileType(fileType);
      uploadForm.setFirst(true);
      uploadForm.setLast(false);
      uploadForm.setHash(md5hash);

      byte[] chunk = new byte[chunkSize];
      InputStream fileStream = new FileInputStream(srcFile);
      int actualChunkSize;
      try
      {
         actualChunkSize = fileStream.read(chunk);
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
      fileStream = new ByteArrayInputStream(chunk, 0, actualChunkSize);
      uploadForm.setFileStream(fileStream);
      ClientResponse<ChunkUploadResponse> response = fileResource.uploadSourceFile(getOpts().getProj(), getOpts().getProjectVersion(), docName, uploadForm);
      ConsoleUtils.endProgressFeedback();

      log.debug("response from server: {}", response.getEntity());
      return response;
   }

   private ClientResponse<ChunkUploadResponse> pushSourceDocumentSubsequentChunk(Long uploadId, String docName, String fileType, File srcFile, String md5hash, int startPos, int chunkSize, boolean isLast) throws FileNotFoundException
   {
      ConsoleUtils.startProgressFeedback();
      DocumentFileUploadForm uploadForm = new DocumentFileUploadForm();
      uploadForm.setUploadId(uploadId);
      uploadForm.setFileType(fileType);
      uploadForm.setFirst(false);
      uploadForm.setLast(isLast);
      uploadForm.setHash(md5hash);

      byte[] chunk = new byte[chunkSize];
      InputStream fileStream = new FileInputStream(srcFile);
      try
      {
         fileStream.skip(startPos);
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
      int actualChunkSize;
      try
      {
         actualChunkSize = fileStream.read(chunk, 0, chunkSize);
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
      fileStream = new ByteArrayInputStream(chunk, 0, actualChunkSize);

      uploadForm.setFileStream(fileStream);
      ClientResponse<ChunkUploadResponse> response = fileResource.uploadSourceFile(getOpts().getProj(), getOpts().getProjectVersion(), docName, uploadForm);
      ConsoleUtils.endProgressFeedback();

      log.debug("response from server: {}", response.getEntity());
      return response;
   }

   private String calculateFileHash(File srcFile) throws FileNotFoundException, NoSuchAlgorithmException, IOException
   {
      InputStream fileStream = new FileInputStream(srcFile);
      MessageDigest md = MessageDigest.getInstance("MD5");
      fileStream = new DigestInputStream(fileStream, md);
      byte[] buffer = new byte[256];
      while (fileStream.read(buffer) > 0)
      {
         // continue
      }
      fileStream.close();
      String md5hash = new String(Hex.encodeHex(md.digest()));
      return md5hash;
   }

}
