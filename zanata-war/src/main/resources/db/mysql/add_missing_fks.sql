/*
 * Warning: Please do not change this file!
 * Any changes to the database schema should be added as
 * Liquibase changeSets in db.changelog.xml
 */

ALTER TABLE Activity ADD CONSTRAINT FKActivity_person FOREIGN KEY IF NOT EXISTS (actor_id) REFERENCES HPerson(id);
ALTER TABLE HAccountOption ADD CONSTRAINT FK_HAccountOption_HAccount FOREIGN KEY IF NOT EXISTS (account_id) REFERENCES HAccount(id);
ALTER TABLE HCredentials ADD CONSTRAINT FK_credentials_account FOREIGN KEY IF NOT EXISTS (account_id) REFERENCES HAccount(id);
ALTER TABLE HDocumentUpload ADD CONSTRAINT FK_HDocumentUpload_Locale FOREIGN KEY IF NOT EXISTS (localeId) REFERENCES HLocale(id);
ALTER TABLE HDocumentUpload ADD CONSTRAINT FK_HDocumentUpload_ProjectIteration FOREIGN KEY IF NOT EXISTS (projectIterationId) REFERENCES HProjectIteration(id);
ALTER TABLE HDocumentUploadPart ADD CONSTRAINT FK_HDocumentUploadPart_DocumentUpload FOREIGN KEY IF NOT EXISTS (documentUploadId) REFERENCES HDocumentUpload(id);
ALTER TABLE HDocument_RawDocument ADD CONSTRAINT FK_HDocumentRawDocument_Document FOREIGN KEY IF NOT EXISTS (documentId) REFERENCES HDocument(id);
ALTER TABLE HDocument_RawDocument ADD CONSTRAINT FK_HDocumentRawDocument_RawDocument FOREIGN KEY IF NOT EXISTS (rawDocumentId) REFERENCES HRawDocument(id);
ALTER TABLE HIterationGroup_Maintainer ADD CONSTRAINT FKiterationGroupMaintainer_iterationGroupId FOREIGN KEY IF NOT EXISTS (iterationGroupId) REFERENCES HIterationGroup(id);
ALTER TABLE HIterationGroup_Maintainer ADD CONSTRAINT FKiterationGroupMaintainer_personId FOREIGN KEY IF NOT EXISTS (personId) REFERENCES HPerson(id);
ALTER TABLE HIterationGroup_ProjectIteration ADD CONSTRAINT FKiterationGroup_ProjectIteration_iterationGroupId FOREIGN KEY IF NOT EXISTS (iterationGroupId) REFERENCES HIterationGroup(id);
ALTER TABLE HIterationGroup_ProjectIteration ADD CONSTRAINT FKiterationGroup_ProjectIteration_projectIterationId FOREIGN KEY IF NOT EXISTS (projectIterationId) REFERENCES HProjectIteration(id);
ALTER TABLE HPersonEmailValidationKey ADD CONSTRAINT FK_HPersonEmailValidationKey_HPerson FOREIGN KEY IF NOT EXISTS (personId) REFERENCES HPerson(id);
ALTER TABLE HProjectIteration_Validation ADD CONSTRAINT FK_HProjectIteration_Validation_HProjectIteration FOREIGN KEY IF NOT EXISTS (projectIterationId) REFERENCES HProjectIteration(id);
ALTER TABLE HProject_AllowedRole ADD CONSTRAINT FK_HProjectAllowedRole_Project FOREIGN KEY IF NOT EXISTS (projectId) REFERENCES HProject(id);
ALTER TABLE HProject_AllowedRole ADD CONSTRAINT FK_HProjectAllowedRole_Role FOREIGN KEY IF NOT EXISTS (roleId) REFERENCES HAccountRole(id);
ALTER TABLE HProject_Validation ADD CONSTRAINT FK_HProject_Validation_HProject FOREIGN KEY IF NOT EXISTS (projectId) REFERENCES HProject(id);
ALTER TABLE HRoleAssignmentRule ADD CONSTRAINT FK_HRoleAssignmentRule_HAccountRole FOREIGN KEY IF NOT EXISTS (role_to_assign_id) REFERENCES HAccountRole(id);
ALTER TABLE HTextFlowContentHistory ADD CONSTRAINT FKcontent_text_flow_history FOREIGN KEY IF NOT EXISTS (text_flow_history_id) REFERENCES HTextFlowHistory(id);
ALTER TABLE HTextFlowTargetContentHistory ADD CONSTRAINT FKcontent_text_flow_target_history FOREIGN KEY IF NOT EXISTS (text_flow_target_history_id) REFERENCES HTextFlowTargetHistory(id);
ALTER TABLE HTextFlowTargetReviewComment ADD CONSTRAINT FKtarget_review_comment FOREIGN KEY IF NOT EXISTS (target_id) REFERENCES HTextFlowTarget(id);
ALTER TABLE HTextFlowTargetReviewComment ADD CONSTRAINT FKtarget_review_commenter FOREIGN KEY IF NOT EXISTS (commenter_id) REFERENCES HPerson(id);
ALTER TABLE IterationGroup_Locale ADD CONSTRAINT FK_IterationGroup_Locale_HIterationGroup FOREIGN KEY IF NOT EXISTS (iteration_group_id) REFERENCES HIterationGroup(id);
ALTER TABLE IterationGroup_Locale ADD CONSTRAINT FK_IterationGroup_Locale_HLocale FOREIGN KEY IF NOT EXISTS (locale_id) REFERENCES HLocale(id);
ALTER TABLE TransMemoryUnit ADD CONSTRAINT FK_tmunit_trans_memory FOREIGN KEY IF NOT EXISTS (tm_id) REFERENCES TransMemory(id);
ALTER TABLE TransMemoryUnitVariant ADD CONSTRAINT FK_TransUnitVariant_TransUnit FOREIGN KEY IF NOT EXISTS (trans_unit_id) REFERENCES TransMemoryUnit(id);
ALTER TABLE TransMemory_Metadata ADD CONSTRAINT FK_Metadata_TransMemory FOREIGN KEY IF NOT EXISTS (trans_memory_id) REFERENCES TransMemory(id);
