
def download(String address, File destFile) {
    def file = new FileOutputStream(destFile)
    def out = new BufferedOutputStream(file)
    out << new URL(address).openStream()
    out.close()
}

def mojarraUrl = "http://sourceforge.net/projects/zanata/files/wildfly/wildfly-8.1.0.Final-module-mojarra-2.1.28.zip/download"
def hibernateUrl = "http://sourceforge.net/projects/zanata/files/wildfly/wildfly-8.1.0.Final-module-hibernate-main-4.2.15.Final.zip/download"

File mojarraZip = new File(project.build.directory, "mojarra.zip")
File hibernateZip = new File(project.build.directory, "hibernate.zip")

download(mojarraUrl, mojarraZip)
download(hibernateUrl, hibernateZip)


def ant = new AntBuilder()

def mojarraModuleDest = project.build.directory + "/mojarra"
ant.unzip(src: mojarraZip.absolutePath, dest: mojarraModuleDest, overwrite: "true" )
def hibernateModuleDest = project.build.directory + "/hibernate"
ant.unzip(src: hibernateZip.absolutePath, dest: hibernateModuleDest, overwrite: "true" )

File wildFlyInstallRoot = new File(project.properties['cargo.extract.dir'])
def dirFilter = {
    it.isDirectory();
} as FileFilter

def listSubDirs = { File it ->
    it.listFiles(dirFilter)
}

def subDirs = listSubDirs(wildFlyInstallRoot)
File wildFlyRoot;
while (subDirs.size() == 1) {
    wildFlyRoot = subDirs[0]
    subDirs = listSubDirs(wildFlyRoot)
}

assert wildFlyRoot != null
println "Wild Fly root is at $wildFlyRoot"

ant.copy(todir: wildFlyRoot, overwrite: true) {
    fileset(dir: mojarraModuleDest)
}

ant.copy(todir: wildFlyRoot, overwrite: true) {
    fileset(dir: hibernateModuleDest)
}
