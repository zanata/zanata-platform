CREATE TRIGGER HTextFlow_Update AFTER UPDATE ON HTextFlow FOR EACH ROW CALL "org.fedorahosted.flies.H2TextFlowHistoryTrigger";
