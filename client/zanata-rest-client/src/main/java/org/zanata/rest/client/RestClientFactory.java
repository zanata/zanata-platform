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
import java.util.function.Consumer;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.rest.MediaTypes;
import org.zanata.rest.RestConstant;
import org.zanata.rest.dto.VersionInfo;

import com.google.common.base.Throwables;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class RestClientFactory {
    private static final Logger log =
            LoggerFactory.getLogger(RestClientFactory.class);
    private String serverVersion;
    private String clientVersion;
    private VersionInfo clientApiVersion;

    private Client client;
    private URI baseURI;

    // for use by InitCommand
    protected RestClientFactory() {
    }

    public RestClientFactory(URI base, String username, String apiKey,
            VersionInfo clientApiVersion, boolean logHttp,
            boolean sslCertDisabled) {
        baseURI = base;
        this.clientApiVersion = clientApiVersion;
        clientVersion = clientApiVersion.getVersionNo();

        this.client = new ResteasyClientBuilder()
                .sslContext(sslConfiguration(sslCertDisabled))
                .register(new RedirectFilter())
                .register(new ResponseStatusFilter())
                .register(new ApiKeyHeaderFilter(username, apiKey, clientVersion))
                .register(new TraceDebugFilter(logHttp))
                .register(new InvalidContentTypeFilter())
                .build();
    }

    /**
     * This constructor will provider an extension point for other class to
     * customize resteasy client builder.
     *
     * @param base
     *         zanata server REST api base url
     * @param username
     *         zanata username
     * @param apiKey
     *         zanata api key
     * @param clientApiVersion
     *         client api version
     * @param logHttp
     *         whether to log http request and response
     * @param sslCertDisabled
     *         whether ssl certificate verification will be disabled
     * @param resteasyClientBuilderModifier
     *         resteasy client builder customization function
     */
    public RestClientFactory(URI base, String username, String apiKey,
            VersionInfo clientApiVersion, boolean logHttp,
            boolean sslCertDisabled,
            Consumer<ResteasyClientBuilder> resteasyClientBuilderModifier) {
        this.baseURI = base;
        this.clientApiVersion = clientApiVersion;
        this.clientVersion = clientApiVersion.getVersionNo();
        ResteasyClientBuilder clientBuilder = new ResteasyClientBuilder()
                .sslContext(sslConfiguration(sslCertDisabled))
                .register(new RedirectFilter())
                .register(new ResponseStatusFilter())
                .register(
                        new ApiKeyHeaderFilter(username, apiKey, clientVersion))
                .register(new TraceDebugFilter(logHttp))
                .register(new InvalidContentTypeFilter());
        resteasyClientBuilderModifier.accept(clientBuilder);
        this.client = clientBuilder
                .build();
        ;
    }

    private static SSLContext sslConfiguration(boolean sslCertDisabled) {
        if (!sslCertDisabled) {
            return null;
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
            return sslContext;
        } catch (Exception e) {
            log.warn("error creating SSL client", e);
            throw Throwables.propagate(e);
        }
    }

    public VersionInfo getServerVersionInfo() {
        return client.target(getBaseUri()).path("version")
                .request(MediaTypes.APPLICATION_ZANATA_VERSION_XML)
                .get(VersionInfo.class);
    }

    public void performVersionCheck() {
        clientVersion = clientApiVersion.getVersionNo();
        String clientScm = clientApiVersion.getScmDescribe();

        VersionInfo serverVersionInfo = getServerVersionInfo();
        serverVersion = serverVersionInfo.getVersionNo();
        String serverScm = serverVersionInfo.getScmDescribe();
        log.info("client API version: {}, server API version: {}",
                clientVersion, serverVersion);
        warnMismatchAPIVersion(clientScm, serverScm);
    }

    private void warnMismatchAPIVersion(String clientScm, String serverScm) {
        if (!serverVersion.equals(clientVersion)) {
            log.warn("client API version is {}, but server API version is {}",
                    clientVersion, serverVersion);
        } else if (serverVersion.contains(RestConstant.SNAPSHOT_VERSION)
                && !serverScm.equalsIgnoreCase(clientScm)) {
            log.warn(
                    "client API SCM id is {}, but server API SCM id is {}",
                    clientScm, serverScm);
        }
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

    /**
     * Compares a given version identifier with the server version.
     *
     * @param version
     *            The version to against which to compare the server version.
     * @return A positive integer if the server version is greater than the
     *         given version. A negative integer if the server version is less
     *         than the given version. 0 if both versions are the same.
     */
    public int compareToServerVersion(String version) {
        DefaultArtifactVersion srvVersion =
                new DefaultArtifactVersion(serverVersion);
        DefaultArtifactVersion providedVersion =
                new DefaultArtifactVersion(version);

        return srvVersion.compareTo(providedVersion);
    }

    public AccountClient getAccountClient() {
        return new AccountClient(this);
    }

    public AsyncProcessClient getAsyncProcessClient() {
        return new AsyncProcessClient(this);
    }

    public CopyTransClient getCopyTransClient() {
        return new CopyTransClient(this);
    }

    public FileResourceClient getFileResourceClient() {
        return new FileResourceClient(this);
    }

    public GlossaryClient getGlossaryClient() {
        return new GlossaryClient(this);
    }

    public ProjectClient getProjectClient(String projectSlug) {
        return new ProjectClient(this, projectSlug);
    }

    public ProjectIterationClient getProjectIterationClient(String projectSlug,
            String versionSlug) {
        return new ProjectIterationClient(this, projectSlug, versionSlug);
    }

    public ProjectsClient getProjectsClient() {
        return new ProjectsClient(this);
    }

    public SourceDocResourceClient getSourceDocResourceClient(
            String projectSlug, String versionSlug) {
        return new SourceDocResourceClient(this, projectSlug, versionSlug);
    }

    public StatisticsResourceClient getStatisticsClient() {
        return new StatisticsResourceClient(this);
    }

    public TransDocResourceClient getTransDocResourceClient(String projectSlug,
            String versionSlug) {
        return new TransDocResourceClient(this, projectSlug, versionSlug);
    }

    public ProjectIterationLocalesClient getProjectLocalesClient(
            String projectSlug, String versionSlug) {
        return new ProjectIterationLocalesClient(this, projectSlug, versionSlug);
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
