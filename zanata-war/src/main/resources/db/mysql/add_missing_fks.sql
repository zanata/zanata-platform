/*
 * Warning: Please do not change this file!
 * Any changes to the database schema should be added as
 * Liquibase changeSets in db.changelog.xml
 */

/* DELIMITER GO */

DROP PROCEDURE IF EXISTS zanataAddMissingFKs
GO

CREATE PROCEDURE zanataAddMissingFKs()
BEGIN
    IF 0 = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS WHERE
           CONSTRAINT_SCHEMA = SCHEMA() AND
           CONSTRAINT_NAME   = 'FKActivity_person' AND
           CONSTRAINT_TYPE   = 'FOREIGN KEY') THEN
        ALTER TABLE Activity ADD CONSTRAINT FKActivity_person FOREIGN KEY (actor_id) REFERENCES HPerson(id);
    END IF;
    IF 0 = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS WHERE
        CONSTRAINT_SCHEMA = SCHEMA() AND
        CONSTRAINT_NAME   = 'FK_HAccountOption_HAccount' AND
        CONSTRAINT_TYPE   = 'FOREIGN KEY') THEN
        ALTER TABLE HAccountOption ADD CONSTRAINT FK_HAccountOption_HAccount FOREIGN KEY (account_id) REFERENCES HAccount(id);
    END IF;
    IF 0 = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS WHERE
        CONSTRAINT_SCHEMA = SCHEMA() AND
        CONSTRAINT_NAME   = 'FK_credentials_account' AND
        CONSTRAINT_TYPE   = 'FOREIGN KEY') THEN
        ALTER TABLE HCredentials ADD CONSTRAINT FK_credentials_account FOREIGN KEY (account_id) REFERENCES HAccount(id);
    END IF;
    IF 0 = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS WHERE
        CONSTRAINT_SCHEMA = SCHEMA() AND
        CONSTRAINT_NAME   = 'FK_HDocumentUpload_Locale' AND
        CONSTRAINT_TYPE   = 'FOREIGN KEY') THEN
        ALTER TABLE HDocumentUpload ADD CONSTRAINT FK_HDocumentUpload_Locale FOREIGN KEY (localeId) REFERENCES HLocale(id);
    END IF;
    IF 0 = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS WHERE
        CONSTRAINT_SCHEMA = SCHEMA() AND
        CONSTRAINT_NAME   = 'FK_HDocumentUpload_ProjectIteration' AND
        CONSTRAINT_TYPE   = 'FOREIGN KEY') THEN
        ALTER TABLE HDocumentUpload ADD CONSTRAINT FK_HDocumentUpload_ProjectIteration FOREIGN KEY (projectIterationId) REFERENCES HProjectIteration(id);
    END IF;
    IF 0 = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS WHERE
        CONSTRAINT_SCHEMA = SCHEMA() AND
        CONSTRAINT_NAME   = 'FK_HDocumentUploadPart_DocumentUpload' AND
        CONSTRAINT_TYPE   = 'FOREIGN KEY') THEN
        ALTER TABLE HDocumentUploadPart ADD CONSTRAINT FK_HDocumentUploadPart_DocumentUpload FOREIGN KEY (documentUploadId) REFERENCES HDocumentUpload(id);
    END IF;
    IF 0 = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS WHERE
        CONSTRAINT_SCHEMA = SCHEMA() AND
        CONSTRAINT_NAME   = 'FK_HDocumentRawDocument_Document' AND
        CONSTRAINT_TYPE   = 'FOREIGN KEY') THEN
        ALTER TABLE HDocument_RawDocument ADD CONSTRAINT FK_HDocumentRawDocument_Document FOREIGN KEY (documentId) REFERENCES HDocument(id);
    END IF;
    IF 0 = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS WHERE
        CONSTRAINT_SCHEMA = SCHEMA() AND
        CONSTRAINT_NAME   = 'FK_HDocumentRawDocument_RawDocument' AND
        CONSTRAINT_TYPE   = 'FOREIGN KEY') THEN
        ALTER TABLE HDocument_RawDocument ADD CONSTRAINT FK_HDocumentRawDocument_RawDocument FOREIGN KEY (rawDocumentId) REFERENCES HRawDocument(id);
    END IF;
    IF 0 = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS WHERE
        CONSTRAINT_SCHEMA = SCHEMA() AND
        CONSTRAINT_NAME   = 'FKiterationGroupMaintainer_iterationGroupId' AND
        CONSTRAINT_TYPE   = 'FOREIGN KEY') THEN
        ALTER TABLE HIterationGroup_Maintainer ADD CONSTRAINT FKiterationGroupMaintainer_iterationGroupId FOREIGN KEY (iterationGroupId) REFERENCES HIterationGroup(id);
    END IF;
    IF 0 = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS WHERE
        CONSTRAINT_SCHEMA = SCHEMA() AND
        CONSTRAINT_NAME   = 'FKiterationGroupMaintainer_personId' AND
        CONSTRAINT_TYPE   = 'FOREIGN KEY') THEN
        ALTER TABLE HIterationGroup_Maintainer ADD CONSTRAINT FKiterationGroupMaintainer_personId FOREIGN KEY (personId) REFERENCES HPerson(id);
    END IF;
    IF 0 = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS WHERE
        CONSTRAINT_SCHEMA = SCHEMA() AND
        CONSTRAINT_NAME   = 'FKiterationGroup_ProjectIteration_iterationGroupId' AND
        CONSTRAINT_TYPE   = 'FOREIGN KEY') THEN
        ALTER TABLE HIterationGroup_ProjectIteration ADD CONSTRAINT FKiterationGroup_ProjectIteration_iterationGroupId FOREIGN KEY (iterationGroupId) REFERENCES HIterationGroup(id);
    END IF;
    IF 0 = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS WHERE
        CONSTRAINT_SCHEMA = SCHEMA() AND
        CONSTRAINT_NAME   = 'FKiterationGroup_ProjectIteration_projectIterationId' AND
        CONSTRAINT_TYPE   = 'FOREIGN KEY') THEN
        ALTER TABLE HIterationGroup_ProjectIteration ADD CONSTRAINT FKiterationGroup_ProjectIteration_projectIterationId FOREIGN KEY (projectIterationId) REFERENCES HProjectIteration(id);
    END IF;
    IF 0 = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS WHERE
        CONSTRAINT_SCHEMA = SCHEMA() AND
        CONSTRAINT_NAME   = 'FK_HPersonEmailValidationKey_HPerson' AND
        CONSTRAINT_TYPE   = 'FOREIGN KEY') THEN
        ALTER TABLE HPersonEmailValidationKey ADD CONSTRAINT FK_HPersonEmailValidationKey_HPerson FOREIGN KEY (personId) REFERENCES HPerson(id);
    END IF;
    IF 0 = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS WHERE
        CONSTRAINT_SCHEMA = SCHEMA() AND
        CONSTRAINT_NAME   = 'FK_HProjectIteration_Validation_HProjectIteration' AND
        CONSTRAINT_TYPE   = 'FOREIGN KEY') THEN
        ALTER TABLE HProjectIteration_Validation ADD CONSTRAINT FK_HProjectIteration_Validation_HProjectIteration FOREIGN KEY (projectIterationId) REFERENCES HProjectIteration(id);
    END IF;
    IF 0 = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS WHERE
        CONSTRAINT_SCHEMA = SCHEMA() AND
        CONSTRAINT_NAME   = 'FK_HProjectAllowedRole_Project' AND
        CONSTRAINT_TYPE   = 'FOREIGN KEY') THEN
        ALTER TABLE HProject_AllowedRole ADD CONSTRAINT FK_HProjectAllowedRole_Project FOREIGN KEY (projectId) REFERENCES HProject(id);
    END IF;
    IF 0 = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS WHERE
        CONSTRAINT_SCHEMA = SCHEMA() AND
        CONSTRAINT_NAME   = 'FK_HProjectAllowedRole_Role' AND
        CONSTRAINT_TYPE   = 'FOREIGN KEY') THEN
        ALTER TABLE HProject_AllowedRole ADD CONSTRAINT FK_HProjectAllowedRole_Role FOREIGN KEY (roleId) REFERENCES HAccountRole(id);
    END IF;
    IF 0 = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS WHERE
        CONSTRAINT_SCHEMA = SCHEMA() AND
        CONSTRAINT_NAME   = 'FK_HProject_Validation_HProject' AND
        CONSTRAINT_TYPE   = 'FOREIGN KEY') THEN
        ALTER TABLE HProject_Validation ADD CONSTRAINT FK_HProject_Validation_HProject FOREIGN KEY (projectId) REFERENCES HProject(id);
    END IF;
    IF 0 = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS WHERE
        CONSTRAINT_SCHEMA = SCHEMA() AND
        CONSTRAINT_NAME   = 'FK_HRoleAssignmentRule_HAccountRole' AND
        CONSTRAINT_TYPE   = 'FOREIGN KEY') THEN
        ALTER TABLE HRoleAssignmentRule ADD CONSTRAINT FK_HRoleAssignmentRule_HAccountRole FOREIGN KEY (role_to_assign_id) REFERENCES HAccountRole(id);
    END IF;
    IF 0 = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS WHERE
        CONSTRAINT_SCHEMA = SCHEMA() AND
        CONSTRAINT_NAME   = 'FKcontent_text_flow_history' AND
        CONSTRAINT_TYPE   = 'FOREIGN KEY') THEN
        ALTER TABLE HTextFlowContentHistory ADD CONSTRAINT FKcontent_text_flow_history FOREIGN KEY (text_flow_history_id) REFERENCES HTextFlowHistory(id);
    END IF;
    IF 0 = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS WHERE
        CONSTRAINT_SCHEMA = SCHEMA() AND
        CONSTRAINT_NAME   = 'FKcontent_text_flow_target_history' AND
        CONSTRAINT_TYPE   = 'FOREIGN KEY') THEN
        ALTER TABLE HTextFlowTargetContentHistory ADD CONSTRAINT FKcontent_text_flow_target_history FOREIGN KEY (text_flow_target_history_id) REFERENCES HTextFlowTargetHistory(id);
    END IF;
    IF 0 = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS WHERE
        CONSTRAINT_SCHEMA = SCHEMA() AND
        CONSTRAINT_NAME   = 'FKtarget_review_comment' AND
        CONSTRAINT_TYPE   = 'FOREIGN KEY') THEN
        ALTER TABLE HTextFlowTargetReviewComment ADD CONSTRAINT FKtarget_review_comment FOREIGN KEY (target_id) REFERENCES HTextFlowTarget(id);
    END IF;
    IF 0 = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS WHERE
        CONSTRAINT_SCHEMA = SCHEMA() AND
        CONSTRAINT_NAME   = 'FKtarget_review_commenter' AND
        CONSTRAINT_TYPE   = 'FOREIGN KEY') THEN
        ALTER TABLE HTextFlowTargetReviewComment ADD CONSTRAINT FKtarget_review_commenter FOREIGN KEY (commenter_id) REFERENCES HPerson(id);
    END IF;
    IF 0 = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS WHERE
        CONSTRAINT_SCHEMA = SCHEMA() AND
        CONSTRAINT_NAME   = 'FK_IterationGroup_Locale_HIterationGroup' AND
        CONSTRAINT_TYPE   = 'FOREIGN KEY') THEN
        ALTER TABLE IterationGroup_Locale ADD CONSTRAINT FK_IterationGroup_Locale_HIterationGroup FOREIGN KEY (iteration_group_id) REFERENCES HIterationGroup(id);
    END IF;
    IF 0 = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS WHERE
        CONSTRAINT_SCHEMA = SCHEMA() AND
        CONSTRAINT_NAME   = 'FK_IterationGroup_Locale_HLocale' AND
        CONSTRAINT_TYPE   = 'FOREIGN KEY') THEN
        ALTER TABLE IterationGroup_Locale ADD CONSTRAINT FK_IterationGroup_Locale_HLocale FOREIGN KEY (locale_id) REFERENCES HLocale(id);
    END IF;
    IF 0 = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS WHERE
        CONSTRAINT_SCHEMA = SCHEMA() AND
        CONSTRAINT_NAME   = 'FK_tmunit_trans_memory' AND
        CONSTRAINT_TYPE   = 'FOREIGN KEY') THEN
        ALTER TABLE TransMemoryUnit ADD CONSTRAINT FK_tmunit_trans_memory FOREIGN KEY (tm_id) REFERENCES TransMemory(id);
    END IF;
    IF 0 = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS WHERE
        CONSTRAINT_SCHEMA = SCHEMA() AND
        CONSTRAINT_NAME   = 'FK_TransUnitVariant_TransUnit' AND
        CONSTRAINT_TYPE   = 'FOREIGN KEY') THEN
        ALTER TABLE TransMemoryUnitVariant ADD CONSTRAINT FK_TransUnitVariant_TransUnit FOREIGN KEY (trans_unit_id) REFERENCES TransMemoryUnit(id);
    END IF;
    IF 0 = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS WHERE
        CONSTRAINT_SCHEMA = SCHEMA() AND
        CONSTRAINT_NAME   = 'FK_Metadata_TransMemory' AND
        CONSTRAINT_TYPE   = 'FOREIGN KEY') THEN
        ALTER TABLE TransMemory_Metadata ADD CONSTRAINT FK_Metadata_TransMemory FOREIGN KEY (trans_memory_id) REFERENCES TransMemory(id);
    END IF;
END
GO
CALL zanataAddMissingFKs()
GO
DROP PROCEDURE zanataAddMissingFKs
GO
