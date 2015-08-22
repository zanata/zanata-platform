import groovy.sql.Sql
import groovy.transform.Field

import java.sql.DatabaseMetaData
import java.sql.ResultSet

/**
 * This is a script to migrate a Zanata database from Mysql to Postgresql.
 *
 * Usage: groovy Mysql2Postgresql -h
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@GrabConfig(systemClassLoader= true)
@Grapes([
    @Grab(group='mysql', module='mysql-connector-java', version='5.1.36'),
    @Grab(group='org.postgresql', module='postgresql', version='9.4-1201-jdbc41')
])

@Field Sql mysqldb
@Field Sql postgresdb

@Field DatabaseMetaData mysqlMetadata

// Global list of already created indexes (for collision detection)
@Field List createdIdxs = []

// Global list of auto-increment sequences
// Will be used to update their values after data insertion
@Field List autoIncrementSeqs = []

// Initialize everything
if(!initialize(args)) {
    return 0
}

// Create All Tables
ResultSet mysqlTables = mysqlMetadata.getTables(null, null, null, ['TABLE'] as String[])
while(mysqlTables.next()) {
    def tableName = mysqlTables.getString(3)
    ResultSet columns = mysqlMetadata.getColumns(null, null, "$tableName", null)

    createTable(tableName, columns)
    columns.close()
}
mysqlTables.close()

// Create indexes
mysqlTables = mysqlMetadata.getTables(null, null, null, ['TABLE'] as String[])
while(mysqlTables.next()) {
    def tableName = mysqlTables.getString(3)
    ResultSet indexes = mysqlMetadata.getIndexInfo(null, null, tableName, false, false)

    createIndexes(tableName, indexes)

    indexes.close()
}
mysqlTables.close()

// Create data
mysqlTables = mysqlMetadata.getTables(null, null, null, ['TABLE'] as String[])
while(mysqlTables.next()) {
    def tableName = mysqlTables.getString(3)
    try {
        extractDataForTable(tableName)
    }
    catch (Exception ex){
        try {
            throw ex.nextException
        }
        catch(MissingPropertyException mpex) {
            throw ex
        }
    }
}
mysqlTables.close()

// Create All Foreign Keys
mysqlTables = mysqlMetadata.getTables(null, null, null, ['TABLE'] as String[])
while(mysqlTables.next()) {
    def tableName = mysqlTables.getString(3)
    ResultSet foreignKeys = mysqlMetadata.getImportedKeys(null, null, tableName)

    createForeignKeys(tableName, foreignKeys)

    foreignKeys.close()
}
mysqlTables.close()

// Create Triigers
createTriggers()

// Manually increment sequences
updateAutoIncrementSequences(autoIncrementSeqs)

// ========================= Helper Functions =========================

// Initialize all necessary variables, using parameters of default values where
// indicated
// Returns an object indicating if the script should proceed
def initialize(args) {
    def cli = new CliBuilder(usage: 'groovy Mysql2Postgresql.groovy [options]', header: 'Options:')

    cli.mysqlhost(args:1, argName: 'mysqlhost', 'Host name or IP address of the mysql database (default: localhost)')
    cli.mysqlport(args:1, argName: 'mysqlport', 'Open port to connect to the mysql database (default: 3306)')
    cli.mysqlschema(args:1, argName: 'mysqlschema', 'MySql schema to migrate')
    cli.mysqluser(args:1, argName: 'mysqluser', 'User for the mysql database (default: "")')
    cli.mysqlpassword(args:1, argName: 'mysqlpassword', 'Password for the mysql database (default: "")')

    cli.psqlhost(args:1, argName: 'psqlhost', 'Host name or IP address of the postgresql database (default: localhost)')
    cli.psqlport(args:1, argName: 'psqlport', 'Open port to connect to the postgresql database (default: 5432)')
    cli.psqldb(args:1, argName: 'psqldb', 'Postgresql database to migrate data into')
    cli.psqluser(args:1, argName: 'psqluser', 'User for the portgresql database (default: "")')
    cli.psqlpassword(args:1, argName: 'psqlpassword', 'Password for the postgresql database (default: "")')

    cli.h(longOpt: 'help', 'Print this message')

    def options = cli.parse(args)

    def cliArgs = options.arguments()
    if(options.help) {
        println cli.usage()
        return false
    }

    def mysqlhost = options.mysqlhost ?: "localhost"
    def mysqlport = options.mysqlport ?: "3306"
    def mysqlschema = options.mysqlschema
    def mysqluser = options.mysqluser ?: ""
    def mysqlpassword = options.mysqlpassword ?: ""

    def psqlhost = options.psqlhost ?: "localhost"
    def psqlport = options.psqlport ?: "5432"
    def psqldb = options.psqldb
    def psqluser = options.psqluser ?: ""
    def psqlpassword = options.psqlpassword ?: ""

    if(!mysqlschema) {
        throw new Exception("You must provide a mysql schema to migrate")
    }
    if(!psqldb) {
        throw new Exception("You must provide a PostgreSql schema to migrate into")
    }

    mysqldb = Sql.newInstance("jdbc:mysql://${mysqlhost}:${mysqlport}/${mysqlschema}",
        "${mysqluser}", "${mysqlpassword}", "com.mysql.jdbc.Driver")
    mysqlMetadata = mysqldb.connection.metaData

    postgresdb = Sql.newInstance("jdbc:postgresql://${psqlhost}:${psqlport}/${psqldb}",
        "${psqluser}", "${psqlpassword}", "org.postgresql.Driver")
}

def createTable(String tableName, ResultSet columns) {
    println "Creating table $tableName"
    StringBuilder createTableSql = new StringBuilder("CREATE TABLE $tableName ( ")
    boolean firstCol = true
    while(columns.next()) {
        def colName = columns.getString(4)
        def dataType = columns.getInt(5)
        def dataTypeName = columns.getString(6)
        def size = columns.getInt(7)
        def isNullable = "YES".equalsIgnoreCase( columns.getString(18) ) ? true : false
        def isAutoIncrement = "YES".equalsIgnoreCase( columns.getString(23) ) ? true : false
        def isGenerated = "YES".equalsIgnoreCase( columns.getString(24) ) ? true : false

        // Create a sequence entry if this is an auto-incremented column
        if(isAutoIncrement) {
            autoIncrementSeqs <<
                [seqName  : "${tableName}_${colName}_seq".toLowerCase(),
                 tableName: tableName,
                 colName  : colName]
        }

        createTableSql.append("\n ${!firstCol ? ', ' : ''} ${translateColumnName(colName)} ${translateTypeName(dataTypeName, isAutoIncrement, size)} ${!isNullable ? ' NOT NULL ' : ''}")

        firstCol = false
    }
    createTableSql.append("\n " + primaryKey(mysqlMetadata, tableName))
    createTableSql.append("\n)")

    postgresdb.execute(createTableSql.toString())
}

// Translates a mysql data type name to postgresql's type name
def translateTypeName(String mysqlTypeName, boolean isAutoIncrement, int size) {
    // Autoincrement columns
    if( (mysqlTypeName == "BIGINT" || mysqlTypeName == "INT")
        && isAutoIncrement) {
        return "BIGSERIAL"
    }
    else if( mysqlTypeName == "DATETIME" ) {
        return "TIMESTAMP"
    }
    else if( mysqlTypeName == "LONGTEXT" ) {
        return "TEXT"
    }
    else if( mysqlTypeName == "LONGBLOB" ) {
        return "BYTEA"
    }
    else if( mysqlTypeName == "VARCHAR" ||
             mysqlTypeName == "CHAR" ) {
        return "$mysqlTypeName($size)"
    }
    else if( mysqlTypeName == "TINYINT" ||
             mysqlTypeName == "BIT" ) {
        return "boolean"
    }
    else {
        return mysqlTypeName
    }
}



// Returns a primary key statement for postgresql
def primaryKey(DatabaseMetaData metaData, String tableName) {
    StringBuilder primaryKeyStatement = new StringBuilder(", PRIMARY KEY( ")
    ResultSet primaryKeyCols = metaData.getPrimaryKeys(null, null, tableName)
    boolean firstCol = true

    while(primaryKeyCols.next()) {
        def colName = primaryKeyCols.getString(4)

        primaryKeyStatement.append("${!firstCol ? ',' : ''} $colName")
        firstCol = false
    }
    primaryKeyCols.close()
    primaryKeyStatement.append(")")

    // There are cases with no primary key
    if(firstCol) {
        return ""
    }
    else {
        return primaryKeyStatement.toString()
    }
}

// Creates foreign keys for a table
// NB: This assumes no multi-columns keys
def createForeignKeys(String tableName, ResultSet foreignKeys) {

    println "Creating foreign keys for table $tableName"

    def fkMap = [:]
    // keep a record of created FKs as postgresql doesn't allow duplicate names
    def createdFKs = []

    while(foreignKeys.next()) {
        def pkTableName = foreignKeys.getString(3)
        def pkColName = foreignKeys.getString(4)
        def fkTableName = foreignKeys.getString(7)
        def fkColName = foreignKeys.getString(8)
        def fkName = foreignKeys.getString(12)

        // Rename the key if there's already another one with the same name
        if(createdFKs.contains(fkName)) {
            fkName = "${fkName}_${fkTableName}"
        }

//        def fkName = foreignKeys.getString(12)
//        if(!fkMap.containsKey(fkName)) {
//            fkMap[fkName] = ['pk']
//        }
//        def fkData = fkMap[fkName]

        def foreignKeyStmt = "ALTER TABLE $fkTableName ADD CONSTRAINT $fkName FOREIGN KEY (${translateColumnName(fkColName)}) REFERENCES $pkTableName(${translateColumnName(pkColName)})"
        postgresdb.execute(foreignKeyStmt.toString())
        createdFKs << fkName
    }
}

def createIndexes(String tableName, ResultSet indexes) {

    println "Creating indexes for table $tableName"

    def indexMap = [:]

    while(indexes.next()) {
        def idxName = indexes.getString(6)

        // Ignore primary key indexes as they will be created automatically.... hopefully
        if(!"PRIMARY".equalsIgnoreCase(idxName)) {

            if (!indexMap.containsKey(idxName)) {
                indexMap.put(idxName, [cols: []])
            }
            def idxData = indexMap[idxName]

            idxData.isUnique = !indexes.getBoolean(4) // This returns if it's NON_UNIQUE
            idxData.idxName = idxName
            idxData.cols << indexes.getString(9)
        }
    }

    indexMap.each { k, v ->
        // If an index with the same name has already been created, rename it
        def idxName = "$k".toLowerCase()
        if(createdIdxs.contains(idxName)) {
            idxName = "${idxName}_${tableName}"
        }

        def indexStmt = "CREATE ${v.isUnique ? 'UNIQUE' : ''} INDEX ${idxName} ON $tableName ( ${v.cols.join(',')} )"
        postgresdb.execute(indexStmt.toString())
        createdIdxs << idxName
    }
}

// Translates the column name from mysql to postegresql
// This will print some warnings when this happens
def String translateColumnName(String mysqlColName) {
    if("user".equalsIgnoreCase(mysqlColName)) {
        println "WARNING: Column $mysqlColName will be migrated as \"user\" as it's a reserved word in postresql"
        return "\"user\""
    }

    return mysqlColName
}

// Extracts the data for a table in mysql and inserts it into postgresql
def extractDataForTable(String tableName) {

    println "Migrating data for table $tableName"

    def tableCols = []

    def rows = mysqldb.rows("select * from $tableName".toString()) { meta ->
        int columnCount = meta.columnCount

        (1..columnCount).each { colIdx ->
            def colName = meta.getColumnName(colIdx)
            def colTypeName = meta.getColumnTypeName(colIdx)

            tableCols << [name:translateColumnName(colName),
                          plainName: colName,
                          type: colTypeName]
        }
    }

    StringBuffer insertStr = new StringBuffer("INSERT INTO $tableName (")
    insertStr.append( tableCols.collect { it.name }.join(",") )
    insertStr.append(") VALUES (")
    insertStr.append( tableCols.collect { ":${it.plainName}" }.join(",") )
    insertStr.append(")")

    postgresdb.withBatch(50, insertStr.toString()) { ps ->
        rows.each { row ->
            ps.addBatch( transformDataForPsql(row, tableCols) )
        }
    }
}

// Transforms a row map from mysql into a row map to be inserted into postegresql
Map transformDataForPsql(Map mysqlRow, def columnDefs) {
    mysqlRow.each { colName, value ->
        def colDef = columnDefs.find { it.name == colName }

        // Tiny ints need to be turned to booleans
        if(value instanceof Integer && colDef.type == "TINYINT") {
            mysqlRow.put(colName, value > 0 ? true : false)
        }
    }

    mysqlRow
}

// Updates all sequences to their appropriate value
def updateAutoIncrementSequences(List seqs) {
    println "Incrementing auto-increment Sequences"

    seqs.each { s ->
        postgresdb.execute("SELECT setval('${s.seqName}', (select max(${s.colName}) from ${s.tableName}));".toString())
    }
}

// Create the necessary db triggers
def createTriggers() {
    println "Creating triggers"

    // NB: Because this is so DB specific, and there is but a single trigger,
    // they are hard-coded
    postgresdb.execute("""
    CREATE FUNCTION add_document_history() RETURNS trigger AS \$add_document_history\$
       BEGIN
          IF NEW.revision != OLD.revision THEN
             INSERT INTO HDocumentHistory(document_id,revision,contentType,docId,locale,name,path,lastChanged,last_modified_by_id,obsolete)
                VALUES (OLD.id,OLD.revision,OLD.contentType,OLD.docId,OLD.locale,OLD.name,OLD.path,OLD.lastChanged,OLD.last_modified_by_id,OLD.obsolete);
          END IF;
       END;
    \$add_document_history\$ LANGUAGE plpgsql;

    CREATE TRIGGER HDocument_Update BEFORE UPDATE ON HDocument
       FOR EACH ROW EXECUTE PROCEDURE add_document_history();
    """.toString())
}

