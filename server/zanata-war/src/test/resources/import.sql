-- executed by hibernate on startup.
-- http://relation.to/Bloggers/RotterdamJBugAndHibernatesImportsql
-- This script is for unit tests only. 
-- It should run the same tasks as the one in src/main/resources/db/h2|mysql/create_trigger.sql

CREATE TRIGGER HDocument_Update AFTER UPDATE ON HDocument FOR EACH ROW CALL "org.zanata.H2DocumentHistoryTrigger";
/*CREATE TRIGGER HTextFlow_Update AFTER UPDATE ON HTextFlow FOR EACH ROW CALL "org.zanata.H2TextFlowHistoryTrigger";*/
/*CREATE TRIGGER HTextFlowTarget_Update AFTER UPDATE ON HTextFlowTarget FOR EACH ROW CALL "org.zanata.H2TextFlowTargetHistoryTrigger";*/
