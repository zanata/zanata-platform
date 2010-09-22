package net.openl10n.flies.rest.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.jboss.resteasy.client.ClientRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.allen_sauer.gwt.log.client.Log;

import net.openl10n.flies.rest.dto.resource.Resource;

public class RestUtilsTest
{
   private ResourceTestObjectFactory resourceTestFactory = new ResourceTestObjectFactory();
   private final Logger log = LoggerFactory.getLogger(RestUtilsTest.class);
   @DataProvider(name = "ResourceTestData")
   public Object[][] getResourceTestData()
   {
      return new Object[][] { new Object[] { resourceTestFactory.getPoHeaderTest() }, new Object[] { resourceTestFactory.getPotEntryHeaderTest() }, new Object[] { resourceTestFactory.getTextFlowCommentTest() }
      // , new Object[] { resourceTestFactory.getPotEntryHeaderComment() }
      };
   }

   @Test(dataProvider = "ResourceTestData")
   public void testUnmarshall(Resource res)
   {
      SeamMockClientExecutor test = new SeamMockClientExecutor();
      ClientRequest client = test.createRequest("http://example.com/");
      MultivaluedMap<String, String> header = client.getHeaders();

      InputStream messageBody = null;
      try
      {
         String testStr = res.toString();
         log.info("expect:" + testStr);

         messageBody = new ByteArrayInputStream(testStr.getBytes("UTF-8"));
         Resource entity = RestUtils.unmarshall(Resource.class, messageBody, MediaType.APPLICATION_XML_TYPE, header);
         Log.info("got:" + entity.toString());
         assertThat(entity.toString(), is(testStr));

      }
      catch (UnsupportedEncodingException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      finally
      {
         if (messageBody != null)
         {
            try
            {
               messageBody.close();
            }
            catch (IOException e)
            {
               // TODO Auto-generated catch block
               e.printStackTrace();
            }
         }
      }

   }

}
