/* DELIMITER GO */

CREATE OR REPLACE FUNCTION add_document_history() RETURNS trigger
    AS $add_document_history$
    BEGIN
        IF NEW.revision != OLD.revision THEN
            INSERT INTO HDocumentHistory(document_id,revision,contentType,docId,locale,name,path,lastChanged,last_modified_by_id,obsolete)
                VALUES (OLD.id,OLD.revision,OLD.contentType,OLD.docId,OLD.locale,OLD.name,OLD.path,OLD.lastChanged,OLD.last_modified_by_id,OLD.obsolete);
        END IF;
    END;
$add_document_history$ LANGUAGE plpgsql
;;;

DROP TRIGGER IF EXISTS HDocument_Update ON HDocument
;;;

CREATE TRIGGER HDocument_Update BEFORE UPDATE ON HDocument
    FOR EACH ROW EXECUTE PROCEDURE add_document_history()
;;;
