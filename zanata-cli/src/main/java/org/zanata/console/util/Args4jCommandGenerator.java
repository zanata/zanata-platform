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

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.net.URL;

import org.jboss.aesh.cl.builder.CommandBuilder;
import org.jboss.aesh.cl.builder.OptionBuilder;
import org.jboss.aesh.cl.converter.CLConverter;
import org.jboss.aesh.cl.exception.CommandLineParserException;
import org.jboss.aesh.cl.exception.OptionParserException;
import org.jboss.aesh.cl.internal.ProcessedCommand;
import org.jboss.aesh.cl.internal.ProcessedOption;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;
import org.zanata.client.commands.ConfigurableCommand;
import org.zanata.client.commands.ConfigurableOptions;
import org.zanata.console.converter.URLConverter;

/**
 * Generates AEsh commands from Args4j annotated classes.
 * 
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class Args4jCommandGenerator {

    public static final ProcessedCommand generateCommand(String name,
            String description, Class<?> args4jAnnotatedCls) {
        try {
            CommandBuilder commandBuilder =
                    new CommandBuilder().name(name).description(description);

            // recursively process all the methods/fields.
            for (Class c = args4jAnnotatedCls; c != null; c = c.getSuperclass()) {
                for (Method m : c.getDeclaredMethods()) {
                    Option o = m.getAnnotation(Option.class);
                    if (o != null) {
                        commandBuilder.addOption(generateOption(o, m));
                    }
                    Argument a = m.getAnnotation(Argument.class);
                    if (a != null) {
                        // commandBuilder.addOption( generateOption(a) )
                    }
                }

                for (Field f : c.getDeclaredFields()) {
                    Option o = f.getAnnotation(Option.class);
                    if (o != null) {
                        commandBuilder.addOption(generateOption(o, f));
                    }
                    Argument a = f.getAnnotation(Argument.class);
                    if (a != null) {
                        // commandBuilder.addArgument?
                    }
                }
            }

            return commandBuilder.generateParameter();
        } catch (CommandLineParserException e) {
            throw new RuntimeException(e);
        }
    }

    private static final ProcessedOption generateOption(Option args4jOpt,
            Member member) throws OptionParserException {
        String optName = args4jOpt.name();
        if (optName.startsWith("--")) {
            optName = optName.replaceFirst("\\-\\-", "");
        }

        Class optionType = getOptionType(member);
        OptionBuilder optionBuilder =
                new OptionBuilder().name(optName)
                        .description(args4jOpt.usage()).type(optionType)
                        .fieldName(getFieldName(member))
                        .converter(getConverter(optionType));

        if (optName.equals("errors")) {
            optionBuilder.shortName('X');
        }

        return optionBuilder.create();
    }

    private static CLConverter getConverter(Class<?> optType) {
        if (optType == URL.class) {
            return new URLConverter();
        }
        return null;
    }

    private static Class getOptionType(Member m) {
        if (m instanceof Field) {
            return ((Field) m).getType();
        } else {
            return ((Method) m).getParameterTypes()[0]; // should be a
                                                        // setter
        }
    }

    private static String getFieldName(Member m) {
        if (m instanceof Field) {
            return m.getName();
        }
        // Getter or Setter
        else if (m instanceof Method) {
            if (m.getName().startsWith("set") || m.getName().startsWith("get")) {
                return m.getName().substring(3, 4).toLowerCase()
                        + m.getName().substring(4);
            }
            return m.getName();
        }
        return null;
    }
}
