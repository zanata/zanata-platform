/**
 * This is where all Remote dependencies are configured for each of the Zanata
 * overlay distributions.
 * Each distribution under the 'distros' folder may have a configuration entry
 * on this file. Each of those entries must have the following structure:
 *
 * <<distro_name>> {
 *     <<dependency_name>> {
 *         url = "Url where the dependency is to be downloaded from"
 *         toFile = "File (on the distro build folder) where the downloaded dependecny should reside"
 *         extract = true|false // Indicates if the dependency is to be extracted in place. If it is, the original file will be removed after extraction.
 *     }
 *
 *     ... More dependencies
 * }
 *
 * The dependency name is not relevant, just an identifier.
 */

'wildfly' {
    hibernatemodule {
        url = "https://bintray.com/artifact/download/sflanigan/generic/wildfly-module-hibernate-main-4.2.20.Final.zip"
        toFile = "/hibernate-module.zip"
        extract = true
    }

}
