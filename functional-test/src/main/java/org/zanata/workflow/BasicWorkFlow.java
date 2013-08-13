package org.zanata.workflow;

import org.openqa.selenium.support.PageFactory;
import org.zanata.page.AbstractPage;
import org.zanata.page.utility.DashboardPage;
import org.zanata.page.utility.HomePage;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class BasicWorkFlow extends AbstractWebWorkFlow
{
   public <P extends AbstractPage> P goToPage(String url, Class<P> pageClass)
   {
      driver.get(toUrl(url));
      return PageFactory.initElements(driver, pageClass);
   }

   private String toUrl(String relativeUrl)
   {
      return hostUrl + removeLeadingSlash(relativeUrl);
   }
   
   private static String removeLeadingSlash(String relativeUrl)
   {
      if (relativeUrl.startsWith("/"))
      {
         return relativeUrl.substring(1, relativeUrl.length());
      }
      return relativeUrl;
   }

}
