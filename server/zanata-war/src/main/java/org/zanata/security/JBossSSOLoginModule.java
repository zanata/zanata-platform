/*
 * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.security;

import java.io.IOException;
import java.util.Map;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.zanata.util.HashUtil;

/**
 * Login Module that works for JBoss SSO. The current implementation uses the
 * REST endpoint for authentication. The server url can be configured using the
 * 'serverURL' option when configuring the JAAS Login Module.
 *
 * @author Carlos Munoz
 *         <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class JBossSSOLoginModule implements LoginModule {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(JBossSSOLoginModule.class);

    private CallbackHandler callbackHandler;
    private Subject subject;
    private Map<String, ?> options;
    private String username;
    private char[] password;
    private String jbossSSOServerUrl = "https://sso.jboss.org";
    private boolean loginSuccessful;

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler,
            Map<String, ?> sharedState, Map<String, ?> options) {
        this.callbackHandler = callbackHandler;
        this.subject = subject;
        this.options = options;
        if (options.containsKey("serverURL")) {
            jbossSSOServerUrl = (String) options.get("serverURL");
        }
    }

    @Override
    public boolean login() throws LoginException {
        loginSuccessful = false;
        try {
            NameCallback cbName = new NameCallback("Enter username");
            PasswordCallback cbPassword =
                    new PasswordCallback("Enter password", false);
            // Get the username and password from the callback handler
            callbackHandler.handle(new Callback[] { cbName, cbPassword });
            username = cbName.getName();
            password = cbPassword.getPassword();
            // Send the request to JBoss.org's REST service
            HttpClient httpClient = new DefaultHttpClient();
            StringBuilder requestUrl =
                    new StringBuilder(jbossSSOServerUrl + "/rest/auth?");
            String passwordHash = HashUtil
                    .md5Hex(username + HashUtil.md5Hex(new String(password)));
            requestUrl.append("u=").append(username);
            requestUrl.append("&h=").append(passwordHash);
            HttpGet getAuthRequest = new HttpGet(requestUrl.toString());
            HttpResponse authResponse = httpClient.execute(getAuthRequest);
            loginSuccessful = authResponse.getStatusLine()
                    .getStatusCode() == HttpStatus.SC_OK;
            if (loginSuccessful) {
                // read json
                ObjectMapper mapper = new ObjectMapper();
                JsonNode parsedResponse =
                        mapper.readTree(authResponse.getEntity().getContent());
                // TODO These values should be used to pre-populate the
                // registration form when a user first registers
                // parsedResponse.get("fullname");
                // parsedResponse.get("email");
                log.info("JBoss.org user " + username
                        + " successfully authenticated");
                return true;
            } else {
                log.info("JBoss.org user " + username
                        + " failed authentication");
                throw new FailedLoginException();
            }
        } catch (UnsupportedCallbackException e) {
            throw new RuntimeException(e);
        } catch (IOException ex) {
            LoginException le = new LoginException(ex.getMessage());
            le.initCause(ex);
            throw le;
        }
    }

    @Override
    public boolean commit() throws LoginException {
        if (!loginSuccessful) {
            return false;
        }
        subject.getPrincipals().add(new SimplePrincipal(username));
        return true;
    }

    @Override
    public boolean abort() throws LoginException {
        return true;
    }

    @Override
    public boolean logout() throws LoginException {
        return true;
    }
}
