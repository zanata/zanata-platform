CREATE TABLE Activity
(
    id BIGSERIAL PRIMARY KEY NOT NULL,
    creationDate TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    lastChanged TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    versionNum INT NOT NULL,
    actor_id BIGINT NOT NULL,
    approxTime TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    startOffsetMillis INT NOT NULL,
    endOffsetMillis INT NOT NULL,
    contextType VARCHAR(150) NOT NULL,
    context_id BIGINT NOT NULL,
    lastTargetType VARCHAR(150) NOT NULL,
    last_target_id BIGINT NOT NULL,
    activityType VARCHAR(150) NOT NULL,
    eventCount INT NOT NULL,
    wordCount INT NOT NULL
);
CREATE TABLE DATABASECHANGELOG
(
    ID VARCHAR(63) NOT NULL,
    AUTHOR VARCHAR(63) NOT NULL,
    FILENAME VARCHAR(200) NOT NULL,
    DATEEXECUTED TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    ORDEREXECUTED INT NOT NULL,
    EXECTYPE VARCHAR(10) NOT NULL,
    MD5SUM VARCHAR(35),
    DESCRIPTION VARCHAR(255),
    COMMENTS VARCHAR(255),
    TAG VARCHAR(255),
    LIQUIBASE VARCHAR(20),
    PRIMARY KEY (ID, AUTHOR, FILENAME)
);
CREATE TABLE DATABASECHANGELOGLOCK
(
    ID INT PRIMARY KEY NOT NULL,
    LOCKED BOOLEAN NOT NULL,
    LOCKGRANTED TIMESTAMP WITHOUT TIME ZONE,
    LOCKEDBY VARCHAR(255)
);
CREATE TABLE HAccount
(
    id BIGSERIAL PRIMARY KEY NOT NULL,
    creationDate TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    lastChanged TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    versionNum INT NOT NULL,
    apiKey VARCHAR(32),
    enabled BOOLEAN NOT NULL,
    passwordHash VARCHAR(255),
    username VARCHAR(255),
    mergedInto BIGINT
);
CREATE TABLE HAccountActivationKey
(
    keyHash VARCHAR(32) PRIMARY KEY NOT NULL,
    accountId BIGINT NOT NULL,
    creationDate TIMESTAMP WITHOUT TIME ZONE DEFAULT '0002-11-30 00:00:01' NOT NULL
);
CREATE TABLE HAccountMembership
(
    accountId BIGINT NOT NULL,
    memberOf INT NOT NULL,
    PRIMARY KEY (accountId, memberOf)
);
CREATE TABLE HAccountOption
(
    name VARCHAR(255) NOT NULL,
    value TEXT,
    account_id BIGINT NOT NULL,
    id BIGSERIAL PRIMARY KEY NOT NULL,
    creationDate TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    lastChanged TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    versionNum INT NOT NULL
);
CREATE TABLE HAccountResetPasswordKey
(
    keyHash VARCHAR(32) PRIMARY KEY NOT NULL,
    accountId BIGINT NOT NULL
);
CREATE TABLE HAccountRole
(
    id BIGSERIAL PRIMARY KEY NOT NULL,
    conditional BOOLEAN NOT NULL,
    name VARCHAR(255),
    roleType CHAR(1) NOT NULL
);
CREATE TABLE HAccountRoleGroup
(
    roleId INT NOT NULL,
    memberOf INT NOT NULL,
    PRIMARY KEY (roleId, memberOf)
);
CREATE TABLE HApplicationConfiguration
(
    id BIGSERIAL PRIMARY KEY NOT NULL,
    creationDate TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    lastChanged TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    versionNum INT NOT NULL,
    config_key VARCHAR(255) NOT NULL,
    config_value TEXT NOT NULL
);
CREATE TABLE HCopyTransOptions
(
    contextMismatchAction CHAR(1) NOT NULL,
    docIdMismatchAction CHAR(1) NOT NULL,
    projectMismatchAction CHAR(1) NOT NULL,
    id BIGSERIAL PRIMARY KEY NOT NULL,
    creationDate TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    lastChanged TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    versionNum INT NOT NULL
);
CREATE TABLE HCredentials
(
    account_id BIGINT NOT NULL,
    type VARCHAR(10) NOT NULL,
    user_string TEXT,
    email VARCHAR(100),
    id BIGSERIAL PRIMARY KEY NOT NULL,
    creationDate TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    lastChanged TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    versionNum INT NOT NULL
);
CREATE TABLE HDocument
(
    id BIGSERIAL PRIMARY KEY NOT NULL,
    creationDate TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    lastChanged TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    versionNum INT NOT NULL,
    contentType VARCHAR(255) NOT NULL,
    docId VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    obsolete BOOLEAN NOT NULL,
    path VARCHAR(255) NOT NULL,
    revision INT NOT NULL,
    last_modified_by_id BIGINT,
    locale BIGINT NOT NULL,
    poHeader_id BIGINT,
    project_iteration_id BIGINT NOT NULL
);
CREATE TABLE HDocumentHistory
(
    id BIGSERIAL PRIMARY KEY NOT NULL,
    contentType VARCHAR(255) NOT NULL,
    docId VARCHAR(255) NOT NULL,
    lastChanged TIMESTAMP WITHOUT TIME ZONE,
    name VARCHAR(255),
    obsolete BOOLEAN NOT NULL,
    path VARCHAR(255),
    revision INT,
    document_id BIGINT,
    last_modified_by_id BIGINT,
    locale BIGINT NOT NULL
);
CREATE TABLE HDocumentUpload
(
    projectIterationId BIGINT NOT NULL,
    docId VARCHAR(255) NOT NULL,
    contentHash CHAR(32) NOT NULL,
    type VARCHAR(255) NOT NULL,
    localeId BIGINT,
    id BIGSERIAL PRIMARY KEY NOT NULL,
    creationDate TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    lastChanged TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    versionNum INT NOT NULL
);
CREATE TABLE HDocumentUploadPart
(
    id BIGSERIAL PRIMARY KEY NOT NULL,
    documentUploadId BIGINT NOT NULL,
    partIndex INT NOT NULL,
    content BYTEA NOT NULL
);
CREATE TABLE HDocument_RawDocument
(
    documentId BIGINT NOT NULL,
    rawDocumentId BIGINT NOT NULL,
    PRIMARY KEY (documentId, rawDocumentId)
);
CREATE TABLE HGlossaryEntry
(
    id BIGSERIAL PRIMARY KEY NOT NULL,
    creationDate TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    lastChanged TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    versionNum INT NOT NULL,
    srcLocaleId BIGINT NOT NULL,
    sourceRef TEXT
);
CREATE TABLE HGlossaryTerm
(
    id BIGSERIAL PRIMARY KEY NOT NULL,
    glossaryEntryId BIGINT NOT NULL,
    localeId BIGINT NOT NULL,
    content TEXT,
    creationDate TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    lastChanged TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    versionNum INT NOT NULL
);
CREATE TABLE HIterationGroup
(
    id BIGSERIAL PRIMARY KEY NOT NULL,
    name VARCHAR(80) NOT NULL,
    slug VARCHAR(40) NOT NULL,
    description VARCHAR(100),
    status CHAR(1) NOT NULL,
    creationDate TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    lastChanged TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    versionNum INT NOT NULL
);
CREATE TABLE HIterationGroup_Maintainer
(
    iterationGroupId BIGINT NOT NULL,
    personId BIGINT NOT NULL,
    PRIMARY KEY (iterationGroupId, personId)
);
CREATE TABLE HIterationGroup_ProjectIteration
(
    iterationGroupId BIGINT NOT NULL,
    projectIterationId BIGINT NOT NULL,
    PRIMARY KEY (iterationGroupId, projectIterationId)
);
CREATE TABLE HLocale
(
    id BIGSERIAL PRIMARY KEY NOT NULL,
    creationDate TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    lastChanged TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    versionNum INT NOT NULL,
    active BOOLEAN NOT NULL,
    localeId VARCHAR(255) NOT NULL,
    enabledByDefault BOOLEAN DEFAULT FALSE,
    pluralForms VARCHAR(100),
    displayName VARCHAR(100),
    nativeName VARCHAR(100)
);
CREATE TABLE HLocale_Member
(
    personId BIGINT NOT NULL,
    supportedLanguageId BIGINT NOT NULL,
    isCoordinator BOOLEAN DEFAULT FALSE NOT NULL,
    isReviewer BOOLEAN DEFAULT FALSE NOT NULL,
    isTranslator BOOLEAN DEFAULT FALSE NOT NULL,
    PRIMARY KEY (supportedLanguageId, personId)
);
CREATE TABLE HPerson
(
    id BIGSERIAL PRIMARY KEY NOT NULL,
    creationDate TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    lastChanged TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    versionNum INT NOT NULL,
    email VARCHAR(255) NOT NULL,
    name VARCHAR(80) NOT NULL,
    accountId BIGINT
);
CREATE TABLE HPersonEmailValidationKey
(
    id BIGSERIAL PRIMARY KEY NOT NULL,
    keyHash VARCHAR(32) NOT NULL,
    personId BIGINT NOT NULL,
    email VARCHAR(255) NOT NULL,
    creationDate TIMESTAMP WITHOUT TIME ZONE NOT NULL
);
CREATE TABLE HPoHeader
(
    id BIGSERIAL PRIMARY KEY NOT NULL,
    creationDate TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    lastChanged TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    versionNum INT NOT NULL,
    entries TEXT,
    comment_id BIGINT
);
CREATE TABLE HPoTargetHeader
(
    id BIGSERIAL PRIMARY KEY NOT NULL,
    creationDate TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    lastChanged TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    versionNum INT NOT NULL,
    entries TEXT,
    comment_id BIGINT,
    document_id BIGINT,
    targetLanguage BIGINT NOT NULL
);
CREATE TABLE HPotEntryData
(
    id BIGSERIAL PRIMARY KEY NOT NULL,
    context VARCHAR(255),
    flags VARCHAR(255),
    refs TEXT,
    comment_id BIGINT
);
CREATE TABLE HProject
(
    id BIGSERIAL PRIMARY KEY NOT NULL,
    creationDate TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    lastChanged TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    versionNum INT NOT NULL,
    slug VARCHAR(40) NOT NULL,
    description VARCHAR(100),
    homeContent TEXT,
    name VARCHAR(80) NOT NULL,
    overrideLocales BOOLEAN NOT NULL,
    status CHAR(1),
    restrictedByRoles BOOLEAN DEFAULT FALSE,
    default_copy_trans_opts_id BIGINT,
    defaultProjectType VARCHAR(255),
    sourceViewURL TEXT,
    sourceCheckoutURL TEXT
);
CREATE TABLE HProjectIteration
(
    id BIGSERIAL PRIMARY KEY NOT NULL,
    creationDate TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    lastChanged TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    versionNum INT NOT NULL,
    slug VARCHAR(40) NOT NULL,
    parentId BIGINT,
    project_id BIGINT NOT NULL,
    overrideLocales BOOLEAN NOT NULL,
    status CHAR(1),
    projectType VARCHAR(255),
    requireTranslationReview BOOLEAN DEFAULT FALSE
);
CREATE TABLE HProjectIteration_Locale
(
    projectIterationId BIGINT NOT NULL,
    localeId BIGINT NOT NULL,
    PRIMARY KEY (projectIterationId, localeId)
);
CREATE TABLE HProjectIteration_LocaleAlias
(
    projectIterationId BIGINT NOT NULL,
    localeId VARCHAR(255) NOT NULL,
    alias VARCHAR(255) NOT NULL,
    PRIMARY KEY (projectIterationId, localeId)
);
CREATE TABLE HProjectIteration_Validation
(
    projectIterationId BIGINT NOT NULL,
    validation VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    PRIMARY KEY (projectIterationId, validation)
);
CREATE TABLE HProject_AllowedRole
(
    projectId BIGINT NOT NULL,
    roleId INT NOT NULL,
    PRIMARY KEY (projectId, roleId)
);
CREATE TABLE HProject_Locale
(
    projectId BIGINT NOT NULL,
    localeId BIGINT NOT NULL,
    PRIMARY KEY (projectId, localeId)
);
CREATE TABLE HProject_LocaleAlias
(
    projectId BIGINT NOT NULL,
    localeId VARCHAR(255) NOT NULL,
    alias VARCHAR(255) NOT NULL,
    PRIMARY KEY (projectId, localeId)
);
CREATE TABLE HProject_Maintainer
(
    personId BIGINT NOT NULL,
    projectId BIGINT NOT NULL,
    PRIMARY KEY (projectId, personId)
);
CREATE TABLE HProject_Validation
(
    projectId BIGINT NOT NULL,
    validation VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    PRIMARY KEY (projectId, validation)
);
CREATE TABLE HRawDocument
(
    type VARCHAR(255) NOT NULL,
    contentHash CHAR(32) NOT NULL,
    uploadedBy VARCHAR(255),
    id BIGSERIAL PRIMARY KEY NOT NULL,
    creationDate TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    lastChanged TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    versionNum INT NOT NULL,
    adapterParameters TEXT,
    fileId TEXT NOT NULL
);
CREATE TABLE HRoleAssignmentRule
(
    policyName VARCHAR(100),
    identityRegExp TEXT,
    role_to_assign_id INT NOT NULL,
    id BIGSERIAL PRIMARY KEY NOT NULL,
    creationDate TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    lastChanged TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    versionNum INT NOT NULL
);
CREATE TABLE HSimpleComment
(
    id BIGSERIAL PRIMARY KEY NOT NULL,
    comment TEXT NOT NULL,
    lastChanged TIMESTAMP WITHOUT TIME ZONE NOT NULL
);
CREATE TABLE HTermComment
(
    id BIGSERIAL PRIMARY KEY NOT NULL,
    comment TEXT NOT NULL,
    pos INT NOT NULL,
    glossaryTermId BIGINT NOT NULL
);
CREATE TABLE HTextFlow
(
    id BIGSERIAL PRIMARY KEY NOT NULL,
    obsolete BOOLEAN NOT NULL,
    pos INT NOT NULL,
    resId VARCHAR(255) NOT NULL,
    revision INT NOT NULL,
    comment_id BIGINT,
    document_id BIGINT NOT NULL,
    potEntryData_id BIGINT,
    wordCount BIGINT NOT NULL,
    contentHash CHAR(32) NOT NULL,
    plural BOOLEAN,
    content0 TEXT,
    content1 TEXT,
    content2 TEXT,
    content3 TEXT,
    content4 TEXT,
    content5 TEXT
);
CREATE TABLE HTextFlowContentHistory
(
    id BIGSERIAL PRIMARY KEY NOT NULL,
    content TEXT NOT NULL,
    pos INT NOT NULL,
    text_flow_history_id BIGINT NOT NULL
);
CREATE TABLE HTextFlowHistory
(
    id BIGSERIAL PRIMARY KEY NOT NULL,
    obsolete BOOLEAN NOT NULL,
    pos INT,
    revision INT,
    tf_id BIGINT
);
CREATE TABLE HTextFlowTarget
(
    id BIGSERIAL PRIMARY KEY NOT NULL,
    creationDate TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    lastChanged TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    versionNum INT NOT NULL,
    state INT NOT NULL,
    tf_revision INT NOT NULL,
    comment_id BIGINT,
    last_modified_by_id BIGINT,
    locale BIGINT NOT NULL,
    tf_id BIGINT,
    content0 TEXT,
    content1 TEXT,
    content2 TEXT,
    content3 TEXT,
    content4 TEXT,
    content5 TEXT,
    translated_by_id BIGINT,
    reviewed_by_id BIGINT,
    revisionComment TEXT
);
CREATE TABLE HTextFlowTargetContentHistory
(
    id BIGSERIAL PRIMARY KEY NOT NULL,
    content TEXT NOT NULL,
    pos INT NOT NULL,
    text_flow_target_history_id BIGINT NOT NULL
);
CREATE TABLE HTextFlowTargetHistory
(
    id BIGSERIAL PRIMARY KEY NOT NULL,
    lastChanged TIMESTAMP WITHOUT TIME ZONE,
    state INT,
    tf_revision INT,
    versionNum INT,
    last_modified_by_id BIGINT,
    target_id BIGINT,
    translated_by_id BIGINT,
    reviewed_by_id BIGINT,
    revisionComment TEXT
);
CREATE TABLE HTextFlowTargetReviewComment
(
    id BIGSERIAL PRIMARY KEY NOT NULL,
    creationDate TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    lastChanged TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    commenter_id BIGINT NOT NULL,
    target_id BIGINT NOT NULL,
    comment TEXT NOT NULL,
    targetVersion INT NOT NULL,
    versionNum INT NOT NULL
);
CREATE TABLE IterationGroup_Locale
(
    iteration_group_id BIGINT NOT NULL,
    locale_id BIGINT NOT NULL,
    PRIMARY KEY (iteration_group_id, locale_id)
);
CREATE TABLE TransMemory
(
    description TEXT,
    slug VARCHAR(40) NOT NULL,
    source_language VARCHAR(255),
    id BIGSERIAL PRIMARY KEY NOT NULL,
    creationDate TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    lastChanged TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    versionNum INT NOT NULL
);
CREATE TABLE TransMemoryUnit
(
    trans_unit_id TEXT,
    source_language VARCHAR(255),
    tm_id BIGINT NOT NULL,
    unique_id VARCHAR(255) NOT NULL,
    position INT,
    metadata_type VARCHAR(75),
    metadata TEXT,
    id BIGSERIAL PRIMARY KEY NOT NULL,
    creationDate TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    lastChanged TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    versionNum INT NOT NULL
);
CREATE TABLE TransMemoryUnitVariant
(
    language VARCHAR(255) NOT NULL,
    trans_unit_id BIGINT NOT NULL,
    tagged_segment TEXT NOT NULL,
    plain_text_segment TEXT NOT NULL,
    plain_text_segment_hash CHAR(32) NOT NULL,
    metadata_type VARCHAR(75),
    metadata TEXT,
    id BIGSERIAL PRIMARY KEY NOT NULL,
    creationDate TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    lastChanged TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    versionNum INT NOT NULL
);
CREATE TABLE TransMemory_Metadata
(
    trans_memory_id BIGINT NOT NULL,
    metadata_type VARCHAR(75) NOT NULL,
    metadata TEXT,
    PRIMARY KEY (trans_memory_id, metadata_type)
);
CREATE TABLE WebHook
(
    id BIGSERIAL PRIMARY KEY NOT NULL,
    projectId BIGINT NOT NULL,
    url TEXT NOT NULL
);
ALTER TABLE Activity ADD FOREIGN KEY (actor_id) REFERENCES HPerson (id);
CREATE UNIQUE INDEX UKactivity ON Activity (actor_id, approxTime, activityType, contextType, context_id);
ALTER TABLE HAccount ADD FOREIGN KEY (mergedInto) REFERENCES HAccount (id);
CREATE UNIQUE INDEX username ON HAccount (username);
CREATE INDEX FK_HAccount_MergedIntoAccount ON HAccount (mergedInto);
ALTER TABLE HAccountActivationKey ADD FOREIGN KEY (accountId) REFERENCES HAccount (id);
CREATE INDEX FK86E79CA4FA68C45F ON HAccountActivationKey (accountId);
ALTER TABLE HAccountMembership ADD FOREIGN KEY (memberOf) REFERENCES HAccountRole (id);
ALTER TABLE HAccountMembership ADD FOREIGN KEY (accountId) REFERENCES HAccount (id);
CREATE INDEX FK9D5DB27B3E684F5E ON HAccountMembership (memberOf);
CREATE INDEX FK9D5DB27BFA68C45F ON HAccountMembership (accountId);
ALTER TABLE HAccountOption ADD FOREIGN KEY (account_id) REFERENCES HAccount (id);
CREATE INDEX FK_HAccountOption_HAccount ON HAccountOption (account_id);
ALTER TABLE HAccountResetPasswordKey ADD FOREIGN KEY (accountId) REFERENCES HAccount (id);
CREATE INDEX FK85C9EFDAFA68C45F ON HAccountResetPasswordKey (accountId);
ALTER TABLE HAccountRoleGroup ADD FOREIGN KEY (roleId) REFERENCES HAccountRole (id);
ALTER TABLE HAccountRoleGroup ADD FOREIGN KEY (memberOf) REFERENCES HAccountRole (id);
CREATE INDEX FK3321CC642DF53D7E ON HAccountRoleGroup (roleId);
CREATE INDEX FK3321CC643E684F5E ON HAccountRoleGroup (memberOf);
CREATE UNIQUE INDEX config_key ON HApplicationConfiguration (config_key);
ALTER TABLE HCredentials ADD FOREIGN KEY (account_id) REFERENCES HAccount (id);
CREATE INDEX FK_credentials_account ON HCredentials (account_id);
ALTER TABLE HDocument ADD FOREIGN KEY (poHeader_id) REFERENCES HPoHeader (id);
ALTER TABLE HDocument ADD FOREIGN KEY (project_iteration_id) REFERENCES HProjectIteration (id);
ALTER TABLE HDocument ADD FOREIGN KEY (last_modified_by_id) REFERENCES HPerson (id);
ALTER TABLE HDocument ADD FOREIGN KEY (locale) REFERENCES HLocale (id);
CREATE UNIQUE INDEX docId ON HDocument (docId, project_iteration_id);
CREATE INDEX FKEA766D83136CC025 ON HDocument (poHeader_id);
CREATE INDEX FKEA766D8351ED6DFD ON HDocument (project_iteration_id);
CREATE INDEX FKEA766D836C9BADC1 ON HDocument (last_modified_by_id);
CREATE INDEX FKEA766D83FEA3B54A ON HDocument (locale);
ALTER TABLE HDocumentHistory ADD FOREIGN KEY (document_id) REFERENCES HDocument (id);
ALTER TABLE HDocumentHistory ADD FOREIGN KEY (last_modified_by_id) REFERENCES HPerson (id);
ALTER TABLE HDocumentHistory ADD FOREIGN KEY (locale) REFERENCES HLocale (id);
CREATE UNIQUE INDEX HDoc_Hist_document_id ON HDocumentHistory (revision);
CREATE INDEX FK279765915383E2F0 ON HDocumentHistory (document_id);
CREATE INDEX FK279765916C9BADC1 ON HDocumentHistory (last_modified_by_id);
CREATE INDEX FK27976591FEA3B54A ON HDocumentHistory (locale);
ALTER TABLE HDocumentUpload ADD FOREIGN KEY (localeId) REFERENCES HLocale (id);
ALTER TABLE HDocumentUpload ADD FOREIGN KEY (projectIterationId) REFERENCES HProjectIteration (id);
CREATE INDEX FK_HDocumentUpload_Locale ON HDocumentUpload (localeId);
CREATE INDEX FK_HDocumentUpload_ProjectIteration ON HDocumentUpload (projectIterationId);
ALTER TABLE HDocumentUploadPart ADD FOREIGN KEY (documentUploadId) REFERENCES HDocumentUpload (id);
CREATE INDEX FK_HDocumentUploadPart_DocumentUpload ON HDocumentUploadPart (documentUploadId);
ALTER TABLE HDocument_RawDocument ADD FOREIGN KEY (documentId) REFERENCES HDocument (id);
ALTER TABLE HDocument_RawDocument ADD FOREIGN KEY (rawDocumentId) REFERENCES HRawDocument (id);
CREATE INDEX FK_HDocumentRawDocument_RawDocument ON HDocument_RawDocument (rawDocumentId);
ALTER TABLE HGlossaryEntry ADD FOREIGN KEY (srcLocaleId) REFERENCES HLocale (id);
CREATE INDEX UKglossaryentry_srcLocaleId ON HGlossaryEntry (srcLocaleId);
ALTER TABLE HGlossaryTerm ADD FOREIGN KEY (glossaryEntryId) REFERENCES HGlossaryEntry (id);
ALTER TABLE HGlossaryTerm ADD FOREIGN KEY (localeId) REFERENCES HLocale (id);
CREATE UNIQUE INDEX UKglossaryEntryId_LocaleId ON HGlossaryTerm (glossaryEntryId, localeId);
CREATE INDEX UKglossaryterm_localeId ON HGlossaryTerm (localeId);
CREATE UNIQUE INDEX iteration_group_slug ON HIterationGroup (slug);
CREATE UNIQUE INDEX UKslug ON HIterationGroup (slug);
ALTER TABLE HIterationGroup_Maintainer ADD FOREIGN KEY (iterationGroupId) REFERENCES HIterationGroup (id);
ALTER TABLE HIterationGroup_Maintainer ADD FOREIGN KEY (personId) REFERENCES HPerson (id);
CREATE INDEX FKiterationGroupMaintainer_personId ON HIterationGroup_Maintainer (personId);
ALTER TABLE HIterationGroup_ProjectIteration ADD FOREIGN KEY (iterationGroupId) REFERENCES HIterationGroup (id);
ALTER TABLE HIterationGroup_ProjectIteration ADD FOREIGN KEY (projectIterationId) REFERENCES HProjectIteration (id);
CREATE INDEX FKiterationGroup_ProjectIteration_projectIterationId ON HIterationGroup_ProjectIteration (projectIterationId);
CREATE UNIQUE INDEX localeId ON HLocale (localeId);
ALTER TABLE HLocale_Member ADD FOREIGN KEY (supportedLanguageId) REFERENCES HLocale (id);
ALTER TABLE HLocale_Member ADD FOREIGN KEY (personId) REFERENCES HPerson (id);
CREATE INDEX FK82DF50D73A932491 ON HLocale_Member (supportedLanguageId);
CREATE INDEX FK82DF50D760C55B1B ON HLocale_Member (personId);
ALTER TABLE HPerson ADD FOREIGN KEY (accountId) REFERENCES HAccount (id);
CREATE UNIQUE INDEX email ON HPerson (email);
CREATE INDEX FK6F0931BDFA68C45F ON HPerson (accountId);
ALTER TABLE HPersonEmailValidationKey ADD FOREIGN KEY (personId) REFERENCES HPerson (id);
CREATE UNIQUE INDEX keyHash ON HPersonEmailValidationKey (keyHash);
ALTER TABLE HPoHeader ADD FOREIGN KEY (comment_id) REFERENCES HSimpleComment (id);
CREATE INDEX FK9A0ABDD4B7A40DF2 ON HPoHeader (comment_id);
ALTER TABLE HPoTargetHeader ADD FOREIGN KEY (document_id) REFERENCES HDocument (id);
ALTER TABLE HPoTargetHeader ADD FOREIGN KEY (targetLanguage) REFERENCES HLocale (id);
ALTER TABLE HPoTargetHeader ADD FOREIGN KEY (comment_id) REFERENCES HSimpleComment (id);
CREATE UNIQUE INDEX document_id ON HPoTargetHeader (targetLanguage);
CREATE INDEX FK1BC719855383E2F0 ON HPoTargetHeader (document_id);
CREATE INDEX FK1BC719857D208AD9 ON HPoTargetHeader (targetLanguage);
CREATE INDEX FK1BC71985B7A40DF2 ON HPoTargetHeader (comment_id);
ALTER TABLE HPotEntryData ADD FOREIGN KEY (comment_id) REFERENCES HSimpleComment (id);
CREATE INDEX FK17A648CFB7A40DF2 ON HPotEntryData (comment_id);
ALTER TABLE HProject ADD FOREIGN KEY (default_copy_trans_opts_id) REFERENCES HCopyTransOptions (id);
CREATE UNIQUE INDEX project_slug ON HProject (slug);
CREATE INDEX FK_HProject_HCopyTransOpts ON HProject (default_copy_trans_opts_id);
ALTER TABLE HProjectIteration ADD FOREIGN KEY (project_id) REFERENCES HProject (id);
ALTER TABLE HProjectIteration ADD FOREIGN KEY (parentId) REFERENCES HProjectIteration (id);
CREATE UNIQUE INDEX iteration_slug ON HProjectIteration (slug, project_id);
CREATE INDEX FK31C1E42C4BCEEA93 ON HProjectIteration (project_id);
CREATE INDEX FK31C1E42C5B1D181F ON HProjectIteration (parentId);
ALTER TABLE HProjectIteration_Locale ADD FOREIGN KEY (localeId) REFERENCES HLocale (id);
ALTER TABLE HProjectIteration_Locale ADD FOREIGN KEY (projectIterationId) REFERENCES HProjectIteration (id);
CREATE INDEX FKHPROJECTITELOCLOC ON HProjectIteration_Locale (localeId);
ALTER TABLE HProjectIteration_LocaleAlias ADD FOREIGN KEY (projectIterationId) REFERENCES HProjectIteration (id);
ALTER TABLE HProjectIteration_Validation ADD FOREIGN KEY (projectIterationId) REFERENCES HProjectIteration (id);
ALTER TABLE HProject_AllowedRole ADD FOREIGN KEY (projectId) REFERENCES HProject (id);
ALTER TABLE HProject_AllowedRole ADD FOREIGN KEY (roleId) REFERENCES HAccountRole (id);
CREATE INDEX FK_HProjectAllowedRole_Role ON HProject_AllowedRole (roleId);
ALTER TABLE HProject_Locale ADD FOREIGN KEY (localeId) REFERENCES HLocale (id);
ALTER TABLE HProject_Locale ADD FOREIGN KEY (projectId) REFERENCES HProject (id);
CREATE INDEX FKHPROJECTLOCALELOC ON HProject_Locale (localeId);
ALTER TABLE HProject_LocaleAlias ADD FOREIGN KEY (projectId) REFERENCES HProject (id);
ALTER TABLE HProject_Maintainer ADD FOREIGN KEY (personId) REFERENCES HPerson (id);
ALTER TABLE HProject_Maintainer ADD FOREIGN KEY (projectId) REFERENCES HProject (id);
CREATE INDEX FK1491F2E660C55B1B ON HProject_Maintainer (personId);
CREATE INDEX FK1491F2E665B5BB37 ON HProject_Maintainer (projectId);
ALTER TABLE HProject_Validation ADD FOREIGN KEY (projectId) REFERENCES HProject (id);
ALTER TABLE HRoleAssignmentRule ADD FOREIGN KEY (role_to_assign_id) REFERENCES HAccountRole (id);
CREATE INDEX FK_HRoleAssignmentRule_HAccountRole ON HRoleAssignmentRule (role_to_assign_id);
ALTER TABLE HTermComment ADD FOREIGN KEY (glossaryTermId) REFERENCES HGlossaryTerm (id);
CREATE INDEX UKtermComment_glossaryTerm ON HTermComment (glossaryTermId);
ALTER TABLE HTextFlow ADD FOREIGN KEY (document_id) REFERENCES HDocument (id);
ALTER TABLE HTextFlow ADD FOREIGN KEY (potEntryData_id) REFERENCES HPotEntryData (id);
ALTER TABLE HTextFlow ADD FOREIGN KEY (comment_id) REFERENCES HSimpleComment (id);
CREATE UNIQUE INDEX UKresIddocument_id ON HTextFlow (resId, document_id);
CREATE INDEX FK7B40F8635383E2F0 ON HTextFlow (document_id);
CREATE INDEX FK7B40F8638D8E70A5 ON HTextFlow (potEntryData_id);
CREATE INDEX FK7B40F863B7A40DF2 ON HTextFlow (comment_id);
CREATE INDEX Idx_ContentHash ON HTextFlow (contentHash);
ALTER TABLE HTextFlowContentHistory ADD FOREIGN KEY (text_flow_history_id) REFERENCES HTextFlowHistory (id);
CREATE INDEX FKcontent_text_flow_history ON HTextFlowContentHistory (text_flow_history_id);
ALTER TABLE HTextFlowHistory ADD FOREIGN KEY (tf_id) REFERENCES HTextFlow (id);
CREATE UNIQUE INDEX revision ON HTextFlowHistory (revision, tf_id);
CREATE INDEX FK46C4DEB1CCAD9D19 ON HTextFlowHistory (tf_id);
ALTER TABLE HTextFlowTarget ADD FOREIGN KEY (last_modified_by_id) REFERENCES HPerson (id);
ALTER TABLE HTextFlowTarget ADD FOREIGN KEY (comment_id) REFERENCES HSimpleComment (id);
ALTER TABLE HTextFlowTarget ADD FOREIGN KEY (tf_id) REFERENCES HTextFlow (id);
ALTER TABLE HTextFlowTarget ADD FOREIGN KEY (locale) REFERENCES HLocale (id);
CREATE UNIQUE INDEX locale ON HTextFlowTarget (tf_id);
CREATE INDEX FK1E933FD46C9BADC1 ON HTextFlowTarget (last_modified_by_id);
CREATE INDEX FK1E933FD4B7A40DF2 ON HTextFlowTarget (comment_id);
CREATE INDEX FK1E933FD4CCAD9D19 ON HTextFlowTarget (tf_id);
CREATE INDEX FK1E933FD4FEA3B54A ON HTextFlowTarget (locale);
CREATE INDEX Idx_TFT_LastChanged ON HTextFlowTarget (lastChanged);
CREATE INDEX Idx_TFT_lastModifiedBy ON HTextFlowTarget (last_modified_by_id);
CREATE INDEX Idx_TFT_ReviewedBy ON HTextFlowTarget (reviewed_by_id);
CREATE INDEX Idx_TFT_TranslatedBy ON HTextFlowTarget (translated_by_id);
ALTER TABLE HTextFlowTargetContentHistory ADD FOREIGN KEY (text_flow_target_history_id) REFERENCES HTextFlowTargetHistory (id);
CREATE INDEX FKcontent_text_flow_target_history ON HTextFlowTargetContentHistory (text_flow_target_history_id);
ALTER TABLE HTextFlowTargetHistory ADD FOREIGN KEY (last_modified_by_id) REFERENCES HPerson (id);
ALTER TABLE HTextFlowTargetHistory ADD FOREIGN KEY (target_id) REFERENCES HTextFlowTarget (id);
CREATE UNIQUE INDEX target_id ON HTextFlowTargetHistory (versionNum);
CREATE INDEX FKF10986206C9BADC1 ON HTextFlowTargetHistory (last_modified_by_id);
CREATE INDEX FKF109862080727E8B ON HTextFlowTargetHistory (target_id);
CREATE INDEX Idx_TFTH_lastChanged ON HTextFlowTargetHistory (lastChanged);
CREATE INDEX Idx_TFTH_lastModifiedBy ON HTextFlowTargetHistory (last_modified_by_id);
CREATE INDEX Idx_TFTH_ReviewedBy ON HTextFlowTargetHistory (reviewed_by_id);
CREATE INDEX Idx_TFTH_TranslatedBy ON HTextFlowTargetHistory (reviewed_by_id);
ALTER TABLE HTextFlowTargetReviewComment ADD FOREIGN KEY (target_id) REFERENCES HTextFlowTarget (id);
ALTER TABLE HTextFlowTargetReviewComment ADD FOREIGN KEY (commenter_id) REFERENCES HPerson (id);
CREATE INDEX FKtarget_review_comment ON HTextFlowTargetReviewComment (target_id);
CREATE INDEX FKtarget_review_commenter ON HTextFlowTargetReviewComment (commenter_id);
ALTER TABLE IterationGroup_Locale ADD FOREIGN KEY (iteration_group_id) REFERENCES HIterationGroup (id);
ALTER TABLE IterationGroup_Locale ADD FOREIGN KEY (locale_id) REFERENCES HLocale (id);
CREATE INDEX FK_IterationGroup_Locale_HLocale ON IterationGroup_Locale (locale_id);
CREATE UNIQUE INDEX trans_memory_slug ON TransMemory (slug);
ALTER TABLE TransMemoryUnit ADD FOREIGN KEY (tm_id) REFERENCES TransMemory (id) ON DELETE CASCADE;
CREATE UNIQUE INDEX UK_natural_id ON TransMemoryUnit (tm_id, unique_id);
ALTER TABLE TransMemoryUnitVariant ADD FOREIGN KEY (trans_unit_id) REFERENCES TransMemoryUnit (id) ON DELETE CASCADE;
CREATE INDEX FK_TransUnitVariant_TransUnit ON TransMemoryUnitVariant (trans_unit_id);
ALTER TABLE TransMemory_Metadata ADD FOREIGN KEY (trans_memory_id) REFERENCES TransMemory (id);
ALTER TABLE WebHook ADD FOREIGN KEY (projectId) REFERENCES HProject (id);
CREATE INDEX FK_WebHook_HProject ON WebHook (projectId);
