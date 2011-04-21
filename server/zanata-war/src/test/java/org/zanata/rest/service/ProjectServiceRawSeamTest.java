package org.zanata.rest.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;


import org.dbunit.operation.DatabaseOperation;
import org.jboss.seam.mock.EnhancedMockHttpServletRequest;
import org.jboss.seam.mock.EnhancedMockHttpServletResponse;
import org.jboss.seam.mock.ResourceRequestEnvironment;
import org.jboss.seam.mock.ResourceRequestEnvironment.Method;
import org.jboss.seam.mock.ResourceRequestEnvironment.ResourceRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.ZanataDBUnitSeamTest;
import org.zanata.rest.MediaTypes;

@Test(groups = { "seam-tests" })
public class ProjectServiceRawSeamTest extends ZanataDBUnitSeamTest
{
   private final Logger log = LoggerFactory.getLogger(ProjectServiceRawSeamTest.class);

   ResourceRequestEnvironment sharedEnvironment;

   @BeforeMethod(firstTimeOnly = true)
   public void prepareSharedEnvironment() throws Exception
   {
      sharedEnvironment = new ResourceRequestEnvironment(this)
      {
         @Override
         public Map<String, Object> getDefaultHeaders()
         {
            return new HashMap<String, Object>()
            {
               {
                  put("X-Auth-User", "admin");
                  put("X-Auth-Token", "b6d7044e9ee3b2447c28fb7c50d86d98");
               }
            };
         }
      };
      Runtime runtime = Runtime.getRuntime();
      log.info("project service raw seam test free memory :" + runtime.freeMemory());

   }

   @Override
   protected void prepareDBUnitOperations()
   {
      beforeTestOperations.add(new DataSetOperation("META-INF/testdata/ProjectsData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
   }

   public void retrieveListOfProjectsAsJson() throws Exception
   {
      new ResourceRequest(sharedEnvironment, Method.GET, "/restv1/projects")
      {
         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            request.addHeader("Accept", MediaType.APPLICATION_JSON);
         }

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assertThat(response.getStatus(), is(200));
         }
      };
   }

   public void retrieveListOfProjectsAsXml() throws Exception
   {
      new ResourceRequest(sharedEnvironment, Method.GET, "/restv1/projects")
      {
         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            request.addHeader("Accept", MediaTypes.APPLICATION_FLIES_PROJECTS_XML);
         }

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assertThat(response.getStatus(), is(200));

            String xmlContent = response.getContentAsString();

            assertThat(xmlContent, containsString("projects"));
            assertThat(xmlContent, containsString("Sample Project"));
         }

      }.run();
   }

}
