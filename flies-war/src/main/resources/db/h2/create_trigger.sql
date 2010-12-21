DROP TRIGGER IF EXISTS `HDocument_Update`;
CREATE TRIGGER HDocument_Update AFTER UPDATE ON HDocument FOR EACH ROW CALL "net.openl10n.flies.H2DocumentHistoryTrigger";
DROP TRIGGER IF EXISTS `HTextFlow_Update`;
CREATE TRIGGER HTextFlow_Update AFTER UPDATE ON HTextFlow FOR EACH ROW CALL "net.openl10n.flies.H2TextFlowHistoryTrigger";
DROP TRIGGER IF EXISTS `HTextFlowTarget_Update`;
CREATE TRIGGER HTextFlowTarget_Update AFTER UPDATE ON HTextFlowTarget FOR EACH ROW CALL "net.openl10n.flies.H2TextFlowTargetHistoryTrigger";
