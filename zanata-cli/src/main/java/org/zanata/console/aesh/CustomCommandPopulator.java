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
package org.zanata.console.aesh;

import org.jboss.aesh.cl.CommandLine;
import org.jboss.aesh.cl.exception.CommandLineParserException;
import org.jboss.aesh.cl.internal.OptionType;
import org.jboss.aesh.cl.internal.ProcessedOption;
import org.jboss.aesh.cl.parser.AeshCommandPopulator;
import org.jboss.aesh.cl.parser.CommandLineParser;
import org.jboss.aesh.cl.parser.CommandPopulator;
import org.jboss.aesh.cl.validator.OptionValidatorException;
import org.jboss.aesh.util.ReflectionUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class CustomCommandPopulator extends AeshCommandPopulator {

    private final CommandLineParser commandLineParser;
    
    public CustomCommandPopulator(
        CommandLineParser commandLineParser) {
        super(commandLineParser);
        this.commandLineParser = commandLineParser;
    }

    @Override
    public void populateObject(Object instance, CommandLine line,
        boolean validate)
        throws CommandLineParserException, OptionValidatorException {
        if(line.hasParserError())
            throw line.getParserException();
        for(ProcessedOption option: commandLineParser.getCommand().getOptions()) {
            if(line.hasOption(option.getName())) {
                ProcessedOption lineOption = line.getOption(option.getName());
                injectValueIntoField(instance, lineOption.getFieldName(), lineOption, validate);
            }
            else if(option.getDefaultValues().size() > 0) {
                injectValueIntoField(instance, option.getFieldName(), option, validate);
            }
            else
                resetField(instance, option.getFieldName(), option.hasValue());
        }
        if(line.getArgument() != null && line.getArgument().getValues().size() > 0) {
            line.getArgument().injectValueIntoField(instance, validate);
        }
        else if(line.getArgument() != null)
            resetField(instance, line.getArgument().getFieldName(), true);
    }

    /**
     * @see {@link AeshCommandPopulator} for original code.
     */
    private void
    resetField(Object instance, String fieldName, boolean hasValue) {
        try {
            Field field = getInstanceField(instance, fieldName);
            if (!Modifier.isPublic(field.getModifiers()))
                field.setAccessible(true);
            if (field.getType().isPrimitive()) {
                if (boolean.class.isAssignableFrom(field.getType()))
                    field.set(instance, false);
                else if (int.class.isAssignableFrom(field.getType()))
                    field.set(instance, 0);
                else if (short.class.isAssignableFrom(field.getType()))
                    field.set(instance, 0);
                else if (char.class.isAssignableFrom(field.getType()))
                    field.set(instance, '\u0000');
                else if (byte.class.isAssignableFrom(field.getType()))
                    field.set(instance, 0);
                else if (long.class.isAssignableFrom(field.getType()))
                    field.set(instance, 0L);
                else if (float.class.isAssignableFrom(field.getType()))
                    field.set(instance, 0.0f);
                else if (double.class.isAssignableFrom(field.getType()))
                    field.set(instance, 0.0d);
            } else if (!hasValue && field.getType().equals(Boolean.class)) {
                field.set(instance, Boolean.FALSE);
            } else
                field.set(instance, null);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static Field getInstanceField(Object instance, String fieldName) {
        for( Class<?> clazz = instance.getClass(); clazz != null; clazz = clazz.getSuperclass() ) {
            Field foundField = null;
            try {
                foundField = clazz.getDeclaredField(fieldName);
                return foundField;
            }
            catch (NoSuchFieldException e) {
                // field not found, keep looking
            }
        }
        return null; // field not found
    }

    public void injectValueIntoField(Object instance, String fieldName, ProcessedOption option) throws OptionValidatorException {
        injectValueIntoField(instance, fieldName, option, true);
    }

    public void injectValueIntoField(Object instance, String fieldName, ProcessedOption option, boolean doValidation) throws OptionValidatorException {
        if(option.getConverter() == null)
            return;
        try {
            Field field = getInstanceField(instance, fieldName);
            if(!Modifier.isPublic(field.getModifiers()))
                field.setAccessible(true);
            if(!Modifier.isPublic(instance.getClass().getModifiers())) {
                Constructor constructor = instance.getClass().getDeclaredConstructor();
                if(constructor != null)
                    constructor.setAccessible(true);
            }
            if(option.getOptionType() == OptionType.NORMAL || option.getOptionType() == OptionType.BOOLEAN) {
                if(option.getValue() != null)
                    field.set(instance, doConvert(option.getValue(), option, doValidation));
                else if(option.getDefaultValues().size() > 0) {
                    field.set(instance, doConvert(
                        option.getDefaultValues().get(0), option, doValidation));
                }
            }
            else if(option.getOptionType() == OptionType.LIST || option.getOptionType() == OptionType.ARGUMENT) {
                if(field.getType().isInterface() || Modifier.isAbstract(field.getType().getModifiers())) {
                    if(Set.class.isAssignableFrom(field.getType())) {
                        Set tmpSet = new HashSet<Object>();
                        if(option.getValues().size() > 0) {
                            for(String in : option.getValues())
                                tmpSet.add(doConvert(in, option, doValidation));
                        }
                        else if(option.getDefaultValues().size() > 0) {
                            for(String in : option.getDefaultValues())
                                tmpSet.add(doConvert(in, option, doValidation));
                        }

                        field.set(instance, tmpSet);
                    }
                    else if(List.class.isAssignableFrom(field.getType())) {
                        List tmpList = new ArrayList();
                        if(option.getValues().size() > 0) {
                            for(String in : option.getValues())
                                tmpList.add(doConvert(in, option, doValidation));
                        }
                        else if(option.getDefaultValues().size() > 0) {
                            for(String in : option.getDefaultValues())
                                tmpList.add(doConvert(in, option, doValidation));
                        }
                        field.set(instance, tmpList);
                    }
                    //todo: should support more that List/Set
                }
                else {
                    Collection tmpInstance = (Collection) field.getType().newInstance();
                    if(option.getValues().size() > 0) {
                        for(String in : option.getValues())
                            tmpInstance.add(doConvert(in, option, doValidation));
                    }
                    else if(option.getDefaultValues().size() > 0) {
                        for(String in : option.getDefaultValues())
                            tmpInstance.add(doConvert(in, option, doValidation));
                    }
                    field.set(instance, tmpInstance);
                }
            }
            else if(option.getOptionType() == OptionType.GROUP) {
                if(field.getType().isInterface() || Modifier.isAbstract(field.getType().getModifiers())) {
                    Map<String, Object> tmpMap = new HashMap<String, Object>();
                    for(String propertyKey : option.getProperties().keySet())
                        tmpMap.put(propertyKey,doConvert(
                            option.getProperties().get(propertyKey), option,
                            doValidation));
                    field.set(instance, tmpMap);
                }
                else {
                    Map<String,Object> tmpMap = (Map<String,Object>) field.getType().newInstance();
                    for(String propertyKey : option.getProperties().keySet())
                        tmpMap.put(propertyKey,doConvert(
                            option.getProperties().get(propertyKey), option,
                            doValidation));
                    field.set(instance, tmpMap);
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    private static Object doConvert(String inputValue, ProcessedOption option, boolean doValidation) throws OptionValidatorException {
        Object result = option.getConverter().convert(inputValue);
        if(option.getValidator() != null && doValidation)
            option.getValidator().validate(result);
        return result;
    }
}
