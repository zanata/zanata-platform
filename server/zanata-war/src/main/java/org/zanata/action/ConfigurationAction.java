/*
 * Copyright 2010, Red Hat, Inc. and individual contributors
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
package org.zanata.action;

import java.io.Serializable;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Model;
import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.servlet.annotations.HttpParam;
import org.zanata.model.HLocale;
import org.zanata.service.ConfigurationService;

@Named("configurationAction")
@RequestScoped
@Model
@Transactional
public class ConfigurationAction implements Serializable {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(ConfigurationAction.class);

    private static final long serialVersionUID = 1L;
    @Inject
    @HttpParam("iterationSlug")
    private String iterationSlug;
    @Inject
    @HttpParam("projectSlug")
    private String projectSlug;
    @Inject
    private ConfigurationService configurationServiceImpl;

    public void downloadGeneralConfig() {
        respondWithFile(configurationServiceImpl.getGeneralConfig(projectSlug,
                iterationSlug));
    }

    public void downloadOfflineTranslationConfig(HLocale locale) {
        respondWithFile(configurationServiceImpl.getConfigForOfflineTranslation(
                projectSlug, iterationSlug, locale));
    }

    private void respondWithFile(String configFileContents) {
        HttpServletResponse response = (HttpServletResponse) FacesContext
                .getCurrentInstance().getExternalContext().getResponse();
        response.setContentType("application/xml");
        response.addHeader("Content-disposition", "attachment; filename=\""
                + configurationServiceImpl.getConfigurationFileName() + "\"");
        response.setCharacterEncoding("UTF-8");
        try {
            ServletOutputStream os = response.getOutputStream();
            os.write(configFileContents.getBytes());
            os.flush();
            os.close();
            FacesContext.getCurrentInstance().responseComplete();
        } catch (Exception e) {
            log.error("Failure : " + e.toString() + "\n");
        }
    }
}
