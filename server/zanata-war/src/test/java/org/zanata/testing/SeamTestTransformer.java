package org.zanata.testing;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jboss.seam.mock.AbstractSeamTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

public class SeamTestTransformer implements IAnnotationTransformer
{
   private static final Logger log = LoggerFactory.getLogger(SeamTestTransformer.class);

   @SuppressWarnings("rawtypes")
   @Override
   public void transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod)
   {
      if (testClass != null)
      {
         if (isSeamTest(testClass))
         {
            addGroup(annotation, "seam-tests", testClass);
         }
      }
      else if (testConstructor != null)
      {
         if (isSeamTest(testConstructor.getDeclaringClass()))
         {
            addGroup(annotation, "seam-tests", testConstructor);
         }
      }
      else if (testMethod != null)
      {
         if (isSeamTest(testMethod.getDeclaringClass()))
         {
            addGroup(annotation, "seam-tests", testMethod);
         }
      }
   }

   private boolean isSeamTest(Class<?> testClass)
   {
      return AbstractSeamTest.class.isAssignableFrom(testClass);
   }

   private void addGroup(ITestAnnotation annotation, String group, Object subject)
   {
      List<String> groups = new ArrayList<String>(Arrays.asList(annotation.getGroups()));
      if (!groups.contains(group))
      {
         log.warn("assuming group '{}' for '{}'", group, subject);
         groups.add(group);
         annotation.setGroups(groups.toArray(new String[groups.size()]));
      }
   }

}