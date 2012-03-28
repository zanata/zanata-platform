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

/*CREATE TRIGGER `HTextFlow_Update`
   BEFORE UPDATE on `HTextFlow`
   FOR EACH ROW BEGIN
      IF NEW.revision != OLD.revision THEN
         INSERT INTO HTextFlowHistory(
            tf_id,revision,
            content0,content1,content2,content3,content4,content5,
            obsolete,pos)
            VALUES (
               OLD.id,OLD.revision,
               OLD.content0,OLD.content1,OLD.content2,OLD.content3,OLD.content4,OLD.content5,
               OLD.obsolete,OLD.pos);
      END IF;
   END
GO*/

DROP TRIGGER IF EXISTS `HTextFlowTarget_Update`
GO

/*CREATE TRIGGER `HTextFlowTarget_Update`
   BEFORE UPDATE on `HTextFlowTarget`
   FOR EACH ROW BEGIN
      IF NEW.versionNum != OLD.versionNum THEN
         INSERT INTO HTextFlowTargetHistory (
            target_id,versionNum,
            content0,content1,content2,content3,content4,content5,
            lastChanged,last_modified_by_id,state,tf_revision)
            VALUES (
            OLD.id,OLD.versionNum,
            OLD.content0,OLD.content1,OLD.content2,OLD.content3,OLD.content4,OLD.content5,
            OLD.lastChanged,OLD.last_modified_by_id,OLD.state,OLD.tf_revision);
      END IF;
   END
GO*/
