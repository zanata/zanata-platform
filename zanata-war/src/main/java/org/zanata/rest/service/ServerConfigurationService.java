package org.zanata.rest.service;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.List;
import javax.annotation.Nonnull;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.jboss.resteasy.util.GenericType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.core.Events;
import org.zanata.ApplicationConfiguration;
import org.zanata.dao.ApplicationConfigurationDAO;
import org.zanata.model.HApplicationConfiguration;
import org.zanata.rest.MediaTypes;
import org.zanata.rest.dto.Configuration;
import org.zanata.rest.dto.Link;
import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Name("serverConfigurationResource")
@Path(ServerConfigurationResource.SERVICE_PATH)
@Transactional
@Restrict("#{s:hasRole('admin')}")
@Slf4j
public class ServerConfigurationService implements ServerConfigurationResource {

    private static List<String> availableKeys;

    @Context
    private UriInfo uriInfo;
    /** Type of media requested. */
    @HeaderParam("Accept")
    @DefaultValue(MediaType.APPLICATION_XML)
    @Context
    private MediaType accept;
    @In
    private ApplicationConfigurationDAO applicationConfigurationDAO;

    @Override
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

    @Override
    public Response get(@Nonnull String configKey) {
        HApplicationConfiguration config =
                applicationConfigurationDAO.findByKey(configKey);
        if (config == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        Configuration configuration =
                new ToConfigurationFunction(accept).apply(config);
        return Response.ok().entity(configuration).build();
    }

    @Override
    public Response put(@Nonnull String configKey, String configValue) {
        if (!isConfigKeyValid(configKey)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("config key not supported: " + configKey).build();
        }
        HApplicationConfiguration configuration =
                applicationConfigurationDAO.findByKey(configKey);
        if (configuration == null) {
            HApplicationConfiguration newConfig =
                    new HApplicationConfiguration(configKey, configValue);
            applicationConfigurationDAO.makePersistent(newConfig);
            return Response.created(URI.create("c/" + configKey)).build();
        } else {
            configuration.setValue(configValue);
            return Response.ok().build();
        }
    }

    public static void persistApplicationConfig(String key,
            HApplicationConfiguration appConfig, String newValue,
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

        if (Events.exists()) {
            Events.instance().raiseTransactionSuccessEvent(
                    ApplicationConfiguration.EVENT_CONFIGURATION_CHANGED, key);
        }
    }

    private boolean isConfigKeyValid(String configKey) {
        return getAvailableKeys().contains(configKey);
    }

    private static List<String> getAvailableKeys() {
        if (availableKeys != null) {
            return availableKeys;
        }
        final HApplicationConfiguration dummy = new HApplicationConfiguration();
        List<Field> availableConfigKeys =
                Lists.newArrayList(HApplicationConfiguration.class.getFields());
        availableKeys = Lists.transform(availableConfigKeys,
                new Function<Field, String>() {
                    @Override
                    public String apply(Field input) {
                        try {
                            input.setAccessible(true);
                            return (String) input.get(dummy);
                        } catch (IllegalAccessException e) {
                            throw Throwables.propagate(e);
                        }
                    }
                });
        return availableKeys;
    }

    @RequiredArgsConstructor
    private class ToConfigurationFunction implements
            Function<HApplicationConfiguration, Configuration> {

        private final MediaType accept;

        @Override
        public Configuration apply(HApplicationConfiguration input) {
            Configuration config =
                    new Configuration(input.getKey(), input.getValue());
            config.getLinks(true).add(
                    new Link(URI.create("c/" + input.getKey()), "self",
                            MediaTypes.createFormatSpecificType(
                                    MediaType.APPLICATION_XML, accept)));
            return config;
        }
    }
}
