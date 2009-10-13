package org.fedorahosted.flies.webtrans.server;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.methods.multipart.ByteArrayPartSource;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;

/**
 * From:
 * http://www.therichwebexperience.com/blog/matt_raible/2009/08/how_to_do_cross_domain_gwt_rpc_with_a_proxyservlet
 * 
 * 
 * ProxyServlet from http://edwardstx.net/wiki/attach/HttpProxyServlet/ProxyServlet.java
 * (This seems to be a derivative of Noodle -- http://noodle.tigris.org/)
 * 
 * Patched to skip "Transfer-Encoding: chunked" headers, avoid double slashes
 * in proxied URLs, handle GZip and allow GWT RPC.
 */
public class ProxyServlet extends HttpServlet {

    private static final int FOUR_KB = 4196;

    /**
     * Serialization UID.
     */
    private static final long serialVersionUID = 1L;
    /**
     * Key for redirect location header.
     */
    private static final String STRING_LOCATION_HEADER = "Location";
    /**
     * Key for content type header.
     */
    private static final String STRING_CONTENT_TYPE_HEADER_NAME = "Content-Type";
    /**
     * Key for content length header.
     */
    private static final String STRING_CONTENT_LENGTH_HEADER_NAME = "Content-Length";
    /**
     * Key for host header
     */
    private static final String STRING_HOST_HEADER_NAME = "Host";
    /**
     * The directory to use to temporarily store uploaded files
     */
    private static final File FILE_UPLOAD_TEMP_DIRECTORY = new File(System.getProperty("java.io.tmpdir"));

    // Proxy host params
    /**
     * The host to which we are proxying requests. Default value is "localhost".
     */
    private String stringProxyHost = "localhost";
    /**
     * The port on the proxy host to wihch we are proxying requests. Default value is 80.
     */
    private int intProxyPort = 80;
    /**
     * The (optional) path on the proxy host to wihch we are proxying requests. Default value is "".
     */
    private String stringProxyPath = "";
    /**
     * Setting that allows removing the initial path from client. Allows specifying /twitter/* as synonym for twitter.com.
     */
    private boolean removePrefix;
    /**
     * The maximum size for uploaded files in bytes. Default value is 5MB.
     */
    private int intMaxFileUploadSize = 5 * 1024 * 1024;
    private boolean isSecure;
    private boolean followRedirects;

    /**
     * Initialize the <code>ProxyServlet</code>
     * @param servletConfig The Servlet configuration passed in by the servlet container
     */
    public void init(ServletConfig servletConfig) {
        // Get the proxy host
        String stringProxyHostNew = servletConfig.getInitParameter("proxyHost");
        if (stringProxyHostNew == null || stringProxyHostNew.length() == 0) {
            throw new IllegalArgumentException("Proxy host not set, please set init-param 'proxyHost' in web.xml");
        }
        this.setProxyHost(stringProxyHostNew);
        // Get the proxy port if specified
        String stringProxyPortNew = servletConfig.getInitParameter("proxyPort");
        if (stringProxyPortNew != null && stringProxyPortNew.length() > 0) {
            this.setProxyPort(Integer.parseInt(stringProxyPortNew));
        }
        // Get the proxy path if specified
        String stringProxyPathNew = servletConfig.getInitParameter("proxyPath");
        if (stringProxyPathNew != null && stringProxyPathNew.length() > 0) {
            this.setProxyPath(stringProxyPathNew);
        }
        // Get the maximum file upload size if specified
        String stringMaxFileUploadSize = servletConfig.getInitParameter("maxFileUploadSize");
        if (stringMaxFileUploadSize != null && stringMaxFileUploadSize.length() > 0) {
            this.setMaxFileUploadSize(Integer.parseInt(stringMaxFileUploadSize));
        }
    }

    /**
     * Performs an HTTP GET request
     * @param httpServletRequest The {@link HttpServletRequest} object passed
     *                            in by the servlet engine representing the
     *                            client request to be proxied
     * @param httpServletResponse The {@link HttpServletResponse} object by which
     *                             we can send a proxied response to the client
     */
    public void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws IOException, ServletException {
        // Create a GET request
        String destinationUrl = this.getProxyURL(httpServletRequest);
        debug("GET Request URL: " + httpServletRequest.getRequestURL(),
              "Destination URL: " + destinationUrl);
        GetMethod getMethodProxyRequest = new GetMethod(destinationUrl);
        // Forward the request headers
        setProxyRequestHeaders(httpServletRequest, getMethodProxyRequest);
        setProxyRequestCookies(httpServletRequest, getMethodProxyRequest);
        // Execute the proxy request
        this.executeProxyRequest(getMethodProxyRequest, httpServletRequest, httpServletResponse);
    }

    /**
     * Performs an HTTP POST request
     * @param httpServletRequest The {@link HttpServletRequest} object passed
     *                            in by the servlet engine representing the
     *                            client request to be proxied
     * @param httpServletResponse The {@link HttpServletResponse} object by which
     *                             we can send a proxied response to the client
     */
    public void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws IOException, ServletException {
        // Create a standard POST request
        String contentType = httpServletRequest.getContentType();
        String destinationUrl = this.getProxyURL(httpServletRequest);
        debug("POST Request URL: " + httpServletRequest.getRequestURL(),
              "    Content Type: " + contentType,
              " Destination URL: " + destinationUrl);
        PostMethod postMethodProxyRequest = new PostMethod(destinationUrl);
        // Forward the request headers
        setProxyRequestHeaders(httpServletRequest, postMethodProxyRequest);
        setProxyRequestCookies(httpServletRequest, postMethodProxyRequest);
        // Check if this is a mulitpart (file upload) POST
        if (ServletFileUpload.isMultipartContent(httpServletRequest)) {
            this.handleMultipartPost(postMethodProxyRequest, httpServletRequest);
        } else {
            if (contentType == null || PostMethod.FORM_URL_ENCODED_CONTENT_TYPE.equals(contentType)) {
                this.handleStandardPost(postMethodProxyRequest, httpServletRequest);
            } else {
                this.handleContentPost(postMethodProxyRequest, httpServletRequest);
            }
        }
        // Execute the proxy request
        this.executeProxyRequest(postMethodProxyRequest, httpServletRequest, httpServletResponse);
    }

    /**
     * Sets up the given {@link PostMethod} to send the same multipart POST
     * data as was sent in the given {@link HttpServletRequest}
     * @param postMethodProxyRequest The {@link PostMethod} that we are
     *                                configuring to send a multipart POST request
     * @param httpServletRequest The {@link HttpServletRequest} that contains
     *                            the mutlipart POST data to be sent via the {@link PostMethod}
     */
    @SuppressWarnings("unchecked")
    private void handleMultipartPost(PostMethod postMethodProxyRequest, HttpServletRequest httpServletRequest)
            throws ServletException {
        // Create a factory for disk-based file items
        DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory();
        // Set factory constraints
        diskFileItemFactory.setSizeThreshold(this.getMaxFileUploadSize());
        diskFileItemFactory.setRepository(FILE_UPLOAD_TEMP_DIRECTORY);
        // Create a new file upload handler
        ServletFileUpload servletFileUpload = new ServletFileUpload(diskFileItemFactory);
        // Parse the request
        try {
            // Get the multipart items as a list
            List<FileItem> listFileItems = (List<FileItem>) servletFileUpload.parseRequest(httpServletRequest);
            // Create a list to hold all of the parts
            List<Part> listParts = new ArrayList<Part>();
            // Iterate the multipart items list
            for (FileItem fileItemCurrent : listFileItems) {
                // If the current item is a form field, then create a string part
                if (fileItemCurrent.isFormField()) {
                    StringPart stringPart = new StringPart(
                            fileItemCurrent.getFieldName(), // The field name
                            fileItemCurrent.getString() // The field value
                            );
                    // Add the part to the list
                    listParts.add(stringPart);
                } else {
                    // The item is a file upload, so we create a FilePart
                    FilePart filePart = new FilePart(
                            fileItemCurrent.getFieldName(), // The field name
                            new ByteArrayPartSource(
                            fileItemCurrent.getName(), // The uploaded file name
                            fileItemCurrent.get() // The uploaded file contents
                            ));
                    // Add the part to the list
                    listParts.add(filePart);
                }
            }
            MultipartRequestEntity multipartRequestEntity = new MultipartRequestEntity(
                    listParts.toArray(new Part[]{}),
                    postMethodProxyRequest.getParams());
            postMethodProxyRequest.setRequestEntity(multipartRequestEntity);
            // The current content-type header (received from the client) IS of
            // type "multipart/form-data", but the content-type header also
            // contains the chunk boundary string of the chunks. Currently, this
            // header is using the boundary of the client request, since we
            // blindly copied all headers from the client request to the proxy
            // request. However, we are creating a new request with a new chunk
            // boundary string, so it is necessary that we re-set the
            // content-type string to reflect the new chunk boundary string
            postMethodProxyRequest.setRequestHeader(STRING_CONTENT_TYPE_HEADER_NAME, multipartRequestEntity.getContentType());
        } catch (FileUploadException fileUploadException) {
            throw new ServletException(fileUploadException);
        }
    }

    /**
     * Sets up the given {@link PostMethod} to send the same standard POST
     * data as was sent in the given {@link HttpServletRequest}
     * @param postMethodProxyRequest The {@link PostMethod} that we are
     *                                configuring to send a standard POST request
     * @param httpServletRequest The {@link HttpServletRequest} that contains
     *                            the POST data to be sent via the {@link PostMethod}
     */
    @SuppressWarnings("unchecked")
    private void handleStandardPost(PostMethod postMethodProxyRequest, HttpServletRequest httpServletRequest) {
        // Get the client POST data as a Map
        Map<String, String[]> mapPostParameters = (Map<String, String[]>) httpServletRequest.getParameterMap();
        // Create a List to hold the NameValuePairs to be passed to the PostMethod
        List<NameValuePair> listNameValuePairs = new ArrayList<NameValuePair>();
        // Iterate the parameter names
        for (String stringParameterName : mapPostParameters.keySet()) {
            // Iterate the values for each parameter name
            String[] stringArrayParameterValues = mapPostParameters.get(stringParameterName);
            for (String stringParamterValue : stringArrayParameterValues) {
                // Create a NameValuePair and store in list
                NameValuePair nameValuePair = new NameValuePair(stringParameterName, stringParamterValue);
                listNameValuePairs.add(nameValuePair);
            }
        }
        // Set the proxy request POST data
        postMethodProxyRequest.setRequestBody(listNameValuePairs.toArray(new NameValuePair[]{}));
    }

    /**
     * Sets up the given {@link PostMethod} to send the same content POST
     * data (JSON, XML, etc.) as was sent in the given {@link HttpServletRequest}
     * @param postMethodProxyRequest The {@link PostMethod} that we are
     *                                configuring to send a standard POST request
     * @param httpServletRequest The {@link HttpServletRequest} that contains
     *                            the POST data to be sent via the {@link PostMethod}
     */
    private void handleContentPost(PostMethod postMethodProxyRequest, HttpServletRequest httpServletRequest) throws IOException, ServletException {
        StringBuilder content = new StringBuilder();
        BufferedReader reader = httpServletRequest.getReader();
        for (;;) {
            String line = reader.readLine();
            if (line == null) break;
            content.append(line);
        }

        String contentType = httpServletRequest.getContentType();
        String postContent = content.toString();

        if (contentType.startsWith("text/x-gwt-rpc")) {
            String clientHost = httpServletRequest.getLocalName();
            if (clientHost.equals("127.0.0.1")) {
                clientHost = "localhost";
            }

            int clientPort = httpServletRequest.getLocalPort();
            String clientUrl = clientHost + ((clientPort != 80) ? ":" + clientPort : "");
            String serverUrl = stringProxyHost + ((intProxyPort != 80) ? ":" + intProxyPort : "") + httpServletRequest.getServletPath();
            //debug("Replacing client (" + clientUrl + ") with server (" + serverUrl + ")");
            postContent = postContent.replace(clientUrl , serverUrl);
        }

        String encoding = httpServletRequest.getCharacterEncoding();
        debug("POST Content Type: " + contentType + " Encoding: " + encoding,
              "Content: " + postContent);
        StringRequestEntity entity;
        try {
            entity = new StringRequestEntity(postContent, contentType, encoding);
        } catch (UnsupportedEncodingException e) {
            throw new ServletException(e);
        }
        // Set the proxy request POST data
        postMethodProxyRequest.setRequestEntity(entity);
    }

    /**
     * Executes the {@link HttpMethod} passed in and sends the proxy response
     * back to the client via the given {@link HttpServletResponse}
     * @param httpMethodProxyRequest An object representing the proxy request to be made
     * @param httpServletResponse An object by which we can send the proxied
     *                             response back to the client
     * @throws IOException Can be thrown by the {@link HttpClient}.executeMethod
     * @throws ServletException Can be thrown to indicate that another error has occurred
     */
    private void executeProxyRequest(
            HttpMethod httpMethodProxyRequest,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse)
            throws IOException, ServletException {
        // Create a default HttpClient
        HttpClient httpClient = new HttpClient();
        httpMethodProxyRequest.setFollowRedirects(false);
        // Execute the request
        int intProxyResponseCode = httpClient.executeMethod(httpMethodProxyRequest);
        String response = httpMethodProxyRequest.getResponseBodyAsString();

        // Check if the proxy response is a redirect
        // The following code is adapted from org.tigris.noodle.filters.CheckForRedirect
        // Hooray for open source software
        if (intProxyResponseCode >= HttpServletResponse.SC_MULTIPLE_CHOICES /* 300 */ && intProxyResponseCode < HttpServletResponse.SC_NOT_MODIFIED /* 304 */) {
            String stringStatusCode = Integer.toString(intProxyResponseCode);
            String stringLocation = httpMethodProxyRequest.getResponseHeader(STRING_LOCATION_HEADER).getValue();
            if (stringLocation == null) {
                throw new ServletException("Received status code: " + stringStatusCode + " but no " + STRING_LOCATION_HEADER + " header was found in the response");
            }
            // Modify the redirect to go to this proxy servlet rather that the proxied host
            String stringMyHostName = httpServletRequest.getServerName();
            if (httpServletRequest.getServerPort() != 80) {
                stringMyHostName += ":" + httpServletRequest.getServerPort();
            }
            stringMyHostName += httpServletRequest.getContextPath();
            if (followRedirects) {
                if (stringLocation.contains("jsessionid")) {
                    Cookie cookie = new Cookie("JSESSIONID", stringLocation.substring(stringLocation.indexOf("jsessionid=") + 11));
                    cookie.setPath("/");
                    httpServletResponse.addCookie(cookie);
                    //debug("redirecting: set jessionid (" + cookie.getValue() + ") cookie from URL");
                } else if (httpMethodProxyRequest.getResponseHeader("Set-Cookie") != null) {
	                Header header = httpMethodProxyRequest.getResponseHeader("Set-Cookie");
                    String[] cookieDetails = header.getValue().split(";");
					String[] nameValue = cookieDetails[0].split("=");

					Cookie cookie = new Cookie(nameValue[0], nameValue[1]);
					cookie.setPath("/");
					//debug("redirecting: setting cookie: " + cookie.getName() + ":" + cookie.getValue() + " on " + cookie.getPath());
					httpServletResponse.addCookie(cookie);
                }
                httpServletResponse.sendRedirect(stringLocation.replace(getProxyHostAndPort() + this.getProxyPath(), stringMyHostName));
                return;
            }
        } else if (intProxyResponseCode == HttpServletResponse.SC_NOT_MODIFIED) {
            // 304 needs special handling.  See:
            // http://www.ics.uci.edu/pub/ietf/http/rfc1945.html#Code304
            // We get a 304 whenever passed an 'If-Modified-Since'
            // header and the data on disk has not changed; server
            // responds w/ a 304 saying I'm not going to send the
            // body because the file has not changed.
            httpServletResponse.setIntHeader(STRING_CONTENT_LENGTH_HEADER_NAME, 0);
            httpServletResponse.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }

        // Pass the response code back to the client
        httpServletResponse.setStatus(intProxyResponseCode);

        // Pass response headers back to the client
        Header[] headerArrayResponse = httpMethodProxyRequest.getResponseHeaders();
        for (Header header : headerArrayResponse) {
            if (header.getName().equals("Transfer-Encoding") && header.getValue().equals("chunked") ||
                    header.getName().equals("Content-Encoding") && header.getValue().equals("gzip") || // don't copy gzip header
                    header.getName().equals("WWW-Authenticate")) { // don't copy WWW-Authenticate header so browser doesn't prompt on failed basic auth
                // proxy servlet does not support chunked encoding
            } else {
                httpServletResponse.setHeader(header.getName(), header.getValue());
            }
        }

        List<Header> responseHeaders = Arrays.asList(headerArrayResponse);

        if (isBodyParameterGzipped(responseHeaders)) {
            debug("GZipped: true");
            if (!followRedirects && intProxyResponseCode == HttpServletResponse.SC_MOVED_TEMPORARILY) {
                response = httpMethodProxyRequest.getResponseHeader(STRING_LOCATION_HEADER).getValue();
                httpServletResponse.setStatus(HttpServletResponse.SC_OK);
                intProxyResponseCode = HttpServletResponse.SC_OK;
                httpServletResponse.setHeader(STRING_LOCATION_HEADER, response);
            } else {
                response = new String(ungzip(httpMethodProxyRequest.getResponseBody()));
            }
            httpServletResponse.setContentLength(response.length());
        }

        // Send the content to the client
        debug("Received status code: " + intProxyResponseCode,
              "Response: " + response);

        httpServletResponse.getWriter().write(response);
    }


    /**
     * The response body will be assumed to be gzipped if the GZIP header has been set.
     *
     * @param responseHeaders of response headers
     * @return true if the body is gzipped
     */
    private boolean isBodyParameterGzipped(List<Header> responseHeaders) {
        for (Header header : responseHeaders) {
            if (header.getValue().equals("gzip")) {
                return true;
            }
        }
        return false;
    }

    /**
     * A highly performant ungzip implementation. Do not refactor this without taking new timings.
     * See ElementTest in ehcache for timings
     *
     * @param gzipped the gzipped content
     * @return an ungzipped byte[]
     * @throws java.io.IOException when something bad happens
     */
    private byte[] ungzip(final byte[] gzipped) throws IOException {
        final GZIPInputStream inputStream = new GZIPInputStream(new ByteArrayInputStream(gzipped));
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(gzipped.length);
        final byte[] buffer = new byte[FOUR_KB];
        int bytesRead = 0;
        while (bytesRead != -1) {
            bytesRead = inputStream.read(buffer, 0, FOUR_KB);
            if (bytesRead != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
        }
        byte[] ungzipped = byteArrayOutputStream.toByteArray();
        inputStream.close();
        byteArrayOutputStream.close();
        return ungzipped;
    }

    public String getServletInfo() {
        return "GWT Proxy Servlet";
    }

    /**
     * Retrieves all of the headers from the servlet request and sets them on
     * the proxy request
     *
     * @param httpServletRequest The request object representing the client's
     *                            request to the servlet engine
     * @param httpMethodProxyRequest The request that we are about to send to
     *                                the proxy host
     */
    @SuppressWarnings("unchecked")
    private void setProxyRequestHeaders(HttpServletRequest httpServletRequest, HttpMethod httpMethodProxyRequest) {
        // Get an Enumeration of all of the header names sent by the client
        Enumeration enumerationOfHeaderNames = httpServletRequest.getHeaderNames();
        while (enumerationOfHeaderNames.hasMoreElements()) {
            String stringHeaderName = (String) enumerationOfHeaderNames.nextElement();
            if (stringHeaderName.equalsIgnoreCase(STRING_CONTENT_LENGTH_HEADER_NAME)) {
                continue;
            }
            // As per the Java Servlet API 2.5 documentation:
            //		Some headers, such as Accept-Language can be sent by clients
            //		as several headers each with a different value rather than
            //		sending the header as a comma separated list.
            // Thus, we get an Enumeration of the header values sent by the client
            Enumeration enumerationOfHeaderValues = httpServletRequest.getHeaders(stringHeaderName);
            while (enumerationOfHeaderValues.hasMoreElements()) {
                String stringHeaderValue = (String) enumerationOfHeaderValues.nextElement();
                // In case the proxy host is running multiple virtual servers,
                // rewrite the Host header to ensure that we get content from
                // the correct virtual server
                if (stringHeaderName.equalsIgnoreCase(STRING_HOST_HEADER_NAME)) {
                    stringHeaderValue = getProxyHostAndPort();
                }
                Header header = new Header(stringHeaderName, stringHeaderValue);
                // Set the same header on the proxy request
                httpMethodProxyRequest.setRequestHeader(header);
            }
        }
    }

    /**
     * Retrieves all of the cookies from the servlet request and sets them on
     * the proxy request
     *
     * @param httpServletRequest The request object representing the client's
     *                            request to the servlet engine
     * @param httpMethodProxyRequest The request that we are about to send to
     *                                the proxy host
     */
    @SuppressWarnings("unchecked")
    private void setProxyRequestCookies(HttpServletRequest httpServletRequest, HttpMethod httpMethodProxyRequest) {
        // Get an array of all of all the cookies sent by the client
        Cookie[] cookies = httpServletRequest.getCookies();
        if (cookies == null) {
            return;
        }

        for (Cookie cookie : cookies) {
            cookie.setDomain(stringProxyHost);
            cookie.setPath(httpServletRequest.getServletPath());
            httpMethodProxyRequest.setRequestHeader("Cookie", cookie.getName() + "=" + cookie.getValue() + "; Path=" + cookie.getPath());
        }
    }

    // Accessors
    private String getProxyURL(HttpServletRequest httpServletRequest) {
        // Set the protocol to HTTP
        String protocol = (isSecure) ? "https://" : "http://";
        String stringProxyURL = protocol + this.getProxyHostAndPort();

        // simply use whatever servlet path that was part of the request as opposed to getting a preset/configurable proxy path
        if (!removePrefix) {
            stringProxyURL += httpServletRequest.getServletPath();
        }
        stringProxyURL += "/";
        
        // Handle the path given to the servlet
        String pathInfo = httpServletRequest.getPathInfo();
        if (pathInfo != null && pathInfo.startsWith("/")) {
            if (stringProxyURL != null && stringProxyURL.endsWith("/")) {
                // avoid double '/'
                stringProxyURL += pathInfo.substring(1);
            }
        } else {
            stringProxyURL += httpServletRequest.getPathInfo();
        }
        // Handle the query string
        if (httpServletRequest.getQueryString() != null) {
            stringProxyURL += "?" + httpServletRequest.getQueryString();
        }
        
        return stringProxyURL;
    }

    private String getProxyHostAndPort() {
        if (this.getProxyPort() == 80) {
            return this.getProxyHost();
        } else {
            return this.getProxyHost() + ":" + this.getProxyPort();
        }
    }

    protected String getProxyHost() {
        return this.stringProxyHost;
    }

    protected void setProxyHost(String stringProxyHostNew) {
        this.stringProxyHost = stringProxyHostNew;
    }

    protected int getProxyPort() {
        return this.intProxyPort;
    }

    protected void setSecure(boolean secure) {
        this.isSecure = secure;
    }
    
    protected void setFollowRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
    }

    protected void setProxyPort(int intProxyPortNew) {
        this.intProxyPort = intProxyPortNew;
    }

    protected String getProxyPath() {
        return this.stringProxyPath;
    }

    protected void setProxyPath(String stringProxyPathNew) {
        this.stringProxyPath = stringProxyPathNew;
    }

    protected void setRemovePrefix(boolean removePrefix) {
        this.removePrefix = removePrefix;
    }

    protected int getMaxFileUploadSize() {
        return this.intMaxFileUploadSize;
    }

    protected void setMaxFileUploadSize(int intMaxFileUploadSizeNew) {
        this.intMaxFileUploadSize = intMaxFileUploadSizeNew;
    }

    private void debug(String ... msg) {
        for (String m : msg) {
            System.out.println("[DEBUG] " + m);
        }
    }
}