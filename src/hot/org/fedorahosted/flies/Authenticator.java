package org.fedorahosted.flies;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Credentials;
import org.jboss.seam.security.Identity;

@Name("authenticator")
public class Authenticator
{
    @Logger Log log;

    @In Identity identity;
    @In Credentials credentials;

    public boolean authenticate()
    {
        log.info("authenticating {0}", credentials.getUsername());
        //write your authentication logic here,
        //return true if the authentication was
        //successful, false otherwise
        if ("admin".equals(credentials.getUsername()))
        {
            identity.addRole("admin");
            return true;
        }
        return false;
    }

}
