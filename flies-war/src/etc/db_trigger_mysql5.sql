DROP TRIGGER /*!50033 IF EXISTS */ `HDocument_Update`;
DELIMITER $$
CREATE TRIGGER `HDocument_Update` BEFORE UPDATE on `HDocument` FOR EACH ROW BEGIN IF NEW.revision != OLD.revision THEN INSERT INTO HDocumentHistory(document_id,revision,contentType,docId,locale,name,path,lastChanged,last_modified_by_id,obsolete) VALUES (OLD.id,OLD.revision,OLD.contentType,OLD.docId,OLD.locale,OLD.name,OLD.path,OLD.lastChanged,OLD.last_modified_by_id,OLD.obsolete); END IF; END$$
DELIMITER ;
DROP TRIGGER /*!50033 IF EXISTS */ `HTextFlow_Update`;
DELIMITER $$
CREATE TRIGGER `HTextFlow_Update` BEFORE UPDATE on `HTextFlow` FOR EACH ROW BEGIN IF NEW.revision != OLD.revision THEN INSERT INTO HTextFlowHistory(tf_id,revision,content, obsolete, pos) VALUES (OLD.id,OLD.revision,OLD.content,OLD.obsolete,OLD.pos); END IF; END$$
DELIMITER ;
DROP TRIGGER /*!50033 IF EXISTS */ `HTextFlowTarget_Update`;
DELIMITER $$
CREATE TRIGGER `HTextFlowTarget_Update` BEFORE UPDATE on `HTextFlowTarget` FOR EACH ROW BEGIN IF NEW.versionNum != OLD.versionNum THEN INSERT INTO HTextFlowTarget(target_id,versionNum,content, lastChanged, last_modified_by_id, state, tf_revision) VALUES (OLD.id,OLD.versionNum,OLD.content,OLD.lastChanged,OLD.last_modified_by_id,OLD.state,OLD.tf_revision); END IF; END$$
DELIMITER ;

