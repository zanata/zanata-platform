INSERT INTO HAccount (id, creationDate, lastChanged, apiKey, enabled, passwordHash, username) VALUES (1,'2009-01-14 11:39:00','2009-01-14 11:39:00','12345678901234567890123456789012',TRUE,'Eyox7xbNQ09MkIfRyH+rjg==','admin');
INSERT INTO HAccount (id, creationDate, lastChanged, apiKey, enabled, passwordHash, username) VALUES (2,'2009-01-14 11:39:00','2009-01-14 11:39:00','23456789012345678901234567890123',TRUE,'/9Se/pfHeUH8FJ4asBD6jQ==','demo');
INSERT INTO HAccount (id, creationDate, lastChanged, apiKey, enabled, passwordHash, username) VALUES (3,'2009-01-14 11:39:00','2009-01-14 11:39:00','34567890123456789012345678901234',TRUE,'pQRgEKG97HuyCfeoOR69Sg==','bob');
	
INSERT INTO HAccountRole (id, conditional, name) VALUES (1,'\0','admin');
INSERT INTO HAccountRole (id, conditional, name) VALUES (2,'\0','user');

INSERT INTO HAccountMembership (accountId, memberOf) VALUES (1,1);
INSERT INTO HAccountMembership (accountId, memberOf) VALUES (2,2);
INSERT INTO HAccountMembership (accountId, memberOf) VALUES (3,2);
	
INSERT INTO HAccountRoleGroup (roleId, memberOf) VALUES  (1,2);

INSERT INTO HPerson (id, creationDate, lastChanged, email, name, accountId) VALUES (1,'2009-01-14 11:39:00','2009-01-14 11:39:00','asgeirf@localhost','Administrator',1);
INSERT INTO HPerson (id, creationDate, lastChanged, email, name, accountId) VALUES (2,'2009-01-14 11:39:00','2009-01-14 11:39:00','asgeirf@localhost','Sample User',2);
INSERT INTO HPerson (id, creationDate, lastChanged, email, name, accountId) VALUES (3,'2009-01-14 11:39:00','2009-01-14 11:39:00','asgeirf@localhost','Bob Translator',3);
	
INSERT INTO HCommunity (id, creationDate, lastChanged, slug, description, homeContent, name, ownerId) VALUES (1,'2009-01-14 11:39:00','2009-01-14 11:39:00','jboss','best community since sliced bread',NULL,'JBoss.org',1);
INSERT INTO HCommunity (id, creationDate, lastChanged, slug, description, homeContent, name, ownerId) VALUES (2,'2009-01-14 11:39:00','2009-01-14 11:39:00','redhat','best community since sliced bread',NULL,'Red Hat',1);
INSERT INTO HCommunity (id, creationDate, lastChanged, slug, description, homeContent, name, ownerId) VALUES (3,'2009-01-14 11:39:00','2009-01-14 11:39:00','hibernate','best community since sliced bread',NULL,'Hibernate.org',1);
INSERT INTO HCommunity (id, creationDate, lastChanged, slug, description, homeContent, name, ownerId) VALUES (4,'2009-01-14 11:39:00','2009-01-14 11:39:00','seam','best community since sliced bread',NULL,'Seam',1);
	
INSERT INTO HFliesLocale (id, icuLocaleId, parentId) VALUES ('as-IN','as_IN',NULL);
INSERT INTO HFliesLocale (id, icuLocaleId, parentId) VALUES ('bn-IN','bn_IN',NULL);
INSERT INTO HFliesLocale (id, icuLocaleId, parentId) VALUES ('de-DE','de_DE',NULL);
INSERT INTO HFliesLocale (id, icuLocaleId, parentId) VALUES ('en-US','en_US',NULL);
INSERT INTO HFliesLocale (id, icuLocaleId, parentId) VALUES ('es-ES','es_ES',NULL);
INSERT INTO HFliesLocale (id, icuLocaleId, parentId) VALUES ('fr-FR','fr_FR',NULL);
INSERT INTO HFliesLocale (id, icuLocaleId, parentId) VALUES ('gu-IN','gu_IN',NULL);
INSERT INTO HFliesLocale (id, icuLocaleId, parentId) VALUES ('hi-IN','hi_IN',NULL);
INSERT INTO HFliesLocale (id, icuLocaleId, parentId) VALUES ('it-IT','it_IT',NULL);
INSERT INTO HFliesLocale (id, icuLocaleId, parentId) VALUES ('ja-JP','ja_JP',NULL);
INSERT INTO HFliesLocale (id, icuLocaleId, parentId) VALUES ('kn-IN','kn_IN',NULL);
INSERT INTO HFliesLocale (id, icuLocaleId, parentId) VALUES ('ko-KR','ko_KR',NULL);
INSERT INTO HFliesLocale (id, icuLocaleId, parentId) VALUES ('ml-IN','ml_IN',NULL);
INSERT INTO HFliesLocale (id, icuLocaleId, parentId) VALUES ('mr-IN','mr_IN',NULL);
INSERT INTO HFliesLocale (id, icuLocaleId, parentId) VALUES ('or-IN','or_IN',NULL);
INSERT INTO HFliesLocale (id, icuLocaleId, parentId) VALUES ('pa-IN','pa_IN',NULL);
INSERT INTO HFliesLocale (id, icuLocaleId, parentId) VALUES ('pt-BR','pt_BR',NULL);
INSERT INTO HFliesLocale (id, icuLocaleId, parentId) VALUES ('ru-RU','ru_RU',NULL);
INSERT INTO HFliesLocale (id, icuLocaleId, parentId) VALUES ('si-LK','si_LK',NULL);
INSERT INTO HFliesLocale (id, icuLocaleId, parentId) VALUES ('ta-IN','ta-IN',NULL);
INSERT INTO HFliesLocale (id, icuLocaleId, parentId) VALUES ('te-IN','te_IN',NULL);
INSERT INTO HFliesLocale (id, icuLocaleId, parentId) VALUES ('zh-CN','zh_CN',NULL);
INSERT INTO HFliesLocale (id, icuLocaleId, parentId) VALUES ('zh-TW','zh_TW',NULL);
	
INSERT INTO HTribe (id, creationDate, lastChanged, chiefId, localeId) VALUES (1,'2009-01-14 11:39:00','2009-01-14 11:39:00',3,'as-IN');
INSERT INTO HTribe (id, creationDate, lastChanged, chiefId, localeId) VALUES (2,'2009-01-14 11:39:00','2009-01-14 11:39:00',3,'bn-IN');
INSERT INTO HTribe (id, creationDate, lastChanged, chiefId, localeId) VALUES (3,'2009-01-14 11:39:00','2009-01-14 11:39:00',3,'de-DE');
INSERT INTO HTribe (id, creationDate, lastChanged, chiefId, localeId) VALUES (4,'2009-01-14 11:39:00','2009-01-14 11:39:00',3,'en-US');
INSERT INTO HTribe (id, creationDate, lastChanged, chiefId, localeId) VALUES (5,'2009-01-14 11:39:00','2009-01-14 11:39:00',3,'es-ES');
INSERT INTO HTribe (id, creationDate, lastChanged, chiefId, localeId) VALUES (6,'2009-01-14 11:39:00','2009-01-14 11:39:00',3,'fr-FR');
INSERT INTO HTribe (id, creationDate, lastChanged, chiefId, localeId) VALUES (7,'2009-01-14 11:39:00','2009-01-14 11:39:00',3,'gu-IN');
INSERT INTO HTribe (id, creationDate, lastChanged, chiefId, localeId) VALUES (8,'2009-01-14 11:39:00','2009-01-14 11:39:00',3,'hi-IN');
INSERT INTO HTribe (id, creationDate, lastChanged, chiefId, localeId) VALUES (9,'2009-01-14 11:39:00','2009-01-14 11:39:00',3,'it-IT');
INSERT INTO HTribe (id, creationDate, lastChanged, chiefId, localeId) VALUES (10,'2009-01-14 11:39:00','2009-01-14 11:39:00',3,'ja-JP');
INSERT INTO HTribe (id, creationDate, lastChanged, chiefId, localeId) VALUES (12,'2009-01-14 11:39:00','2009-01-14 11:39:00',3,'ko-KR');
INSERT INTO HTribe (id, creationDate, lastChanged, chiefId, localeId) VALUES (13,'2009-01-14 11:39:00','2009-01-14 11:39:00',3,'ml-IN');
INSERT INTO HTribe (id, creationDate, lastChanged, chiefId, localeId) VALUES (14,'2009-01-14 11:39:00','2009-01-14 11:39:00',3,'mr-IN');
INSERT INTO HTribe (id, creationDate, lastChanged, chiefId, localeId) VALUES (15,'2009-01-14 11:39:00','2009-01-14 11:39:00',3,'or-IN');
INSERT INTO HTribe (id, creationDate, lastChanged, chiefId, localeId) VALUES (16,'2009-01-14 11:39:00','2009-01-14 11:39:00',3,'pa-IN');
INSERT INTO HTribe (id, creationDate, lastChanged, chiefId, localeId) VALUES (17,'2009-01-14 11:39:00','2009-01-14 11:39:00',3,'pt-BR');
INSERT INTO HTribe (id, creationDate, lastChanged, chiefId, localeId) VALUES (18,'2009-01-14 11:39:00','2009-01-14 11:39:00',3,'ru-RU');
INSERT INTO HTribe (id, creationDate, lastChanged, chiefId, localeId) VALUES (19,'2009-01-14 11:39:00','2009-01-14 11:39:00',3,'si-LK');
INSERT INTO HTribe (id, creationDate, lastChanged, chiefId, localeId) VALUES (20,'2009-01-14 11:39:00','2009-01-14 11:39:00',3,'ta-IN');
INSERT INTO HTribe (id, creationDate, lastChanged, chiefId, localeId) VALUES (21,'2009-01-14 11:39:00','2009-01-14 11:39:00',3,'te-IN');
INSERT INTO HTribe (id, creationDate, lastChanged, chiefId, localeId) VALUES (22,'2009-01-14 11:39:00','2009-01-14 11:39:00',3,'zh-CN');
INSERT INTO HTribe (id, creationDate, lastChanged, chiefId, localeId) VALUES (23,'2009-01-14 11:39:00','2009-01-14 11:39:00',3,'zh-TW');

INSERT INTO HProjectContainer (id, creationDate, lastChanged) VALUES (1,'2009-09-01 20:30:46','2009-09-01 20:30:46');
INSERT INTO HProjectContainer (id, creationDate, lastChanged) VALUES (2,'2009-09-01 20:31:32','2009-09-01 20:31:32');
INSERT INTO HProject (projecttype, id, creationDate, lastChanged, slug, description, homeContent, name, project_container_id) VALUES ('iteration',1,'2009-09-01 20:30:16','2009-09-01 20:30:16','sample-project','An example Project','','Sample Project',NULL);
INSERT INTO HProjectSeries (id, creationDate, lastChanged, name, parentId, projectId) VALUES (1,'2009-09-01 20:30:16','2009-09-01 20:30:16','default',NULL,1);
INSERT INTO HProjectIteration (id, creationDate, lastChanged, slug, active, description, name, project_container_id, parentId, project_id, projectSeriesId) VALUES (1,'2009-09-01 20:30:46','2009-09-01 20:30:46','1.0',TRUE,'Initial Release','Version 1.0',1,NULL,1,1);
INSERT INTO HProjectIteration (id, creationDate, lastChanged, slug, active, description, name, project_container_id, parentId, project_id, projectSeriesId) VALUES (2,'2009-09-01 20:31:32','2009-09-01 20:31:32','1.1',TRUE,'Next Release','Version 1.1',2,NULL,1,1);
INSERT INTO HProject_Maintainer (personId, projectId) VALUES (3,1);

