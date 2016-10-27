#!/bin/bash

# once you have run cargowait.sh script and cargo has started, you can run this
# script to take a snapshot of current database and store it as
# target/database.sql.
# subsequent cargowait.sh execution can then use -n option (no clean) to make
# use of this script.

scriptBaseDir=$(dirname $0)

functionalTestTarget=${scriptBaseDir}/../../functional-test/target

targetPath=$(readlink -f ${functionalTestTarget})

${targetPath}/mysql-dist/bin/mysqldump --port=13306 --user=root --password=root --socket=${targetPath}/mdb/mysql.sock root > ${targetPath}/database.sql

ls -ltr ${targetPath}/database.sql
