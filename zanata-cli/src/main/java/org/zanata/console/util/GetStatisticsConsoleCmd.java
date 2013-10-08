/*
 * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.console.util;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.jboss.aesh.console.AeshConsole;
import org.jboss.aesh.console.Command;
import org.jboss.aesh.console.CommandResult;
import org.jboss.aesh.console.operator.ControlOperator;
import org.zanata.client.commands.stats.GetStatisticsCommand;
import org.zanata.client.commands.stats.GetStatisticsOptions;
import org.zanata.client.commands.stats.GetStatisticsOptionsImpl;

/**
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class GetStatisticsConsoleCmd implements Command {

    private boolean includeDetails;
    private boolean includeWordLevelStats;
    private String format;
    private String documentId;
    private String proj;
    private java.io.File projectConfig;
    private String projectVersion;
    private String projectType;
    private org.zanata.client.config.LocaleList localeMapList;
    private String key;
    private java.net.URL url;
    private java.io.File userConfig;
    private String username;
    private boolean logHttp;
    private boolean disableSSLCert;
    private Boolean debug = null;
    private Boolean errors = null;
    private boolean help;
    private Boolean quiet = null;
    private boolean interactiveMode;
    private boolean batchMode;

    @Override
    public CommandResult execute(AeshConsole aeshConsole,
            ControlOperator operator) throws IOException {

        try {
            new GetStatisticsCommand(this.toOptions()).run();
        } catch (Exception e) {
            e.printStackTrace();
            return CommandResult.FAILURE;
        }
        return CommandResult.SUCCESS;
    }

    /*
     * This is the only way to convert to an options implementation as the
     * methods cannot be overriden because they would lose their args4j
     * annotations.
     */
    private GetStatisticsOptions toOptions() {
        GetStatisticsOptions options = new GetStatisticsOptionsImpl();
        Class<? extends GetStatisticsOptions> optionsClass = options.getClass();

        for (Field f : this.getClass().getDeclaredFields()) {
            try {
                Method setter =
                        optionsClass.getMethod("set"
                                + f.getName().substring(0, 1).toUpperCase()
                                + f.getName().substring(1), f.getType());

                setter.invoke(options, f.get(this));
            } catch (NoSuchMethodException e) {
                // TODO
                // This happens when fields are declared as Boolean but the
                // setter method accepts boolean. For now we can ignore this,
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return options;
    }
}
