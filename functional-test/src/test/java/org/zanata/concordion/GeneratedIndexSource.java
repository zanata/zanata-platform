package org.zanata.concordion;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.concordion.api.Resource;
import org.concordion.internal.ClassPathSource;
import org.junit.runners.Suite;
import com.google.common.base.CaseFormat;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;

/**
 * This overrides ClassPathSource. It ignores the classpath resource and generates one on the fly.
 *
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
*/
@Slf4j
class GeneratedIndexSource extends ClassPathSource
{
   private static final String TEMPLATE = "<html><head><title>%s</title></head><body><h4>%s</h4>%s</body></html>";

   private Class<?> testSuiteClass;
   private String descriptionHeading;

   GeneratedIndexSource(Class<?> testSuiteClass, String descriptionHeading)
   {
      this.testSuiteClass = testSuiteClass;
      this.descriptionHeading = descriptionHeading;
   }

   /**
    * It will generate an index page for a package.
    * Package name will be parsed to sentence as title.
    * Description heading will come from field. If null will use generated title.
    * If test suite class is set, it will use test suite classes to generate an ordered list as links to individual test.
    * If test suite class is not set, it will find all spec files under the package and generate a unordered list of links.
    *
    * @param resource resource of the package index class. See http://www.concordion.org/dist/1.4.2/test-output/concordion/spec/concordion/results/breadcrumbs/Breadcrumbs.html
    * @return a input stream that contains the generated index page.
    * @throws IOException
    */
   @Override
   public InputStream createInputStream(Resource resource) throws IOException
   {
      String title = convertPackageNameToTitle(resource);
      log.info("index page title: {}", title);

      descriptionHeading = Objects.firstNonNull(descriptionHeading, title);
      log.info("description heading is: {}", descriptionHeading);

      List<String> specFiles;
      if (testSuiteClass != null)
      {
         specFiles = getSpecFilesFromTestSuite(resource);
      }
      else
      {
         specFiles = getSpecFilesUnderPackage(resource);
      }
      log.info("index page will include following files: {}", specFiles);

      String list = createListOfLinksToSpecFiles(specFiles, testSuiteClass != null);

      return new ByteArrayInputStream(String.format(TEMPLATE, title, descriptionHeading, list).getBytes("UTF-8"));
   }

   private static String convertPackageNameToTitle(Resource resource)
   {
      String packageName = resource.getParent().getName();
      return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, packageName).replaceAll("_", " ");
   }

   private static List<String> getSpecFilesUnderPackage(Resource resource)
   {
      String parentPath = getParentPath(resource);
      URL parentUrl = Thread.currentThread().getContextClassLoader().getResource(parentPath);
      File parentDir = new File(parentUrl.getFile());
      String[] specFiles = parentDir.list(new FilenameFilter()
      {
         @Override
         public boolean accept(File dir, String name)
         {
            return name.endsWith(".html");
         }
      });
      return Lists.newArrayList(specFiles);
   }

   private static String getParentPath(Resource resource)
   {
      String path = resource.getParent().getPath();
      return path.startsWith("/") ? path.substring(1) : path;
   }

   private List<String> getSpecFilesFromTestSuite(Resource resource)
   {
      Suite.SuiteClasses suiteClasses = testSuiteClass.getAnnotation(Suite.SuiteClasses.class);
      List<Class<?>> testClasses = Lists.newArrayList(suiteClasses.value());
      log.info("test classes under this suite: {}", testClasses);
      List<String> specFiles = Lists.transform(testClasses, new Function<Class<?>, String>()
      {
         @Override
         public String apply(Class<?> input)
         {
            String simpleName = input.getSimpleName();
            return simpleName.substring(0, simpleName.length() - 4) + ".html";
         }
      });
      specFiles.remove(resource.getName()); // we don't want index page itself appearing in the list
      return specFiles;
   }

   private static String createListOfLinksToSpecFiles(List<String> specFiles, boolean useOrderedList)
   {
      if (specFiles.isEmpty())
      {
         log.warn("empty spec file list!!!");
         return "";
      }
      List<String> listItems = Lists.transform(specFiles, new Function<String, String>()
      {
         @Override
         public String apply(String input)
         {
            // TODO the text for link is using file name. Should improve a bit
            return String.format("<li><a href='%s'>%s</a></li>", input, input);
         }
      });
      String specJoined = Joiner.on("\n").join(listItems);
      if (useOrderedList)
      {
         return "<ol>" + specJoined + "</ol>";
      }
      else
      {
         return "<ul>" + specJoined + "</ul>";
      }
   }
}
