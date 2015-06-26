# Export the structure from mysql
mysqldump  --tz-utc=FALSE --create-options=FALSE --protocol=tcp --comments=FALSE --default-character-set=utf8 --host=localhost --user=root --quote-names=FALSE --lock-tables=FALSE --add-locks=FALSE --port=3306  --compatible=postgresql --no-data "zanata" > db-baseline-struct.sql

# Export the data
mysqldump  --tz-utc=FALSE --create-options=FALSE --protocol=tcp --comments=FALSE --default-character-set=utf8 --host=localhost --user=root --quote-names=FALSE --lock-tables=FALSE --add-locks=FALSE --port=3306 --no-create-info=TRUE --skip-triggers --compatible=postgresql "zanata" > Zanata-pgsql-compatible-dump.sql