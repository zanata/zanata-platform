insert into HAccount(id,versionNum,creationDate,lastChanged,apiKey,enabled,passwordHash,username) 
  values(1,1,NOW(),NOW(),null,true,null,'asgeirf');
insert into HPerson(id,versionNum,creationDate,lastChanged,email,name,accountId) 
  values(1,1,NOW(),NOW(),'asgeirf@redhat.com','Asgeir Frimannsson',1);
insert into HAccountMembership(accountId,memberOf) values (1,1);
