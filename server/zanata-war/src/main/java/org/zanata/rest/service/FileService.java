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
package org.zanata.rest.service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.zanata.adapter.po.PoWriter2;
import org.zanata.common.LocaleId;
import org.zanata.dao.DocumentDAO;
import org.zanata.model.HDocument;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;
import org.zanata.rest.dto.extensions.gettext.PoHeader;
import org.zanata.rest.dto.extensions.gettext.PotEntryHeader;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TextFlow;
import org.zanata.rest.dto.resource.TranslationsResource;

@Name("fileService")
@Path(FileService.SERVICE_PATH)
@Produces( { MediaType.APPLICATION_OCTET_STREAM })
@Consumes( { MediaType.APPLICATION_OCTET_STREAM })
public class FileService implements TranslationFileResource
{
   public static final String SERVICE_PATH = ProjectIterationService.SERVICE_PATH + "/file";
   
   @PathParam("projectSlug")
   private String projectSlug;

   @PathParam("iterationSlug")
   private String iterationSlug;
   
   @In
   private DocumentDAO documentDAO;
   
   @In(create=true)
   private TranslationResourcesService translationResourcesService;
   
   @In
   private ResourceUtils resourceUtils;
   
   public FileService()
   {
   }
   
   public FileService( final DocumentDAO documentDAO )
   {
      this.documentDAO = documentDAO;
   }
   
   @Override
   @GET
   @Path(FILE_DOWNLOAD_TEMPLATE)
   // /file/{locale}/{docId}.{fileExt}
   public Response downloadTranslationFile( @PathParam("locale") String locale, @PathParam("docId") String docId, 
         @PathParam("fileExt") String fileExtension )
   {
      final Response response; 
      StreamingOutput output = this.getStreamForFileExtension(fileExtension, docId, locale);
      
      if( output == null )
      {
         response = Response.status(Status.NOT_FOUND).build();
      }
      else
      {
         response = Response.ok().entity(output).build();
      }
      
      return response;
   }
   
   @Override
   @GET
   @Path(ALL_FILES_DOWNLOAD_TEMPLATE)
   // /file/{locale}
   public Response downloadAllFiles( @PathParam("locale") String locale )
   {
      final Response response; 
      StreamingOutput output = this.getStreamForFileExtension("zip", null, locale);
      
      if( output == null )
      {
         response = Response.status(Status.NOT_FOUND).build();
      }
      else
      {
         response = Response.ok()
               .header("Content-Disposition", "attachment; filename=\"" + this.iterationSlug + "-" + locale + "\"")
               .entity(output)
               .build();
      }
      
      return response;
   }
   
   
   private StreamingOutput getStreamForFileExtension( String fileExt, String docId, String locale )
   {
      // PO files
      if( fileExt.equalsIgnoreCase("po") )
      {
         HDocument document = this.documentDAO.getByProjectIterationAndDocId(this.projectSlug, this.iterationSlug, docId);
         
         // Perform translation of Hibernate DTOs to JAXB DTOs
         TranslationsResource transRes = 
               (TranslationsResource)this.translationResourcesService.getTranslations(docId, new LocaleId(locale)).getEntity();
         Resource res = this.resourceUtils.buildResource(document);
               
         return new POStreamingOutput(res, transRes);
      }
      // Zip files for the whole iteration
      else if( fileExt.equalsIgnoreCase("zip") && docId == null )
      {
         final List<HDocument> allIterationDocs = this.documentDAO.getAllByProjectIteration(this.projectSlug, this.iterationSlug);
         
         final TranslationsResource[] transRes = this.translationResourcesService.loadTranslations(allIterationDocs, new LocaleId(locale));
         final Resource[] res = new Resource[ allIterationDocs.size() ];
         
         for( int i=0; i<allIterationDocs.size(); i++ )
         {
            res[i] = this.resourceUtils.buildResource( allIterationDocs.get(i) );
         }
         
         return new ZippedPOStreamingOutput(res, transRes);
      }
      return null;
   }
   
   
   /*
    * Private class that implements PO file streaming of a document.
    */
   private class POStreamingOutput implements StreamingOutput
   {
      private Resource resource;
      private TranslationsResource transRes;
      
      public POStreamingOutput( Resource resource, TranslationsResource transRes )
      {
         this.resource = resource;
         this.transRes = transRes;
      }
      
      @Override
      public void write(OutputStream output) throws IOException, WebApplicationException
      {         
         PoWriter2 writer = new PoWriter2();
         writer.writePo(output, this.resource, this.transRes);
      }
   }
   
   
   /*
    * Private class that implements multi-file (as a zip archive) streaming for sets of PO files.
    */
   private class ZippedPOStreamingOutput implements StreamingOutput
   {
      private Resource[] resources;
      private TranslationsResource[] transResources;
      
      public ZippedPOStreamingOutput( Resource[] resources, TranslationsResource[] transResources )
      {
         if( resources.length != transResources.length )
         {
            throw new RuntimeException("The number of resources must match the number of Translation Resources");
         }
         
         this.resources = resources;
         this.transResources = transResources;
      }
      
      @Override
      public void write(OutputStream output) throws IOException, WebApplicationException
      {
         final ZipOutputStream zipOutput = new ZipOutputStream(output);
         zipOutput.setLevel(9);
         zipOutput.setMethod(ZipOutputStream.DEFLATED);
         final PoWriter2 poWriter = new PoWriter2();
         
         for( int i=0; i<this.resources.length; i++ )
         {
            zipOutput.putNextEntry( new ZipEntry( this.resources[i].getName() + ".po" ) );
            poWriter.writePo(zipOutput, this.resources[i], this.transResources[i]);
            zipOutput.closeEntry();
         }
         
         zipOutput.flush();
         zipOutput.close();
      }
   }
}
