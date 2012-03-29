-- executed by hibernate on startup.
-- http://relation.to/Bloggers/RotterdamJBugAndHibernatesImportsql
-- This script is for unit tests only. 
-- It should run the same tasks as the one in src/main/resources/db/h2|mysql/create_trigger.sql

CREATE TRIGGER HDocument_Update AFTER UPDATE ON HDocument FOR EACH ROW CALL "org.zanata.H2DocumentHistoryTrigger";