package org.zanata.client.commands.glossary.search;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.client.commands.ConfigurableCommand;
import org.zanata.client.commands.OptionsUtil;
import org.zanata.rest.client.GlossaryClient;
import org.zanata.rest.client.RestClientFactory;

import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Damian Jansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 *
 **/
public class GlossarySearchCommand extends ConfigurableCommand<GlossarySearchOptions> {

    private static final Logger log = LoggerFactory
            .getLogger(GlossarySearchCommand.class);
    private final GlossaryClient client;

    public GlossarySearchCommand(GlossarySearchOptions opts,
                                 RestClientFactory clientFactory) {
        super(opts, clientFactory);
        client = getClientFactory().getGlossaryClient();
    }

    public GlossarySearchCommand(GlossarySearchOptions opts) {
        this(opts, OptionsUtil.createClientFactory(opts));
    }

    @Override
    public void run() throws Exception {
        log.info("Server: {}", getOpts().getUrl());
        log.info("Username: {}", getOpts().getUsername());

        if (StringUtils.isNotBlank(getOpts().getProject())) {
            log.info("Project: {}", getOpts().getProject());
        }

        String project = getOpts().getProject();
        String qualifiedName;
        try {
            qualifiedName = StringUtils.isBlank(project)
                    ? client.getGlobalQualifiedName()
                    : client.getProjectQualifiedName(project);
        } catch (ResponseProcessingException rpe) {
            if (rpe.getResponse().getStatus() == 404) {
                log.error("Project {} not found", project);
                return;
            } else {
                throw rpe;
            }
        }
        if (StringUtils.isBlank(getOpts().getFilter())) {
            log.error("Filter query parameter required");
            return;
        }
        Response response = client.find(getOpts().getFilter(), qualifiedName);
        String responseText = response.readEntity(String.class);
        if (getOpts().getRaw()) {
            log.info("Raw JSON:\n{}", responseText);
        } else {
            printFind(responseText);
        }
        log.info("Search complete");
    }

    private void printFind(String jsonResponse) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        Map<?,?> jsonMap = mapper.readValue(jsonResponse, Map.class);
        log.debug("Entries {}", jsonMap);
        log.info("Entries found: {}", jsonMap.get("totalCount"));

        ArrayList<ObjectNode> results = (ArrayList)jsonMap.get("results");
        for (Object node : results) {
            LinkedHashMap<String,JsonNode> item = (LinkedHashMap)node;
            log.info("");
            log.debug("Entry: {}", node);
            log.info("Result:");
            log.info("ID: {}", item.get("id"));
            log.info("Part of Speech: {}", item.get("pos"));
            log.info("Terms: {}", item.get("glossaryTerms"));
        }
    }
}
