package org.zanata.testing;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jboss.seam.mock.AbstractSeamTest;
import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

@SuppressWarnings("rawtypes")
public class TestTransformer implements IAnnotationTransformer
{
   private static final String UNIT_TESTS = "unit-tests";
   private static final boolean WARN_ABOUT_MISSING_GROUP = false;

   @Override
   public void transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod)
   {
      if (testClass != null)
      {
         fixGroups(annotation, testClass, testClass);
      }
      else if (testConstructor != null)
      {
         fixGroups(annotation, testConstructor.getDeclaringClass(), testConstructor);
      }
      else if (testMethod != null)
      {
         fixGroups(annotation, testMethod.getDeclaringClass(), testMethod);
      }
   }

   private void fixGroups(ITestAnnotation annotation, Class testClass, Object subject)
   {
      if (isSeamTest(testClass))
      {
         ensureSeamGroup(annotation, subject);
      }
      else
      {
         checkUnitTestGroup(annotation, subject);
      }
   }

   private boolean isSeamTest(Class testClass)
   {
      return AbstractSeamTest.class.isAssignableFrom(testClass);
   }

   private void ensureSeamGroup(ITestAnnotation annotation, Object subject)
   {
      String group = "seam-tests";
      List<String> groups = new ArrayList<String>(Arrays.asList(annotation.getGroups()));
      if (!groups.contains(group))
      {
         System.err.printf("assuming group '%s' for '%s'\n", group, subject);
         groups.add(group);
         annotation.setGroups(groups.toArray(new String[groups.size()]));
      }
   }

   private void checkUnitTestGroup(ITestAnnotation annotation, Object subject)
   {
      String rightGroup = UNIT_TESTS;
      String wrongGroup = "unit-test";
      List<String> groups = new ArrayList<String>(Arrays.asList(annotation.getGroups()));
      if (groups.size() == 0)
      {
         if (WARN_ABOUT_MISSING_GROUP)
         {
            System.err.printf("assuming group '%s' for '%s' (no groups specified)\n", rightGroup, subject);
         }
         annotation.setGroups(new String[] { rightGroup });
      }
      else
      {
         if (groups.contains(wrongGroup))
         {
            groups.add(UNIT_TESTS);
            System.err.printf("adding group '%s' for '%s' (was '%s)'\n", rightGroup, subject, wrongGroup);
            annotation.setGroups(groups.toArray(new String[groups.size()]));
         }
      }
   }

}