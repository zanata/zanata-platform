package org.zanata.action;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.zanata.async.Async;
import org.zanata.async.AsyncTaskResult;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.dao.LocaleDAO;
import org.zanata.dao.TextFlowDAO;
import org.zanata.dao.TextFlowTargetDAO;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.rest.RestConstant;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Future;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Named("mtService")
@RequestScoped
public class MTService implements Serializable{
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(MTService.class);

    @Inject
    private LocaleDAO localeDAO;

    @Inject
    private TextFlowDAO textFlowDAO;

    @Inject
    private TextFlowTargetDAO textFlowTargetDAO;

    @Inject
    private EntityManager entityManager;

    private String baseURL =
            "https://service-zanata-mt-prod.int.open.paas.redhat.com";

    @Async
    @Transactional
    public Future<Void> merge(List<Long> ids, LocaleId targetLocale) {

        try {
            HLocale locale = localeDAO.findByLocaleId(targetLocale);

            for (int tfCount = 0; tfCount < ids.size(); tfCount++) {
                HTextFlow tf = textFlowDAO.findById(ids.get(tfCount));
                List<String> translations = new ArrayList<>();
                for (String content : tf.getContents()) {
                    List<String> results = getMTTranslation(content, locale.getLocaleId());
                    if (!results.isEmpty()) {
                        translations.add(results.get(0));
                    }
                }
                if (!translations.isEmpty()) {
                    HTextFlowTarget tft =
                            textFlowTargetDAO.getOrCreateTarget(tf, locale);

                    tft.setContents(translations);
                    tft.setState(ContentState.Translated);
                    tft.setTextFlowRevision(tf.getRevision());
                    if (tft.getId() != null && tft.getId() != 0L) {
                        entityManager.persist(tft);
                    } else {
                        entityManager.merge(tft);
                    }
                    log.info("persisted tf #" + tfCount + " of " + ids.size() +
                            ":" + translations.get(0));
                }
            }
            entityManager.flush();
        } catch (Exception e) {
            log.error("Error:", e);
        }
        return AsyncTaskResult.completed();
    }

    public List<String> getMTTranslation(String content, LocaleId targetLocale)
            throws IOException {
        Client client = ResteasyClientBuilder.newClient();

        String es = content.replace("\"", "\'");

        String request =
                "{\"contents\":[{\"value\": \"" + es + "\", \"type\": \"text/html\"}], " +
                        "\"localeCode\":\"en-us\",\"url\":\"https://translate.engineering.redhat.com/iteration/view/trial/1.0\"}";

        Entity<String> entity = Entity.json(request);

        Response response =
                client.target(baseURL)
                        .path("api/document/translate")
                        .register(new ApiKeyHeaderFilter("zanataMT", "Wj7cYcZyTYqj"))
                        .queryParam("toLocaleCode", targetLocale.getId())
                        .request(MediaType.APPLICATION_JSON_TYPE).post(entity);

        List<String> results = new ArrayList<>();

        if (response.getStatus() == 200) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(response.readEntity(String.class), ObjectNode.class);
            ArrayNode contentsNode = (ArrayNode)node.findValue("contents");
            Iterator<JsonNode> iter = contentsNode.getElements();
            while (iter.hasNext()) {
                JsonNode contentNode = iter.next();
                results.add(contentNode.get("value").asText());
            }
        } else {
            log.error("Get mt error: " + request + ":" + response.readEntity(String.class));
        }
        return results;
    }

    @Provider
    public class ApiKeyHeaderFilter implements ClientRequestFilter {
        private String apiKey;
        private String username;

        public ApiKeyHeaderFilter(String username, String apiKey) {
            this.username = username;
            this.apiKey = apiKey;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        @Override
        public void filter(ClientRequestContext requestContext) throws IOException {
            MultivaluedMap<String, Object> headers = requestContext.getHeaders();
            headers.add(RestConstant.HEADER_USERNAME, username);
            headers.add(RestConstant.HEADER_API_KEY, apiKey);
        }
    }
}
