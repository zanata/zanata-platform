package org.fedorahosted.flies.rest.impl;

import org.jboss.resteasy.plugins.providers.atom.Content;
import org.jboss.resteasy.plugins.providers.atom.Entry;

import java.io.*;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import java.lang.String;

@Path("atom")
public class ProjectUpload
{
   @POST
   @Path("entry")
   @Produces("application/atom+xml")
   public String postProject(Entry entry) throws IOException
   {
      Content content = entry.getContent();
      String cont = content.getText();
      File file = new File("/home/jamesni/Desktop", "test.po");
      PrintWriter out = new PrintWriter(new FileWriter(file));
      out.print(cont);
      out.close();
      return "success";
   }
   
   
}

