package org.zanata.client;

import java.io.PrintStream;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;
import org.simpleframework.http.core.Container;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class HTTPMockContainer implements Container {
    private static final Logger log =
        LoggerFactory.getLogger(HTTPMockContainer.class);

    private String responseContent;
    private Status status;

    public HTTPMockContainer(String responseContent, Status status) {
        this.responseContent = responseContent;
        this.status = status;
    }

    public static HTTPMockContainer okResponse(String expectedResponse) {
        return new HTTPMockContainer(expectedResponse, Status.OK);
    }

    public static HTTPMockContainer notOkResponse(Status status) {
        return new HTTPMockContainer("", status);
    }

    @Override
    public void handle(Request request, Response response) {
        try {
            PrintStream body = response.getPrintStream();
            long time = System.currentTimeMillis();

            response.setStatus(status);
            response.setContentType("text/plain");
            response.setDate("Date", time);
            response.setDate("Last-Modified", time);

            log.info("mock container returning: status [{}], content [{}]",
                status, responseContent);

            body.println(responseContent);
            body.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
