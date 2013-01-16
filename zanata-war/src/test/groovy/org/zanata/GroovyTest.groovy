package org.zanata

import org.testng.annotations.Test
import org.zanata.model.HProject
import org.zanata.test.data.ProjectData

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.greaterThan
import static org.hamcrest.Matchers.equalTo

/**
 * Sample Groovy test.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
class GroovyTest extends ZanataGroovyJpaTest
{

   @Override
   List getConfigScriptClasses()
   {
      [
         ProjectData.class
      ]
   }

   @Override
   List getEntitiesToCreate()
   {
      [
         new HProject(slug: "test-proj", name: "Test Project", description: "A Test Project")
      ];
   }

   @Test
   void sampleTest()
   {
      def project = getEm().createQuery("from HProject where slug = 'groovy-test-project1'").singleResult
      assertThat(project.name, equalTo("Groovy Test 1"))
   }

   @Test
   void sampleTest2()
   {
      def projects = getEm().createQuery("from HProject").resultList
      assertThat(projects.size(), greaterThan(0))
   }

}
