/*
 * Copyright 2015, Red Hat, Inc. and individual contributors as indicated by the
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

package org.zanata;

import org.apache.commons.lang.StringUtils;
import javax.annotation.PostConstruct;
import javax.inject.Named;

import javax.faces.application.ResourceHandler;
import javax.faces.context.FacesContext;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.Set;

/**
 * Utility component for accessing zanata-assets resources from JSF/HTML page.
 *
 * Usage in JSF/HTML page: <link rel="shortcut icon" href="#{assets['img/logo/logo.ico']}"/>
 * Rendered URL from example above is: {@link #DEFAULT_WEB_ASSETS_URL}/img/logo/logo.ico
 *
 * {@link #DEFAULT_WEB_ASSETS_URL} can be overridden in system property {@link #ASSETS_PROPERTY_KEY}
 *
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */

@Named("assets")
@javax.enterprise.context.ApplicationScoped
public class WebAssetsConfiguration extends AbstractMap<String, String> {

    /**
     * system property for zanata assets url
     */
    private final static String ASSETS_PROPERTY_KEY = "zanata.assets.url";

    /**
     *  Default url for zanata-assets, http://{zanata.url}/javax.faces.resource/jars/assets
     */
    private final static String DEFAULT_WEB_ASSETS_URL = String.format("%s%s/%s/%s",
            FacesContext.getCurrentInstance().getExternalContext()
                    .getRequestContextPath(),
            ResourceHandler.RESOURCE_IDENTIFIER, "jars", "assets");

    private String webAssetsUrlBase;

    public WebAssetsConfiguration() {
        String assetsProperty = System.getProperty(ASSETS_PROPERTY_KEY);

        /**
         * Try with system property of {@link #ASSETS_PROPERTY_KEY} if exists,
         * otherwise {@link #DEFAULT_WEB_ASSETS_URL}
         */
        webAssetsUrlBase =
            StringUtils.isEmpty(assetsProperty) ? DEFAULT_WEB_ASSETS_URL
                : assetsProperty;
    }

    private String getWebAssetsUrl(String resource) {
        return webAssetsUrlBase + "/" + resource;
    }

    @Override
    public String get(Object key) {
        return getWebAssetsUrl((String) key);
    }

    @Override
    public Set<Entry<String, String>> entrySet() {
        return Collections.emptySet();
    }
}
