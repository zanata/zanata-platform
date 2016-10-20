package org.zanata.client.commands;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.Map;

import org.apache.commons.codec.Charsets;
import org.apache.commons.io.IOUtils;
import org.hamcrest.Matcher;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;
import org.simpleframework.http.core.Container;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class HTTPMockContainer implements Container {
    private static final Logger log =
            LoggerFactory.getLogger(HTTPMockContainer.class);

    private final Map<Matcher<String>, StatusAndContent> pathToResponseMap;

    public HTTPMockContainer(
            Map<Matcher<String>, StatusAndContent> pathToResponseMap) {
        this.pathToResponseMap = pathToResponseMap;
    }

    @Override
    public void handle(Request request, Response response) {
        try {
            PrintStream body = response.getPrintStream();
            long time = System.currentTimeMillis();

            response.setValue("Content-Type", "text/plain");
            response.setContentType("text/xml;charset=utf-8");
            response.setDate("Date", time);
            response.setDate("Last-Modified", time);
            String path = request.getAddress().getPath().getPath();
            log.trace("request path is {}", path);

            StatusAndContent statusAndContent = tryMatchPath(path);
            Status status = statusAndContent.status;
            response.setStatus(status);

            String content = statusAndContent.content;
            log.trace("mock container returning: status [{}], content [{}]",
                    status, content);

            body.println(content);
            body.close();
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(Status.INTERNAL_SERVER_ERROR);
            try {
                response.close();
            } catch (IOException e1) {
                throw Throwables.propagate(e1);
            }
        }
    }

    private StatusAndContent tryMatchPath(String path) {
        for (Map.Entry<Matcher<String>, StatusAndContent> entry : pathToResponseMap
                .entrySet()) {
            Matcher<String> pathMatcher = entry.getKey();
            if (pathMatcher.matches(path)) {
                return entry.getValue();
            }
        }
        throw new IllegalStateException(
                "can not find matching response for path:" + path);
    }

    public static class Builder {
        private ImmutableMap.Builder<Matcher<String>, StatusAndContent> mapBuilder =
                ImmutableMap.builder();

        public static Builder builder() {
            return new Builder();
        }

        public Builder onPathReturnOk(Matcher<String> pathMatcher,
                String content) {
            mapBuilder.put(pathMatcher,
                    new StatusAndContent(Status.OK, content));
            return this;
        }

        public Builder onPathReturnStatus(Matcher<String> pathMatcher,
                int status, String content) {
            mapBuilder.put(pathMatcher,
                    new StatusAndContent(Status.getStatus(status), content));
            return this;
        }

        public HTTPMockContainer build() {
            return new HTTPMockContainer(mapBuilder.build());
        }

    }

    private static class StatusAndContent {
        private final Status status;
        private final String content;

        private StatusAndContent(Status status, String content) {
            this.status = status;
            this.content = content;
        }
    }
}
