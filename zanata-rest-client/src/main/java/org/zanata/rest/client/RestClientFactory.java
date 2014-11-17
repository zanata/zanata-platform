/*
 * Copyright 2014, Red Hat, Inc. and individual contributors
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

package org.zanata.rest.client;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.rest.dto.VersionInfo;

import com.google.common.base.Throwables;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties;
import com.sun.jersey.multipart.impl.MultiPartWriter;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class RestClientFactory {
    private static final Logger log =
            LoggerFactory.getLogger(RestClientFactory.class);
    private String clientVersion;
    private VersionInfo clientApiVersion;

    private Client client;
    private URI baseURI;

    public RestClientFactory(URI base, String username, String apiKey,
            VersionInfo clientApiVersion, boolean logHttp,
            boolean sslCertDisabled) {
        baseURI = base;
        this.clientApiVersion = clientApiVersion;
        clientVersion = clientApiVersion.getVersionNo();
        DefaultClientConfig clientConfig =
                new DefaultClientConfig(MultiPartWriter.class);

        sslConfiguration(sslCertDisabled, clientConfig);
        clientConfig.getClasses().add(JacksonJsonProvider.class);

        client = Client.create(clientConfig);
        client.addFilter(
                new ApiKeyHeaderFilter(username, apiKey, clientVersion));
        client.addFilter(new TraceDebugFilter(logHttp));
    }

    private static void sslConfiguration(boolean sslCertDisabled,
            ClientConfig clientConfig) {
        if (!sslCertDisabled) {
            return;
        }
        try {
            final SSLContext sslContext = SSLContext.getInstance("TLS");

            // Create a trust manager that does not validate certificate chains
            // against our server
            final TrustManager[] trustAllCerts;
            trustAllCerts =
                    new TrustManager[] { new AcceptAllX509TrustManager() };
            sslContext.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection
                    .setDefaultSSLSocketFactory(sslContext
                            .getSocketFactory());
            clientConfig.getProperties().put(
                    HTTPSProperties.PROPERTY_HTTPS_PROPERTIES,
                    new HTTPSProperties(
                            new HostnameVerifier() {
                                @Override
                                public boolean verify(String s,
                                        SSLSession sslSession) {
                                    // whatever your matching policy states
                                    return true;
                                }
                            }, sslContext
                    ));
        } catch (Exception e) {
            log.warn("error creating SSL client", e);
            Throwables.propagate(e);
        }
    }

    public VersionInfo getServerVersionInfo() {
        return client.resource(getBaseUri()).path("version")
                .get(VersionInfo.class);
    }

    private URL getBaseUrl() {
        try {
            return new URL(fixBase(baseURI).toString() + getUrlPrefix());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    protected URI getBaseUri() {
        try {
            return getBaseUrl().toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static URI fixBase(URI base) {
        if (base != null) {
            String baseString = base.toString();
            if (!baseString.endsWith("/")) {
                try {
                    URI result = new URI(baseString + "/");
                    log.warn("Appending '/' to base URL '{}': using '{}'",
                            baseString, result);
                    return result;
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return base;
    }

    protected String getUrlPrefix() {
        return "rest/";
    }

    protected Client getClient() {
        return client;
    }

    private static class AcceptAllX509TrustManager implements X509TrustManager {
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public void
                checkClientTrusted(X509Certificate[] certs, String authType)
                        throws CertificateException {
        }

        public void
                checkServerTrusted(X509Certificate[] certs, String authType)
                        throws CertificateException {
        }
    }
}
