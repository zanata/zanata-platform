insert into AccountRole (id, name, conditional) values (1, 'admin', false);
insert into AccountRole (id, name, conditional) values (2, 'user', false);
insert into AccountRole (id, name, conditional) values (3, 'project-maintainer', true);
insert into AccountRole (id, name, conditional) values (4, 'translation-team-lead', true);
insert into AccountRole (id, name, conditional) values (5, 'translator', true);
insert into AccountRole (id, name, conditional) values (6, 'reviewer', true);
insert into AccountRoleGroup (roleId, memberOf) values (2, 1);

insert into Account (id, creationDate, timestamp, enabled, passwordhash, username) 
	values (1, NOW(), NOW(), 1, 'Eyox7xbNQ09MkIfRyH+rjg==', 'admin');
insert into Person (id, creationDate, timestamp, email, name, accountId) 
	values (1, NOW(), NOW(), 'asgeir+flies@gmail.com', 'Administrator', 1);
insert into AccountMembership (accountId, memberOf) values (1, 1);

insert into Account (id, creationDate, timestamp, enabled, passwordhash, username) 
	values (2, NOW(), NOW(), 1, '/9Se/pfHeUH8FJ4asBD6jQ==', 'demo');
insert into Person (id, creationDate, timestamp, email, name, accountId) 
	values (2, NOW(), NOW(), 'asgeirf+flies@gmail.com', 'Sample User', 2);
insert into AccountMembership (accountId, memberOf) 
	values (2, 2);

insert into Account (id, creationDate, timestamp, enabled, passwordhash, username) 
	values (3, NOW(), NOW(), 1, 'pQRgEKG97HuyCfeoOR69Sg==', 'bob');
insert into Person (id, creationDate, timestamp, email, name, accountId) 
	values (3, NOW(), NOW(), 'asgeirf+flies@gmail.com', 'Bob Translator', 3);
insert into AccountMembership (accountId, memberOf) 
	values (3, 5);

# insert into ResourceCategory (id, creationDate, timestamp, name) values(1, NOW(), NOW(), 'Documentation');
# insert into ResourceCategory (id, creationDate, timestamp, name) values(2, NOW(), NOW(), 'User Interface');
# insert into ResourceCategory (id, creationDate, timestamp, name) values(3, NOW(), NOW(), 'Website');

insert into FliesLocale (id, icuLocaleId, parentId)
	values ('as-IN','as_IN', NULL);
insert into FliesLocale (id, icuLocaleId, parentId)
	values ('de-DE','de_DE', NULL);
insert into FliesLocale (id, icuLocaleId, parentId)
	values ('es-ES','es_ES', NULL);
insert into FliesLocale (id, icuLocaleId, parentId)
	values ('gu-IN','gu_IN', NULL);
insert into FliesLocale (id, icuLocaleId, parentId)
	values ('it-IT','it_IT', NULL);
insert into FliesLocale (id, icuLocaleId, parentId)
	values ('kn-IN','kn_IN', NULL);
insert into FliesLocale (id, icuLocaleId, parentId)
	values ('ml-IN','ml_IN', NULL);
insert into FliesLocale (id, icuLocaleId, parentId)
	values ('or-IN','or_IN', NULL);
insert into FliesLocale (id, icuLocaleId, parentId)
	values ('ru-RU','ru_RU', NULL);
insert into FliesLocale (id, icuLocaleId, parentId)
	values ('ta-IN','ta-IN', NULL);
insert into FliesLocale (id, icuLocaleId, parentId)
	values ('zh-TW','zh_TW', NULL);
insert into FliesLocale (id, icuLocaleId, parentId)
	values ('bn-IN','bn_IN', NULL);
insert into FliesLocale (id, icuLocaleId, parentId)
	values ('en-US','en_US', NULL);
insert into FliesLocale (id, icuLocaleId, parentId)
	values ('fr-FR','fr_FR', NULL);
insert into FliesLocale (id, icuLocaleId, parentId)
	values ('hi-IN','hi_IN', NULL);
insert into FliesLocale (id, icuLocaleId, parentId)
	values ('ja-JP','ja_JP', NULL);
insert into FliesLocale (id, icuLocaleId, parentId)
	values ('ko-KR','ko_KR', NULL);
insert into FliesLocale (id, icuLocaleId, parentId)
	values ('mr-IN','mr_IN', NULL);
insert into FliesLocale (id, icuLocaleId, parentId)
	values ('pa-IN','pa_IN', NULL);
insert into FliesLocale (id, icuLocaleId, parentId)
	values ('pt-BR','pt_BR', NULL);
insert into FliesLocale (id, icuLocaleId, parentId)
	values ('si-LK','si_LK', NULL);
insert into FliesLocale (id, icuLocaleId, parentId)
	values ('te-IN','te_IN', NULL);
insert into FliesLocale (id, icuLocaleId, parentId)
	values ('zh-CN','zh_CN', NULL);

insert into Tribe (id, creationDate, timestamp, chiefId, localeId) 
	values(1, NOW(), NOW(), 3, 'as-IN');
insert into Tribe (id, creationDate, timestamp, chiefId, localeId) 
	values(2, NOW(), NOW(), 3, 'bn-IN');
insert into Tribe (id, creationDate, timestamp, chiefId, localeId) 
	values(3, NOW(), NOW(), 3, 'de-DE');
insert into Tribe (id, creationDate, timestamp, chiefId, localeId) 
	values(4, NOW(), NOW(), 3, 'en-US');
insert into Tribe (id, creationDate, timestamp, chiefId, localeId) 
	values(5, NOW(), NOW(), 3, 'es-ES');
insert into Tribe (id, creationDate, timestamp, chiefId, localeId) 
	values(6, NOW(), NOW(), 3, 'fr-FR');
insert into Tribe (id, creationDate, timestamp, chiefId, localeId) 
	values(7, NOW(), NOW(), 3, 'gu-IN');
insert into Tribe (id, creationDate, timestamp, chiefId, localeId) 
	values(8, NOW(), NOW(), 3, 'hi-IN');
insert into Tribe (id, creationDate, timestamp, chiefId, localeId) 
	values(9, NOW(), NOW(), 3, 'it-IT');
insert into Tribe (id, creationDate, timestamp, chiefId, localeId) 
	values(10, NOW(), NOW(), 3, 'ja-JP');
insert into Tribe (id, creationDate, timestamp, chiefId, localeId) 
	values(12, NOW(), NOW(), 3, 'ko-KR');
insert into Tribe (id, creationDate, timestamp, chiefId, localeId) 
	values(13, NOW(), NOW(), 3, 'ml-IN');
insert into Tribe (id, creationDate, timestamp, chiefId, localeId) 
	values(14, NOW(), NOW(), 3, 'mr-IN');
insert into Tribe (id, creationDate, timestamp, chiefId, localeId) 
	values(15, NOW(), NOW(), 3, 'or-IN');
insert into Tribe (id, creationDate, timestamp, chiefId, localeId) 
	values(16, NOW(), NOW(), 3, 'pa-IN');
insert into Tribe (id, creationDate, timestamp, chiefId, localeId) 
	values(17, NOW(), NOW(), 3, 'pt-BR');
insert into Tribe (id, creationDate, timestamp, chiefId, localeId) 
	values(18, NOW(), NOW(), 3, 'ru-RU');
insert into Tribe (id, creationDate, timestamp, chiefId, localeId) 
	values(19, NOW(), NOW(), 3, 'si-LK');
insert into Tribe (id, creationDate, timestamp, chiefId, localeId) 
	values(20, NOW(), NOW(), 3, 'ta-IN');
insert into Tribe (id, creationDate, timestamp, chiefId, localeId) 
	values(21, NOW(), NOW(), 3, 'te-IN');
insert into Tribe (id, creationDate, timestamp, chiefId, localeId) 
	values(22, NOW(), NOW(), 3, 'zh-CN');
insert into Tribe (id, creationDate, timestamp, chiefId, localeId) 
	values(23, NOW(), NOW(), 3, 'zh-TW');
	
insert into Community (id, slug, name, description, homeContent, ownerId, creationDate, timestamp)
	values (1, 'jboss', 'JBoss.org', 'best community since sliced bread', NULL, 1, NOW(), NOW());
insert into Community (id, slug, name, description, homeContent, ownerId, creationDate, timestamp)
	values (2, 'redhat', 'Red Hat', 'best community since sliced bread', NULL, 1, NOW(), NOW());
insert into Community (id, slug, name, description, homeContent, ownerId, creationDate, timestamp)
	values (3, 'hibernate', 'Hibernate.org', 'best community since sliced bread', NULL, 1, NOW(), NOW());
insert into Community (id, slug, name, description, homeContent, ownerId, creationDate, timestamp)
	values (4, 'seam', 'Seam', 'best community since sliced bread', NULL, 1, NOW(), NOW());
	
	