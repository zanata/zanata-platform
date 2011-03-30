package net.openl10n.flies.security;

import javax.faces.context.FacesContext;

import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.ui.ClientUidSelector;
import org.jboss.seam.util.RandomStringUtils;

@Name("org.jboss.seam.ui.clientUidSelector")
@Install(precedence = Install.DEPLOYMENT)
/**
 * Workaround for https://jira.jboss.org/browse/JBSEAM-4503
 * supplied by "wolfgang geck" in 
 * http://seamframework.org/Community/SeamTokenTagProblemCSRF#comment110227
 */
public class FixedClientUidSelector extends ClientUidSelector
{

   private static final long serialVersionUID = -4923235748771706010L;
   private String clientUid;

   @Create
   public void onCreate()
   {
      String requestContextPath = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
      // workaround for https://issues.jboss.org/browse/JBSEAM-4701
      if (requestContextPath.isEmpty()) {
         requestContextPath = "/";
      }
      setCookiePath(requestContextPath);
      setCookieMaxAge(-1);
      setCookieEnabled(true);
      clientUid = getCookieValue();
   }

   public void seed()
   {
      if (!isSet())
      {
         // workaround for https://issues.jboss.org/browse/JBSEAM-4503
         clientUid = RandomStringUtils.random(50, true, true); // Fixed
         setCookieValueIfEnabled(clientUid);
      }
   }

   public boolean isSet()
   {
      return clientUid != null;
   }

   public String getClientUid()
   {
      return clientUid;
   }

   @Override
   protected String getCookieName()
   {
      return "javax.faces.ClientToken";
   }

}