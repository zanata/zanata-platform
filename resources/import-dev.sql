insert into Person (id, name) values (1, 'Administrator');
insert into Person (id, name) values (2, 'Sample User');

insert into Account (id, username, passwordhash, enabled, person_id) values (1, 'admin', 'Eyox7xbNQ09MkIfRyH+rjg==', 1, 1);
insert into Account (id, username, passwordhash, enabled, person_id) values (2, 'demo', '/9Se/pfHeUH8FJ4asBD6jQ==', 1, 2);

insert into AccountRole (id, name, conditional) values (1, 'admin', false);
insert into AccountRole (id, name, conditional) values (2, 'user', false);
insert into AccountRole (id, name, conditional) values (3, 'project-maintainer', true);
insert into AccountRole (id, name, conditional) values (4, 'translation-team-lead', true);
insert into AccountRole (id, name, conditional) values (5, 'translator', true);
insert into AccountRole (id, name, conditional) values (6, 'reviewer', true);

insert into AccountMembership (account_id, member_of) values (1, 1);
insert into AccountMembership (account_id, member_of) values (2, 2);

insert into AccountRoleGroup (role_id, member_of) values (2, 1);

