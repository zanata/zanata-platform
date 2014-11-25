#!/bin/bash

scriptBaseDir=$(dirname $0)

functionalTestTarget=${scriptBaseDir}/../../functional-test/target

targetPath=$(readlink -f ${functionalTestTarget})

${targetPath}/mysql-dist/bin/mysqldump --port=13306 --user=root --password=root --socket=${targetPath}/mdb/mysql.sock root > ${targetPath}/database.sql

ls -ltr ${targetPath}/database.sql
