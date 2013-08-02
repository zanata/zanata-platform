;              
CREATE USER IF NOT EXISTS SA PASSWORD '' ADMIN;
DROP TABLE IF EXISTS PUBLIC.ACTIVITY CASCADE;  
DROP TABLE IF EXISTS PUBLIC.DATABASECHANGELOGLOCK CASCADE;     
DROP TABLE IF EXISTS PUBLIC.DATABASECHANGELOG CASCADE;         
DROP TABLE IF EXISTS PUBLIC.HROLEASSIGNMENTRULE CASCADE;       
DROP TABLE IF EXISTS PUBLIC.HACCOUNTMEMBERSHIP CASCADE;        
DROP TABLE IF EXISTS PUBLIC.HACCOUNTRESETPASSWORDKEY CASCADE;  
DROP TABLE IF EXISTS PUBLIC.HPROJECT_ALLOWEDROLE CASCADE;      
DROP TABLE IF EXISTS PUBLIC.HACCOUNTROLEGROUP CASCADE;         
DROP TABLE IF EXISTS PUBLIC.HAPPLICATIONCONFIGURATION CASCADE; 
DROP TABLE IF EXISTS PUBLIC.HGLOSSARYENTRY CASCADE;            
DROP TABLE IF EXISTS PUBLIC.HCREDENTIALS CASCADE;              
DROP TABLE IF EXISTS PUBLIC.HGLOSSARYTERM CASCADE;             
DROP TABLE IF EXISTS PUBLIC.HDOCUMENT CASCADE; 
DROP TABLE IF EXISTS PUBLIC.HDOCUMENTHISTORY CASCADE;          
DROP TABLE IF EXISTS PUBLIC.HPERSON CASCADE;   
DROP TABLE IF EXISTS PUBLIC.HPOHEADER CASCADE; 
DROP TABLE IF EXISTS PUBLIC.HPOTARGETHEADER CASCADE;           
DROP TABLE IF EXISTS PUBLIC.HPOTENTRYDATA CASCADE;             
DROP TABLE IF EXISTS PUBLIC.HPROJECT_LOCALE CASCADE;           
DROP TABLE IF EXISTS PUBLIC.HPROJECT_MAINTAINER CASCADE;       
DROP TABLE IF EXISTS PUBLIC.HSIMPLECOMMENT CASCADE;            
DROP TABLE IF EXISTS PUBLIC.HPROJECT_VALIDATION CASCADE;       
DROP TABLE IF EXISTS PUBLIC.HTEXTFLOWTARGETHISTORY CASCADE;    
DROP TABLE IF EXISTS PUBLIC.HTEXTFLOW CASCADE; 
DROP TABLE IF EXISTS PUBLIC.HCOPYTRANSOPTIONS CASCADE;         
DROP TABLE IF EXISTS PUBLIC.HACCOUNT CASCADE;  
DROP TABLE IF EXISTS PUBLIC.HACCOUNTROLE CASCADE;              
DROP TABLE IF EXISTS PUBLIC.HPROJECTITERATION_LOCALE CASCADE;  
DROP TABLE IF EXISTS PUBLIC.HPROJECTITERATION_VALIDATION CASCADE;              
DROP TABLE IF EXISTS PUBLIC.HLOCALE_MEMBER CASCADE;            
DROP TABLE IF EXISTS PUBLIC.HTEXTFLOWTARGET CASCADE;           
DROP TABLE IF EXISTS PUBLIC.HTEXTFLOWHISTORY CASCADE;          
DROP TABLE IF EXISTS PUBLIC.HTERMCOMMENT CASCADE;              
DROP TABLE IF EXISTS PUBLIC.HACCOUNTOPTION CASCADE;            
DROP TABLE IF EXISTS PUBLIC.HTEXTFLOWCONTENTHISTORY CASCADE;   
DROP TABLE IF EXISTS PUBLIC.HITERATIONGROUP CASCADE;           
DROP TABLE IF EXISTS PUBLIC.HPROJECT CASCADE;  
DROP TABLE IF EXISTS PUBLIC.HPERSONEMAILVALIDATIONKEY CASCADE; 
DROP TABLE IF EXISTS PUBLIC.HTEXTFLOWTARGETCONTENTHISTORY CASCADE;             
DROP TABLE IF EXISTS PUBLIC.HITERATIONGROUP_MAINTAINER CASCADE;
DROP TABLE IF EXISTS PUBLIC.HITERATIONGROUP_PROJECTITERATION CASCADE;          
DROP TABLE IF EXISTS PUBLIC.HLOCALE CASCADE;   
DROP TABLE IF EXISTS PUBLIC.HPROJECTITERATION CASCADE;         
DROP TABLE IF EXISTS PUBLIC.HDOCUMENT_RAWDOCUMENT CASCADE;     
DROP TABLE IF EXISTS PUBLIC.HTEXTFLOWTARGETREVIEWCOMMENT CASCADE;              
DROP TABLE IF EXISTS PUBLIC.HDOCUMENTUPLOAD CASCADE;           
DROP TABLE IF EXISTS PUBLIC.HDOCUMENTUPLOADPART CASCADE;       
DROP TABLE IF EXISTS PUBLIC.HACCOUNTACTIVATIONKEY CASCADE;     
DROP TABLE IF EXISTS PUBLIC.HRAWDOCUMENT CASCADE;              
DROP ALIAS IF EXISTS PUBLIC.MD5;               
CREATE FORCE ALIAS PUBLIC.MD5 FOR "org.zanata.util.HashUtil.generateHash";     
CREATE SEQUENCE PUBLIC.SYSTEM_SEQUENCE_BC68D000_36BD_46A0_A17C_3DA9C3259527 START WITH 1 BELONGS_TO_TABLE;     
CREATE SEQUENCE PUBLIC.SYSTEM_SEQUENCE_9CF6B1FC_4DD5_4C01_A0FE_7ABDFC4EC9B6 START WITH 1 BELONGS_TO_TABLE;     
CREATE SEQUENCE PUBLIC.SYSTEM_SEQUENCE_BBB37388_F5A2_4CEA_A2A7_B35443A44A4B START WITH 1 BELONGS_TO_TABLE;     
CREATE SEQUENCE PUBLIC.SYSTEM_SEQUENCE_63A04B08_CEFC_4031_A7C2_132598EA144D START WITH 1 BELONGS_TO_TABLE;     
CREATE SEQUENCE PUBLIC.SYSTEM_SEQUENCE_50231309_6061_4BBE_BCF4_FBFA62B5395A START WITH 5 BELONGS_TO_TABLE;     
CREATE SEQUENCE PUBLIC.SYSTEM_SEQUENCE_2859F01D_3D5A_4B75_B09B_1EA847883279 START WITH 5 BELONGS_TO_TABLE;     
CREATE SEQUENCE PUBLIC.SYSTEM_SEQUENCE_CABC6C67_570F_4ED0_B44B_3B742AA1AAEC START WITH 6 BELONGS_TO_TABLE;     
CREATE SEQUENCE PUBLIC.SYSTEM_SEQUENCE_FF6FE0BB_F212_43C9_89BF_A2D05A61669D START WITH 1 BELONGS_TO_TABLE;     
CREATE SEQUENCE PUBLIC.SYSTEM_SEQUENCE_4476349D_CEA3_4CB2_B750_65A31A471046 START WITH 2 BELONGS_TO_TABLE;     
CREATE SEQUENCE PUBLIC.SYSTEM_SEQUENCE_706F25FE_B1BA_4E11_9341_00C53A817296 START WITH 1 BELONGS_TO_TABLE;     
CREATE SEQUENCE PUBLIC.SYSTEM_SEQUENCE_EA725082_6ECC_4D7E_BF87_903C38282725 START WITH 1 BELONGS_TO_TABLE;     
CREATE SEQUENCE PUBLIC.SYSTEM_SEQUENCE_22FD7714_6107_4A7D_9727_145CA5CFA196 START WITH 1 BELONGS_TO_TABLE;     
CREATE SEQUENCE PUBLIC.SYSTEM_SEQUENCE_BA183351_98A7_4661_8F21_67F11853E409 START WITH 1 BELONGS_TO_TABLE;     
CREATE SEQUENCE PUBLIC.SYSTEM_SEQUENCE_F12E281E_B9F3_4A3F_83AF_E148A99F882C START WITH 1 BELONGS_TO_TABLE;     
CREATE SEQUENCE PUBLIC.SYSTEM_SEQUENCE_E586F775_482B_4C56_85C3_E444F5B8ACA5 START WITH 39 BELONGS_TO_TABLE;    
CREATE SEQUENCE PUBLIC.SYSTEM_SEQUENCE_047C9444_782F_437D_8E7F_3C9574C59E37 START WITH 5 BELONGS_TO_TABLE;     
CREATE SEQUENCE PUBLIC.SYSTEM_SEQUENCE_79179848_56F7_4E3D_B30B_97780C5EF1D9 START WITH 28 BELONGS_TO_TABLE;    
CREATE SEQUENCE PUBLIC.SYSTEM_SEQUENCE_435770F8_300C_4DBA_BB72_232B96E46942 START WITH 1 BELONGS_TO_TABLE;     
CREATE SEQUENCE PUBLIC.SYSTEM_SEQUENCE_D2605C38_87CB_4B62_BBF4_606092B88E99 START WITH 39 BELONGS_TO_TABLE;    
CREATE SEQUENCE PUBLIC.SYSTEM_SEQUENCE_194DC8F8_3EFE_45BC_AAFF_4E15D769D59F START WITH 1 BELONGS_TO_TABLE;     
CREATE SEQUENCE PUBLIC.SYSTEM_SEQUENCE_ECA62EAF_3B88_41D5_8207_CDCAFF596FAE START WITH 1 BELONGS_TO_TABLE;     
CREATE SEQUENCE PUBLIC.SYSTEM_SEQUENCE_32C3B4DF_E36D_43C5_8D7A_A813086234A9 START WITH 1 BELONGS_TO_TABLE;     
CREATE SEQUENCE PUBLIC.SYSTEM_SEQUENCE_C505BDE1_8BCD_4405_BD9A_7768172D6EAD START WITH 3 BELONGS_TO_TABLE;     
CREATE SEQUENCE PUBLIC.SYSTEM_SEQUENCE_6043B321_A8F8_4C45_A6B4_1EF6F9CF166A START WITH 5 BELONGS_TO_TABLE;     
CREATE SEQUENCE PUBLIC.SYSTEM_SEQUENCE_A5745DF6_79D4_4803_922C_9A94CDE2125F START WITH 2 BELONGS_TO_TABLE;     
CREATE SEQUENCE PUBLIC.SYSTEM_SEQUENCE_237D196E_0F89_425D_8EC6_235AB3EAAC6F START WITH 43 BELONGS_TO_TABLE;    
CREATE SEQUENCE PUBLIC.SYSTEM_SEQUENCE_3A887215_E661_446A_A33C_53135877FA1D START WITH 2 BELONGS_TO_TABLE;     
CREATE SEQUENCE PUBLIC.SYSTEM_SEQUENCE_1AAF14F4_E938_44FA_8DAE_3901615B003F START WITH 1 BELONGS_TO_TABLE;     
CREATE SEQUENCE PUBLIC.SYSTEM_SEQUENCE_5353524A_FF38_4137_B9A1_237693B880D7 START WITH 1 BELONGS_TO_TABLE;     
CREATE SEQUENCE PUBLIC.SYSTEM_SEQUENCE_5A6A6C5B_91E8_4369_A0E4_2C9BEE15F730 START WITH 1 BELONGS_TO_TABLE;     
CREATE SEQUENCE PUBLIC.SYSTEM_SEQUENCE_CC5CDC49_B24D_4DC6_B29B_40253688B07E START WITH 1 BELONGS_TO_TABLE;     
CREATE SEQUENCE PUBLIC.SYSTEM_SEQUENCE_C1BE3B1C_73A3_47E6_B6C4_EB11664C9991 START WITH 5 BELONGS_TO_TABLE;     
CREATE SEQUENCE PUBLIC.SYSTEM_SEQUENCE_397EA988_B34A_4EF4_B7C5_B83F4C043D71 START WITH 1 BELONGS_TO_TABLE;     
CREATE SEQUENCE PUBLIC.SYSTEM_SEQUENCE_D3718621_6075_4F69_9890_C5013A61A5A0 START WITH 1 BELONGS_TO_TABLE;     
CREATE CACHED TABLE PUBLIC.ACTIVITY(
    ID BIGINT DEFAULT (NEXT VALUE FOR PUBLIC.SYSTEM_SEQUENCE_C505BDE1_8BCD_4405_BD9A_7768172D6EAD) NOT NULL NULL_TO_DEFAULT SEQUENCE PUBLIC.SYSTEM_SEQUENCE_C505BDE1_8BCD_4405_BD9A_7768172D6EAD,
    CREATIONDATE TIMESTAMP NOT NULL,
    LASTCHANGED TIMESTAMP NOT NULL,
    VERSIONNUM INT NOT NULL,
    ACTOR_ID BIGINT NOT NULL,
    APPROXTIME TIMESTAMP NOT NULL,
    STARTOFFSETMILLIS INT NOT NULL,
    ENDOFFSETMILLIS INT NOT NULL,
    CONTEXTTYPE VARCHAR(150) NOT NULL,
    CONTEXT_ID BIGINT NOT NULL,
    LASTTARGETTYPE VARCHAR(150) NOT NULL,
    LAST_TARGET_ID BIGINT NOT NULL,
    ACTIVITYTYPE VARCHAR(150) NOT NULL,
    EVENTCOUNT INT NOT NULL,
    WORDCOUNT INT NOT NULL
);    
ALTER TABLE PUBLIC.ACTIVITY ADD CONSTRAINT PUBLIC.CONSTRAINT_CB PRIMARY KEY(ID);               
-- 2 +/- SELECT COUNT(*) FROM PUBLIC.ACTIVITY; 
INSERT INTO PUBLIC.ACTIVITY(ID, CREATIONDATE, LASTCHANGED, VERSIONNUM, ACTOR_ID, APPROXTIME, STARTOFFSETMILLIS, ENDOFFSETMILLIS, CONTEXTTYPE, CONTEXT_ID, LASTTARGETTYPE, LAST_TARGET_ID, ACTIVITYTYPE, EVENTCOUNT, WORDCOUNT) VALUES
(1, TIMESTAMP '2013-08-02 13:51:29.119', TIMESTAMP '2013-08-02 13:52:20.832', 5, 1, TIMESTAMP '2013-08-02 13:00:00.0', 3089119, 3140827, 'HProjectIteration', 1, 'HTexFlowTarget', 1, 'UPDATE_TRANSLATION', 6, 146),
(2, TIMESTAMP '2013-08-02 13:52:21.113', TIMESTAMP '2013-08-02 13:52:21.113', 0, 1, TIMESTAMP '2013-08-02 13:00:00.0', 3141113, 3141113, 'HProjectIteration', 1, 'HDocument', 1, 'UPLOAD_TRANSLATION_DOCUMENT', 1, 479);            
CREATE CACHED TABLE PUBLIC.DATABASECHANGELOGLOCK(
    ID INT NOT NULL,
    LOCKED BOOLEAN NOT NULL,
    LOCKGRANTED TIMESTAMP,
    LOCKEDBY VARCHAR(255)
);    
ALTER TABLE PUBLIC.DATABASECHANGELOGLOCK ADD CONSTRAINT PUBLIC.PK_DATABASECHANGELOGLOCK PRIMARY KEY(ID);       
-- 1 +/- SELECT COUNT(*) FROM PUBLIC.DATABASECHANGELOGLOCK;    
INSERT INTO PUBLIC.DATABASECHANGELOGLOCK(ID, LOCKED, LOCKGRANTED, LOCKEDBY) VALUES
(1, FALSE, NULL, NULL);     
CREATE CACHED TABLE PUBLIC.DATABASECHANGELOG(
    ID VARCHAR(63) NOT NULL,
    AUTHOR VARCHAR(63) NOT NULL,
    FILENAME VARCHAR(200) NOT NULL,
    DATEEXECUTED TIMESTAMP NOT NULL,
    ORDEREXECUTED INT NOT NULL,
    EXECTYPE VARCHAR(10) NOT NULL,
    MD5SUM VARCHAR(35),
    DESCRIPTION VARCHAR(255),
    COMMENTS VARCHAR(255),
    TAG VARCHAR(255),
    LIQUIBASE VARCHAR(20)
);    
ALTER TABLE PUBLIC.DATABASECHANGELOG ADD CONSTRAINT PUBLIC.PK_DATABASECHANGELOG PRIMARY KEY(ID, AUTHOR, FILENAME);             
-- 95 +/- SELECT COUNT(*) FROM PUBLIC.DATABASECHANGELOG;       
INSERT INTO PUBLIC.DATABASECHANGELOG(ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, EXECTYPE, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES
('h2_baseline', 'flies', 'db/changelogs/db.changelog-1.0.xml', TIMESTAMP '2013-06-24 13:46:06.098', 1, 'EXECUTED', '3:2f1959708ebefa8c1d863774b7562155', 'SQL From File', 'h2 baseline schema', NULL, '2.0.3'),
('1', 'sflaniga@redhat.com', 'db/changelogs/db.changelog-1.2.xml', TIMESTAMP '2013-06-24 13:46:06.119', 2, 'EXECUTED', '3:bea5e9d4e6adfb998f2880dc253ecd56', 'Drop Foreign Key Constraint (x2), Drop Table, Drop Foreign Key Constraint (x2), Drop Table, Drop Foreign Key Constraint, Drop Table', 'remove HCommunity tables', NULL, '2.0.3'),
('2', 'sflaniga@redhat.com', 'db/changelogs/db.changelog-1.2.xml', TIMESTAMP '2013-06-24 13:46:06.147', 3, 'EXECUTED', '3:2b618bbabe2156f18de8cba877c71cc0', 'Add Column', 'add wordCount to HTextFlow', NULL, '2.0.3'),
('3', 'sflaniga@redhat.com', 'db/changelogs/db.changelog-1.2.xml', TIMESTAMP '2013-06-24 13:46:06.156', 4, 'EXECUTED', '3:ef40d4195c6f2e66a287c29771ba751f', 'Custom Change, Modify Column', 'calculate existing word counts and enable non-null constraint on HTextFlow', NULL, '2.0.3'),
('4', 'sflaniga@redhat.com', 'db/changelogs/db.changelog-1.2.xml', TIMESTAMP '2013-06-24 13:46:06.163', 5, 'EXECUTED', '3:3eaecee20ba6be6f39b4845649d30756', 'Add Unique Constraint', 'add unique constraint(resId,document_id) on HTextFlow', NULL, '2.0.3'),
('6', 'sflaniga@redhat.com', 'db/changelogs/db.changelog-1.2.xml', TIMESTAMP '2013-06-24 13:46:06.168', 6, 'EXECUTED', '3:4887d52f83049de4c6fa0935a00c6414', 'Drop Unique Constraint', 'remove unique constraint(document_id,resId) on HTextFlow', NULL, '2.0.3'),
('1', 'hding@redhat.com', 'db/changelogs/db.changelog-1.3.xml', TIMESTAMP '2013-06-24 13:46:06.203', 7, 'EXECUTED', '3:00a2667e9984735aa55efcbf9922e503', 'Add Column (x2)', 'Add flag for project and project version to support a customized list of locales', NULL, '2.0.3'),
('2', 'hding@redhat.com', 'db/changelogs/db.changelog-1.3.xml', TIMESTAMP '2013-06-24 13:46:06.216', 8, 'EXECUTED', '3:945ac3f359077ce73a59871604b6d4e3', 'Create Table, Add Primary Key, Add Foreign Key Constraint (x2)', 'Add tables to store the customized list of locales for project', NULL, '2.0.3'),
('3', 'hding@redhat.com', 'db/changelogs/db.changelog-1.3.xml', TIMESTAMP '2013-06-24 13:46:06.234', 9, 'EXECUTED', '3:9daf4b35db7b0aaa343f1503c425184c', 'Create Table, Add Primary Key, Add Foreign Key Constraint (x2)', 'Add tables to store the customized list of locales for project version', NULL, '2.0.3'),
('4', 'hding@redhat.com', 'db/changelogs/db.changelog-1.3.xml', TIMESTAMP '2013-06-24 13:46:06.265', 10, 'EXECUTED', '3:ab82ddbef8dcfee351c203d471ffecb7', 'Drop Column (x2)', 'drop column name and description to HProjectIteration table', NULL, '2.0.3'),
('1', 'sflaniga@redhat.com', 'db/changelogs/db.changelog-1.3.xml', TIMESTAMP '2013-06-24 13:46:06.281', 11, 'EXECUTED', '3:b3391cc53df50582fb23a0386b665270', 'Update Data, Custom Change', 'reset and recalculate word counts', NULL, '2.0.3'),
('1', 'camunoz@redhat.com', 'db/changelogs/db.changelog-1.4.xml', TIMESTAMP '2013-06-24 13:46:06.294', 12, 'EXECUTED', '3:26c8b53b7f6a21a7061dcaf114356e86', 'Add Column', 'Add a flag indicating when a member of a Language team (locale) is a team coordinator.', NULL, '2.0.3'),
('1', 'damason@redhat.com', 'db/changelogs/db.changelog-1.4.xml', TIMESTAMP '2013-06-24 13:46:06.298', 13, 'EXECUTED', '3:1bf53a786961a27caf226d46fed9fc9d', 'Update Data', 'rename admin email key to zanata from address', NULL, '2.0.3'),
('2', 'camunoz@redhat.com', 'db/changelogs/db.changelog-1.4.xml', TIMESTAMP '2013-06-24 13:46:06.305', 14, 'EXECUTED', '3:9acc5a9b9d6ae18c59a604b74a07cbe9', 'Delete Data', STRINGDECODE('Remove language team memberships from all non-translator users. \n          Only done when users are not part of the translator group by default.'), NULL, '2.0.3'),
('1', 'aeng@redhat.com', 'db/changelogs/db.changelog-1.5.xml', TIMESTAMP '2013-06-24 13:46:06.313', 15, 'EXECUTED', '3:449a84c8eeefa29a938a648608a90797', 'Create Table, Add Primary Key', 'Add table to store glossary entry', NULL, '2.0.3');             
INSERT INTO PUBLIC.DATABASECHANGELOG(ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, EXECTYPE, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES
('2', 'aeng@redhat.com', 'db/changelogs/db.changelog-1.5.xml', TIMESTAMP '2013-06-24 13:46:06.329', 16, 'EXECUTED', '3:969377144d2e5dbd7da390458fbf4a07', 'Create Table, Add Primary Key, Add Unique Constraint, Add Foreign Key Constraint (x2)', 'Add table to store glossary term', NULL, '2.0.3'),
('3', 'aeng@redhat.com', 'db/changelogs/db.changelog-1.5.xml', TIMESTAMP '2013-06-24 13:46:06.34', 17, 'EXECUTED', '3:5f33895f8d83b0c185a7fc3e5a22fd19', 'Create Table, Add Primary Key, Add Foreign Key Constraint', 'Add table to store glossary term comment', NULL, '2.0.3'),
('4', 'aeng@redhat.com', 'db/changelogs/db.changelog-1.5.xml', TIMESTAMP '2013-06-24 13:46:06.351', 18, 'EXECUTED', '3:2864e95d545b62f55a411f709cf76620', 'Add Column, Add Foreign Key Constraint', 'Alter glossary entry table', NULL, '2.0.3'),
('5', 'aeng@redhat.com', 'db/changelogs/db.changelog-1.5.xml', TIMESTAMP '2013-06-24 13:46:06.361', 19, 'EXECUTED', '3:9bf9125e3f015805a6ed9dcf4bd8c4aa', 'Add Column', 'Add obsolete column to HTermComment', NULL, '2.0.3'),
('6', 'aeng@redhat.com', 'db/changelogs/db.changelog-1.5.xml', TIMESTAMP '2013-06-24 13:46:06.371', 20, 'EXECUTED', '3:2fe78b0e14c270ed074d23c491bb6a89', 'Drop Column', 'Alter HTermComment - removed obsolete', NULL, '2.0.3'),
('7', 'aeng@redhat.com', 'db/changelogs/db.changelog-1.5.xml', TIMESTAMP '2013-06-24 13:46:06.397', 21, 'EXECUTED', '3:6e1188d4bb3e516ae11f83ef09331b98', 'Drop Foreign Key Constraint, Drop Column, Add Column, Add Foreign Key Constraint, Add Column', 'Alter glossary entry table - remove srcTermId/add srcLocaleId/add sourceRef', NULL, '2.0.3'),
('8.5', 'aeng@redhat.com', 'db/changelogs/db.changelog-1.5.xml', TIMESTAMP '2013-06-24 13:46:06.412', 22, 'EXECUTED', '3:9aa1eed5fb8a91a25bb5becfe59813dd', 'Drop Column', 'Alter glossary tables - remove sourceRef/add auto increment', NULL, '2.0.3'),
('9', 'aeng@redhat.com', 'db/changelogs/db.changelog-1.5.xml', TIMESTAMP '2013-06-24 13:46:06.417', 23, 'EXECUTED', '3:e69feef0634f750d4a80ee002764d4d7', 'Drop Foreign Key Constraint', 'Alter glossary term table - remove constraint', NULL, '2.0.3'),
('10', 'aeng@redhat.com', 'db/changelogs/db.changelog-1.5.xml', TIMESTAMP '2013-06-24 13:46:06.431', 24, 'EXECUTED', '3:4c2c05bd96679a1240734af06d295bea', 'Add Column', 'Add obsolete column to HProject', NULL, '2.0.3'),
('11', 'aeng@redhat.com', 'db/changelogs/db.changelog-1.5.xml', TIMESTAMP '2013-06-24 13:46:06.447', 25, 'EXECUTED', '3:924f5f3fc061cf5b3d6de7ae44c59ce4', 'Add Column', 'Add obsolete column to HProjectIteration', NULL, '2.0.3'),
('1', 'aeng@redhat.com', 'db/changelogs/db.changelog-1.6.xml', TIMESTAMP '2013-06-24 13:46:06.482', 26, 'EXECUTED', '3:ae8f33a1c57cde2709d4e369e10c2157', 'Add Column (x2)', 'Alter HProjectIteration/HProject - add status column', NULL, '2.0.3'),
('2', 'aeng@redhat.com', 'db/changelogs/db.changelog-1.6.xml', TIMESTAMP '2013-06-24 13:46:06.49', 27, 'EXECUTED', '3:467b8a788294f9d0ecb86d9c5e21aa0c', 'Custom SQL (x3)', 'Insert HProjectIteration/HProject status column with ''Current''', NULL, '2.0.3'),
('3', 'aeng@redhat.com', 'db/changelogs/db.changelog-1.6.xml', TIMESTAMP '2013-06-24 13:46:06.504', 28, 'EXECUTED', '3:be39c867044fd50c4ca81d1f99acfd74', 'Drop Column', 'Alter HProjectIteration - removed active column', NULL, '2.0.3'),
('4', 'aeng@redhat.com', 'db/changelogs/db.changelog-1.6.xml', TIMESTAMP '2013-06-24 13:46:06.524', 29, 'EXECUTED', '3:dd066a6a2387ba2c4af7eeccdd7a8abd', 'Drop Column (x2)', 'Alter HProjectIteration/HProject - removed obsolete column', NULL, '2.0.3'),
('5', 'aeng@redhat.com', 'db/changelogs/db.changelog-1.6.xml', TIMESTAMP '2013-06-24 13:46:06.533', 30, 'EXECUTED', '3:3e92954644f0539537878d2235b8a644', 'Custom SQL (x4)', 'Update ''Current'' to ''ACTIVE'' and ''Retired'' to ''READONLY''', NULL, '2.0.3'),
('6', 'aeng@redhat.com', 'db/changelogs/db.changelog-1.6.xml', TIMESTAMP '2013-06-24 13:46:06.538', 31, 'EXECUTED', '3:2d7f33d1d6d8b56754bf378e0f5660ce', 'Custom SQL (x2)', 'Update ''Obsolete'' to ''OBSOLETE''', NULL, '2.0.3');            
INSERT INTO PUBLIC.DATABASECHANGELOG(ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, EXECTYPE, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES
('7', 'aeng@redhat.com', 'db/changelogs/db.changelog-1.6.xml', TIMESTAMP '2013-06-24 13:46:06.555', 32, 'EXECUTED', '3:b70d3f2296d16fee198c26c73e3788f9', 'Custom SQL, Modify Column', 'Alter HProjectIteration - change status to char(1)', NULL, '2.0.3'),
('1', 'sflaniga@redhat.com', 'db/changelogs/db.changelog-1.6.xml', TIMESTAMP '2013-06-24 13:46:06.568', 33, 'EXECUTED', '3:0725ed62d51880890a1342badb1d880b', 'Custom SQL, Modify Column', 'Alter HProjectIteration/HProject - change status to char(1)', NULL, '2.0.3'),
('1-h2', 'camunoz@redhat.com', 'db/changelogs/db.changelog-1.6.xml', TIMESTAMP '2013-06-24 13:46:06.584', 34, 'EXECUTED', '3:39eb0db77e95419fc6147f405780cb06', 'Custom SQL', 'Create H2 database Hash function.', NULL, '2.0.3'),
('1', 'camunoz@redhat.com', 'db/changelogs/db.changelog-1.6.xml', TIMESTAMP '2013-06-24 13:46:06.601', 35, 'EXECUTED', '3:7c40ce957f30ab67a6ad64054f6e33d0', 'Add Column, Create Index, Custom SQL', 'Add and populate Content Hash column for the HTextFlow table.', NULL, '2.0.3'),
('1', 'aeng@redhat.com', 'db/changelogs/db.changelog-1.6a.xml', TIMESTAMP '2013-06-24 13:46:06.634', 36, 'EXECUTED', '3:27a8a832c1156a25889257e3b20b35e3', 'Drop Unique Constraint, Drop Primary Key (x3), Set Column as Auto-Increment (x3)', 'Alter glossary tables - add auto increment', NULL, '2.0.3'),
('1', 'camunoz@redhat.com', 'db/changelogs/db.changelog-1.6a.xml', TIMESTAMP '2013-06-24 13:46:06.647', 37, 'EXECUTED', '3:1ad87ddef37859d3978cf57a4b1d1a46', 'Create Table, Add Foreign Key Constraint', 'Add HTextFlowContent table to support plural forms.', NULL, '2.0.3'),
('2', 'camunoz@redhat.com', 'db/changelogs/db.changelog-1.6a.xml', TIMESTAMP '2013-06-24 13:46:06.662', 38, 'EXECUTED', '3:6792d05a3db7afd93791f9ad64b81c7a', 'Create Table, Add Foreign Key Constraint', 'Add HTextFlowContentHistory table to support plural forms.', NULL, '2.0.3'),
('3', 'camunoz@redhat.com', 'db/changelogs/db.changelog-1.6a.xml', TIMESTAMP '2013-06-24 13:46:06.671', 39, 'EXECUTED', '3:016cef75cd2a98d34dabdad91802e470', 'Custom SQL (x2)', 'Replace content columns from HTextFlow into the new HTextFlowContent table', NULL, '2.0.3'),
('4', 'camunoz@redhat.com', 'db/changelogs/db.changelog-1.6a.xml', TIMESTAMP '2013-06-24 13:46:06.683', 40, 'EXECUTED', '3:88c3307a880cc74bf05ffc749f8f554b', 'Create Table, Add Foreign Key Constraint', 'Add HTextFlowTargetContent table to support plural forms.', NULL, '2.0.3'),
('5', 'camunoz@redhat.com', 'db/changelogs/db.changelog-1.6a.xml', TIMESTAMP '2013-06-24 13:46:06.697', 41, 'EXECUTED', '3:4c1c000bd9aaa5661998206f6e89832c', 'Create Table, Add Foreign Key Constraint', 'Add HTextFlowTargetContentHistory table to support plural forms.', NULL, '2.0.3'),
('6', 'camunoz@redhat.com', 'db/changelogs/db.changelog-1.6a.xml', TIMESTAMP '2013-06-24 13:46:06.705', 42, 'EXECUTED', '3:dbf2bd41e0b7628b49c6df7207733f1c', 'Custom SQL (x2)', 'Replace content columns from HTextFlowTarget into the new HTextFlowTargetContent table', NULL, '2.0.3'),
('7', 'camunoz@redhat.com', 'db/changelogs/db.changelog-1.6a.xml', TIMESTAMP '2013-06-24 13:46:06.777', 43, 'EXECUTED', '3:f0b1f7c4ab926c1f9c52d7282f6ccce9', 'Drop Column (x4), Add Column', 'Remove unnecessary columns / Add new columns', NULL, '2.0.3'),
('7.5', 'aeng@redhat.com', 'db/changelogs/db.changelog-1.6a.xml', TIMESTAMP '2013-06-24 13:46:06.785', 44, 'EXECUTED', '3:cf753fa6168ce2aef3689fec52bde695', 'Custom SQL (x2)', 'Clear orphan records in HTermComment and HGlossaryTerm', NULL, '2.0.3'),
('21', 'aeng@redhat.com', 'db/changelogs/db.changelog-1.6a.xml', TIMESTAMP '2013-06-24 13:46:06.792', 45, 'EXECUTED', '3:353ef7c39faa704fb797e2942be83b36', 'Create Table, Add Unique Constraint', 'Add table to store iteration group', NULL, '2.0.3'),
('22', 'aeng@redhat.com', 'db/changelogs/db.changelog-1.6a.xml', TIMESTAMP '2013-06-24 13:46:06.8', 46, 'EXECUTED', '3:3d37d4f1aa7834724d24b14dcf83b980', 'Create Table, Add Primary Key, Add Foreign Key Constraint (x2)', 'Add table to store many-many HIterationGroup to HPerson', NULL, '2.0.3');           
INSERT INTO PUBLIC.DATABASECHANGELOG(ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, EXECTYPE, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES
('23', 'aeng@redhat.com', 'db/changelogs/db.changelog-1.6a.xml', TIMESTAMP '2013-06-24 13:46:06.808', 47, 'EXECUTED', '3:68c32a3f8874d1779f8d6fef0f5bb4b6', 'Create Table, Add Primary Key, Add Foreign Key Constraint (x2)', 'Add table to store many-many HIterationGroup to HProjectIteration', NULL, '2.0.3'),
('21', 'camunoz@redhat.com', 'db/changelogs/db.changelog-1.6a.xml', TIMESTAMP '2013-06-24 13:46:06.822', 48, 'EXECUTED', '3:5a8e950a981e674b98d0f9a41182da5c', 'Add Column', 'Add enabledByDefault column on HLocale', NULL, '2.0.3'),
('1', 'camunoz@redhat.com', 'db/changelogs/db.changelog-2.0.xml', TIMESTAMP '2013-06-24 13:46:06.834', 49, 'EXECUTED', '3:9dabcf5b39bc8fcdd00a450ede870f16', 'Update Data (x8)', 'Rename application configuration properties', NULL, '2.0.3'),
('2', 'camunoz@redhat.com', 'db/changelogs/db.changelog-2.0.xml', TIMESTAMP '2013-06-24 13:46:06.842', 50, 'EXECUTED', '3:3807c694c132ae4b688f4a4784253a8e', 'Create Table, Add Foreign Key Constraint', 'Create HCredentials table.', NULL, '2.0.3'),
('3', 'camunoz@redhat.com', 'db/changelogs/db.changelog-2.0.xml', TIMESTAMP '2013-06-24 13:46:06.847', 51, 'EXECUTED', '3:fd3ad0d2df2e7b96aec4a5a450dde31a', 'Custom Change', 'Migrate data to HCredentials table.', NULL, '2.0.3'),
('4', 'camunoz@redhat.com', 'db/changelogs/db.changelog-2.0.xml', TIMESTAMP '2013-06-24 13:46:06.86', 52, 'EXECUTED', '3:5b5a67d78aa8489717a3521a21d65448', 'Add Column, Update Data', 'Add role type column.', NULL, '2.0.3'),
('5', 'camunoz@redhat.com', 'db/changelogs/db.changelog-2.0.xml', TIMESTAMP '2013-06-24 13:46:06.881', 53, 'EXECUTED', '3:b52980fdc775212a9ffe9e9709759f02', 'Create Table, Add Primary Key, Add Foreign Key Constraint (x2)', 'Add HProject_AllowedRole table.', NULL, '2.0.3'),
('6', 'camunoz@redhat.com', 'db/changelogs/db.changelog-2.0.xml', TIMESTAMP '2013-06-24 13:46:06.907', 54, 'EXECUTED', '3:af70c034094fe62c5c11255d9dc4ad0c', 'Add Column', 'Add ''Restricted by Roles'' column to HProject table.', NULL, '2.0.3'),
('7', 'camunoz@redhat.com', 'db/changelogs/db.changelog-2.0.xml', TIMESTAMP '2013-06-24 13:46:06.934', 55, 'EXECUTED', '3:10ccea516deca0950bc681f345dc3a44', 'Add Column, Add Foreign Key Constraint', 'Add ''merged_into'' column to HAccount table.', NULL, '2.0.3'),
('8', 'camunoz@redhat.com', 'db/changelogs/db.changelog-2.0.xml', TIMESTAMP '2013-06-24 13:46:06.951', 56, 'EXECUTED', '3:f2a8b42715e2a339e010850d931fa266', 'Create Table, Add Foreign Key Constraint', 'Add HRoleAssignmentRule table.', NULL, '2.0.3'),
('9', 'camunoz@redhat.com', 'db/changelogs/db.changelog-2.0.xml', TIMESTAMP '2013-06-24 13:46:06.983', 57, 'EXECUTED', '3:d019cb272cc5752738249011ec8e9fe1', 'Create Table, Add Column, Add Foreign Key Constraint', 'Add Copy Trans Options table and project column.', NULL, '2.0.3'),
('1', 'damason@redhat.com', 'db/changelogs/db.changelog-2.0.xml', TIMESTAMP '2013-06-24 13:46:06.995', 58, 'EXECUTED', '3:dd363da8a10318fa7d49d6fa20060780', 'Create Table', 'Add HRawDocument table.', NULL, '2.0.3'),
('2', 'damason@redhat.com', 'db/changelogs/db.changelog-2.0.xml', TIMESTAMP '2013-06-24 13:46:07.007', 59, 'EXECUTED', '3:e4b9de392ee8030ef8d5b55f4af28a25', 'Create Table, Add Primary Key', 'Add HDocument_RawDocument table.', NULL, '2.0.3'),
('3', 'damason@redhat.com', 'db/changelogs/db.changelog-2.0.xml', TIMESTAMP '2013-06-24 13:46:07.014', 60, 'EXECUTED', '3:9aa4ddbb80de71319e4916e01d5a0a38', 'Add Foreign Key Constraint', 'Add HDocument foreign key for HDocument_RawDocument table.', NULL, '2.0.3'),
('4', 'damason@redhat.com', 'db/changelogs/db.changelog-2.0.xml', TIMESTAMP '2013-06-24 13:46:07.023', 61, 'EXECUTED', '3:8bf2798f67bff6bcaf620a59edf004cb', 'Add Foreign Key Constraint', 'Add HRawDocument foreign key for HDocument_RawDocument table.', NULL, '2.0.3'),
('5', 'damason@redhat.com', 'db/changelogs/db.changelog-2.0.xml', TIMESTAMP '2013-06-24 13:46:07.037', 62, 'EXECUTED', '3:e043e9407e6ffee9c4600c123dba67e9', 'Create Table', 'Add HDocumentUpload table.', NULL, '2.0.3');      
INSERT INTO PUBLIC.DATABASECHANGELOG(ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, EXECTYPE, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES
('6', 'damason@redhat.com', 'db/changelogs/db.changelog-2.0.xml', TIMESTAMP '2013-06-24 13:46:07.047', 63, 'EXECUTED', '3:7da622d5e17d6230ede6a1388fba533a', 'Add Foreign Key Constraint', 'Add HProjectIteration foreign key for HDocumentUpload table.', NULL, '2.0.3'),
('7', 'damason@redhat.com', 'db/changelogs/db.changelog-2.0.xml', TIMESTAMP '2013-06-24 13:46:07.056', 64, 'EXECUTED', '3:eb2c6cd9dee91aef1a4d47b37aee0d4d', 'Add Foreign Key Constraint', 'Add HLocale foreign key for HDocumentUpload table.', NULL, '2.0.3'),
('8', 'damason@redhat.com', 'db/changelogs/db.changelog-2.0.xml', TIMESTAMP '2013-06-24 13:46:07.065', 65, 'EXECUTED', '3:055ef14fee1c27077b4b0acedadfef70', 'Create Table', 'Add HDocumentUploadPart table.', NULL, '2.0.3'),
('9', 'damason@redhat.com', 'db/changelogs/db.changelog-2.0.xml', TIMESTAMP '2013-06-24 13:46:07.075', 66, 'EXECUTED', '3:c21ddc922d225617a6d1431f174b82cb', 'Add Foreign Key Constraint', 'Add HDocumentUpload foreign key for HDocumentUploadPart table.', NULL, '2.0.3'),
('10', 'damason@redhat.com', 'db/changelogs/db.changelog-2.0.xml', TIMESTAMP '2013-06-24 13:46:07.088', 67, 'EXECUTED', '3:873cf708fad23a4c91e8a2df7f25f50a', 'Modify data type, Add Not-Null Constraint', 'Change HRawDocument content to use larger blob field.', NULL, '2.0.3'),
('11', 'damason@redhat.com', 'db/changelogs/db.changelog-2.0.xml', TIMESTAMP '2013-06-24 13:46:07.093', 68, 'EXECUTED', '3:64d2484ae4b24d38f764f8c0e412ba7d', 'Modify data type, Add Not-Null Constraint', 'Change HDocumentUploadPart content to use larger blob field.', NULL, '2.0.3'),
('10', 'camunoz@redhat.com', 'db/changelogs/db.changelog-2.0.xml', TIMESTAMP '2013-06-24 13:46:07.158', 69, 'EXECUTED', '3:5c6bf099572202917ea42d576a6d4da9', 'Add Column', 'Create Content fields on HTextFlow table.', NULL, '2.0.3'),
('11', 'camunoz@redhat.com', 'db/changelogs/db.changelog-2.0.xml', TIMESTAMP '2013-06-24 13:46:07.174', 70, 'EXECUTED', '3:04dd2eeebfeee52b9b1ca323c0f66c55', 'Custom SQL (x6)', 'Migrate HTextFlowContent data to HTextFlow table.', NULL, '2.0.3'),
('12', 'camunoz@redhat.com', 'db/changelogs/db.changelog-2.0.xml', TIMESTAMP '2013-06-24 13:46:07.179', 71, 'EXECUTED', '3:09e2709f9bbc97e57502fcacb31a2563', 'Drop Table', 'Remove HTextFlowContent table.', NULL, '2.0.3'),
('13', 'camunoz@redhat.com', 'db/changelogs/db.changelog-2.0.xml', TIMESTAMP '2013-06-24 13:46:07.247', 72, 'EXECUTED', '3:13d4fcddfe6787b5a5affcfa710a7caf', 'Add Column', 'Create Content fields on HTextFlowTarget table.', NULL, '2.0.3'),
('14', 'camunoz@redhat.com', 'db/changelogs/db.changelog-2.0.xml', TIMESTAMP '2013-06-24 13:46:07.256', 73, 'EXECUTED', '3:ede12ce55f9d30a5e377163313999451', 'Custom SQL (x6)', 'Migrate HTextFlowTargetContent data to HTextFlowTarget table.', NULL, '2.0.3'),
('15', 'camunoz@redhat.com', 'db/changelogs/db.changelog-2.0.xml', TIMESTAMP '2013-06-24 13:46:07.263', 74, 'EXECUTED', '3:99800633ca1996a35b7b068b800342ec', 'Drop Table', 'Remove HTextFlowTargetContent table.', NULL, '2.0.3'),
('16', 'camunoz@redhat.com', 'db/changelogs/db.changelog-2.0.xml', TIMESTAMP '2013-06-24 13:46:07.267', 75, 'EXECUTED', '3:5c99a5eee9a5c4e1f5f84dd3dc447226', 'Custom Change', 'Validate user account passwords.', NULL, '2.0.3'),
('1', 'camunoz@redhat.com', 'db/changelogs/db.changelog-2.1.xml', TIMESTAMP '2013-06-24 13:46:07.272', 76, 'EXECUTED', '3:b7198b7c2a74ac5a0b75f6fa81b9a280', 'Create Table', 'Add HEditorOption table.', NULL, '2.0.3'),
('2', 'camunoz@redhat.com', 'db/changelogs/db.changelog-2.1.xml', TIMESTAMP '2013-06-24 13:46:07.276', 77, 'EXECUTED', '3:15aba8ec7d580788714e3715853a5f08', 'Add Foreign Key Constraint', 'Add HAccountOption foreign key to HAccount.', NULL, '2.0.3'),
('3', 'aeng@redhat.com', 'db/changelogs/db.changelog-2.1.xml', TIMESTAMP '2013-06-24 13:46:07.282', 78, 'EXECUTED', '3:f6f24cd76d086b6a9c0b50f7d80a8dbb', 'Custom SQL', 'Cleanup HAccountActivationKey which account already activated', NULL, '2.0.3'),
('4', 'aeng@redhat.com', 'db/changelogs/db.changelog-2.1.xml', TIMESTAMP '2013-06-24 13:46:07.293', 79, 'EXECUTED', '3:90372540c61704111bbefe5f74c2359e', 'Create Table, Add Foreign Key Constraint', 'Add HPersonEmailValidationKey table', NULL, '2.0.3');   
INSERT INTO PUBLIC.DATABASECHANGELOG(ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, EXECTYPE, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE) VALUES
('1', 'alex.eng@redhat.com', 'db/changelogs/db.changelog-2.2.xml', TIMESTAMP '2013-06-24 13:46:07.308', 80, 'EXECUTED', '3:d922b7ee15a1b12f28351cd0a95ef36f', 'Add Column', 'Add projectType column to HProject', NULL, '2.0.3'),
('2', 'alex.eng@redhat.com', 'db/changelogs/db.changelog-2.2.xml', TIMESTAMP '2013-06-24 13:46:07.321', 81, 'EXECUTED', '3:690e62b9b1350e6c2b8b4a7c0bb2fc29', 'Drop Column', 'drop projecttype column in HProject', NULL, '2.0.3'),
('3', 'alex.eng@redhat.com', 'db/changelogs/db.changelog-2.2.xml', TIMESTAMP '2013-06-24 13:46:07.338', 82, 'EXECUTED', '3:6ca0c560af350353858a5edd2cf62631', 'Add Column', 'Add projectType column to HProjectIteration', NULL, '2.0.3'),
('1', 'damason@redhat.com', 'db/changelogs/db.changelog-2.2.xml', TIMESTAMP '2013-06-24 13:46:07.349', 83, 'EXECUTED', '3:f9728f8176825113070da39b0b61b5ec', 'Add Column', 'Add source control URL column to HProject.', NULL, '2.0.3'),
('2', 'damason@redhat.com', 'db/changelogs/db.changelog-2.2.xml', TIMESTAMP '2013-06-24 13:46:07.363', 84, 'EXECUTED', '3:ecd7a03c463ad1e7573a7334b58b7019', 'Add Column', 'Add machine-readable source control URL column to HProject.', NULL, '2.0.3'),
('4', 'alex.eng@redhat.com', 'db/changelogs/db.changelog-2.2.xml', TIMESTAMP '2013-06-24 13:46:07.373', 85, 'EXECUTED', '3:3de8499104fef70c2b87bdbe8ec9ab4c', 'Create Table, Add Primary Key, Add Foreign Key Constraint', 'Add HProject_Validation table.', NULL, '2.0.3'),
('5', 'alex.eng@redhat.com', 'db/changelogs/db.changelog-2.2.xml', TIMESTAMP '2013-06-24 13:46:07.387', 86, 'EXECUTED', '3:9fce0a92d34e438372e0e1d26b71f576', 'Add Column', 'Add overrideValidations column to HProject', NULL, '2.0.3'),
('6', 'alex.eng@redhat.com', 'db/changelogs/db.changelog-2.2.xml', TIMESTAMP '2013-06-24 13:46:07.393', 87, 'EXECUTED', '3:2d1519dc7b19e6d624d553e196726dfe', 'Create Table, Add Primary Key, Add Foreign Key Constraint', 'Add HProjectIteration_Validation table.', NULL, '2.0.3'),
('7', 'alex.eng@redhat.com', 'db/changelogs/db.changelog-2.2.xml', TIMESTAMP '2013-06-24 13:46:07.405', 88, 'EXECUTED', '3:5f430861696abed24f60c2edeb4dc117', 'Add Column', 'Add overrideValidations column to HProjectIteration', NULL, '2.0.3'),
('8', 'alex.eng@redhat.com', 'db/changelogs/db.changelog-2.2.xml', TIMESTAMP '2013-06-24 13:46:07.413', 89, 'EXECUTED', '3:e43dcb5cd50871b29ec35050b6ff38a1', 'Add Column', 'Add creationDate column to HAccountActivationKey', NULL, '2.0.3'),
('3', 'damason@redhat.com', 'db/changelogs/db.changelog-2.2.xml', TIMESTAMP '2013-06-24 13:46:07.421', 90, 'EXECUTED', '3:25ba01dabe26dca2020069132425e6fe', 'Update Data (x2)', 'Change project type from Raw to File in HProject and HProjectIteration.', NULL, '2.0.3'),
('1', 'damason@redhat.com', 'db/changelogs/db.changelog-3.0.xml', TIMESTAMP '2013-06-24 13:46:07.431', 91, 'EXECUTED', '3:e4ed063ffa38732d9089c8b0b3fe2e0f', 'Add Column', 'Add adapter parameter string column to HRawDocument.', NULL, '2.0.3'),
('1', 'pahuang@redhat.com', 'db/changelogs/db.changelog-3.0.xml', TIMESTAMP '2013-06-24 13:46:07.447', 92, 'EXECUTED', '3:966d08902f77bd20774cee44822a63d2', 'Add Column', 'Add need review column to project iteration', NULL, '2.0.3'),
('2', 'pahuang@redhat.com', 'db/changelogs/db.changelog-3.0.xml', TIMESTAMP '2013-06-24 13:46:07.474', 93, 'EXECUTED', '3:c5f11008ebe1ad2a2dd22a9ba645517b', 'Add Column, Custom SQL', 'Add review related columns to HTextFlowTarget', NULL, '2.0.3'),
('3', 'pahuang@redhat.com', 'db/changelogs/db.changelog-3.0.xml', TIMESTAMP '2013-06-24 13:46:07.496', 94, 'EXECUTED', '3:461098686f604b50e028b51a79549c48', 'Add Column, Custom SQL', 'Add review related columns to HTextFlowTargetHistory', NULL, '2.0.3'),
('h2_triggers', 'zanata', 'db/changelogs/db.changelog-triggers.xml', TIMESTAMP '2013-06-24 13:46:07.526', 95, 'EXECUTED', '3:a10092c42e654144adc60a82e3366c1f', 'SQL From File', 'h2 triggers', NULL, '2.0.3');    
CREATE CACHED TABLE PUBLIC.HROLEASSIGNMENTRULE(
    POLICYNAME VARCHAR(100),
    IDENTITYREGEXP LONGTEXT,
    ROLE_TO_ASSIGN_ID INT NOT NULL,
    ID BIGINT DEFAULT (NEXT VALUE FOR PUBLIC.SYSTEM_SEQUENCE_BC68D000_36BD_46A0_A17C_3DA9C3259527) NOT NULL NULL_TO_DEFAULT SEQUENCE PUBLIC.SYSTEM_SEQUENCE_BC68D000_36BD_46A0_A17C_3DA9C3259527,
    CREATIONDATE TIMESTAMP NOT NULL,
    LASTCHANGED TIMESTAMP NOT NULL,
    VERSIONNUM INT NOT NULL
);        
ALTER TABLE PUBLIC.HROLEASSIGNMENTRULE ADD CONSTRAINT PUBLIC.CONSTRAINT_7B PRIMARY KEY(ID);    
-- 0 +/- SELECT COUNT(*) FROM PUBLIC.HROLEASSIGNMENTRULE;      
CREATE CACHED TABLE PUBLIC.HACCOUNTMEMBERSHIP(
    ACCOUNTID BIGINT NOT NULL,
    MEMBEROF INTEGER NOT NULL
); 
ALTER TABLE PUBLIC.HACCOUNTMEMBERSHIP ADD CONSTRAINT PUBLIC.CONSTRAINT_3 PRIMARY KEY(ACCOUNTID, MEMBEROF);     
-- 6 +/- SELECT COUNT(*) FROM PUBLIC.HACCOUNTMEMBERSHIP;       
INSERT INTO PUBLIC.HACCOUNTMEMBERSHIP(ACCOUNTID, MEMBEROF) VALUES
(1, 1),
(1, 4),
(2, 1),
(2, 5),
(3, 2),
(4, 3);              
CREATE CACHED TABLE PUBLIC.HACCOUNTRESETPASSWORDKEY(
    KEYHASH VARCHAR(32) NOT NULL,
    ACCOUNTID BIGINT NOT NULL
);        
ALTER TABLE PUBLIC.HACCOUNTRESETPASSWORDKEY ADD CONSTRAINT PUBLIC.CONSTRAINT_B PRIMARY KEY(KEYHASH);           
-- 0 +/- SELECT COUNT(*) FROM PUBLIC.HACCOUNTRESETPASSWORDKEY; 
CREATE CACHED TABLE PUBLIC.HPROJECT_ALLOWEDROLE(
    PROJECTID BIGINT NOT NULL,
    ROLEID INT NOT NULL
);     
ALTER TABLE PUBLIC.HPROJECT_ALLOWEDROLE ADD CONSTRAINT PUBLIC.CONSTRAINT_C7 PRIMARY KEY(PROJECTID, ROLEID);    
-- 0 +/- SELECT COUNT(*) FROM PUBLIC.HPROJECT_ALLOWEDROLE;     
CREATE CACHED TABLE PUBLIC.HACCOUNTROLEGROUP(
    ROLEID INTEGER NOT NULL,
    MEMBEROF INTEGER NOT NULL
);    
ALTER TABLE PUBLIC.HACCOUNTROLEGROUP ADD CONSTRAINT PUBLIC.CONSTRAINT_A3 PRIMARY KEY(ROLEID, MEMBEROF);        
-- 3 +/- SELECT COUNT(*) FROM PUBLIC.HACCOUNTROLEGROUP;        
INSERT INTO PUBLIC.HACCOUNTROLEGROUP(ROLEID, MEMBEROF) VALUES
(3, 2),
(4, 3),
(4, 1);          
CREATE CACHED TABLE PUBLIC.HAPPLICATIONCONFIGURATION(
    ID BIGINT DEFAULT (NEXT VALUE FOR PUBLIC.SYSTEM_SEQUENCE_CC5CDC49_B24D_4DC6_B29B_40253688B07E) NOT NULL NULL_TO_DEFAULT SEQUENCE PUBLIC.SYSTEM_SEQUENCE_CC5CDC49_B24D_4DC6_B29B_40253688B07E,
    CREATIONDATE TIMESTAMP NOT NULL,
    LASTCHANGED TIMESTAMP NOT NULL,
    VERSIONNUM INTEGER NOT NULL,
    CONFIG_KEY VARCHAR(255) NOT NULL,
    CONFIG_VALUE CLOB NOT NULL
);      
ALTER TABLE PUBLIC.HAPPLICATIONCONFIGURATION ADD CONSTRAINT PUBLIC.CONSTRAINT_6 PRIMARY KEY(ID);               
-- 0 +/- SELECT COUNT(*) FROM PUBLIC.HAPPLICATIONCONFIGURATION;
CREATE CACHED TABLE PUBLIC.HGLOSSARYENTRY(
    ID BIGINT DEFAULT (NEXT VALUE FOR PUBLIC.SYSTEM_SEQUENCE_32C3B4DF_E36D_43C5_8D7A_A813086234A9) NOT NULL NULL_TO_DEFAULT SEQUENCE PUBLIC.SYSTEM_SEQUENCE_32C3B4DF_E36D_43C5_8D7A_A813086234A9,
    CREATIONDATE TIMESTAMP NOT NULL,
    LASTCHANGED TIMESTAMP NOT NULL,
    VERSIONNUM INT NOT NULL,
    SRCLOCALEID BIGINT NOT NULL,
    SOURCEREF LONGTEXT
);  
ALTER TABLE PUBLIC.HGLOSSARYENTRY ADD CONSTRAINT PUBLIC.CONSTRAINT_69 PRIMARY KEY(ID);         
-- 0 +/- SELECT COUNT(*) FROM PUBLIC.HGLOSSARYENTRY;           
CREATE CACHED TABLE PUBLIC.HCREDENTIALS(
    ACCOUNT_ID BIGINT NOT NULL,
    TYPE VARCHAR(10) NOT NULL,
    USER LONGTEXT,
    EMAIL VARCHAR(100),
    ID BIGINT DEFAULT (NEXT VALUE FOR PUBLIC.SYSTEM_SEQUENCE_9CF6B1FC_4DD5_4C01_A0FE_7ABDFC4EC9B6) NOT NULL NULL_TO_DEFAULT SEQUENCE PUBLIC.SYSTEM_SEQUENCE_9CF6B1FC_4DD5_4C01_A0FE_7ABDFC4EC9B6,
    CREATIONDATE TIMESTAMP NOT NULL,
    LASTCHANGED TIMESTAMP NOT NULL,
    VERSIONNUM INT NOT NULL
);   
ALTER TABLE PUBLIC.HCREDENTIALS ADD CONSTRAINT PUBLIC.CONSTRAINT_BB PRIMARY KEY(ID);           
-- 0 +/- SELECT COUNT(*) FROM PUBLIC.HCREDENTIALS;             
CREATE CACHED TABLE PUBLIC.HGLOSSARYTERM(
    ID BIGINT DEFAULT (NEXT VALUE FOR PUBLIC.SYSTEM_SEQUENCE_5A6A6C5B_91E8_4369_A0E4_2C9BEE15F730) NOT NULL NULL_TO_DEFAULT SEQUENCE PUBLIC.SYSTEM_SEQUENCE_5A6A6C5B_91E8_4369_A0E4_2C9BEE15F730,
    GLOSSARYENTRYID BIGINT NOT NULL,
    LOCALEID BIGINT NOT NULL,
    CONTENT LONGTEXT,
    CREATIONDATE TIMESTAMP NOT NULL,
    LASTCHANGED TIMESTAMP NOT NULL,
    VERSIONNUM INT NOT NULL
);   
ALTER TABLE PUBLIC.HGLOSSARYTERM ADD CONSTRAINT PUBLIC.CONSTRAINT_16 PRIMARY KEY(ID);          
-- 0 +/- SELECT COUNT(*) FROM PUBLIC.HGLOSSARYTERM;            
CREATE CACHED TABLE PUBLIC.HDOCUMENT(
    ID BIGINT DEFAULT (NEXT VALUE FOR PUBLIC.SYSTEM_SEQUENCE_6043B321_A8F8_4C45_A6B4_1EF6F9CF166A) NOT NULL NULL_TO_DEFAULT SEQUENCE PUBLIC.SYSTEM_SEQUENCE_6043B321_A8F8_4C45_A6B4_1EF6F9CF166A,
    CREATIONDATE TIMESTAMP NOT NULL,
    LASTCHANGED TIMESTAMP NOT NULL,
    VERSIONNUM INTEGER NOT NULL,
    CONTENTTYPE VARCHAR(255) NOT NULL,
    DOCID VARCHAR(255) NOT NULL,
    NAME VARCHAR(255) NOT NULL,
    OBSOLETE BIT NOT NULL,
    PATH VARCHAR(255) NOT NULL,
    REVISION INTEGER NOT NULL,
    LAST_MODIFIED_BY_ID BIGINT,
    LOCALE BIGINT NOT NULL,
    POHEADER_ID BIGINT,
    PROJECT_ITERATION_ID BIGINT NOT NULL
);            
ALTER TABLE PUBLIC.HDOCUMENT ADD CONSTRAINT PUBLIC.CONSTRAINT_15 PRIMARY KEY(ID);              
-- 4 +/- SELECT COUNT(*) FROM PUBLIC.HDOCUMENT;
INSERT INTO PUBLIC.HDOCUMENT(ID, CREATIONDATE, LASTCHANGED, VERSIONNUM, CONTENTTYPE, DOCID, NAME, OBSOLETE, PATH, REVISION, LAST_MODIFIED_BY_ID, LOCALE, POHEADER_ID, PROJECT_ITERATION_ID) VALUES
(1, TIMESTAMP '2013-06-24 13:55:18.144', TIMESTAMP '2013-06-24 13:55:18.144', 1, 'application/x-gettext', 'About_Fedora', 'About_Fedora', FALSE, '', 1, 1, 1, 1, 1),
(2, TIMESTAMP '2013-06-24 13:55:24.363', TIMESTAMP '2013-06-24 13:55:24.363', 1, 'application/x-gettext', 'Article_Info', 'Article_Info', FALSE, '', 1, 1, 1, 2, 1),
(3, TIMESTAMP '2013-06-24 13:55:28.59', TIMESTAMP '2013-06-24 13:55:28.59', 1, 'application/x-gettext', 'Author_Group', 'Author_Group', FALSE, '', 1, 1, 1, 3, 1),
(4, TIMESTAMP '2013-06-24 13:55:32.788', TIMESTAMP '2013-06-24 13:55:32.788', 1, 'application/x-gettext', 'Revision_History', 'Revision_History', FALSE, '', 1, 1, 1, 4, 1);   
CREATE CACHED TABLE PUBLIC.HDOCUMENTHISTORY(
    ID BIGINT DEFAULT (NEXT VALUE FOR PUBLIC.SYSTEM_SEQUENCE_194DC8F8_3EFE_45BC_AAFF_4E15D769D59F) NOT NULL NULL_TO_DEFAULT SEQUENCE PUBLIC.SYSTEM_SEQUENCE_194DC8F8_3EFE_45BC_AAFF_4E15D769D59F,
    CONTENTTYPE VARCHAR(255) NOT NULL,
    DOCID VARCHAR(255) NOT NULL,
    LASTCHANGED TIMESTAMP,
    NAME VARCHAR(255),
    OBSOLETE BIT NOT NULL,
    PATH VARCHAR(255),
    REVISION INTEGER,
    DOCUMENT_ID BIGINT,
    LAST_MODIFIED_BY_ID BIGINT,
    LOCALE BIGINT NOT NULL
);         
ALTER TABLE PUBLIC.HDOCUMENTHISTORY ADD CONSTRAINT PUBLIC.CONSTRAINT_4 PRIMARY KEY(ID);        
-- 0 +/- SELECT COUNT(*) FROM PUBLIC.HDOCUMENTHISTORY;         
CREATE CACHED TABLE PUBLIC.HPERSON(
    ID BIGINT DEFAULT (NEXT VALUE FOR PUBLIC.SYSTEM_SEQUENCE_50231309_6061_4BBE_BCF4_FBFA62B5395A) NOT NULL NULL_TO_DEFAULT SEQUENCE PUBLIC.SYSTEM_SEQUENCE_50231309_6061_4BBE_BCF4_FBFA62B5395A,
    CREATIONDATE TIMESTAMP NOT NULL,
    LASTCHANGED TIMESTAMP NOT NULL,
    VERSIONNUM INTEGER NOT NULL,
    EMAIL VARCHAR(255) NOT NULL,
    NAME VARCHAR(80) NOT NULL,
    ACCOUNTID BIGINT
);        
ALTER TABLE PUBLIC.HPERSON ADD CONSTRAINT PUBLIC.CONSTRAINT_6D PRIMARY KEY(ID);
-- 4 +/- SELECT COUNT(*) FROM PUBLIC.HPERSON;  
INSERT INTO PUBLIC.HPERSON(ID, CREATIONDATE, LASTCHANGED, VERSIONNUM, EMAIL, NAME, ACCOUNTID) VALUES
(1, TIMESTAMP '2012-04-03 15:06:28.0', TIMESTAMP '2012-04-03 15:06:28.0', 0, 'admin@example.com', 'Administrator', 1),
(2, TIMESTAMP '2012-04-03 15:06:28.0', TIMESTAMP '2012-04-03 15:06:28.0', 0, 'translator@example.com', 'translator', 2),
(3, TIMESTAMP '2013-06-24 15:41:33.0', TIMESTAMP '2013-06-24 15:41:33.0', 0, 'glossarist@example.com', 'glossarist', 3),
(4, TIMESTAMP '2013-06-25 11:43:28.354', TIMESTAMP '2013-06-25 11:43:28.354', 0, 'glossary-admin@example.com', 'glossary-admin', 4);             
CREATE CACHED TABLE PUBLIC.HPOHEADER(
    ID BIGINT DEFAULT (NEXT VALUE FOR PUBLIC.SYSTEM_SEQUENCE_047C9444_782F_437D_8E7F_3C9574C59E37) NOT NULL NULL_TO_DEFAULT SEQUENCE PUBLIC.SYSTEM_SEQUENCE_047C9444_782F_437D_8E7F_3C9574C59E37,
    CREATIONDATE TIMESTAMP NOT NULL,
    LASTCHANGED TIMESTAMP NOT NULL,
    VERSIONNUM INTEGER NOT NULL,
    ENTRIES CLOB,
    COMMENT_ID BIGINT
);   
ALTER TABLE PUBLIC.HPOHEADER ADD CONSTRAINT PUBLIC.CONSTRAINT_F PRIMARY KEY(ID);               
-- 4 +/- SELECT COUNT(*) FROM PUBLIC.HPOHEADER;
INSERT INTO PUBLIC.HPOHEADER(ID, CREATIONDATE, LASTCHANGED, VERSIONNUM, ENTRIES, COMMENT_ID) VALUES
(1, TIMESTAMP '2013-06-24 13:55:19.074', TIMESTAMP '2013-06-24 13:55:19.074', 0, STRINGDECODE('Project-Id-Version=0\nPOT-Creation-Date=2012-12-04T23\\:32\\:46\nMIME-Version=1.0\nContent-Type=application/x-publican; charset\\=UTF-8\nContent-Transfer-Encoding=8bit\n'), 28),
(2, TIMESTAMP '2013-06-24 13:55:24.381', TIMESTAMP '2013-06-24 13:55:24.381', 0, STRINGDECODE('Project-Id-Version=0\nPOT-Creation-Date=2012-12-04T23\\:32\\:46\nMIME-Version=1.0\nContent-Type=application/x-publican; charset\\=UTF-8\nContent-Transfer-Encoding=8bit\n'), 33),
(3, TIMESTAMP '2013-06-24 13:55:28.602', TIMESTAMP '2013-06-24 13:55:28.602', 0, STRINGDECODE('Project-Id-Version=0\nPOT-Creation-Date=2012-12-04T23\\:32\\:46\nMIME-Version=1.0\nContent-Type=application/x-publican; charset\\=UTF-8\nContent-Transfer-Encoding=8bit\n'), 37),
(4, TIMESTAMP '2013-06-24 13:55:32.811', TIMESTAMP '2013-06-24 13:55:32.811', 0, STRINGDECODE('Project-Id-Version=0\nPOT-Creation-Date=2012-12-04T23\\:32\\:46\nMIME-Version=1.0\nContent-Type=application/x-publican; charset\\=UTF-8\nContent-Transfer-Encoding=8bit\n'), 42);        
CREATE CACHED TABLE PUBLIC.HPOTARGETHEADER(
    ID BIGINT DEFAULT (NEXT VALUE FOR PUBLIC.SYSTEM_SEQUENCE_3A887215_E661_446A_A33C_53135877FA1D) NOT NULL NULL_TO_DEFAULT SEQUENCE PUBLIC.SYSTEM_SEQUENCE_3A887215_E661_446A_A33C_53135877FA1D,
    CREATIONDATE TIMESTAMP NOT NULL,
    LASTCHANGED TIMESTAMP NOT NULL,
    VERSIONNUM INTEGER NOT NULL,
    ENTRIES CLOB,
    COMMENT_ID BIGINT,
    DOCUMENT_ID BIGINT,
    TARGETLANGUAGE BIGINT NOT NULL
); 
ALTER TABLE PUBLIC.HPOTARGETHEADER ADD CONSTRAINT PUBLIC.CONSTRAINT_25 PRIMARY KEY(ID);        
-- 1 +/- SELECT COUNT(*) FROM PUBLIC.HPOTARGETHEADER;          
INSERT INTO PUBLIC.HPOTARGETHEADER(ID, CREATIONDATE, LASTCHANGED, VERSIONNUM, ENTRIES, COMMENT_ID, DOCUMENT_ID, TARGETLANGUAGE) VALUES
(1, TIMESTAMP '2013-08-02 13:52:20.188', TIMESTAMP '2013-08-02 13:52:20.188', 0, STRINGDECODE('PO-Revision-Date=2013-08-02 01\\:51+1000\nLast-Translator=Administrator <admin@example.com>\nLanguage-Team=Polish\nLanguage=pl\nX-Generator=Zanata 3.0.0-alpha-2-SNAPSHOT\nPlural-Forms=nplurals\\=3; plural\\=(n\\=\\=1 ? 0 \\: n%10>\\=2 && n%10<\\=4 && (n%100<10 || n%100>\\=20) ? 1 \\: 2)\n'), NULL, 1, 4);        
CREATE CACHED TABLE PUBLIC.HPOTENTRYDATA(
    ID BIGINT DEFAULT (NEXT VALUE FOR PUBLIC.SYSTEM_SEQUENCE_E586F775_482B_4C56_85C3_E444F5B8ACA5) NOT NULL NULL_TO_DEFAULT SEQUENCE PUBLIC.SYSTEM_SEQUENCE_E586F775_482B_4C56_85C3_E444F5B8ACA5,
    CONTEXT VARCHAR(255),
    FLAGS VARCHAR(255),
    REFS CLOB,
    COMMENT_ID BIGINT,
    TF_ID BIGINT
);        
ALTER TABLE PUBLIC.HPOTENTRYDATA ADD CONSTRAINT PUBLIC.CONSTRAINT_20 PRIMARY KEY(ID);          
-- 38 +/- SELECT COUNT(*) FROM PUBLIC.HPOTENTRYDATA;           
INSERT INTO PUBLIC.HPOTENTRYDATA(ID, CONTEXT, FLAGS, REFS, COMMENT_ID, TF_ID) VALUES
(1, NULL, 'no-c-format', NULL, NULL, NULL),
(2, NULL, 'no-c-format', NULL, NULL, NULL),
(3, NULL, 'no-c-format', NULL, NULL, NULL),
(4, NULL, 'no-c-format', NULL, NULL, NULL),
(5, NULL, 'no-c-format', NULL, NULL, NULL),
(6, NULL, 'no-c-format', NULL, NULL, NULL),
(7, NULL, 'no-c-format', NULL, NULL, NULL),
(8, NULL, 'no-c-format', NULL, NULL, NULL),
(9, NULL, 'no-c-format', NULL, NULL, NULL),
(10, NULL, 'no-c-format', NULL, NULL, NULL),
(11, NULL, 'no-c-format', NULL, NULL, NULL),
(12, NULL, 'no-c-format', NULL, NULL, NULL),
(13, NULL, 'no-c-format', NULL, NULL, NULL),
(14, NULL, 'no-c-format', NULL, NULL, NULL),
(15, NULL, 'no-c-format', NULL, NULL, NULL),
(16, NULL, 'no-c-format', NULL, NULL, NULL),
(17, NULL, 'no-c-format', NULL, NULL, NULL),
(18, NULL, 'no-c-format', NULL, NULL, NULL),
(19, NULL, 'no-c-format', NULL, NULL, NULL),
(20, NULL, 'no-c-format', NULL, NULL, NULL),
(21, NULL, 'no-c-format', NULL, NULL, NULL),
(22, NULL, 'no-c-format', NULL, NULL, NULL),
(23, NULL, 'no-c-format', NULL, NULL, NULL),
(24, NULL, NULL, NULL, NULL, NULL),
(25, NULL, NULL, NULL, NULL, NULL),
(26, NULL, 'no-c-format', NULL, NULL, NULL),
(27, NULL, NULL, NULL, NULL, NULL),
(28, NULL, 'no-c-format', NULL, NULL, NULL),
(29, NULL, 'no-c-format', NULL, NULL, NULL),
(30, NULL, 'no-c-format', NULL, NULL, NULL),
(31, NULL, 'no-c-format', NULL, NULL, NULL),
(32, NULL, 'no-c-format', NULL, NULL, NULL),
(33, NULL, 'no-c-format', NULL, NULL, NULL),
(34, NULL, 'no-c-format', NULL, NULL, NULL),
(35, NULL, 'no-c-format', NULL, NULL, NULL),
(36, NULL, 'no-c-format', NULL, NULL, NULL),
(37, NULL, 'no-c-format', NULL, NULL, NULL),
(38, NULL, 'no-c-format', NULL, NULL, NULL); 
CREATE CACHED TABLE PUBLIC.HPROJECT_LOCALE(
    PROJECTID BIGINT NOT NULL,
    LOCALEID BIGINT NOT NULL
);     
ALTER TABLE PUBLIC.HPROJECT_LOCALE ADD CONSTRAINT PUBLIC.CONSTRAINT_8 PRIMARY KEY(PROJECTID, LOCALEID);        
-- 0 +/- SELECT COUNT(*) FROM PUBLIC.HPROJECT_LOCALE;          
CREATE CACHED TABLE PUBLIC.HPROJECT_MAINTAINER(
    PERSONID BIGINT NOT NULL,
    PROJECTID BIGINT NOT NULL
); 
ALTER TABLE PUBLIC.HPROJECT_MAINTAINER ADD CONSTRAINT PUBLIC.CONSTRAINT_35 PRIMARY KEY(PROJECTID, PERSONID);   
-- 1 +/- SELECT COUNT(*) FROM PUBLIC.HPROJECT_MAINTAINER;      
INSERT INTO PUBLIC.HPROJECT_MAINTAINER(PERSONID, PROJECTID) VALUES
(1, 1);     
CREATE CACHED TABLE PUBLIC.HSIMPLECOMMENT(
    ID BIGINT DEFAULT (NEXT VALUE FOR PUBLIC.SYSTEM_SEQUENCE_237D196E_0F89_425D_8EC6_235AB3EAAC6F) NOT NULL NULL_TO_DEFAULT SEQUENCE PUBLIC.SYSTEM_SEQUENCE_237D196E_0F89_425D_8EC6_235AB3EAAC6F,
    COMMENT CLOB NOT NULL
);      
ALTER TABLE PUBLIC.HSIMPLECOMMENT ADD CONSTRAINT PUBLIC.CONSTRAINT_9 PRIMARY KEY(ID);          
-- 42 +/- SELECT COUNT(*) FROM PUBLIC.HSIMPLECOMMENT;          
INSERT INTO PUBLIC.HSIMPLECOMMENT(ID, COMMENT) VALUES
(1, 'Tag: para'),
(2, 'Tag: para'),
(3, 'Tag: title'),
(4, 'Tag: para'),
(5, 'Tag: title'),
(6, 'Tag: para'),
(7, 'Tag: title'),
(8, 'Tag: para'),
(9, 'Tag: title'),
(10, 'Tag: para'),
(11, 'Tag: title'),
(12, 'Tag: para'),
(13, 'Tag: title'),
(14, 'Tag: para'),
(15, 'Tag: title'),
(16, 'Tag: para'),
(17, 'Tag: para'),
(18, 'Tag: para'),
(19, 'Tag: para'),
(20, 'Tag: para'),
(21, 'Tag: title'),
(22, 'Tag: para'),
(23, 'Tag: title'),
(24, ''),
(25, ''),
(26, 'Tag: para'),
(27, ''),
(28, STRINGDECODE('\nAUTHOR <EMAIL@ADDRESS>, YEAR.\n')),
(29, 'Tag: title'),
(30, 'Tag: subtitle'),
(31, 'Tag: productname'),
(32, 'Tag: para'),
(33, STRINGDECODE('\nAUTHOR <EMAIL@ADDRESS>, YEAR.\n')),
(34, 'Tag: firstname'),
(35, 'Tag: othername'),
(36, 'Tag: surname'),
(37, STRINGDECODE('\nAUTHOR <EMAIL@ADDRESS>, YEAR.\n')),
(38, 'Tag: title'),
(39, 'Tag: firstname'),
(40, 'Tag: surname'),
(41, 'Tag: member'),
(42, STRINGDECODE('\nAUTHOR <EMAIL@ADDRESS>, YEAR.\n'));           
CREATE CACHED TABLE PUBLIC.HPROJECT_VALIDATION(
    PROJECTID BIGINT NOT NULL,
    VALIDATION VARCHAR(100) NOT NULL
);         
ALTER TABLE PUBLIC.HPROJECT_VALIDATION ADD CONSTRAINT PUBLIC.CONSTRAINT_DF PRIMARY KEY(PROJECTID, VALIDATION); 
-- 0 +/- SELECT COUNT(*) FROM PUBLIC.HPROJECT_VALIDATION;      
CREATE CACHED TABLE PUBLIC.HTEXTFLOWTARGETHISTORY(
    ID BIGINT DEFAULT (NEXT VALUE FOR PUBLIC.SYSTEM_SEQUENCE_EA725082_6ECC_4D7E_BF87_903C38282725) NOT NULL NULL_TO_DEFAULT SEQUENCE PUBLIC.SYSTEM_SEQUENCE_EA725082_6ECC_4D7E_BF87_903C38282725,
    LASTCHANGED TIMESTAMP,
    STATE INTEGER,
    TF_REVISION INTEGER,
    VERSIONNUM INTEGER,
    LAST_MODIFIED_BY_ID BIGINT,
    TARGET_ID BIGINT,
    TRANSLATED_BY_ID BIGINT,
    REVIEWED_BY_ID BIGINT
);            
ALTER TABLE PUBLIC.HTEXTFLOWTARGETHISTORY ADD CONSTRAINT PUBLIC.CONSTRAINT_32 PRIMARY KEY(ID); 
-- 0 +/- SELECT COUNT(*) FROM PUBLIC.HTEXTFLOWTARGETHISTORY;   
CREATE CACHED TABLE PUBLIC.HTEXTFLOW(
    ID BIGINT DEFAULT (NEXT VALUE FOR PUBLIC.SYSTEM_SEQUENCE_D2605C38_87CB_4B62_BBF4_606092B88E99) NOT NULL NULL_TO_DEFAULT SEQUENCE PUBLIC.SYSTEM_SEQUENCE_D2605C38_87CB_4B62_BBF4_606092B88E99,
    OBSOLETE BIT NOT NULL,
    POS INTEGER NOT NULL,
    RESID VARCHAR(255) NOT NULL,
    REVISION INTEGER NOT NULL,
    COMMENT_ID BIGINT,
    DOCUMENT_ID BIGINT NOT NULL,
    POTENTRYDATA_ID BIGINT,
    WORDCOUNT BIGINT,
    CONTENTHASH CHAR(32) NOT NULL,
    PLURAL BOOLEAN,
    CONTENT0 LONGTEXT,
    CONTENT1 LONGTEXT,
    CONTENT2 LONGTEXT,
    CONTENT3 LONGTEXT,
    CONTENT4 LONGTEXT,
    CONTENT5 LONGTEXT
);      
-- 38 +/- SELECT COUNT(*) FROM PUBLIC.HTEXTFLOW;               
INSERT INTO PUBLIC.HTEXTFLOW(ID, OBSOLETE, POS, RESID, REVISION, COMMENT_ID, DOCUMENT_ID, POTENTRYDATA_ID, WORDCOUNT, CONTENTHASH, PLURAL, CONTENT0, CONTENT1, CONTENT2, CONTENT3, CONTENT4, CONTENT5) VALUES
(1, FALSE, 0, 'd033787962c24b1dc3e00316c86e578c', 1, 1, 1, 1, 65, 'd033787962c24b1dc3e00316c86e578c', FALSE, 'Fedora is an open, innovative, forward looking operating system and platform, based on Linux, that is always free for anyone to use, modify and distribute, now and forever. It is developed by a large community of people who strive to provide and maintain the very best in free, open source software and standards. Fedora is part of the Fedora Project, sponsored by Red Hat, Inc.', NULL, NULL, NULL, NULL, NULL),
(2, FALSE, 1, '2211ed6e04756e55820fba941bd2e034', 1, 2, 1, 2, 6, '2211ed6e04756e55820fba941bd2e034', FALSE, 'Visit the Fedora community Wiki at <ulink url="http://fedoraproject.org/wiki/" />.', NULL, NULL, NULL, NULL, NULL),
(3, FALSE, 2, '781235c768fbde76b4924525a1fae846', 1, 3, 1, 3, 2, '781235c768fbde76b4924525a1fae846', FALSE, 'Fedora Documentation', NULL, NULL, NULL, NULL, NULL),
(4, FALSE, 3, 'e90fbcbf5ee54a7e2efbae6d0d3947b0', 1, 4, 1, 4, 32, 'e90fbcbf5ee54a7e2efbae6d0d3947b0', FALSE, 'The Fedora Documentation Project provides 100% Free/Libre Open Source Software (FLOSS) content, services, and tools for documentation. We welcome volunteers and contributors of all skill levels. Visit our Web page at <ulink url="http://fedoraproject.org/wiki/DocsProject" />.', NULL, NULL, NULL, NULL, NULL),
(5, FALSE, 4, '8dbb4e858d76a47458bbd0519cc7a427', 1, 5, 1, 5, 2, '8dbb4e858d76a47458bbd0519cc7a427', FALSE, 'Fedora Translation', NULL, NULL, NULL, NULL, NULL),
(6, FALSE, 5, 'ada3b592395e2014a52c697c1e55755d', 1, 6, 1, 6, 24, 'ada3b592395e2014a52c697c1e55755d', FALSE, 'The goal of the Translation Project is to translate the software and the documentation associated with the Fedora Project. Visit our Web page at <ulink url="http://fedoraproject.org/wiki/Translation" />.', NULL, NULL, NULL, NULL, NULL),
(7, FALSE, 6, '748b7ddcf8192b9dc419b8e45612217e', 1, 7, 1, 7, 3, '748b7ddcf8192b9dc419b8e45612217e', FALSE, 'Fedora Bug Squad', NULL, NULL, NULL, NULL, NULL),
(8, FALSE, 7, 'ec5660dc6375ef1db694a633e9442d24', 1, 8, 1, 8, 36, 'ec5660dc6375ef1db694a633e9442d24', FALSE, 'The primary mission of the Fedora Bug Squad is to track down and clear bugs in <ulink url="https://bugzilla.redhat.com/bugzilla">Bugzilla</ulink> that are related to Fedora, and act as a bridge between users and developers. Visit our Web page at <ulink url="http://fedoraproject.org/wiki/BugZappers" />.', NULL, NULL, NULL, NULL, NULL),
(9, FALSE, 8, 'eeb1808b01779907fff7c0d91263492f', 1, 9, 1, 9, 2, 'eeb1808b01779907fff7c0d91263492f', FALSE, 'Fedora Marketing', NULL, NULL, NULL, NULL, NULL),
(10, FALSE, 9, 'b7fc137b0ef6893a48cfa2f87f6ae6a9', 1, 10, 1, 10, 33, 'b7fc137b0ef6893a48cfa2f87f6ae6a9', FALSE, 'The Fedora Marketing Project is the Fedora Project&#39;s public voice. Our goal is to promote Fedora and to help promote other Linux and open source projects. Visit our Web page at <ulink url="http://fedoraproject.org/wiki/Marketing" />.', NULL, NULL, NULL, NULL, NULL),
(11, FALSE, 10, '5a3e17c5be59b22c46112f572fe3861e', 1, 11, 1, 11, 2, '5a3e17c5be59b22c46112f572fe3861e', FALSE, 'Fedora Ambassadors', NULL, NULL, NULL, NULL, NULL),
(12, FALSE, 11, '4b91ba3fb1bbe3f7129368f842e609cb', 1, 12, 1, 12, 32, '4b91ba3fb1bbe3f7129368f842e609cb', FALSE, 'Fedora Ambassadors are people who go to places where other Linux users and potential converts gather and tell them about Fedora &mdash; the project and the distribution. Visit our Web page at <ulink url="http://fedoraproject.org/wiki/Ambassadors" />.', NULL, NULL, NULL, NULL, NULL),
(13, FALSE, 12, 'b76cbfcd38cf05446d550e7491028c04', 1, 13, 1, 13, 2, 'b76cbfcd38cf05446d550e7491028c04', FALSE, 'Fedora Infrastructure', NULL, NULL, NULL, NULL, NULL),
(14, FALSE, 13, '743086d7352c204a6034f20aa9f2f5f9', 1, 14, 1, 14, 47, '743086d7352c204a6034f20aa9f2f5f9', FALSE, 'The Fedora Infrastructure Project is about helping all Fedora contributors get their work done with minimum hassle and maximum efficiency. Information technology this umbrella include the build system, the <ulink url="http://fedoraproject.org/wiki/Infrastructure/AccountSystem">Fedora Account System</ulink>, the <ulink url="http://cvs.fedoraproject.org/">CVS repositories</ulink>, the <ulink url="http://fedoraproject.org/wiki/Communicate">mailing lists</ulink>, and the <ulink url="http://fedoraproject.org/wiki/Websites">Websites</ulink> infrastructure. Visit our Web site at <ulink url="http://fedoraproject.org/wiki/Infrastructure" />.', NULL, NULL, NULL, NULL, NULL);         
INSERT INTO PUBLIC.HTEXTFLOW(ID, OBSOLETE, POS, RESID, REVISION, COMMENT_ID, DOCUMENT_ID, POTENTRYDATA_ID, WORDCOUNT, CONTENTHASH, PLURAL, CONTENT0, CONTENT1, CONTENT2, CONTENT3, CONTENT4, CONTENT5) VALUES
(15, FALSE, 14, 'd0e9f3d321681a7255515e7551900f45', 1, 15, 1, 15, 2, 'd0e9f3d321681a7255515e7551900f45', FALSE, 'Fedora Websites', NULL, NULL, NULL, NULL, NULL),
(16, FALSE, 15, '091907c3f95ffc807ca28848876787db', 1, 16, 1, 16, 21, '091907c3f95ffc807ca28848876787db', FALSE, 'The Fedora Websites initiative aims to improve Fedora&#39;s image on the Internet. The key goals of this effort include:', NULL, NULL, NULL, NULL, NULL),
(17, FALSE, 16, '801c86730e4227965a77bd9d42af70ad', 1, 17, 1, 17, 12, '801c86730e4227965a77bd9d42af70ad', FALSE, 'Trying to consolidate all the key Fedora websites onto one uniform scheme', NULL, NULL, NULL, NULL, NULL),
(18, FALSE, 17, '3d82c7d587d408dcdfea1526961ee248', 1, 18, 1, 18, 12, '3d82c7d587d408dcdfea1526961ee248', FALSE, 'Maintaining the content that doesn&#39;t fall under any particular sub-project', NULL, NULL, NULL, NULL, NULL),
(19, FALSE, 18, '985c1ef8a7186375a6d9f71e39a29dfb', 1, 19, 1, 19, 13, '985c1ef8a7186375a6d9f71e39a29dfb', FALSE, 'Generally, making the sites as fun and exciting as the project they represent!', NULL, NULL, NULL, NULL, NULL),
(20, FALSE, 19, '55d0cab9c57f9e71cd2ed168863fb0ff', 1, 20, 1, 20, 5, '55d0cab9c57f9e71cd2ed168863fb0ff', FALSE, 'Visit our Web page at <ulink url="http://fedoraproject.org/wiki/Websites" />.', NULL, NULL, NULL, NULL, NULL),
(21, FALSE, 20, '7b55551499be579f29edc6169c103bfa', 1, 21, 1, 21, 2, '7b55551499be579f29edc6169c103bfa', FALSE, 'Fedora Artwork', NULL, NULL, NULL, NULL, NULL),
(22, FALSE, 21, 'feb98248bc9ad2b7383dadf90e00338f', 1, 22, 1, 22, 28, 'feb98248bc9ad2b7383dadf90e00338f', FALSE, 'Making things look pretty is the name of the game. Icons, desktop backgrounds, and themes are all parts of the Fedora Artwork Project. Visit our Web page at <ulink url="http://fedoraproject.org/wiki/Artwork" />.', NULL, NULL, NULL, NULL, NULL),
(23, FALSE, 22, 'cc6732177c183110e5dff6205185d7cb', 1, 23, 1, 23, 2, 'cc6732177c183110e5dff6205185d7cb', FALSE, 'Planet Fedora', NULL, NULL, NULL, NULL, NULL),
(24, FALSE, 23, 'ed8e041cc32690a6a41f3d68bd4eaffa', 1, 24, 1, 24, 4, 'ed8e041cc32690a6a41f3d68bd4eaffa', FALSE, 'Planet Fedora <tag-name>blah blah</tag-name>', NULL, NULL, NULL, NULL, NULL),
(25, FALSE, 24, 'c1625b1cd8b74f7cae20f8ff7efb0242', 1, 25, 1, 25, 0, 'c1625b1cd8b74f7cae20f8ff7efb0242', FALSE, '<![CDATA[<?xml version="1.0" encoding="UTF-8"?>', NULL, NULL, NULL, NULL, NULL),
(26, FALSE, 25, '2beeaaa81b957c2ca893ce1415601103', 1, 26, 1, 26, 12, '2beeaaa81b957c2ca893ce1415601103', FALSE, 'You can read weblogs of many Fedora contributors at our official aggregator, <ulink url="http://planet.fedoraproject.org/" />.', NULL, NULL, NULL, NULL, NULL),
(27, FALSE, 26, '66004b5cb80dab9e5a16e45c83cb0f34', 1, 27, 1, 27, 78, '66004b5cb80dab9e5a16e45c83cb0f34', FALSE, 'JBoss EAP 6  allows you to package Hibernate 3.5 (or greater) persistence provider jars with the application. To direct the server to use only the Hibernate 3 libraries and to exclude the Hibernate 4 libraries, you need to set the <Pcode>jboss.as.jpa.providerModule</code> to <literal>hibernate3-bundled</literal> in the <filename>persistence.xml</filename> as follows: <programlisting language="XML"><![CDATA[<?xml version="1.0" encoding="UTF-8"?><persistence xmlns="http://java.sun.com/xml/ns/persistence" version="1.0">    <persistence-unit name="plannerdatasource_pu">        <description>Hibernate 3 Persistence Unit.</description>        <jta-data-source>java:jboss/datasources/PlannerDS</jta-data-source>        <properties>            <property name="hibernate.show_sql" value="false" />            <property name="jboss.as.jpa.providerModule" value="hibernate3-bundled" />        </properties>    </persistence-unit></persistence>]]>          </programlisting> The Java Persistence API (JPA) deployer will detect the presence of a persistence provider in the application and use the Hibernate 3 libraries.]}', NULL, NULL, NULL, NULL, NULL);        
INSERT INTO PUBLIC.HTEXTFLOW(ID, OBSOLETE, POS, RESID, REVISION, COMMENT_ID, DOCUMENT_ID, POTENTRYDATA_ID, WORDCOUNT, CONTENTHASH, PLURAL, CONTENT0, CONTENT1, CONTENT2, CONTENT3, CONTENT4, CONTENT5) VALUES
(28, FALSE, 0, '361d47f91f01eb0f9434696906f04a98', 1, 29, 2, 28, 2, '361d47f91f01eb0f9434696906f04a98', FALSE, 'About Fedora', NULL, NULL, NULL, NULL, NULL),
(29, FALSE, 1, '6ecee8b9ebb17c00ea7f982fc8d9302e', 1, 30, 2, 29, 9, '6ecee8b9ebb17c00ea7f982fc8d9302e', FALSE, 'Fedora, the Fedora Project, and how you can help.', NULL, NULL, NULL, NULL, NULL),
(30, FALSE, 2, 'f12528995e5edd96b95dd904444dea04', 1, 31, 2, 30, 1, 'f12528995e5edd96b95dd904444dea04', FALSE, 'Fedora', NULL, NULL, NULL, NULL, NULL),
(31, FALSE, 3, '5e7bb40de6e6c319f27a9f7fa11e7bc1', 1, 32, 2, 31, 10, '5e7bb40de6e6c319f27a9f7fa11e7bc1', FALSE, 'Describes Fedora, the Fedora Project, and how you can help.', NULL, NULL, NULL, NULL, NULL),
(32, FALSE, 0, 'c13e13da2073260c2194c15d782e86a9', 1, 34, 3, 32, 1, 'c13e13da2073260c2194c15d782e86a9', FALSE, 'Paul', NULL, NULL, NULL, NULL, NULL),
(33, FALSE, 1, '09ff4f6721aa47fba648e7ebfa50fad8', 1, 35, 3, 33, 1, '09ff4f6721aa47fba648e7ebfa50fad8', FALSE, 'W.', NULL, NULL, NULL, NULL, NULL),
(34, FALSE, 2, '9977ab23acdb18e37082598af0582735', 1, 36, 3, 34, 1, '9977ab23acdb18e37082598af0582735', FALSE, 'Frields', NULL, NULL, NULL, NULL, NULL),
(35, FALSE, 0, 'e0583393371074d791e836b0087dcbae', 1, 38, 4, 35, 2, 'e0583393371074d791e836b0087dcbae', FALSE, 'Revision History', NULL, NULL, NULL, NULL, NULL),
(36, FALSE, 1, '1e24dbee1c6059f3d0a10a737da8ba83', 1, 39, 4, 36, 1, '1e24dbee1c6059f3d0a10a737da8ba83', FALSE, STRINGDECODE('R\u00fcdiger'), NULL, NULL, NULL, NULL, NULL),
(37, FALSE, 2, '0e691f16eedcf75c08ae4329b4a27ace', 1, 40, 4, 37, 1, '0e691f16eedcf75c08ae4329b4a27ace', FALSE, 'Landmann', NULL, NULL, NULL, NULL, NULL),
(38, FALSE, 3, 'cc1c5fd994a2f92fb2baeafac7f3758c', 1, 41, 4, 38, 3, 'cc1c5fd994a2f92fb2baeafac7f3758c', FALSE, 'Update for F13', NULL, NULL, NULL, NULL, NULL);
CREATE INDEX PUBLIC.IDX_CONTENTHASH ON PUBLIC.HTEXTFLOW(CONTENTHASH);          
CREATE CACHED TABLE PUBLIC.HCOPYTRANSOPTIONS(
    CONTEXTMISMATCHACTION CHAR(1) NOT NULL,
    DOCIDMISMATCHACTION CHAR(1) NOT NULL,
    PROJECTMISMATCHACTION CHAR(1) NOT NULL,
    ID BIGINT DEFAULT (NEXT VALUE FOR PUBLIC.SYSTEM_SEQUENCE_397EA988_B34A_4EF4_B7C5_B83F4C043D71) NOT NULL NULL_TO_DEFAULT SEQUENCE PUBLIC.SYSTEM_SEQUENCE_397EA988_B34A_4EF4_B7C5_B83F4C043D71,
    CREATIONDATE TIMESTAMP NOT NULL,
    LASTCHANGED TIMESTAMP NOT NULL,
    VERSIONNUM INT NOT NULL
);      
ALTER TABLE PUBLIC.HCOPYTRANSOPTIONS ADD CONSTRAINT PUBLIC.CONSTRAINT_ED PRIMARY KEY(ID);      
-- 0 +/- SELECT COUNT(*) FROM PUBLIC.HCOPYTRANSOPTIONS;        
CREATE CACHED TABLE PUBLIC.HACCOUNT(
    ID BIGINT DEFAULT (NEXT VALUE FOR PUBLIC.SYSTEM_SEQUENCE_2859F01D_3D5A_4B75_B09B_1EA847883279) NOT NULL NULL_TO_DEFAULT SEQUENCE PUBLIC.SYSTEM_SEQUENCE_2859F01D_3D5A_4B75_B09B_1EA847883279,
    CREATIONDATE TIMESTAMP NOT NULL,
    LASTCHANGED TIMESTAMP NOT NULL,
    VERSIONNUM INTEGER NOT NULL,
    APIKEY VARCHAR(32),
    ENABLED BIT NOT NULL,
    PASSWORDHASH VARCHAR(255),
    USERNAME VARCHAR(255),
    MERGEDINTO BIGINT
);          
ALTER TABLE PUBLIC.HACCOUNT ADD CONSTRAINT PUBLIC.CONSTRAINT_1 PRIMARY KEY(ID);
-- 4 +/- SELECT COUNT(*) FROM PUBLIC.HACCOUNT; 
INSERT INTO PUBLIC.HACCOUNT(ID, CREATIONDATE, LASTCHANGED, VERSIONNUM, APIKEY, ENABLED, PASSWORDHASH, USERNAME, MERGEDINTO) VALUES
(1, TIMESTAMP '2012-04-03 15:06:28.0', TIMESTAMP '2012-04-03 15:06:28.0', 1, 'b6d7044e9ee3b2447c28fb7c50d86d98', TRUE, 'Eyox7xbNQ09MkIfRyH+rjg==', 'admin', NULL),
(2, TIMESTAMP '2012-04-03 15:06:28.0', TIMESTAMP '2012-04-03 15:06:28.0', 1, 'd83882201764f7d339e97c4b087f0806', TRUE, 'Fr5JHlcaEqKLSHjnBm4gXg==', 'translator', NULL),
(3, TIMESTAMP '2013-06-24 15:41:33.0', TIMESTAMP '2013-06-24 16:02:38.292', 2, 'b1e3daa18e41c0ce79829e87ce66b201', TRUE, 'fRIeiPDPlSMtHbBNoqDjNQ==', 'glossarist', NULL),
(4, TIMESTAMP '2013-06-25 11:43:28.294', TIMESTAMP '2013-06-25 15:27:46.491', 5, '5a6a34d28d39ff90ea47402311f339d4', TRUE, '/W0YpteXk+WtymQ7H84kPQ==', 'glossaryadmin', NULL);         
CREATE CACHED TABLE PUBLIC.HACCOUNTROLE(
    ID INTEGER DEFAULT (NEXT VALUE FOR PUBLIC.SYSTEM_SEQUENCE_CABC6C67_570F_4ED0_B44B_3B742AA1AAEC) NOT NULL NULL_TO_DEFAULT SEQUENCE PUBLIC.SYSTEM_SEQUENCE_CABC6C67_570F_4ED0_B44B_3B742AA1AAEC,
    CONDITIONAL BIT NOT NULL,
    NAME VARCHAR(255),
    ROLETYPE CHAR(1) NOT NULL
);              
ALTER TABLE PUBLIC.HACCOUNTROLE ADD CONSTRAINT PUBLIC.CONSTRAINT_D PRIMARY KEY(ID);            
-- 5 +/- SELECT COUNT(*) FROM PUBLIC.HACCOUNTROLE;             
INSERT INTO PUBLIC.HACCOUNTROLE(ID, CONDITIONAL, NAME, ROLETYPE) VALUES
(1, FALSE, 'user', 'M'),
(2, FALSE, 'glossarist', 'M'),
(3, FALSE, 'glossary-admin', 'M'),
(4, FALSE, 'admin', 'M'),
(5, FALSE, 'translator', 'M');    
CREATE CACHED TABLE PUBLIC.HPROJECTITERATION_LOCALE(
    PROJECTITERATIONID BIGINT NOT NULL,
    LOCALEID BIGINT NOT NULL
);   
ALTER TABLE PUBLIC.HPROJECTITERATION_LOCALE ADD CONSTRAINT PUBLIC.CONSTRAINT_7 PRIMARY KEY(PROJECTITERATIONID, LOCALEID);      
-- 0 +/- SELECT COUNT(*) FROM PUBLIC.HPROJECTITERATION_LOCALE; 
CREATE CACHED TABLE PUBLIC.HPROJECTITERATION_VALIDATION(
    PROJECTITERATIONID BIGINT NOT NULL,
    VALIDATION VARCHAR(100) NOT NULL
);       
ALTER TABLE PUBLIC.HPROJECTITERATION_VALIDATION ADD CONSTRAINT PUBLIC.CONSTRAINT_87 PRIMARY KEY(PROJECTITERATIONID, VALIDATION);               
-- 0 +/- SELECT COUNT(*) FROM PUBLIC.HPROJECTITERATION_VALIDATION;             
CREATE CACHED TABLE PUBLIC.HLOCALE_MEMBER(
    PERSONID BIGINT NOT NULL,
    SUPPORTEDLANGUAGEID BIGINT NOT NULL,
    ISCOORDINATOR BOOLEAN DEFAULT FALSE NOT NULL,
    ISREVIEWER BOOLEAN DEFAULT FALSE NOT NULL,
    ISTRANSLATOR BOOLEAN DEFAULT FALSE NOT NULL
);          
ALTER TABLE PUBLIC.HLOCALE_MEMBER ADD CONSTRAINT PUBLIC.CONSTRAINT_E PRIMARY KEY(SUPPORTEDLANGUAGEID, PERSONID);               
-- 3 +/- SELECT COUNT(*) FROM PUBLIC.HLOCALE_MEMBER;           
INSERT INTO PUBLIC.HLOCALE_MEMBER(PERSONID, SUPPORTEDLANGUAGEID, ISCOORDINATOR, ISREVIEWER, ISTRANSLATOR) VALUES
(2, 2, FALSE, FALSE, FALSE),
(2, 3, FALSE, FALSE, TRUE),
(2, 4, FALSE, FALSE, FALSE);         
CREATE CACHED TABLE PUBLIC.HTEXTFLOWTARGET(
    ID BIGINT DEFAULT (NEXT VALUE FOR PUBLIC.SYSTEM_SEQUENCE_79179848_56F7_4E3D_B30B_97780C5EF1D9) NOT NULL NULL_TO_DEFAULT SEQUENCE PUBLIC.SYSTEM_SEQUENCE_79179848_56F7_4E3D_B30B_97780C5EF1D9,
    CREATIONDATE TIMESTAMP NOT NULL,
    LASTCHANGED TIMESTAMP NOT NULL,
    VERSIONNUM INTEGER NOT NULL,
    STATE INTEGER NOT NULL,
    TF_REVISION INTEGER NOT NULL,
    COMMENT_ID BIGINT,
    LAST_MODIFIED_BY_ID BIGINT,
    LOCALE BIGINT NOT NULL,
    TF_ID BIGINT,
    CONTENT0 LONGTEXT,
    CONTENT1 LONGTEXT,
    CONTENT2 LONGTEXT,
    CONTENT3 LONGTEXT,
    CONTENT4 LONGTEXT,
    CONTENT5 LONGTEXT,
    TRANSLATED_BY_ID BIGINT,
    REVIEWED_BY_ID BIGINT
); 
ALTER TABLE PUBLIC.HTEXTFLOWTARGET ADD CONSTRAINT PUBLIC.CONSTRAINT_98 PRIMARY KEY(ID);        
-- 27 +/- SELECT COUNT(*) FROM PUBLIC.HTEXTFLOWTARGET;         
INSERT INTO PUBLIC.HTEXTFLOWTARGET(ID, CREATIONDATE, LASTCHANGED, VERSIONNUM, STATE, TF_REVISION, COMMENT_ID, LAST_MODIFIED_BY_ID, LOCALE, TF_ID, CONTENT0, CONTENT1, CONTENT2, CONTENT3, CONTENT4, CONTENT5, TRANSLATED_BY_ID, REVIEWED_BY_ID) VALUES
(1, TIMESTAMP '2013-08-02 13:51:28.913', TIMESTAMP '2013-08-02 13:51:28.913', 1, 2, 1, NULL, 1, 4, 1, 'Fedora is an open, innovative, forward looking operating system and platform, based on Linux, that is always free for anyone to use, modify and distribute, now and forever. It is developed by a large community of people who strive to provide and maintain the very best in free, open source software and standards. Fedora is part of the Fedora Project, sponsored by Red Hat, Inc.', NULL, NULL, NULL, NULL, NULL, 1, NULL),
(2, TIMESTAMP '2013-08-02 13:51:31.014', TIMESTAMP '2013-08-02 13:51:31.014', 1, 2, 1, NULL, 1, 4, 2, 'Visit the Fedora community Wiki at <ulink url="http://fedoraproject.org/wiki/" />.', NULL, NULL, NULL, NULL, NULL, 1, NULL),
(3, TIMESTAMP '2013-08-02 13:51:33.5', TIMESTAMP '2013-08-02 13:51:33.5', 1, 2, 1, NULL, 1, 4, 3, 'Fedora Documentation', NULL, NULL, NULL, NULL, NULL, 1, NULL),
(4, TIMESTAMP '2013-08-02 13:52:20.256', TIMESTAMP '2013-08-02 13:52:20.256', 1, 0, 1, NULL, NULL, 4, 4, '', NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(5, TIMESTAMP '2013-08-02 13:52:20.271', TIMESTAMP '2013-08-02 13:52:20.271', 1, 2, 1, NULL, NULL, 4, 5, 'Fedora Translation', NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(6, TIMESTAMP '2013-08-02 13:52:20.292', TIMESTAMP '2013-08-02 13:52:20.292', 1, 0, 1, NULL, NULL, 4, 6, '', NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(7, TIMESTAMP '2013-08-02 13:52:20.311', TIMESTAMP '2013-08-02 13:52:20.311', 1, 0, 1, NULL, NULL, 4, 7, '', NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(8, TIMESTAMP '2013-08-02 13:52:20.332', TIMESTAMP '2013-08-02 13:52:20.332', 1, 0, 1, NULL, NULL, 4, 8, '', NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(9, TIMESTAMP '2013-08-02 13:52:20.357', TIMESTAMP '2013-08-02 13:52:20.357', 1, 0, 1, NULL, NULL, 4, 9, '', NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(10, TIMESTAMP '2013-08-02 13:52:20.373', TIMESTAMP '2013-08-02 13:52:20.373', 1, 0, 1, NULL, NULL, 4, 10, '', NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(11, TIMESTAMP '2013-08-02 13:52:20.39', TIMESTAMP '2013-08-02 13:52:20.39', 1, 0, 1, NULL, NULL, 4, 11, '', NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(12, TIMESTAMP '2013-08-02 13:52:20.403', TIMESTAMP '2013-08-02 13:52:20.403', 1, 0, 1, NULL, NULL, 4, 12, '', NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(13, TIMESTAMP '2013-08-02 13:52:20.417', TIMESTAMP '2013-08-02 13:52:20.417', 1, 0, 1, NULL, NULL, 4, 13, '', NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(14, TIMESTAMP '2013-08-02 13:52:20.432', TIMESTAMP '2013-08-02 13:52:20.432', 1, 0, 1, NULL, NULL, 4, 14, '', NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(15, TIMESTAMP '2013-08-02 13:52:20.448', TIMESTAMP '2013-08-02 13:52:20.448', 1, 0, 1, NULL, NULL, 4, 15, '', NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(16, TIMESTAMP '2013-08-02 13:52:20.464', TIMESTAMP '2013-08-02 13:52:20.464', 1, 0, 1, NULL, NULL, 4, 16, '', NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(17, TIMESTAMP '2013-08-02 13:52:20.48', TIMESTAMP '2013-08-02 13:52:20.48', 1, 0, 1, NULL, NULL, 4, 17, '', NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(18, TIMESTAMP '2013-08-02 13:52:20.499', TIMESTAMP '2013-08-02 13:52:20.499', 1, 0, 1, NULL, NULL, 4, 18, '', NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(19, TIMESTAMP '2013-08-02 13:52:20.514', TIMESTAMP '2013-08-02 13:52:20.514', 1, 0, 1, NULL, NULL, 4, 19, '', NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(20, TIMESTAMP '2013-08-02 13:52:20.533', TIMESTAMP '2013-08-02 13:52:20.533', 1, 0, 1, NULL, NULL, 4, 20, '', NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(21, TIMESTAMP '2013-08-02 13:52:20.552', TIMESTAMP '2013-08-02 13:52:20.552', 1, 0, 1, NULL, NULL, 4, 21, '', NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(22, TIMESTAMP '2013-08-02 13:52:20.567', TIMESTAMP '2013-08-02 13:52:20.567', 1, 0, 1, NULL, NULL, 4, 22, '', NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(23, TIMESTAMP '2013-08-02 13:52:20.581', TIMESTAMP '2013-08-02 13:52:20.581', 1, 0, 1, NULL, NULL, 4, 23, '', NULL, NULL, NULL, NULL, NULL, NULL, NULL);             
INSERT INTO PUBLIC.HTEXTFLOWTARGET(ID, CREATIONDATE, LASTCHANGED, VERSIONNUM, STATE, TF_REVISION, COMMENT_ID, LAST_MODIFIED_BY_ID, LOCALE, TF_ID, CONTENT0, CONTENT1, CONTENT2, CONTENT3, CONTENT4, CONTENT5, TRANSLATED_BY_ID, REVIEWED_BY_ID) VALUES
(24, TIMESTAMP '2013-08-02 13:52:20.595', TIMESTAMP '2013-08-02 13:52:20.595', 1, 0, 1, NULL, NULL, 4, 24, '', NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(25, TIMESTAMP '2013-08-02 13:52:20.635', TIMESTAMP '2013-08-02 13:52:20.635', 1, 0, 1, NULL, NULL, 4, 25, '', NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(26, TIMESTAMP '2013-08-02 13:52:20.652', TIMESTAMP '2013-08-02 13:52:20.652', 1, 0, 1, NULL, NULL, 4, 26, '', NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(27, TIMESTAMP '2013-08-02 13:52:20.67', TIMESTAMP '2013-08-02 13:52:20.67', 1, 0, 1, NULL, NULL, 4, 27, '', NULL, NULL, NULL, NULL, NULL, NULL, NULL);   
CREATE CACHED TABLE PUBLIC.HTEXTFLOWHISTORY(
    ID BIGINT DEFAULT (NEXT VALUE FOR PUBLIC.SYSTEM_SEQUENCE_ECA62EAF_3B88_41D5_8207_CDCAFF596FAE) NOT NULL NULL_TO_DEFAULT SEQUENCE PUBLIC.SYSTEM_SEQUENCE_ECA62EAF_3B88_41D5_8207_CDCAFF596FAE,
    OBSOLETE BIT NOT NULL,
    POS INTEGER,
    REVISION INTEGER,
    TF_ID BIGINT
);           
ALTER TABLE PUBLIC.HTEXTFLOWHISTORY ADD CONSTRAINT PUBLIC.CONSTRAINT_FE PRIMARY KEY(ID);       
-- 0 +/- SELECT COUNT(*) FROM PUBLIC.HTEXTFLOWHISTORY;         
CREATE CACHED TABLE PUBLIC.HTERMCOMMENT(
    ID BIGINT DEFAULT (NEXT VALUE FOR PUBLIC.SYSTEM_SEQUENCE_22FD7714_6107_4A7D_9727_145CA5CFA196) NOT NULL NULL_TO_DEFAULT SEQUENCE PUBLIC.SYSTEM_SEQUENCE_22FD7714_6107_4A7D_9727_145CA5CFA196,
    COMMENT LONGTEXT NOT NULL,
    POS INT NOT NULL,
    GLOSSARYTERMID BIGINT NOT NULL
);          
ALTER TABLE PUBLIC.HTERMCOMMENT ADD CONSTRAINT PUBLIC.CONSTRAINT_3B PRIMARY KEY(ID);           
-- 0 +/- SELECT COUNT(*) FROM PUBLIC.HTERMCOMMENT;             
CREATE CACHED TABLE PUBLIC.HACCOUNTOPTION(
    NAME VARCHAR(255) NOT NULL,
    VALUE LONGTEXT,
    ACCOUNT_ID BIGINT NOT NULL,
    ID BIGINT DEFAULT (NEXT VALUE FOR PUBLIC.SYSTEM_SEQUENCE_F12E281E_B9F3_4A3F_83AF_E148A99F882C) NOT NULL NULL_TO_DEFAULT SEQUENCE PUBLIC.SYSTEM_SEQUENCE_F12E281E_B9F3_4A3F_83AF_E148A99F882C,
    CREATIONDATE TIMESTAMP NOT NULL,
    LASTCHANGED TIMESTAMP NOT NULL,
    VERSIONNUM INT NOT NULL
);       
ALTER TABLE PUBLIC.HACCOUNTOPTION ADD CONSTRAINT PUBLIC.CONSTRAINT_3BE PRIMARY KEY(ID);        
-- 0 +/- SELECT COUNT(*) FROM PUBLIC.HACCOUNTOPTION;           
CREATE CACHED TABLE PUBLIC.HTEXTFLOWCONTENTHISTORY(
    ID BIGINT DEFAULT (NEXT VALUE FOR PUBLIC.SYSTEM_SEQUENCE_5353524A_FF38_4137_B9A1_237693B880D7) NOT NULL NULL_TO_DEFAULT SEQUENCE PUBLIC.SYSTEM_SEQUENCE_5353524A_FF38_4137_B9A1_237693B880D7,
    CONTENT LONGTEXT NOT NULL,
    POS INT NOT NULL,
    TEXT_FLOW_HISTORY_ID BIGINT NOT NULL
);         
ALTER TABLE PUBLIC.HTEXTFLOWCONTENTHISTORY ADD CONSTRAINT PUBLIC.CONSTRAINT_C PRIMARY KEY(ID); 
-- 0 +/- SELECT COUNT(*) FROM PUBLIC.HTEXTFLOWCONTENTHISTORY;  
CREATE CACHED TABLE PUBLIC.HITERATIONGROUP(
    ID BIGINT DEFAULT (NEXT VALUE FOR PUBLIC.SYSTEM_SEQUENCE_63A04B08_CEFC_4031_A7C2_132598EA144D) NOT NULL NULL_TO_DEFAULT SEQUENCE PUBLIC.SYSTEM_SEQUENCE_63A04B08_CEFC_4031_A7C2_132598EA144D,
    NAME VARCHAR(80) NOT NULL,
    SLUG VARCHAR(40) NOT NULL,
    DESCRIPTION VARCHAR(100),
    STATUS CHAR(1) NOT NULL,
    CREATIONDATE TIMESTAMP NOT NULL,
    LASTCHANGED TIMESTAMP NOT NULL,
    VERSIONNUM INT NOT NULL
); 
ALTER TABLE PUBLIC.HITERATIONGROUP ADD CONSTRAINT PUBLIC.CONSTRAINT_DD PRIMARY KEY(ID);        
-- 0 +/- SELECT COUNT(*) FROM PUBLIC.HITERATIONGROUP;          
CREATE CACHED TABLE PUBLIC.HPROJECT(
    ID BIGINT DEFAULT (NEXT VALUE FOR PUBLIC.SYSTEM_SEQUENCE_A5745DF6_79D4_4803_922C_9A94CDE2125F) NOT NULL NULL_TO_DEFAULT SEQUENCE PUBLIC.SYSTEM_SEQUENCE_A5745DF6_79D4_4803_922C_9A94CDE2125F,
    CREATIONDATE TIMESTAMP NOT NULL,
    LASTCHANGED TIMESTAMP NOT NULL,
    VERSIONNUM INTEGER NOT NULL,
    SLUG VARCHAR(40) NOT NULL,
    DESCRIPTION VARCHAR(100),
    HOMECONTENT CLOB,
    NAME VARCHAR(80) NOT NULL,
    OVERRIDELOCALES BOOLEAN NOT NULL,
    STATUS CHAR(1) NOT NULL,
    RESTRICTEDBYROLES BOOLEAN DEFAULT FALSE,
    DEFAULT_COPY_TRANS_OPTS_ID BIGINT,
    DEFAULTPROJECTTYPE VARCHAR(255),
    SOURCEVIEWURL LONGTEXT,
    SOURCECHECKOUTURL LONGTEXT,
    OVERRIDEVALIDATIONS BOOLEAN DEFAULT FALSE
);    
ALTER TABLE PUBLIC.HPROJECT ADD CONSTRAINT PUBLIC.CONSTRAINT_4F PRIMARY KEY(ID);               
-- 1 +/- SELECT COUNT(*) FROM PUBLIC.HPROJECT; 
INSERT INTO PUBLIC.HPROJECT(ID, CREATIONDATE, LASTCHANGED, VERSIONNUM, SLUG, DESCRIPTION, HOMECONTENT, NAME, OVERRIDELOCALES, STATUS, RESTRICTEDBYROLES, DEFAULT_COPY_TRANS_OPTS_ID, DEFAULTPROJECTTYPE, SOURCEVIEWURL, SOURCECHECKOUTURL, OVERRIDEVALIDATIONS) VALUES
(1, TIMESTAMP '2013-06-24 13:49:53.481', TIMESTAMP '2013-06-24 13:49:53.481', 0, 'about-fedora', '', NULL, 'about fedora', FALSE, 'A', FALSE, NULL, 'Podir', '', '', FALSE);            
CREATE CACHED TABLE PUBLIC.HPERSONEMAILVALIDATIONKEY(
    ID BIGINT DEFAULT (NEXT VALUE FOR PUBLIC.SYSTEM_SEQUENCE_BBB37388_F5A2_4CEA_A2A7_B35443A44A4B) NOT NULL NULL_TO_DEFAULT SEQUENCE PUBLIC.SYSTEM_SEQUENCE_BBB37388_F5A2_4CEA_A2A7_B35443A44A4B,
    KEYHASH VARCHAR(32) NOT NULL,
    PERSONID BIGINT NOT NULL,
    EMAIL VARCHAR(255) NOT NULL,
    CREATIONDATE TIMESTAMP NOT NULL
);
ALTER TABLE PUBLIC.HPERSONEMAILVALIDATIONKEY ADD CONSTRAINT PUBLIC.CONSTRAINT_7F PRIMARY KEY(ID);              
-- 0 +/- SELECT COUNT(*) FROM PUBLIC.HPERSONEMAILVALIDATIONKEY;
CREATE CACHED TABLE PUBLIC.HTEXTFLOWTARGETCONTENTHISTORY(
    ID BIGINT DEFAULT (NEXT VALUE FOR PUBLIC.SYSTEM_SEQUENCE_435770F8_300C_4DBA_BB72_232B96E46942) NOT NULL NULL_TO_DEFAULT SEQUENCE PUBLIC.SYSTEM_SEQUENCE_435770F8_300C_4DBA_BB72_232B96E46942,
    CONTENT LONGTEXT NOT NULL,
    POS INT NOT NULL,
    TEXT_FLOW_TARGET_HISTORY_ID BIGINT NOT NULL
);            
ALTER TABLE PUBLIC.HTEXTFLOWTARGETCONTENTHISTORY ADD CONSTRAINT PUBLIC.CONSTRAINT_11 PRIMARY KEY(ID);          
-- 0 +/- SELECT COUNT(*) FROM PUBLIC.HTEXTFLOWTARGETCONTENTHISTORY;            
CREATE CACHED TABLE PUBLIC.HITERATIONGROUP_MAINTAINER(
    ITERATIONGROUPID BIGINT NOT NULL,
    PERSONID BIGINT NOT NULL
);   
ALTER TABLE PUBLIC.HITERATIONGROUP_MAINTAINER ADD CONSTRAINT PUBLIC.CONSTRAINT_68 PRIMARY KEY(ITERATIONGROUPID, PERSONID);     
-- 0 +/- SELECT COUNT(*) FROM PUBLIC.HITERATIONGROUP_MAINTAINER;               
CREATE CACHED TABLE PUBLIC.HITERATIONGROUP_PROJECTITERATION(
    ITERATIONGROUPID BIGINT NOT NULL,
    PROJECTITERATIONID BIGINT NOT NULL
);   
ALTER TABLE PUBLIC.HITERATIONGROUP_PROJECTITERATION ADD CONSTRAINT PUBLIC.CONSTRAINT_C2 PRIMARY KEY(ITERATIONGROUPID, PROJECTITERATIONID);     
-- 0 +/- SELECT COUNT(*) FROM PUBLIC.HITERATIONGROUP_PROJECTITERATION;         
CREATE CACHED TABLE PUBLIC.HLOCALE(
    ID BIGINT DEFAULT (NEXT VALUE FOR PUBLIC.SYSTEM_SEQUENCE_C1BE3B1C_73A3_47E6_B6C4_EB11664C9991) NOT NULL NULL_TO_DEFAULT SEQUENCE PUBLIC.SYSTEM_SEQUENCE_C1BE3B1C_73A3_47E6_B6C4_EB11664C9991,
    CREATIONDATE TIMESTAMP NOT NULL,
    LASTCHANGED TIMESTAMP NOT NULL,
    VERSIONNUM INTEGER NOT NULL,
    ACTIVE BIT NOT NULL,
    LOCALEID VARCHAR(255) NOT NULL,
    ENABLEDBYDEFAULT BOOLEAN DEFAULT FALSE
);     
ALTER TABLE PUBLIC.HLOCALE ADD CONSTRAINT PUBLIC.CONSTRAINT_66 PRIMARY KEY(ID);
-- 4 +/- SELECT COUNT(*) FROM PUBLIC.HLOCALE;  
INSERT INTO PUBLIC.HLOCALE(ID, CREATIONDATE, LASTCHANGED, VERSIONNUM, ACTIVE, LOCALEID, ENABLEDBYDEFAULT) VALUES
(1, TIMESTAMP '2013-06-24 13:46:14.431', TIMESTAMP '2013-06-24 13:46:14.431', 0, TRUE, 'en-US', FALSE),
(2, TIMESTAMP '2013-06-24 13:52:35.82', TIMESTAMP '2013-06-24 13:52:35.82', 0, TRUE, 'hi', TRUE),
(3, TIMESTAMP '2013-06-24 13:52:40.593', TIMESTAMP '2013-06-24 13:52:40.593', 0, TRUE, 'fr', TRUE),
(4, TIMESTAMP '2013-06-24 13:52:46.931', TIMESTAMP '2013-06-24 13:52:46.931', 0, TRUE, 'pl', TRUE);             
CREATE CACHED TABLE PUBLIC.HPROJECTITERATION(
    ID BIGINT DEFAULT (NEXT VALUE FOR PUBLIC.SYSTEM_SEQUENCE_4476349D_CEA3_4CB2_B750_65A31A471046) NOT NULL NULL_TO_DEFAULT SEQUENCE PUBLIC.SYSTEM_SEQUENCE_4476349D_CEA3_4CB2_B750_65A31A471046,
    CREATIONDATE TIMESTAMP NOT NULL,
    LASTCHANGED TIMESTAMP NOT NULL,
    VERSIONNUM INTEGER NOT NULL,
    SLUG VARCHAR(40) NOT NULL,
    PARENTID BIGINT,
    PROJECT_ID BIGINT NOT NULL,
    OVERRIDELOCALES BOOLEAN NOT NULL,
    STATUS CHAR(1) NOT NULL,
    PROJECTTYPE VARCHAR(255),
    OVERRIDEVALIDATIONS BOOLEAN DEFAULT FALSE,
    REQUIRETRANSLATIONREVIEW BOOLEAN DEFAULT FALSE
);            
ALTER TABLE PUBLIC.HPROJECTITERATION ADD CONSTRAINT PUBLIC.CONSTRAINT_FC PRIMARY KEY(ID);      
-- 1 +/- SELECT COUNT(*) FROM PUBLIC.HPROJECTITERATION;        
INSERT INTO PUBLIC.HPROJECTITERATION(ID, CREATIONDATE, LASTCHANGED, VERSIONNUM, SLUG, PARENTID, PROJECT_ID, OVERRIDELOCALES, STATUS, PROJECTTYPE, OVERRIDEVALIDATIONS, REQUIRETRANSLATIONREVIEW) VALUES
(1, TIMESTAMP '2013-06-24 13:50:02.329', TIMESTAMP '2013-06-24 13:50:02.329', 0, 'master', NULL, 1, FALSE, 'A', 'Podir', FALSE, FALSE);
CREATE CACHED TABLE PUBLIC.HDOCUMENT_RAWDOCUMENT(
    DOCUMENTID BIGINT NOT NULL,
    RAWDOCUMENTID BIGINT NOT NULL
);         
ALTER TABLE PUBLIC.HDOCUMENT_RAWDOCUMENT ADD CONSTRAINT PUBLIC.CONSTRAINT_41 PRIMARY KEY(DOCUMENTID, RAWDOCUMENTID);           
-- 0 +/- SELECT COUNT(*) FROM PUBLIC.HDOCUMENT_RAWDOCUMENT;    
CREATE CACHED TABLE PUBLIC.HTEXTFLOWTARGETREVIEWCOMMENT(
    ID BIGINT DEFAULT (NEXT VALUE FOR PUBLIC.SYSTEM_SEQUENCE_706F25FE_B1BA_4E11_9341_00C53A817296) NOT NULL NULL_TO_DEFAULT SEQUENCE PUBLIC.SYSTEM_SEQUENCE_706F25FE_B1BA_4E11_9341_00C53A817296,
    CREATIONDATE TIMESTAMP NOT NULL,
    LASTCHANGED TIMESTAMP NOT NULL,
    COMMENTER_ID BIGINT NOT NULL,
    TARGET_ID BIGINT NOT NULL,
    COMMENT LONGTEXT NOT NULL,
    TARGETVERSION INT NOT NULL,
    VERSIONNUM INT NOT NULL
);             
ALTER TABLE PUBLIC.HTEXTFLOWTARGETREVIEWCOMMENT ADD CONSTRAINT PUBLIC.CONSTRAINT_EC PRIMARY KEY(ID);           
-- 0 +/- SELECT COUNT(*) FROM PUBLIC.HTEXTFLOWTARGETREVIEWCOMMENT;             
CREATE CACHED TABLE PUBLIC.HDOCUMENTUPLOAD(
    PROJECTITERATIONID BIGINT NOT NULL,
    DOCID VARCHAR(255) NOT NULL,
    CONTENTHASH CHAR(32) NOT NULL,
    TYPE VARCHAR(255) NOT NULL,
    LOCALEID BIGINT,
    ID BIGINT DEFAULT (NEXT VALUE FOR PUBLIC.SYSTEM_SEQUENCE_1AAF14F4_E938_44FA_8DAE_3901615B003F) NOT NULL NULL_TO_DEFAULT SEQUENCE PUBLIC.SYSTEM_SEQUENCE_1AAF14F4_E938_44FA_8DAE_3901615B003F,
    CREATIONDATE TIMESTAMP NOT NULL,
    LASTCHANGED TIMESTAMP NOT NULL,
    VERSIONNUM INT NOT NULL
);         
ALTER TABLE PUBLIC.HDOCUMENTUPLOAD ADD CONSTRAINT PUBLIC.CONSTRAINT_5 PRIMARY KEY(ID);         
-- 0 +/- SELECT COUNT(*) FROM PUBLIC.HDOCUMENTUPLOAD;          
CREATE CACHED TABLE PUBLIC.HDOCUMENTUPLOADPART(
    ID BIGINT DEFAULT (NEXT VALUE FOR PUBLIC.SYSTEM_SEQUENCE_D3718621_6075_4F69_9890_C5013A61A5A0) NOT NULL NULL_TO_DEFAULT SEQUENCE PUBLIC.SYSTEM_SEQUENCE_D3718621_6075_4F69_9890_C5013A61A5A0,
    DOCUMENTUPLOADID BIGINT NOT NULL,
    PARTINDEX INT NOT NULL,
    CONTENT LONGVARBINARY NOT NULL
);      
ALTER TABLE PUBLIC.HDOCUMENTUPLOADPART ADD CONSTRAINT PUBLIC.CONSTRAINT_4C PRIMARY KEY(ID);    
-- 0 +/- SELECT COUNT(*) FROM PUBLIC.HDOCUMENTUPLOADPART;      
CREATE CACHED TABLE PUBLIC.HACCOUNTACTIVATIONKEY(
    KEYHASH VARCHAR(32) NOT NULL,
    ACCOUNTID BIGINT NOT NULL,
    CREATIONDATE TIMESTAMP DEFAULT '0002-11-30 00:00:01.0' NOT NULL
);      
ALTER TABLE PUBLIC.HACCOUNTACTIVATIONKEY ADD CONSTRAINT PUBLIC.CONSTRAINT_A PRIMARY KEY(KEYHASH);              
-- 1 +/- SELECT COUNT(*) FROM PUBLIC.HACCOUNTACTIVATIONKEY;    
INSERT INTO PUBLIC.HACCOUNTACTIVATIONKEY(KEYHASH, ACCOUNTID, CREATIONDATE) VALUES
('d053726a8aac0ff26ef19900f6b7833f', 4, TIMESTAMP '2013-06-25 11:43:28.429');
CREATE CACHED TABLE PUBLIC.HRAWDOCUMENT(
    TYPE VARCHAR(255) NOT NULL,
    CONTENTHASH CHAR(32) NOT NULL,
    UPLOADEDBY VARCHAR(255),
    ID BIGINT DEFAULT (NEXT VALUE FOR PUBLIC.SYSTEM_SEQUENCE_FF6FE0BB_F212_43C9_89BF_A2D05A61669D) NOT NULL NULL_TO_DEFAULT SEQUENCE PUBLIC.SYSTEM_SEQUENCE_FF6FE0BB_F212_43C9_89BF_A2D05A61669D,
    CREATIONDATE TIMESTAMP NOT NULL,
    LASTCHANGED TIMESTAMP NOT NULL,
    VERSIONNUM INT NOT NULL,
    ADAPTERPARAMETERS LONGTEXT,
    FILEID LONGTEXT NOT NULL
);               
ALTER TABLE PUBLIC.HRAWDOCUMENT ADD CONSTRAINT PUBLIC.CONSTRAINT_A7 PRIMARY KEY(ID);           
-- 0 +/- SELECT COUNT(*) FROM PUBLIC.HRAWDOCUMENT;             
ALTER TABLE PUBLIC.HDOCUMENTHISTORY ADD CONSTRAINT PUBLIC.CONSTRAINT_42 UNIQUE(DOCUMENT_ID, REVISION);         
ALTER TABLE PUBLIC.HITERATIONGROUP ADD CONSTRAINT PUBLIC.UKSLUG UNIQUE(SLUG);  
ALTER TABLE PUBLIC.HPERSON ADD CONSTRAINT PUBLIC.CONSTRAINT_6D3 UNIQUE(EMAIL); 
ALTER TABLE PUBLIC.HTEXTFLOW ADD CONSTRAINT PUBLIC.CONSTRAINT_A61 UNIQUE(DOCUMENT_ID, RESID);  
ALTER TABLE PUBLIC.HTEXTFLOWTARGETHISTORY ADD CONSTRAINT PUBLIC.CONSTRAINT_32C UNIQUE(TARGET_ID, VERSIONNUM);  
ALTER TABLE PUBLIC.HPOTENTRYDATA ADD CONSTRAINT PUBLIC.CONSTRAINT_209 UNIQUE(TF_ID);           
ALTER TABLE PUBLIC.HAPPLICATIONCONFIGURATION ADD CONSTRAINT PUBLIC.CONSTRAINT_6E UNIQUE(CONFIG_KEY);           
ALTER TABLE PUBLIC.HDOCUMENT ADD CONSTRAINT PUBLIC.CONSTRAINT_154 UNIQUE(DOCID, PROJECT_ITERATION_ID);         
ALTER TABLE PUBLIC.ACTIVITY ADD CONSTRAINT PUBLIC.UKACTIVITY UNIQUE(ACTOR_ID, APPROXTIME, ACTIVITYTYPE, CONTEXTTYPE, CONTEXT_ID);              
ALTER TABLE PUBLIC.HPERSONEMAILVALIDATIONKEY ADD CONSTRAINT PUBLIC.CONSTRAINT_7F91 UNIQUE(KEYHASH);            
ALTER TABLE PUBLIC.HTEXTFLOW ADD CONSTRAINT PUBLIC.UKRESIDDOCUMENT_ID UNIQUE(RESID, DOCUMENT_ID);              
ALTER TABLE PUBLIC.HGLOSSARYTERM ADD CONSTRAINT PUBLIC.UKGLOSSARYENTRYID_LOCALEID UNIQUE(GLOSSARYENTRYID, LOCALEID);           
ALTER TABLE PUBLIC.HPROJECT ADD CONSTRAINT PUBLIC.CONSTRAINT_4FB UNIQUE(SLUG); 
ALTER TABLE PUBLIC.HITERATIONGROUP ADD CONSTRAINT PUBLIC.CONSTRAINT_DD9 UNIQUE(SLUG);          
ALTER TABLE PUBLIC.HTEXTFLOWHISTORY ADD CONSTRAINT PUBLIC.CONSTRAINT_FE2 UNIQUE(REVISION, TF_ID);              
ALTER TABLE PUBLIC.HLOCALE ADD CONSTRAINT PUBLIC.CONSTRAINT_66E UNIQUE(LOCALEID);              
ALTER TABLE PUBLIC.HPERSONEMAILVALIDATIONKEY ADD CONSTRAINT PUBLIC.CONSTRAINT_7F9 UNIQUE(PERSONID);            
ALTER TABLE PUBLIC.HACCOUNTRESETPASSWORDKEY ADD CONSTRAINT PUBLIC.CONSTRAINT_B0 UNIQUE(ACCOUNTID);             
ALTER TABLE PUBLIC.HTEXTFLOWTARGET ADD CONSTRAINT PUBLIC.CONSTRAINT_981 UNIQUE(LOCALE, TF_ID); 
ALTER TABLE PUBLIC.HPOTENTRYDATA ADD CONSTRAINT PUBLIC.CONSTRAINT_2096 UNIQUE(TF_ID);          
ALTER TABLE PUBLIC.HPOTARGETHEADER ADD CONSTRAINT PUBLIC.CONSTRAINT_254 UNIQUE(DOCUMENT_ID, TARGETLANGUAGE);   
ALTER TABLE PUBLIC.HACCOUNTACTIVATIONKEY ADD CONSTRAINT PUBLIC.CONSTRAINT_AE UNIQUE(ACCOUNTID);
ALTER TABLE PUBLIC.HPROJECTITERATION ADD CONSTRAINT PUBLIC.CONSTRAINT_FCE UNIQUE(SLUG, PROJECT_ID);            
ALTER TABLE PUBLIC.HACCOUNT ADD CONSTRAINT PUBLIC.CONSTRAINT_1B UNIQUE(USERNAME);              
ALTER TABLE PUBLIC.HITERATIONGROUP_MAINTAINER ADD CONSTRAINT PUBLIC.FKITERATIONGROUPMAINTAINER_PERSONID FOREIGN KEY(PERSONID) REFERENCES PUBLIC.HPERSON(ID) NOCHECK;           
ALTER TABLE PUBLIC.HITERATIONGROUP_PROJECTITERATION ADD CONSTRAINT PUBLIC.FKITERATIONGROUP_PROJECTITERATION_PROJECTITERATIONID FOREIGN KEY(PROJECTITERATIONID) REFERENCES PUBLIC.HPROJECTITERATION(ID) NOCHECK;
ALTER TABLE PUBLIC.HPROJECT_LOCALE ADD CONSTRAINT PUBLIC.FKHPROJECTLOCALELOC FOREIGN KEY(LOCALEID) REFERENCES PUBLIC.HLOCALE(ID) NOCHECK;      
ALTER TABLE PUBLIC.HACCOUNTOPTION ADD CONSTRAINT PUBLIC.FK_HACCOUNTOPTION_HACCOUNT FOREIGN KEY(ACCOUNT_ID) REFERENCES PUBLIC.HACCOUNT(ID) NOCHECK;             
ALTER TABLE PUBLIC.HTEXTFLOW ADD CONSTRAINT PUBLIC.FK7B40F863B7A40DF2 FOREIGN KEY(COMMENT_ID) REFERENCES PUBLIC.HSIMPLECOMMENT(ID) NOCHECK;    
ALTER TABLE PUBLIC.HPROJECTITERATION_VALIDATION ADD CONSTRAINT PUBLIC.FK_HPROJECTITERATION_VALIDATION_HPROJECTITERATION FOREIGN KEY(PROJECTITERATIONID) REFERENCES PUBLIC.HPROJECTITERATION(ID) NOCHECK;       
ALTER TABLE PUBLIC.HTEXTFLOWTARGET ADD CONSTRAINT PUBLIC.FK1E933FD4CCAD9D19 FOREIGN KEY(TF_ID) REFERENCES PUBLIC.HTEXTFLOW(ID) NOCHECK;        
ALTER TABLE PUBLIC.HPOTARGETHEADER ADD CONSTRAINT PUBLIC.FK1BC719855383E2F0 FOREIGN KEY(DOCUMENT_ID) REFERENCES PUBLIC.HDOCUMENT(ID) NOCHECK;  
ALTER TABLE PUBLIC.HITERATIONGROUP_MAINTAINER ADD CONSTRAINT PUBLIC.FKITERATIONGROUPMAINTAINER_ITERATIONGROUPID FOREIGN KEY(ITERATIONGROUPID) REFERENCES PUBLIC.HITERATIONGROUP(ID) NOCHECK;   
ALTER TABLE PUBLIC.HPROJECT_ALLOWEDROLE ADD CONSTRAINT PUBLIC.FK_HPROJECTALLOWEDROLE_PROJECT FOREIGN KEY(PROJECTID) REFERENCES PUBLIC.HPROJECT(ID) NOCHECK;    
ALTER TABLE PUBLIC.HDOCUMENT ADD CONSTRAINT PUBLIC.FKEA766D83136CC025 FOREIGN KEY(POHEADER_ID) REFERENCES PUBLIC.HPOHEADER(ID) NOCHECK;        
ALTER TABLE PUBLIC.HTEXTFLOW ADD CONSTRAINT PUBLIC.FK7B40F8638D8E70A5 FOREIGN KEY(POTENTRYDATA_ID) REFERENCES PUBLIC.HPOTENTRYDATA(ID) NOCHECK;
ALTER TABLE PUBLIC.HCREDENTIALS ADD CONSTRAINT PUBLIC.FK_CREDENTIALS_ACCOUNT FOREIGN KEY(ACCOUNT_ID) REFERENCES PUBLIC.HACCOUNT(ID) NOCHECK;   
ALTER TABLE PUBLIC.HPERSON ADD CONSTRAINT PUBLIC.FK6F0931BDFA68C45F FOREIGN KEY(ACCOUNTID) REFERENCES PUBLIC.HACCOUNT(ID) NOCHECK;             
ALTER TABLE PUBLIC.HPROJECT_MAINTAINER ADD CONSTRAINT PUBLIC.FK1491F2E660C55B1B FOREIGN KEY(PERSONID) REFERENCES PUBLIC.HPERSON(ID) NOCHECK;   
ALTER TABLE PUBLIC.HPOTENTRYDATA ADD CONSTRAINT PUBLIC.FK17A648CFB7A40DF2 FOREIGN KEY(COMMENT_ID) REFERENCES PUBLIC.HSIMPLECOMMENT(ID) NOCHECK;
ALTER TABLE PUBLIC.HACCOUNTROLEGROUP ADD CONSTRAINT PUBLIC.FK3321CC643E684F5E FOREIGN KEY(MEMBEROF) REFERENCES PUBLIC.HACCOUNTROLE(ID) NOCHECK;
ALTER TABLE PUBLIC.HGLOSSARYTERM ADD CONSTRAINT PUBLIC.UKGLOSSARYTERM_LOCALEID FOREIGN KEY(LOCALEID) REFERENCES PUBLIC.HLOCALE(ID) NOCHECK;    
ALTER TABLE PUBLIC.HPROJECT_ALLOWEDROLE ADD CONSTRAINT PUBLIC.FK_HPROJECTALLOWEDROLE_ROLE FOREIGN KEY(ROLEID) REFERENCES PUBLIC.HACCOUNTROLE(ID) NOCHECK;      
ALTER TABLE PUBLIC.HDOCUMENTHISTORY ADD CONSTRAINT PUBLIC.FK279765915383E2F0 FOREIGN KEY(DOCUMENT_ID) REFERENCES PUBLIC.HDOCUMENT(ID) NOCHECK; 
ALTER TABLE PUBLIC.HLOCALE_MEMBER ADD CONSTRAINT PUBLIC.FK82DF50D760C55B1B FOREIGN KEY(PERSONID) REFERENCES PUBLIC.HPERSON(ID) NOCHECK;        
ALTER TABLE PUBLIC.HTEXTFLOWTARGET ADD CONSTRAINT PUBLIC.FK1E933FD4FEA3B54A FOREIGN KEY(LOCALE) REFERENCES PUBLIC.HLOCALE(ID) NOCHECK;         
ALTER TABLE PUBLIC.HTEXTFLOWTARGETHISTORY ADD CONSTRAINT PUBLIC.FKF10986206C9BADC1 FOREIGN KEY(LAST_MODIFIED_BY_ID) REFERENCES PUBLIC.HPERSON(ID) NOCHECK;     
ALTER TABLE PUBLIC.HDOCUMENT_RAWDOCUMENT ADD CONSTRAINT PUBLIC.FK_HDOCUMENTRAWDOCUMENT_DOCUMENT FOREIGN KEY(DOCUMENTID) REFERENCES PUBLIC.HDOCUMENT(ID) NOCHECK;               
ALTER TABLE PUBLIC.HTEXTFLOWTARGETHISTORY ADD CONSTRAINT PUBLIC.FKF109862080727E8B FOREIGN KEY(TARGET_ID) REFERENCES PUBLIC.HTEXTFLOWTARGET(ID) NOCHECK;       
ALTER TABLE PUBLIC.HDOCUMENTUPLOADPART ADD CONSTRAINT PUBLIC.FK_HDOCUMENTUPLOADPART_DOCUMENTUPLOAD FOREIGN KEY(DOCUMENTUPLOADID) REFERENCES PUBLIC.HDOCUMENTUPLOAD(ID) NOCHECK;
ALTER TABLE PUBLIC.HDOCUMENT ADD CONSTRAINT PUBLIC.FKEA766D8351ED6DFD FOREIGN KEY(PROJECT_ITERATION_ID) REFERENCES PUBLIC.HPROJECTITERATION(ID) NOCHECK;       
ALTER TABLE PUBLIC.HDOCUMENTUPLOAD ADD CONSTRAINT PUBLIC.FK_HDOCUMENTUPLOAD_PROJECTITERATION FOREIGN KEY(PROJECTITERATIONID) REFERENCES PUBLIC.HPROJECTITERATION(ID) NOCHECK;  
ALTER TABLE PUBLIC.HTEXTFLOWCONTENTHISTORY ADD CONSTRAINT PUBLIC.FKCONTENT_TEXT_FLOW_HISTORY FOREIGN KEY(TEXT_FLOW_HISTORY_ID) REFERENCES PUBLIC.HTEXTFLOWHISTORY(ID) NOCHECK; 
ALTER TABLE PUBLIC.HTEXTFLOWTARGET ADD CONSTRAINT PUBLIC.FK1E933FD4B7A40DF2 FOREIGN KEY(COMMENT_ID) REFERENCES PUBLIC.HSIMPLECOMMENT(ID) NOCHECK;              
ALTER TABLE PUBLIC.HDOCUMENT ADD CONSTRAINT PUBLIC.FKEA766D83FEA3B54A FOREIGN KEY(LOCALE) REFERENCES PUBLIC.HLOCALE(ID) NOCHECK;               
ALTER TABLE PUBLIC.HTEXTFLOWTARGET ADD CONSTRAINT PUBLIC.FK1E933FD46C9BADC1 FOREIGN KEY(LAST_MODIFIED_BY_ID) REFERENCES PUBLIC.HPERSON(ID) NOCHECK;            
ALTER TABLE PUBLIC.HACCOUNTMEMBERSHIP ADD CONSTRAINT PUBLIC.FK9D5DB27BFA68C45F FOREIGN KEY(ACCOUNTID) REFERENCES PUBLIC.HACCOUNT(ID) NOCHECK;  
ALTER TABLE PUBLIC.HLOCALE_MEMBER ADD CONSTRAINT PUBLIC.FK82DF50D73A932491 FOREIGN KEY(SUPPORTEDLANGUAGEID) REFERENCES PUBLIC.HLOCALE(ID) NOCHECK;             
ALTER TABLE PUBLIC.HDOCUMENTHISTORY ADD CONSTRAINT PUBLIC.FK279765916C9BADC1 FOREIGN KEY(LAST_MODIFIED_BY_ID) REFERENCES PUBLIC.HPERSON(ID) NOCHECK;           
ALTER TABLE PUBLIC.HPROJECT_VALIDATION ADD CONSTRAINT PUBLIC.FK_HPROJECT_VALIDATION_HPROJECT FOREIGN KEY(PROJECTID) REFERENCES PUBLIC.HPROJECT(ID) NOCHECK;    
ALTER TABLE PUBLIC.HPROJECTITERATION ADD CONSTRAINT PUBLIC.FK31C1E42C5B1D181F FOREIGN KEY(PARENTID) REFERENCES PUBLIC.HPROJECTITERATION(ID) NOCHECK;           
ALTER TABLE PUBLIC.HPROJECT ADD CONSTRAINT PUBLIC.FK_HPROJECT_HCOPYTRANSOPTS FOREIGN KEY(DEFAULT_COPY_TRANS_OPTS_ID) REFERENCES PUBLIC.HCOPYTRANSOPTIONS(ID) NOCHECK;          
ALTER TABLE PUBLIC.HPOTARGETHEADER ADD CONSTRAINT PUBLIC.FK1BC71985B7A40DF2 FOREIGN KEY(COMMENT_ID) REFERENCES PUBLIC.HSIMPLECOMMENT(ID) NOCHECK;              
ALTER TABLE PUBLIC.HTEXTFLOWHISTORY ADD CONSTRAINT PUBLIC.FK46C4DEB1CCAD9D19 FOREIGN KEY(TF_ID) REFERENCES PUBLIC.HTEXTFLOW(ID) NOCHECK;       
ALTER TABLE PUBLIC.HROLEASSIGNMENTRULE ADD CONSTRAINT PUBLIC.FK_HROLEASSIGNMENTRULE_HACCOUNTROLE FOREIGN KEY(ROLE_TO_ASSIGN_ID) REFERENCES PUBLIC.HACCOUNTROLE(ID) NOCHECK;    
ALTER TABLE PUBLIC.HACCOUNT ADD CONSTRAINT PUBLIC.FK_HACCOUNT_MERGEDINTOACCOUNT FOREIGN KEY(MERGEDINTO) REFERENCES PUBLIC.HACCOUNT(ID) NOCHECK;
ALTER TABLE PUBLIC.HPROJECT_MAINTAINER ADD CONSTRAINT PUBLIC.FK1491F2E665B5BB37 FOREIGN KEY(PROJECTID) REFERENCES PUBLIC.HPROJECT(ID) NOCHECK; 
ALTER TABLE PUBLIC.HTEXTFLOW ADD CONSTRAINT PUBLIC.FK7B40F8635383E2F0 FOREIGN KEY(DOCUMENT_ID) REFERENCES PUBLIC.HDOCUMENT(ID) NOCHECK;        
ALTER TABLE PUBLIC.HPOTENTRYDATA ADD CONSTRAINT PUBLIC.FK17A648CFCCAD9D19 FOREIGN KEY(TF_ID) REFERENCES PUBLIC.HTEXTFLOW(ID) NOCHECK;          
ALTER TABLE PUBLIC.HPOTARGETHEADER ADD CONSTRAINT PUBLIC.FK1BC719857D208AD9 FOREIGN KEY(TARGETLANGUAGE) REFERENCES PUBLIC.HLOCALE(ID) NOCHECK; 
ALTER TABLE PUBLIC.HPROJECT_LOCALE ADD CONSTRAINT PUBLIC.FKHPROJECTLOCALEPRO FOREIGN KEY(PROJECTID) REFERENCES PUBLIC.HPROJECT(ID) NOCHECK;    
ALTER TABLE PUBLIC.HITERATIONGROUP_PROJECTITERATION ADD CONSTRAINT PUBLIC.FKITERATIONGROUP_PROJECTITERATION_ITERATIONGROUPID FOREIGN KEY(ITERATIONGROUPID) REFERENCES PUBLIC.HITERATIONGROUP(ID) NOCHECK;      
ALTER TABLE PUBLIC.HPOHEADER ADD CONSTRAINT PUBLIC.FK9A0ABDD4B7A40DF2 FOREIGN KEY(COMMENT_ID) REFERENCES PUBLIC.HSIMPLECOMMENT(ID) NOCHECK;    
ALTER TABLE PUBLIC.HDOCUMENTHISTORY ADD CONSTRAINT PUBLIC.FK27976591FEA3B54A FOREIGN KEY(LOCALE) REFERENCES PUBLIC.HLOCALE(ID) NOCHECK;        
ALTER TABLE PUBLIC.HPROJECTITERATION ADD CONSTRAINT PUBLIC.FK31C1E42C4BCEEA93 FOREIGN KEY(PROJECT_ID) REFERENCES PUBLIC.HPROJECT(ID) NOCHECK;  
ALTER TABLE PUBLIC.HACCOUNTACTIVATIONKEY ADD CONSTRAINT PUBLIC.FK86E79CA4FA68C45F FOREIGN KEY(ACCOUNTID) REFERENCES PUBLIC.HACCOUNT(ID) NOCHECK;               
ALTER TABLE PUBLIC.HGLOSSARYENTRY ADD CONSTRAINT PUBLIC.UKGLOSSARYENTRY_SRCLOCALEID FOREIGN KEY(SRCLOCALEID) REFERENCES PUBLIC.HLOCALE(ID) NOCHECK;            
ALTER TABLE PUBLIC.HPROJECTITERATION_LOCALE ADD CONSTRAINT PUBLIC.FKHPROJECTITELOCLOC FOREIGN KEY(LOCALEID) REFERENCES PUBLIC.HLOCALE(ID) NOCHECK;             
ALTER TABLE PUBLIC.HPROJECTITERATION_LOCALE ADD CONSTRAINT PUBLIC.FKHPROJECTITELOCPRO FOREIGN KEY(PROJECTITERATIONID) REFERENCES PUBLIC.HPROJECTITERATION(ID) NOCHECK;         
ALTER TABLE PUBLIC.HACCOUNTROLEGROUP ADD CONSTRAINT PUBLIC.FK3321CC642DF53D7E FOREIGN KEY(ROLEID) REFERENCES PUBLIC.HACCOUNTROLE(ID) NOCHECK;  
ALTER TABLE PUBLIC.HACCOUNTRESETPASSWORDKEY ADD CONSTRAINT PUBLIC.FK85C9EFDAFA68C45F FOREIGN KEY(ACCOUNTID) REFERENCES PUBLIC.HACCOUNT(ID) NOCHECK;            
ALTER TABLE PUBLIC.HDOCUMENT ADD CONSTRAINT PUBLIC.FKEA766D836C9BADC1 FOREIGN KEY(LAST_MODIFIED_BY_ID) REFERENCES PUBLIC.HPERSON(ID) NOCHECK;  
ALTER TABLE PUBLIC.HPERSONEMAILVALIDATIONKEY ADD CONSTRAINT PUBLIC.FK_HPERSONEMAILVALIDATIONKEY_HPERSON FOREIGN KEY(PERSONID) REFERENCES PUBLIC.HPERSON(ID) NOCHECK;           
ALTER TABLE PUBLIC.HDOCUMENTUPLOAD ADD CONSTRAINT PUBLIC.FK_HDOCUMENTUPLOAD_LOCALE FOREIGN KEY(LOCALEID) REFERENCES PUBLIC.HLOCALE(ID) NOCHECK;
ALTER TABLE PUBLIC.HACCOUNTMEMBERSHIP ADD CONSTRAINT PUBLIC.FK9D5DB27B3E684F5E FOREIGN KEY(MEMBEROF) REFERENCES PUBLIC.HACCOUNTROLE(ID) NOCHECK;               
ALTER TABLE PUBLIC.HTEXTFLOWTARGETCONTENTHISTORY ADD CONSTRAINT PUBLIC.FKCONTENT_TEXT_FLOW_TARGET_HISTORY FOREIGN KEY(TEXT_FLOW_TARGET_HISTORY_ID) REFERENCES PUBLIC.HTEXTFLOWTARGETHISTORY(ID) NOCHECK;       
CREATE FORCE TRIGGER PUBLIC.HDOCUMENT_UPDATE AFTER UPDATE ON PUBLIC.HDOCUMENT FOR EACH ROW QUEUE 1024 CALL "org.zanata.H2DocumentHistoryTrigger";              
