insert into HAccountRole(id,conditional,name) values(1,false,'admin');
insert into HAccountRole(id,conditional,name) values(2,false,'user');
insert into HAccountRoleGroup(roleId,memberOf) values(1,2);
