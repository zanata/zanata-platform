package org.fedorahosted.flies.rest.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.jboss.resteasy.plugins.providers.atom.Content;
import org.jboss.resteasy.plugins.providers.atom.Entry;

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

