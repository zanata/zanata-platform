package net.openl10n.flies.testing;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

public class UnitTestTransformer implements IAnnotationTransformer
{
   private static final Logger log = LoggerFactory.getLogger(UnitTestTransformer.class);

   private static final String UNIT_TESTS = "unit-tests";

   @SuppressWarnings("rawtypes")
   @Override
   public void transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod)
   {
      if (testClass != null)
      {
         setDefaultGroup(annotation, UNIT_TESTS, testClass);
      }
      else if (testConstructor != null)
      {
         setDefaultGroup(annotation, UNIT_TESTS, testConstructor);
      }
      else if (testMethod != null)
      {
         setDefaultGroup(annotation, UNIT_TESTS, testMethod);
      }
   }

   private void setDefaultGroup(ITestAnnotation annotation, String group, Object subject)
   {
      if (annotation.getGroups().length == 0)
      {
         log.warn("assuming group '{}' for '{}' (no groups specified)", group, subject);
         annotation.setGroups(new String[] { group });
      }
   }

}