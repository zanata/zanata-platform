package org.zanata.rest.service;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.annotations.providers.jaxb.Wrapped;
import org.jboss.resteasy.util.GenericType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.security.Restrict;
import org.zanata.common.Namespaces;
import org.zanata.dao.ApplicationConfigurationDAO;
import org.zanata.events.ConfigurationChanged;
import org.zanata.model.HApplicationConfiguration;
import org.zanata.rest.MediaTypes;
import org.zanata.rest.dto.Configuration;
import org.zanata.rest.dto.Link;
import com.google.common.annotations.Beta;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.zanata.util.Event;

/**
 * This API is experimental only and subject to change or even removal.
 *
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Name("serverConfigurationResource")
@Path("/configurations")
@Produces("application/xml")
@Consumes("application/xml")
@Transactional
@Restrict("#{s:hasRole('admin')}")
@Slf4j
@Beta
public class ServerConfigurationService {

    private static List<String> availableKeys;

    /** Type of media requested. */
    @HeaderParam("Accept")
    @DefaultValue(MediaType.APPLICATION_XML)
    @Context
    private MediaType accept;
    @In
    private ApplicationConfigurationDAO applicationConfigurationDAO;

    @In("event")
    private Event<ConfigurationChanged> zanataConfigurationChangedEvent;

    /**
     * Retrieves all existing server configurations.
     *
     * @return The following response status codes will be returned from this
     *         operation:<br>
     *         OK(200) - Response containing value for the config key.<br>
     *         INTERNAL SERVER ERROR(500) - If there is an unexpected error in
     *         the server while performing this operation.
     */
    @GET
    @Wrapped(element = "configurations", namespace = Namespaces.ZANATA_API)
    public Response get() {
        List<HApplicationConfiguration> all =
                applicationConfigurationDAO.findAll();
        List<Configuration> allConfig =
                Lists.transform(all, new ToConfigurationFunction(accept));
        Type genericType = new GenericType<List<Configuration>>() {
        }.getGenericType();
        Object entity =
                new GenericEntity<List<Configuration>>(allConfig, genericType);
        return Response.ok().entity(entity).build();

    }

    /**
     * Retrieves a specific server configuration.
     *
     * @return The following response status codes will be returned from this
     *         operation:<br>
     *         OK(200) - Response containing value for the config key.<br>
     *         NOT_FOUND(404) - If server does not have given configuration set.<br>
     *         INTERNAL SERVER ERROR(500) - If there is an unexpected error in
     *         the server while performing this operation.
     */
    @GET
    @Path("c/{configKey}")
    public Response get(@PathParam("configKey") @Nonnull String configKey) {
        HApplicationConfiguration config =
                applicationConfigurationDAO.findByKey(configKey);
        if (config == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        Configuration configuration =
                new ToConfigurationFunction(accept).apply(config);
        return Response.ok().entity(configuration).build();
    }

    /**
     * Creates or updates a server configuration. If a configuration with the
     * given key already exists, the value will be overwritten with the provided
     * data. Otherwise, a new config will be created.
     *
     * @param configKey
     *            The configuration item to be created/updated.
     * @return The following response status codes will be returned from this
     *         operation:<br>
     *         OK(200) - If an existing configuration was modified.<br>
     *         CREATED(201) - If a new configuration was created.<br>
     *         UNAUTHORIZED(401) - If the user does not have the proper
     *         permissions to perform this operation.<br>
     *         INTERNAL SERVER ERROR(500) - If there is an unexpected error in
     *         the server while performing this operation.
     */
    @PUT
    @Path("/c/{configKey}")
    public Response put(@PathParam("configKey") @Nonnull String configKey,
            String configValue) {
        if (!isConfigKeyValid(configKey)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("config key not supported: " + configKey).build();
        }
        HApplicationConfiguration configuration =
                applicationConfigurationDAO.findByKey(configKey);
        boolean created = configuration == null;
        persistApplicationConfig(configKey, configuration, configValue,
                applicationConfigurationDAO);
        if (created) {
            return Response.created(URI.create("c/" + configKey)).build();
        } else {
            return Response.ok().build();
        }
    }

    /**
     * This method is shared by REST service and JSF backend bean. It will raise
     * event to notify configuration change.
     *
     * @see org.zanata.action.ServerConfigurationBean
     * @param key
     *            config key
     * @param appConfig
     *            configuration entity
     * @param newValue
     *            new config value (if null or empty will remove the config
     *            entity)
     * @param applicationConfigurationDAO
     *            dao
     */
    public void persistApplicationConfig(String key,
            @Nullable HApplicationConfiguration appConfig, String newValue,
            ApplicationConfigurationDAO applicationConfigurationDAO) {
        if (appConfig != null) {
            if (newValue == null || newValue.isEmpty()) {
                applicationConfigurationDAO.makeTransient(appConfig);
            } else {
                appConfig.setValue(newValue);
            }
        } else if (newValue != null && !newValue.isEmpty()) {
            appConfig = new HApplicationConfiguration(key, newValue);
            applicationConfigurationDAO.makePersistent(appConfig);
        }
        zanataConfigurationChangedEvent.fireAfterSuccess(new ConfigurationChanged(key));
    }

    private boolean isConfigKeyValid(String configKey) {
        return HApplicationConfiguration.getAvailableKeys().contains(configKey);
    }

    /**
     * Converts HApplicationConfiguration to dto Configuration. It also contains
     * a link to the configuration itself.
     */
    @RequiredArgsConstructor
    private class ToConfigurationFunction implements
            Function<HApplicationConfiguration, Configuration> {

        private final MediaType accept;

        @Override
        public Configuration apply(HApplicationConfiguration input) {
            Configuration config =
                    new Configuration(input.getKey(), input.getValue());
            config.getLinks(true).add(
                    // a link to get this specific configuration details
                    new Link(URI.create("c/" + input.getKey()), "self",
                            MediaTypes.createFormatSpecificType(
                                    MediaType.APPLICATION_XML, accept)));
            return config;
        }
    }
}
