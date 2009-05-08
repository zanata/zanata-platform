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
	values (3, NOW(), NOW(), 1, '/9Se/pfHeUH8FJ4asBD6jQ==', 'bob');
insert into Person (id, creationDate, timestamp, email, name, accountId) 
	values (3, NOW(), NOW(), 'asgeirf+flies@gmail.com', 'Bob Translator', 3);
insert into AccountMembership (accountId, memberOf) 
	values (3, 5);

# insert into ResourceCategory (id, creationDate, timestamp, name) values(1, NOW(), NOW(), 'Documentation');
# insert into ResourceCategory (id, creationDate, timestamp, name) values(2, NOW(), NOW(), 'User Interface');
# insert into ResourceCategory (id, creationDate, timestamp, name) values(3, NOW(), NOW(), 'Website');

# insert into FliesLocale (id, icuLocaleId, parentId)
#	values ('gu-IN','gu_IN', NULL);
	
insert into Tribe (id, creationDate, timestamp, chiefId, localeId) 
	values(1, NOW(), NOW(), 3, 'gu-IN')
