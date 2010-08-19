CREATE TRIGGER HDocument_Update AFTER UPDATE ON HDocument FOR EACH ROW CALL "net.openl10n.flies.H2DocumentHistoryTrigger";
CREATE TRIGGER HTextFlow_Update AFTER UPDATE ON HTextFlow FOR EACH ROW CALL "net.openl10n.flies.H2TextFlowHistoryTrigger";
CREATE TRIGGER HTextFlowTarget_Update AFTER UPDATE ON HTextFlowTarget FOR EACH ROW CALL "net.openl10n.flies.H2TextFlowTargetHistoryTrigger";