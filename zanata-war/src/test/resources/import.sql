-- executed by hibernate on startup.
-- http://relation.to/Bloggers/RotterdamJBugAndHibernatesImportsql
-- This script is for unit tests only. 
-- It should run a superset of the tasks in src/main/resources/db/h2|mysql/create_trigger.sql

CREATE TRIGGER IF NOT EXISTS HDocument_Update AFTER UPDATE ON HDocument FOR EACH ROW CALL "org.zanata.H2DocumentHistoryTrigger";
CREATE ALIAS IF NOT EXISTS MD5 FOR "org.zanata.util.HashUtil.generateHash";
