/*
 * This file creates triggers for the H2 database. Please see src/test/resources/import.sql for a similar script that 
 * should do the same for unit tests.
 */
DROP TRIGGER IF EXISTS `HDocument_Update`;
CREATE TRIGGER HDocument_Update AFTER UPDATE ON HDocument FOR EACH ROW CALL "org.zanata.H2DocumentHistoryTrigger";
DROP TRIGGER IF EXISTS `HTextFlow_Update`;
DROP TRIGGER IF EXISTS `HTextFlowTarget_Update`;