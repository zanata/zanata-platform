CREATE TABLE Activity ( 
  id BIGSERIAL  NOT NULL 
 ,  creationDate TIMESTAMP  NOT NULL 
 ,  lastChanged TIMESTAMP  NOT NULL 
 ,  versionNum INT  NOT NULL 
 ,  actor_id BIGINT  NOT NULL 
 ,  approxTime TIMESTAMP  NOT NULL 
 ,  startOffsetMillis INT  NOT NULL 
 ,  endOffsetMillis INT  NOT NULL 
 ,  contextType VARCHAR(150)  NOT NULL 
 ,  context_id BIGINT  NOT NULL 
 ,  lastTargetType VARCHAR(150)  NOT NULL 
 ,  last_target_id BIGINT  NOT NULL 
 ,  activityType VARCHAR(150)  NOT NULL 
 ,  eventCount INT  NOT NULL 
 ,  wordCount INT  NOT NULL 
 , PRIMARY KEY(  id)
);
CREATE TABLE IF NOT EXISTS DATABASECHANGELOG (
  ID VARCHAR(63)  NOT NULL 
 ,  AUTHOR VARCHAR(63)  NOT NULL 
 ,  FILENAME VARCHAR(200)  NOT NULL 
 ,  DATEEXECUTED TIMESTAMP  NOT NULL 
 ,  ORDEREXECUTED INT  NOT NULL 
 ,  EXECTYPE VARCHAR(10)  NOT NULL 
 ,  MD5SUM VARCHAR(35) 
 ,  DESCRIPTION VARCHAR(255) 
 ,  COMMENTS VARCHAR(255) 
 ,  TAG VARCHAR(255) 
 ,  LIQUIBASE VARCHAR(20) 
 , PRIMARY KEY(  AUTHOR, FILENAME, ID)
);
CREATE TABLE IF NOT EXISTS DATABASECHANGELOGLOCK (
  ID INT  NOT NULL 
 ,  LOCKED boolean  NOT NULL 
 ,  LOCKGRANTED TIMESTAMP 
 ,  LOCKEDBY VARCHAR(255) 
 , PRIMARY KEY(  ID)
);
CREATE TABLE HAccount ( 
  id BIGSERIAL  NOT NULL 
 ,  creationDate TIMESTAMP  NOT NULL 
 ,  lastChanged TIMESTAMP  NOT NULL 
 ,  versionNum INT  NOT NULL 
 ,  apiKey VARCHAR(32) 
 ,  enabled boolean  NOT NULL 
 ,  passwordHash VARCHAR(255) 
 ,  username VARCHAR(255) 
 ,  mergedInto BIGINT 
 , PRIMARY KEY(  id)
);
CREATE TABLE HAccountActivationKey ( 
  keyHash VARCHAR(32)  NOT NULL 
 ,  accountId BIGINT  NOT NULL 
 ,  creationDate TIMESTAMP  NOT NULL 
 , PRIMARY KEY(  keyHash)
);
CREATE TABLE HAccountMembership ( 
  accountId BIGINT  NOT NULL 
 ,  memberOf INT  NOT NULL 
 , PRIMARY KEY(  accountId, memberOf)
);
CREATE TABLE HAccountOption ( 
  name VARCHAR(255)  NOT NULL 
 ,  value TEXT 
 ,  account_id BIGINT  NOT NULL 
 ,  id BIGSERIAL  NOT NULL 
 ,  creationDate TIMESTAMP  NOT NULL 
 ,  lastChanged TIMESTAMP  NOT NULL 
 ,  versionNum INT  NOT NULL 
 , PRIMARY KEY(  id)
);
CREATE TABLE HAccountResetPasswordKey ( 
  keyHash VARCHAR(32)  NOT NULL 
 ,  accountId BIGINT  NOT NULL 
 , PRIMARY KEY(  keyHash)
);
CREATE TABLE HAccountRole ( 
  id BIGSERIAL  NOT NULL 
 ,  conditional boolean  NOT NULL 
 ,  name VARCHAR(255) 
 ,  roleType CHAR(1)  NOT NULL 
 , PRIMARY KEY(  id)
);
CREATE TABLE HAccountRoleGroup ( 
  roleId INT  NOT NULL 
 ,  memberOf INT  NOT NULL 
 , PRIMARY KEY(  memberOf, roleId)
);
CREATE TABLE HApplicationConfiguration ( 
  id BIGSERIAL  NOT NULL 
 ,  creationDate TIMESTAMP  NOT NULL 
 ,  lastChanged TIMESTAMP  NOT NULL 
 ,  versionNum INT  NOT NULL 
 ,  config_key VARCHAR(255)  NOT NULL 
 ,  config_value TEXT  NOT NULL 
 , PRIMARY KEY(  id)
);
CREATE TABLE HCopyTransOptions ( 
  contextMismatchAction CHAR(1)  NOT NULL 
 ,  docIdMismatchAction CHAR(1)  NOT NULL 
 ,  projectMismatchAction CHAR(1)  NOT NULL 
 ,  id BIGSERIAL  NOT NULL 
 ,  creationDate TIMESTAMP  NOT NULL 
 ,  lastChanged TIMESTAMP  NOT NULL 
 ,  versionNum INT  NOT NULL 
 , PRIMARY KEY(  id)
);
CREATE TABLE HCredentials ( 
  account_id BIGINT  NOT NULL 
 ,  type VARCHAR(10)  NOT NULL 
 ,  "user" TEXT 
 ,  email VARCHAR(100) 
 ,  id BIGSERIAL  NOT NULL 
 ,  creationDate TIMESTAMP  NOT NULL 
 ,  lastChanged TIMESTAMP  NOT NULL 
 ,  versionNum INT  NOT NULL 
 , PRIMARY KEY(  id)
);
CREATE TABLE HDocument ( 
  id BIGSERIAL  NOT NULL 
 ,  creationDate TIMESTAMP  NOT NULL 
 ,  lastChanged TIMESTAMP  NOT NULL 
 ,  versionNum INT  NOT NULL 
 ,  contentType VARCHAR(255)  NOT NULL 
 ,  docId VARCHAR(255)  NOT NULL 
 ,  name VARCHAR(255)  NOT NULL 
 ,  obsolete boolean  NOT NULL 
 ,  path VARCHAR(255)  NOT NULL 
 ,  revision INT  NOT NULL 
 ,  last_modified_by_id BIGINT 
 ,  locale BIGINT  NOT NULL 
 ,  poHeader_id BIGINT 
 ,  project_iteration_id BIGINT  NOT NULL 
 , PRIMARY KEY(  id)
);
CREATE TABLE HDocumentHistory ( 
  id BIGSERIAL  NOT NULL 
 ,  contentType VARCHAR(255)  NOT NULL 
 ,  docId VARCHAR(255)  NOT NULL 
 ,  lastChanged TIMESTAMP 
 ,  name VARCHAR(255) 
 ,  obsolete boolean  NOT NULL 
 ,  path VARCHAR(255) 
 ,  revision INT 
 ,  document_id BIGINT 
 ,  last_modified_by_id BIGINT 
 ,  locale BIGINT  NOT NULL 
 , PRIMARY KEY(  id)
);
CREATE TABLE HDocumentUpload ( 
  projectIterationId BIGINT  NOT NULL 
 ,  docId VARCHAR(255)  NOT NULL 
 ,  contentHash CHAR(32)  NOT NULL 
 ,  type VARCHAR(255)  NOT NULL 
 ,  localeId BIGINT 
 ,  id BIGSERIAL  NOT NULL 
 ,  creationDate TIMESTAMP  NOT NULL 
 ,  lastChanged TIMESTAMP  NOT NULL 
 ,  versionNum INT  NOT NULL 
 , PRIMARY KEY(  id)
);
CREATE TABLE HDocumentUploadPart ( 
  id BIGSERIAL  NOT NULL 
 ,  documentUploadId BIGINT  NOT NULL 
 ,  partIndex INT  NOT NULL 
 ,  content BYTEA  NOT NULL 
 , PRIMARY KEY(  id)
);
CREATE TABLE HDocument_RawDocument ( 
  documentId BIGINT  NOT NULL 
 ,  rawDocumentId BIGINT  NOT NULL 
 , PRIMARY KEY(  documentId, rawDocumentId)
);
CREATE TABLE HGlossaryEntry ( 
  id BIGSERIAL  NOT NULL 
 ,  creationDate TIMESTAMP  NOT NULL 
 ,  lastChanged TIMESTAMP  NOT NULL 
 ,  versionNum INT  NOT NULL 
 ,  srcLocaleId BIGINT  NOT NULL 
 ,  sourceRef TEXT 
 , PRIMARY KEY(  id)
);
CREATE TABLE HGlossaryTerm ( 
  id BIGSERIAL  NOT NULL 
 ,  glossaryEntryId BIGINT  NOT NULL 
 ,  localeId BIGINT  NOT NULL 
 ,  content TEXT 
 ,  creationDate TIMESTAMP  NOT NULL 
 ,  lastChanged TIMESTAMP  NOT NULL 
 ,  versionNum INT  NOT NULL 
 , PRIMARY KEY(  id)
);
CREATE TABLE HIterationGroup ( 
  id BIGSERIAL  NOT NULL 
 ,  name VARCHAR(80)  NOT NULL 
 ,  slug VARCHAR(40)  NOT NULL 
 ,  description VARCHAR(100) 
 ,  status CHAR(1)  NOT NULL 
 ,  creationDate TIMESTAMP  NOT NULL 
 ,  lastChanged TIMESTAMP  NOT NULL 
 ,  versionNum INT  NOT NULL 
 , PRIMARY KEY(  id)
);
CREATE TABLE HIterationGroup_Maintainer ( 
  iterationGroupId BIGINT  NOT NULL 
 ,  personId BIGINT  NOT NULL 
 , PRIMARY KEY(  iterationGroupId, personId)
);
CREATE TABLE HIterationGroup_ProjectIteration ( 
  iterationGroupId BIGINT  NOT NULL 
 ,  projectIterationId BIGINT  NOT NULL 
 , PRIMARY KEY(  iterationGroupId, projectIterationId)
);
CREATE TABLE HLocale ( 
  id BIGSERIAL  NOT NULL 
 ,  creationDate TIMESTAMP  NOT NULL 
 ,  lastChanged TIMESTAMP  NOT NULL 
 ,  versionNum INT  NOT NULL 
 ,  active boolean  NOT NULL 
 ,  localeId VARCHAR(255)  NOT NULL 
 ,  enabledByDefault boolean 
 ,  pluralForms VARCHAR(100) 
 ,  displayName VARCHAR(100) 
 ,  nativeName VARCHAR(100) 
 , PRIMARY KEY(  id)
);
CREATE TABLE HLocale_Member ( 
  personId BIGINT  NOT NULL 
 ,  supportedLanguageId BIGINT  NOT NULL 
 ,  isCoordinator boolean  NOT NULL 
 ,  isReviewer boolean  NOT NULL 
 ,  isTranslator boolean  NOT NULL 
 , PRIMARY KEY(  personId, supportedLanguageId)
);
CREATE TABLE HPerson ( 
  id BIGSERIAL  NOT NULL 
 ,  creationDate TIMESTAMP  NOT NULL 
 ,  lastChanged TIMESTAMP  NOT NULL 
 ,  versionNum INT  NOT NULL 
 ,  email VARCHAR(255)  NOT NULL 
 ,  name VARCHAR(80)  NOT NULL 
 ,  accountId BIGINT 
 , PRIMARY KEY(  id)
);
CREATE TABLE HPersonEmailValidationKey ( 
  id BIGSERIAL  NOT NULL 
 ,  keyHash VARCHAR(32)  NOT NULL 
 ,  personId BIGINT  NOT NULL 
 ,  email VARCHAR(255)  NOT NULL 
 ,  creationDate TIMESTAMP  NOT NULL 
 , PRIMARY KEY(  id)
);
CREATE TABLE HPoHeader ( 
  id BIGSERIAL  NOT NULL 
 ,  creationDate TIMESTAMP  NOT NULL 
 ,  lastChanged TIMESTAMP  NOT NULL 
 ,  versionNum INT  NOT NULL 
 ,  entries TEXT 
 ,  comment_id BIGINT 
 , PRIMARY KEY(  id)
);
CREATE TABLE HPoTargetHeader ( 
  id BIGSERIAL  NOT NULL 
 ,  creationDate TIMESTAMP  NOT NULL 
 ,  lastChanged TIMESTAMP  NOT NULL 
 ,  versionNum INT  NOT NULL 
 ,  entries TEXT 
 ,  comment_id BIGINT 
 ,  document_id BIGINT 
 ,  targetLanguage BIGINT  NOT NULL 
 , PRIMARY KEY(  id)
);
CREATE TABLE HPotEntryData ( 
  id BIGSERIAL  NOT NULL 
 ,  context VARCHAR(255) 
 ,  flags VARCHAR(255) 
 ,  refs TEXT 
 ,  comment_id BIGINT 
 , PRIMARY KEY(  id)
);
CREATE TABLE HProject ( 
  id BIGSERIAL  NOT NULL 
 ,  creationDate TIMESTAMP  NOT NULL 
 ,  lastChanged TIMESTAMP  NOT NULL 
 ,  versionNum INT  NOT NULL 
 ,  slug VARCHAR(40)  NOT NULL 
 ,  description VARCHAR(100) 
 ,  homeContent TEXT 
 ,  name VARCHAR(80)  NOT NULL 
 ,  overrideLocales boolean  NOT NULL 
 ,  status CHAR(1) 
 ,  restrictedByRoles boolean 
 ,  default_copy_trans_opts_id BIGINT 
 ,  defaultProjectType VARCHAR(255) 
 ,  sourceViewURL TEXT 
 ,  sourceCheckoutURL TEXT 
 , PRIMARY KEY(  id)
);
CREATE TABLE HProjectIteration ( 
  id BIGSERIAL  NOT NULL 
 ,  creationDate TIMESTAMP  NOT NULL 
 ,  lastChanged TIMESTAMP  NOT NULL 
 ,  versionNum INT  NOT NULL 
 ,  slug VARCHAR(40)  NOT NULL 
 ,  parentId BIGINT 
 ,  project_id BIGINT  NOT NULL 
 ,  overrideLocales boolean  NOT NULL 
 ,  status CHAR(1) 
 ,  projectType VARCHAR(255) 
 ,  requireTranslationReview boolean 
 , PRIMARY KEY(  id)
);
CREATE TABLE HProjectIteration_Locale ( 
  projectIterationId BIGINT  NOT NULL 
 ,  localeId BIGINT  NOT NULL 
 , PRIMARY KEY(  localeId, projectIterationId)
);
CREATE TABLE HProjectIteration_LocaleAlias ( 
  projectIterationId BIGINT  NOT NULL 
 ,  localeId VARCHAR(255)  NOT NULL 
 ,  alias VARCHAR(255)  NOT NULL 
 , PRIMARY KEY(  localeId, projectIterationId)
);
CREATE TABLE HProjectIteration_Validation ( 
  projectIterationId BIGINT  NOT NULL 
 ,  validation VARCHAR(100)  NOT NULL 
 ,  state VARCHAR(100)  NOT NULL 
 , PRIMARY KEY(  projectIterationId, validation)
);
CREATE TABLE HProject_AllowedRole ( 
  projectId BIGINT  NOT NULL 
 ,  roleId INT  NOT NULL 
 , PRIMARY KEY(  projectId, roleId)
);
CREATE TABLE HProject_Locale ( 
  projectId BIGINT  NOT NULL 
 ,  localeId BIGINT  NOT NULL 
 , PRIMARY KEY(  localeId, projectId)
);
CREATE TABLE HProject_LocaleAlias ( 
  projectId BIGINT  NOT NULL 
 ,  localeId VARCHAR(255)  NOT NULL 
 ,  alias VARCHAR(255)  NOT NULL 
 , PRIMARY KEY(  localeId, projectId)
);
CREATE TABLE HProject_Maintainer ( 
  personId BIGINT  NOT NULL 
 ,  projectId BIGINT  NOT NULL 
 , PRIMARY KEY(  personId, projectId)
);
CREATE TABLE HProject_Validation ( 
  projectId BIGINT  NOT NULL 
 ,  validation VARCHAR(100)  NOT NULL 
 ,  state VARCHAR(100)  NOT NULL 
 , PRIMARY KEY(  projectId, validation)
);
CREATE TABLE HRawDocument ( 
  type VARCHAR(255)  NOT NULL 
 ,  contentHash CHAR(32)  NOT NULL 
 ,  uploadedBy VARCHAR(255) 
 ,  id BIGSERIAL  NOT NULL 
 ,  creationDate TIMESTAMP  NOT NULL 
 ,  lastChanged TIMESTAMP  NOT NULL 
 ,  versionNum INT  NOT NULL 
 ,  adapterParameters TEXT 
 ,  fileId TEXT  NOT NULL 
 , PRIMARY KEY(  id)
);
CREATE TABLE HRoleAssignmentRule ( 
  policyName VARCHAR(100) 
 ,  identityRegExp TEXT 
 ,  role_to_assign_id INT  NOT NULL 
 ,  id BIGSERIAL  NOT NULL 
 ,  creationDate TIMESTAMP  NOT NULL 
 ,  lastChanged TIMESTAMP  NOT NULL 
 ,  versionNum INT  NOT NULL 
 , PRIMARY KEY(  id)
);
CREATE TABLE HSI_LuceneIndexesData ( 
  id VARCHAR(255)  NOT NULL 
 ,  datum BYTEA 
 ,  version BIGINT 
 , PRIMARY KEY(  id)
);
CREATE TABLE HSI_LuceneIndexesMetadata ( 
  id VARCHAR(255)  NOT NULL 
 ,  datum BYTEA 
 ,  version BIGINT 
 , PRIMARY KEY(  id)
);
CREATE TABLE HSimpleComment ( 
  id BIGSERIAL  NOT NULL 
 ,  comment TEXT  NOT NULL 
 ,  lastChanged TIMESTAMP  NOT NULL 
 , PRIMARY KEY(  id)
);
CREATE TABLE HTermComment ( 
  id BIGSERIAL  NOT NULL 
 ,  comment TEXT  NOT NULL 
 ,  pos INT  NOT NULL 
 ,  glossaryTermId BIGINT  NOT NULL 
 , PRIMARY KEY(  id)
);
CREATE TABLE HTextFlow ( 
  id BIGSERIAL  NOT NULL 
 ,  obsolete boolean  NOT NULL 
 ,  pos INT  NOT NULL 
 ,  resId VARCHAR(255)  NOT NULL 
 ,  revision INT  NOT NULL 
 ,  comment_id BIGINT 
 ,  document_id BIGINT  NOT NULL 
 ,  potEntryData_id BIGINT 
 ,  wordCount BIGINT  NOT NULL 
 ,  contentHash CHAR(32)  NOT NULL 
 ,  plural boolean 
 ,  content0 TEXT 
 ,  content1 TEXT 
 ,  content2 TEXT 
 ,  content3 TEXT 
 ,  content4 TEXT 
 ,  content5 TEXT 
 , PRIMARY KEY(  id)
);
CREATE TABLE HTextFlowContentHistory ( 
  id BIGSERIAL  NOT NULL 
 ,  content TEXT  NOT NULL 
 ,  pos INT  NOT NULL 
 ,  text_flow_history_id BIGINT  NOT NULL 
 , PRIMARY KEY(  id)
);
CREATE TABLE HTextFlowHistory ( 
  id BIGSERIAL  NOT NULL 
 ,  obsolete boolean  NOT NULL 
 ,  pos INT 
 ,  revision INT 
 ,  tf_id BIGINT 
 , PRIMARY KEY(  id)
);
CREATE TABLE HTextFlowTarget ( 
  id BIGSERIAL  NOT NULL 
 ,  creationDate TIMESTAMP  NOT NULL 
 ,  lastChanged TIMESTAMP  NOT NULL 
 ,  versionNum INT  NOT NULL 
 ,  state INT  NOT NULL 
 ,  tf_revision INT  NOT NULL 
 ,  comment_id BIGINT 
 ,  last_modified_by_id BIGINT 
 ,  locale BIGINT  NOT NULL 
 ,  tf_id BIGINT 
 ,  content0 TEXT 
 ,  content1 TEXT 
 ,  content2 TEXT 
 ,  content3 TEXT 
 ,  content4 TEXT 
 ,  content5 TEXT 
 ,  translated_by_id BIGINT 
 ,  reviewed_by_id BIGINT 
 ,  revisionComment TEXT 
 ,  copiedEntityType CHAR(3) 
 ,  copiedEntityId TEXT 
 ,  sourceType CHAR(3) 
 ,  automatedEntry boolean 
 , PRIMARY KEY(  id)
);
CREATE TABLE HTextFlowTargetContentHistory ( 
  id BIGSERIAL  NOT NULL 
 ,  content TEXT  NOT NULL 
 ,  pos INT  NOT NULL 
 ,  text_flow_target_history_id BIGINT  NOT NULL 
 , PRIMARY KEY(  id)
);
CREATE TABLE HTextFlowTargetHistory ( 
  id BIGSERIAL  NOT NULL 
 ,  lastChanged TIMESTAMP 
 ,  state INT 
 ,  tf_revision INT 
 ,  versionNum INT 
 ,  last_modified_by_id BIGINT 
 ,  target_id BIGINT 
 ,  translated_by_id BIGINT 
 ,  reviewed_by_id BIGINT 
 ,  revisionComment TEXT 
 ,  copiedEntityType CHAR(3) 
 ,  copiedEntityId TEXT 
 ,  sourceType CHAR(3) 
 ,  automatedEntry boolean 
 , PRIMARY KEY(  id)
);
CREATE TABLE HTextFlowTargetReviewComment ( 
  id BIGSERIAL  NOT NULL 
 ,  creationDate TIMESTAMP  NOT NULL 
 ,  lastChanged TIMESTAMP  NOT NULL 
 ,  commenter_id BIGINT  NOT NULL 
 ,  target_id BIGINT  NOT NULL 
 ,  comment TEXT  NOT NULL 
 ,  targetVersion INT  NOT NULL 
 ,  versionNum INT  NOT NULL 
 , PRIMARY KEY(  id)
);
CREATE TABLE IterationGroup_Locale ( 
  iteration_group_id BIGINT  NOT NULL 
 ,  locale_id BIGINT  NOT NULL 
 , PRIMARY KEY(  iteration_group_id, locale_id)
);
CREATE TABLE TransMemory ( 
  description TEXT 
 ,  slug VARCHAR(40)  NOT NULL 
 ,  source_language VARCHAR(255) 
 ,  id BIGSERIAL  NOT NULL 
 ,  creationDate TIMESTAMP  NOT NULL 
 ,  lastChanged TIMESTAMP  NOT NULL 
 ,  versionNum INT  NOT NULL 
 , PRIMARY KEY(  id)
);
CREATE TABLE TransMemoryUnit ( 
  trans_unit_id TEXT 
 ,  source_language VARCHAR(255) 
 ,  tm_id BIGINT  NOT NULL 
 ,  unique_id VARCHAR(255)  NOT NULL 
 ,  position INT 
 ,  metadata_type VARCHAR(75) 
 ,  metadata TEXT 
 ,  id BIGSERIAL  NOT NULL 
 ,  creationDate TIMESTAMP  NOT NULL 
 ,  lastChanged TIMESTAMP  NOT NULL 
 ,  versionNum INT  NOT NULL 
 , PRIMARY KEY(  id)
);
CREATE TABLE TransMemoryUnitVariant ( 
  language VARCHAR(255)  NOT NULL 
 ,  trans_unit_id BIGINT  NOT NULL 
 ,  tagged_segment TEXT  NOT NULL 
 ,  plain_text_segment TEXT  NOT NULL 
 ,  plain_text_segment_hash CHAR(32)  NOT NULL 
 ,  metadata_type VARCHAR(75) 
 ,  metadata TEXT 
 ,  id BIGSERIAL  NOT NULL 
 ,  creationDate TIMESTAMP  NOT NULL 
 ,  lastChanged TIMESTAMP  NOT NULL 
 ,  versionNum INT  NOT NULL 
 , PRIMARY KEY(  id)
);
CREATE TABLE TransMemory_Metadata ( 
  trans_memory_id BIGINT  NOT NULL 
 ,  metadata_type VARCHAR(75)  NOT NULL 
 ,  metadata TEXT 
 , PRIMARY KEY(  metadata_type, trans_memory_id)
);
CREATE TABLE WebHook ( 
  id BIGSERIAL  NOT NULL 
 ,  projectId BIGINT  NOT NULL 
 ,  url TEXT  NOT NULL 
 ,  secret VARCHAR(255) 
 , PRIMARY KEY(  id)
);
CREATE UNIQUE INDEX ukactivity ON Activity ( actor_id,approxTime,activityType,contextType,context_id );
CREATE UNIQUE INDEX username ON HAccount ( username );
CREATE  INDEX fk_haccount_mergedintoaccount ON HAccount ( mergedInto );
CREATE UNIQUE INDEX accountid ON HAccountActivationKey ( accountId );
CREATE  INDEX fk86e79ca4fa68c45f ON HAccountActivationKey ( accountId );
CREATE  INDEX fk9d5db27b3e684f5e ON HAccountMembership ( memberOf );
CREATE  INDEX fk9d5db27bfa68c45f ON HAccountMembership ( accountId );
CREATE  INDEX fk_haccountoption_haccount ON HAccountOption ( account_id );
CREATE UNIQUE INDEX accountid_HAccountResetPasswordKey ON HAccountResetPasswordKey ( accountId );
CREATE  INDEX fk85c9efdafa68c45f ON HAccountResetPasswordKey ( accountId );
CREATE  INDEX fk3321cc642df53d7e ON HAccountRoleGroup ( roleId );
CREATE  INDEX fk3321cc643e684f5e ON HAccountRoleGroup ( memberOf );
CREATE UNIQUE INDEX config_key ON HApplicationConfiguration ( config_key );
CREATE  INDEX fk_credentials_account ON HCredentials ( account_id );
CREATE UNIQUE INDEX docid ON HDocument ( docId,project_iteration_id );
CREATE  INDEX fkea766d83136cc025 ON HDocument ( poHeader_id );
CREATE  INDEX fkea766d8351ed6dfd ON HDocument ( project_iteration_id );
CREATE  INDEX fkea766d836c9badc1 ON HDocument ( last_modified_by_id );
CREATE  INDEX fkea766d83fea3b54a ON HDocument ( locale );
CREATE UNIQUE INDEX document_id ON HDocumentHistory ( document_id,revision );
CREATE  INDEX fk279765915383e2f0 ON HDocumentHistory ( document_id );
CREATE  INDEX fk279765916c9badc1 ON HDocumentHistory ( last_modified_by_id );
CREATE  INDEX fk27976591fea3b54a ON HDocumentHistory ( locale );
CREATE  INDEX fk_hdocumentupload_locale ON HDocumentUpload ( localeId );
CREATE  INDEX fk_hdocumentupload_projectiteration ON HDocumentUpload ( projectIterationId );
CREATE  INDEX fk_hdocumentuploadpart_documentupload ON HDocumentUploadPart ( documentUploadId );
CREATE  INDEX fk_hdocumentrawdocument_rawdocument ON HDocument_RawDocument ( rawDocumentId );
CREATE  INDEX ukglossaryentry_srclocaleid ON HGlossaryEntry ( srcLocaleId );
CREATE UNIQUE INDEX ukglossaryentryid_localeid ON HGlossaryTerm ( glossaryEntryId,localeId );
CREATE  INDEX ukglossaryterm_localeid ON HGlossaryTerm ( localeId );
CREATE UNIQUE INDEX slug ON HIterationGroup ( slug );
CREATE UNIQUE INDEX ukslug ON HIterationGroup ( slug );
CREATE  INDEX fkiterationgroupmaintainer_personid ON HIterationGroup_Maintainer ( personId );
CREATE  INDEX fkiterationgroup_projectiteration_projectiterationid ON HIterationGroup_ProjectIteration ( projectIterationId );
CREATE UNIQUE INDEX localeid ON HLocale ( localeId );
CREATE  INDEX fk82df50d73a932491 ON HLocale_Member ( supportedLanguageId );
CREATE  INDEX fk82df50d760c55b1b ON HLocale_Member ( personId );
CREATE UNIQUE INDEX email ON HPerson ( email );
CREATE  INDEX fk6f0931bdfa68c45f ON HPerson ( accountId );
CREATE UNIQUE INDEX keyhash ON HPersonEmailValidationKey ( keyHash );
CREATE UNIQUE INDEX personid ON HPersonEmailValidationKey ( personId );
CREATE  INDEX fk9a0abdd4b7a40df2 ON HPoHeader ( comment_id );
CREATE UNIQUE INDEX document_id_HPoTargetHeader ON HPoTargetHeader ( document_id,targetLanguage );
CREATE  INDEX fk1bc719855383e2f0 ON HPoTargetHeader ( document_id );
CREATE  INDEX fk1bc719857d208ad9 ON HPoTargetHeader ( targetLanguage );
CREATE  INDEX fk1bc71985b7a40df2 ON HPoTargetHeader ( comment_id );
CREATE  INDEX fk17a648cfb7a40df2 ON HPotEntryData ( comment_id );
CREATE UNIQUE INDEX slug_HProject ON HProject ( slug );
CREATE  INDEX fk_hproject_hcopytransopts ON HProject ( default_copy_trans_opts_id );
CREATE UNIQUE INDEX slug_HProjectIteration ON HProjectIteration ( slug,project_id );
CREATE  INDEX fk31c1e42c4bceea93 ON HProjectIteration ( project_id );
CREATE  INDEX fk31c1e42c5b1d181f ON HProjectIteration ( parentId );
CREATE  INDEX fkhprojectitelocloc ON HProjectIteration_Locale ( localeId );
CREATE  INDEX fk_hprojectallowedrole_role ON HProject_AllowedRole ( roleId );
CREATE  INDEX fkhprojectlocaleloc ON HProject_Locale ( localeId );
CREATE  INDEX fk1491f2e660c55b1b ON HProject_Maintainer ( personId );
CREATE  INDEX fk1491f2e665b5bb37 ON HProject_Maintainer ( projectId );
CREATE  INDEX fk_hroleassignmentrule_haccountrole ON HRoleAssignmentRule ( role_to_assign_id );
CREATE  INDEX uktermcomment_glossaryterm ON HTermComment ( glossaryTermId );
CREATE UNIQUE INDEX ukresiddocument_id ON HTextFlow ( resId,document_id );
CREATE  INDEX fk7b40f8635383e2f0 ON HTextFlow ( document_id );
CREATE  INDEX fk7b40f8638d8e70a5 ON HTextFlow ( potEntryData_id );
CREATE  INDEX fk7b40f863b7a40df2 ON HTextFlow ( comment_id );
CREATE  INDEX idx_contenthash ON HTextFlow ( contentHash );
CREATE  INDEX fkcontent_text_flow_history ON HTextFlowContentHistory ( text_flow_history_id );
CREATE UNIQUE INDEX revision ON HTextFlowHistory ( revision,tf_id );
CREATE  INDEX fk46c4deb1ccad9d19 ON HTextFlowHistory ( tf_id );
CREATE UNIQUE INDEX locale ON HTextFlowTarget ( locale,tf_id );
CREATE  INDEX fk1e933fd46c9badc1 ON HTextFlowTarget ( last_modified_by_id );
CREATE  INDEX fk1e933fd4b7a40df2 ON HTextFlowTarget ( comment_id );
CREATE  INDEX fk1e933fd4ccad9d19 ON HTextFlowTarget ( tf_id );
CREATE  INDEX fk1e933fd4fea3b54a ON HTextFlowTarget ( locale );
CREATE  INDEX idx_automatedentry ON HTextFlowTarget ( automatedEntry );
CREATE  INDEX idx_lastchanged ON HTextFlowTarget ( lastChanged );
CREATE  INDEX idx_lastmodifiedby ON HTextFlowTarget ( last_modified_by_id );
CREATE  INDEX idx_reviewedby ON HTextFlowTarget ( reviewed_by_id );
CREATE  INDEX idx_translatedby ON HTextFlowTarget ( translated_by_id );
CREATE  INDEX fkcontent_text_flow_target_history ON HTextFlowTargetContentHistory ( text_flow_target_history_id );
CREATE UNIQUE INDEX target_id ON HTextFlowTargetHistory ( target_id,versionNum );
CREATE  INDEX fkf10986206c9badc1 ON HTextFlowTargetHistory ( last_modified_by_id );
CREATE  INDEX fkf109862080727e8b ON HTextFlowTargetHistory ( target_id );
CREATE  INDEX idx_automatedentry_HTextFlowTargetHistory ON HTextFlowTargetHistory ( automatedEntry );
CREATE  INDEX idx_lastchanged_HTextFlowTargetHistory ON HTextFlowTargetHistory ( lastChanged );
CREATE  INDEX idx_lastmodifiedby_HTextFlowTargetHistory ON HTextFlowTargetHistory ( last_modified_by_id );
CREATE  INDEX idx_reviewedby_HTextFlowTargetHistory ON HTextFlowTargetHistory ( reviewed_by_id );
CREATE  INDEX idx_translatedby_HTextFlowTargetHistory ON HTextFlowTargetHistory ( reviewed_by_id );
CREATE  INDEX fktarget_review_comment ON HTextFlowTargetReviewComment ( target_id );
CREATE  INDEX fktarget_review_commenter ON HTextFlowTargetReviewComment ( commenter_id );
CREATE  INDEX fk_iterationgroup_locale_hlocale ON IterationGroup_Locale ( locale_id );
CREATE UNIQUE INDEX slug_TransMemory ON TransMemory ( slug );
CREATE UNIQUE INDEX uk_natural_id ON TransMemoryUnit ( tm_id,unique_id );
CREATE  INDEX fk_transunitvariant_transunit ON TransMemoryUnitVariant ( trans_unit_id );
CREATE  INDEX fk_webhook_hproject ON WebHook ( projectId );
ALTER TABLE Activity ADD CONSTRAINT FKActivity_person FOREIGN KEY (actor_id) REFERENCES HPerson(id);
ALTER TABLE HAccount ADD CONSTRAINT FK_HAccount_MergedIntoAccount FOREIGN KEY (mergedInto) REFERENCES HAccount(id);
ALTER TABLE HAccountActivationKey ADD CONSTRAINT FK86E79CA4FA68C45F FOREIGN KEY (accountId) REFERENCES HAccount(id);
ALTER TABLE HAccountMembership ADD CONSTRAINT FK9D5DB27B3E684F5E FOREIGN KEY (memberOf) REFERENCES HAccountRole(id);
ALTER TABLE HAccountMembership ADD CONSTRAINT FK9D5DB27BFA68C45F FOREIGN KEY (accountId) REFERENCES HAccount(id);
ALTER TABLE HAccountOption ADD CONSTRAINT FK_HAccountOption_HAccount FOREIGN KEY (account_id) REFERENCES HAccount(id);
ALTER TABLE HAccountResetPasswordKey ADD CONSTRAINT FK85C9EFDAFA68C45F FOREIGN KEY (accountId) REFERENCES HAccount(id);
ALTER TABLE HAccountRoleGroup ADD CONSTRAINT FK3321CC642DF53D7E FOREIGN KEY (roleId) REFERENCES HAccountRole(id);
ALTER TABLE HAccountRoleGroup ADD CONSTRAINT FK3321CC643E684F5E FOREIGN KEY (memberOf) REFERENCES HAccountRole(id);
ALTER TABLE HCredentials ADD CONSTRAINT FK_credentials_account FOREIGN KEY (account_id) REFERENCES HAccount(id);
ALTER TABLE HDocument ADD CONSTRAINT FKEA766D83136CC025 FOREIGN KEY (poHeader_id) REFERENCES HPoHeader(id);
ALTER TABLE HDocument ADD CONSTRAINT FKEA766D8351ED6DFD FOREIGN KEY (project_iteration_id) REFERENCES HProjectIteration(id);
ALTER TABLE HDocument ADD CONSTRAINT FKEA766D836C9BADC1 FOREIGN KEY (last_modified_by_id) REFERENCES HPerson(id);
ALTER TABLE HDocument ADD CONSTRAINT FKEA766D83FEA3B54A FOREIGN KEY (locale) REFERENCES HLocale(id);
ALTER TABLE HDocumentHistory ADD CONSTRAINT FK279765915383E2F0 FOREIGN KEY (document_id) REFERENCES HDocument(id);
ALTER TABLE HDocumentHistory ADD CONSTRAINT FK279765916C9BADC1 FOREIGN KEY (last_modified_by_id) REFERENCES HPerson(id);
ALTER TABLE HDocumentHistory ADD CONSTRAINT FK27976591FEA3B54A FOREIGN KEY (locale) REFERENCES HLocale(id);
ALTER TABLE HDocumentUpload ADD CONSTRAINT FK_HDocumentUpload_Locale FOREIGN KEY (localeId) REFERENCES HLocale(id);
ALTER TABLE HDocumentUpload ADD CONSTRAINT FK_HDocumentUpload_ProjectIteration FOREIGN KEY (projectIterationId) REFERENCES HProjectIteration(id);
ALTER TABLE HDocumentUploadPart ADD CONSTRAINT FK_HDocumentUploadPart_DocumentUpload FOREIGN KEY (documentUploadId) REFERENCES HDocumentUpload(id);
ALTER TABLE HDocument_RawDocument ADD CONSTRAINT FK_HDocumentRawDocument_Document FOREIGN KEY (documentId) REFERENCES HDocument(id);
ALTER TABLE HDocument_RawDocument ADD CONSTRAINT FK_HDocumentRawDocument_RawDocument FOREIGN KEY (rawDocumentId) REFERENCES HRawDocument(id);
ALTER TABLE HGlossaryEntry ADD CONSTRAINT UKglossaryentry_srcLocaleId FOREIGN KEY (srcLocaleId) REFERENCES HLocale(id);
ALTER TABLE HGlossaryTerm ADD CONSTRAINT UKglossaryterm_glossary_entry_id FOREIGN KEY (glossaryEntryId) REFERENCES HGlossaryEntry(id);
ALTER TABLE HGlossaryTerm ADD CONSTRAINT UKglossaryterm_localeId FOREIGN KEY (localeId) REFERENCES HLocale(id);
ALTER TABLE HIterationGroup_Maintainer ADD CONSTRAINT FKiterationGroupMaintainer_iterationGroupId FOREIGN KEY (iterationGroupId) REFERENCES HIterationGroup(id);
ALTER TABLE HIterationGroup_Maintainer ADD CONSTRAINT FKiterationGroupMaintainer_personId FOREIGN KEY (personId) REFERENCES HPerson(id);
ALTER TABLE HIterationGroup_ProjectIteration ADD CONSTRAINT FKiterationGroup_ProjectIteration_iterationGroupId FOREIGN KEY (iterationGroupId) REFERENCES HIterationGroup(id);
ALTER TABLE HIterationGroup_ProjectIteration ADD CONSTRAINT FKiterationGroup_ProjectIteration_projectIterationId FOREIGN KEY (projectIterationId) REFERENCES HProjectIteration(id);
ALTER TABLE HLocale_Member ADD CONSTRAINT FK82DF50D73A932491 FOREIGN KEY (supportedLanguageId) REFERENCES HLocale(id);
ALTER TABLE HLocale_Member ADD CONSTRAINT FK82DF50D760C55B1B FOREIGN KEY (personId) REFERENCES HPerson(id);
ALTER TABLE HPerson ADD CONSTRAINT FK6F0931BDFA68C45F FOREIGN KEY (accountId) REFERENCES HAccount(id);
ALTER TABLE HPersonEmailValidationKey ADD CONSTRAINT FK_HPersonEmailValidationKey_HPerson FOREIGN KEY (personId) REFERENCES HPerson(id);
ALTER TABLE HPoHeader ADD CONSTRAINT FK9A0ABDD4B7A40DF2 FOREIGN KEY (comment_id) REFERENCES HSimpleComment(id);
ALTER TABLE HPoTargetHeader ADD CONSTRAINT FK1BC719855383E2F0 FOREIGN KEY (document_id) REFERENCES HDocument(id);
ALTER TABLE HPoTargetHeader ADD CONSTRAINT FK1BC719857D208AD9 FOREIGN KEY (targetLanguage) REFERENCES HLocale(id);
ALTER TABLE HPoTargetHeader ADD CONSTRAINT FK1BC71985B7A40DF2 FOREIGN KEY (comment_id) REFERENCES HSimpleComment(id);
ALTER TABLE HPotEntryData ADD CONSTRAINT FK17A648CFB7A40DF2 FOREIGN KEY (comment_id) REFERENCES HSimpleComment(id);
ALTER TABLE HProject ADD CONSTRAINT FK_HProject_HCopyTransOpts FOREIGN KEY (default_copy_trans_opts_id) REFERENCES HCopyTransOptions(id);
ALTER TABLE HProjectIteration ADD CONSTRAINT FK31C1E42C4BCEEA93 FOREIGN KEY (project_id) REFERENCES HProject(id);
ALTER TABLE HProjectIteration ADD CONSTRAINT FK31C1E42C5B1D181F FOREIGN KEY (parentId) REFERENCES HProjectIteration(id);
ALTER TABLE HProjectIteration_Locale ADD CONSTRAINT FKHPROJECTITELOCLOC FOREIGN KEY (localeId) REFERENCES HLocale(id);
ALTER TABLE HProjectIteration_Locale ADD CONSTRAINT FKHPROJECTITELOCPRO FOREIGN KEY (projectIterationId) REFERENCES HProjectIteration(id);
ALTER TABLE HProjectIteration_LocaleAlias ADD CONSTRAINT FK_HProjectIteration_LocaleAlias_HProjectIteration FOREIGN KEY (projectIterationId) REFERENCES HProjectIteration(id);
ALTER TABLE HProjectIteration_Validation ADD CONSTRAINT FK_HProjectIteration_Validation_HProjectIteration FOREIGN KEY (projectIterationId) REFERENCES HProjectIteration(id);
ALTER TABLE HProject_AllowedRole ADD CONSTRAINT FK_HProjectAllowedRole_Project FOREIGN KEY (projectId) REFERENCES HProject(id);
ALTER TABLE HProject_AllowedRole ADD CONSTRAINT FK_HProjectAllowedRole_Role FOREIGN KEY (roleId) REFERENCES HAccountRole(id);
ALTER TABLE HProject_Locale ADD CONSTRAINT FKHPROJECTLOCALELOC FOREIGN KEY (localeId) REFERENCES HLocale(id);
ALTER TABLE HProject_Locale ADD CONSTRAINT FKHPROJECTLOCALEPRO FOREIGN KEY (projectId) REFERENCES HProject(id);
ALTER TABLE HProject_LocaleAlias ADD CONSTRAINT FK_HProject_LocaleAlias_HProject FOREIGN KEY (projectId) REFERENCES HProject(id);
ALTER TABLE HProject_Maintainer ADD CONSTRAINT FK1491F2E660C55B1B FOREIGN KEY (personId) REFERENCES HPerson(id);
ALTER TABLE HProject_Maintainer ADD CONSTRAINT FK1491F2E665B5BB37 FOREIGN KEY (projectId) REFERENCES HProject(id);
ALTER TABLE HProject_Validation ADD CONSTRAINT FK_HProject_Validation_HProject FOREIGN KEY (projectId) REFERENCES HProject(id);
ALTER TABLE HRoleAssignmentRule ADD CONSTRAINT FK_HRoleAssignmentRule_HAccountRole FOREIGN KEY (role_to_assign_id) REFERENCES HAccountRole(id);
ALTER TABLE HTermComment ADD CONSTRAINT UKtermComment_glossaryTerm FOREIGN KEY (glossaryTermId) REFERENCES HGlossaryTerm(id);
ALTER TABLE HTextFlow ADD CONSTRAINT FK7B40F8635383E2F0 FOREIGN KEY (document_id) REFERENCES HDocument(id);
ALTER TABLE HTextFlow ADD CONSTRAINT FK7B40F8638D8E70A5 FOREIGN KEY (potEntryData_id) REFERENCES HPotEntryData(id);
ALTER TABLE HTextFlow ADD CONSTRAINT FK7B40F863B7A40DF2 FOREIGN KEY (comment_id) REFERENCES HSimpleComment(id);
ALTER TABLE HTextFlowContentHistory ADD CONSTRAINT FKcontent_text_flow_history FOREIGN KEY (text_flow_history_id) REFERENCES HTextFlowHistory(id);
ALTER TABLE HTextFlowHistory ADD CONSTRAINT FK46C4DEB1CCAD9D19 FOREIGN KEY (tf_id) REFERENCES HTextFlow(id);
ALTER TABLE HTextFlowTarget ADD CONSTRAINT FK1E933FD46C9BADC1 FOREIGN KEY (last_modified_by_id) REFERENCES HPerson(id);
ALTER TABLE HTextFlowTarget ADD CONSTRAINT FK1E933FD4B7A40DF2 FOREIGN KEY (comment_id) REFERENCES HSimpleComment(id);
ALTER TABLE HTextFlowTarget ADD CONSTRAINT FK1E933FD4CCAD9D19 FOREIGN KEY (tf_id) REFERENCES HTextFlow(id);
ALTER TABLE HTextFlowTarget ADD CONSTRAINT FK1E933FD4FEA3B54A FOREIGN KEY (locale) REFERENCES HLocale(id);
ALTER TABLE HTextFlowTargetContentHistory ADD CONSTRAINT FKcontent_text_flow_target_history FOREIGN KEY (text_flow_target_history_id) REFERENCES HTextFlowTargetHistory(id);
ALTER TABLE HTextFlowTargetHistory ADD CONSTRAINT FKF10986206C9BADC1 FOREIGN KEY (last_modified_by_id) REFERENCES HPerson(id);
ALTER TABLE HTextFlowTargetHistory ADD CONSTRAINT FKF109862080727E8B FOREIGN KEY (target_id) REFERENCES HTextFlowTarget(id);
ALTER TABLE HTextFlowTargetReviewComment ADD CONSTRAINT FKtarget_review_comment FOREIGN KEY (target_id) REFERENCES HTextFlowTarget(id);
ALTER TABLE HTextFlowTargetReviewComment ADD CONSTRAINT FKtarget_review_commenter FOREIGN KEY (commenter_id) REFERENCES HPerson(id);
ALTER TABLE IterationGroup_Locale ADD CONSTRAINT FK_IterationGroup_Locale_HIterationGroup FOREIGN KEY (iteration_group_id) REFERENCES HIterationGroup(id);
ALTER TABLE IterationGroup_Locale ADD CONSTRAINT FK_IterationGroup_Locale_HLocale FOREIGN KEY (locale_id) REFERENCES HLocale(id);
ALTER TABLE TransMemoryUnit ADD CONSTRAINT FK_tmunit_trans_memory FOREIGN KEY (tm_id) REFERENCES TransMemory(id);
ALTER TABLE TransMemoryUnitVariant ADD CONSTRAINT FK_TransUnitVariant_TransUnit FOREIGN KEY (trans_unit_id) REFERENCES TransMemoryUnit(id);
ALTER TABLE TransMemory_Metadata ADD CONSTRAINT FK_Metadata_TransMemory FOREIGN KEY (trans_memory_id) REFERENCES TransMemory(id);
ALTER TABLE WebHook ADD CONSTRAINT FK_WebHook_HProject FOREIGN KEY (projectId) REFERENCES HProject(id);

CREATE FUNCTION add_document_history() RETURNS trigger AS $add_document_history$
   BEGIN
      IF NEW.revision != OLD.revision THEN
         INSERT INTO HDocumentHistory(document_id,revision,contentType,docId,locale,name,path,lastChanged,last_modified_by_id,obsolete)
            VALUES (OLD.id,OLD.revision,OLD.contentType,OLD.docId,OLD.locale,OLD.name,OLD.path,OLD.lastChanged,OLD.last_modified_by_id,OLD.obsolete);
      END IF;
      RETURN NEW;
   END;
$add_document_history$ LANGUAGE plpgsql;

CREATE TRIGGER HDocument_Update BEFORE UPDATE ON HDocument
   FOR EACH ROW EXECUTE PROCEDURE add_document_history();
