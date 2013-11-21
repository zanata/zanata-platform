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

import com.google.common.collect.Maps;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import org.jboss.aesh.cl.internal.ProcessedCommand;
import org.jboss.aesh.cl.internal.ProcessedOption;
import org.jboss.aesh.console.command.Command;
import org.zanata.client.commands.ConfigurableOptions;
import org.zanata.client.commands.stats.GetStatisticsOptions;
import org.zanata.client.commands.stats.GetStatisticsOptionsImpl;
import org.zanata.console.command.ConsoleCommand;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class AeshCommandGenerator {

    public static Class<?> generateCommandClass(ProcessedCommand processedCommand) {

        String dynaClassName = processedCommand.getName() + "Class";
        Map<String, Class<?>> fields = Maps.newHashMap();

        for( ProcessedOption opt : processedCommand.getOptions() ) {
            fields.put( opt.getFieldName(), opt.getType() );
        }

        try {
            return generate(dynaClassName, fields);
        }
        catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
        catch (CannotCompileException e) {
            throw new RuntimeException(e);
        }
    }

    private static Class<?> generate(String className, Map<String, Class<?>> properties) throws
        NotFoundException,
        CannotCompileException {

        ClassPool pool = ClassPool.getDefault();
        CtClass cc = pool.getAndRename("org.zanata.console.command.ConsoleCommand", "org.zanata.console.generated." + className);

        // add this to define an interface to implement

        for (Map.Entry<String, Class<?>> entry : properties.entrySet()) {
            CtField cf = new CtField(resolveCtClass(entry.getValue()), entry
                .getKey(), cc);
            cc.addField(cf);

            // add getter
            //cc.addMethod(generateGetter(cc, entry.getKey(), entry.getValue()));

            // add setter
            //cc.addMethod(generateSetter(cc, entry.getKey(), entry.getValue()));
        }

        return cc.toClass();
    }

    private static CtMethod generateGetter(CtClass declaringClass, String fieldName, Class fieldClass)
        throws CannotCompileException {

        String getterName = "get" + fieldName.substring(0, 1).toUpperCase()
            + fieldName.substring(1);

        StringBuffer sb = new StringBuffer();
        sb.append("public ").append(fieldClass.getName()).append(" ")
            .append(getterName).append("(){").append("return this.")
            .append(fieldName).append(";").append("}");

        return CtMethod.make(sb.toString(), declaringClass);
    }

    private static CtMethod generateSetter(CtClass declaringClass, String fieldName, Class fieldClass)
        throws CannotCompileException {

        String setterName = "set" + fieldName.substring(0, 1).toUpperCase()
            + fieldName.substring(1);

        StringBuffer sb = new StringBuffer();
        sb.append("public void ").append(setterName).append("(")
            .append(fieldClass.getName()).append(" ").append(fieldName)
            .append(")").append("{").append("this.").append(fieldName)
            .append("=").append(fieldName).append(";").append("}");
        return CtMethod.make(sb.toString(), declaringClass);
    }

    private static CtClass resolveCtClass(Class clazz) throws NotFoundException {
        ClassPool pool = ClassPool.getDefault();
        return pool.get(clazz.getName());
    }


//    public static void main(String ... args) throws Exception {
//        ProcessedCommand pc = Args4jCommandGenerator.generateCommand("stats", "",
//            GetStatisticsOptionsImpl.class);
//        Class ci = generateCommandClass(pc);
//        for(Field f : ci.getDeclaredFields()) {
//            System.out.println(f.getName());
//        }
//
//        System.out.println(ci);
//    }
}
