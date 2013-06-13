package org.zanata

import org.concordion.integration.junit4.ConcordionRunner
import org.junit.After
import org.junit.runner.RunWith

// This is a quick and dirty way of generating an index page for all concordion tests.
@RunWith(ConcordionRunner)
class ZanataTest {

    @After
    void generateIndex()
    {
        URL url = Thread.currentThread().getContextClassLoader().getResource(getClass().name.replace('.', '/') + ".class");

        File currentDir = new File(url.getFile());

        def packagePath = getClass().getPackage().getName().replace('.', '/') + '/'

        def dirPath = "$currentDir.parentFile.absolutePath/"

        def htmls = []
        currentDir.getParentFile().eachFileRecurse {
            if (it.name.endsWith(".html"))
            {
                // because we write index.html to concordion output dir root,
                // we need to prepend package path of this class
                def relativePath = packagePath + (it.absolutePath - dirPath)
                htmls << "<li><a href='$relativePath'>$it.name</a></li>"
            }
        }

//        println(htmls)

        def links = htmls.join("\n")
        String content = """
            <html>
            <head><title>Concordion Tests</title></head>
            <body>
                <ul>
                    $links
                </ul>
            </body>
            </html>
        """

        def concordionOutputDir = System.getProperty("concordion.output.dir", "/tmp/concordion")

        def indexFile = new File(concordionOutputDir, "index.html")

        indexFile.withWriter {
            it.write(content)
        }
    }
}
