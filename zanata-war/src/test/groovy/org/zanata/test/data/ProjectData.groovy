package org.zanata.test.data

import org.zanata.model.HLocale
import org.zanata.common.LocaleId
import org.zanata.model.HTextFlowTarget
import org.zanata.model.HTextFlow
import org.zanata.model.HDocument
import org.zanata.model.HProjectIteration
import org.zanata.model.HProject
import org.zanata.model.HAccount
import org.zanata.model.HPerson
import org.zanata.model.HIterationProject

/**
 * Locale Data in the form of a groovy script.
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
zanata {
   test {
      insert = [
            new HIterationProject(slug: "groovy-test-project1", name: "Groovy Test 1", description: "This is a test project"),
            new HIterationProject(slug: "groovy-test-project2", name: "Groovy Test 2", description: "This is a test project"),
            new HIterationProject(slug: "groovy-test-project3", name: "Groovy Test 3", description: "This is a test project"),
            new HIterationProject(slug: "groovy-test-project4", name: "Groovy Test 4", description: "This is a test project"),
            new HIterationProject(slug: "groovy-test-project5", name: "Groovy Test 5", description: "This is a test project"),
            new HIterationProject(slug: "groovy-test-project6", name: "Groovy Test 6", description: "This is a test project")
      ]
   }
}