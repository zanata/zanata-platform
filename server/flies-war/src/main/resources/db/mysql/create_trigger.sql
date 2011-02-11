/* DELIMITER GO */

 /* 
 NB: This file contains several comments which prevent Liquibase from 
 splitting triggers into their component statements. 
 */

DROP TRIGGER IF EXISTS `HDocument_Update`
GO

CREATE TRIGGER `HDocument_Update`
   BEFORE UPDATE ON `HDocument`
   FOR EACH ROW BEGIN
      IF NEW.revision != OLD.revision THEN
         INSERT INTO HDocumentHistory(document_id,revision,contentType,docId,locale,name,path,lastChanged,last_modified_by_id,obsolete) 
            VALUES (OLD.id,OLD.revision,OLD.contentType,OLD.docId,OLD.locale,OLD.name,OLD.path,OLD.lastChanged,OLD.last_modified_by_id,OLD.obsolete); /* don't GO */
      END IF; /* don't GO */
   END
GO

DROP TRIGGER IF EXISTS `HTextFlow_Update`
GO

CREATE TRIGGER `HTextFlow_Update`
   BEFORE UPDATE on `HTextFlow`
   FOR EACH ROW BEGIN
      IF NEW.revision != OLD.revision THEN
         INSERT INTO HTextFlowHistory(tf_id,revision,content, obsolete, pos)
            VALUES (OLD.id,OLD.revision,OLD.content,OLD.obsolete,OLD.pos); /* don't GO */
      END IF; /* don't GO */
   END
GO

DROP TRIGGER IF EXISTS `HTextFlowTarget_Update`;

CREATE TRIGGER `HTextFlowTarget_Update`
   BEFORE UPDATE on `HTextFlowTarget`
   FOR EACH ROW BEGIN
      IF NEW.versionNum != OLD.versionNum THEN
         INSERT INTO HTextFlowTargetHistory (target_id,versionNum,content, lastChanged, last_modified_by_id, state, tf_revision)
            VALUES (OLD.id,OLD.versionNum,OLD.content,OLD.lastChanged,OLD.last_modified_by_id,OLD.state,OLD.tf_revision); /* don't GO */
      END IF; /* don't GO */
   END
GO
