-- this will create user admin/admin
INSERT INTO HAccount (id,creationDate,lastChanged,versionNum,apiKey,enabled,passwordHash,username,mergedInto) VALUES (1,{ts '2012-04-03 15:06:28'},{ts '2012-04-03 15:06:28'},1,'b6d7044e9ee3b2447c28fb7c50d86d98',true,'Eyox7xbNQ09MkIfRyH+rjg==','admin',null);
INSERT INTO HPerson (id,creationDate,lastChanged,versionNum,email,name,accountId) VALUES (1,{ts '2012-04-03 15:06:28'},{ts '2012-04-03 15:06:28'},0,'pahuang@redhat.com','Administrator',1);

INSERT INTO HAccountMembership (accountId,memberOf) VALUES (1,1);
INSERT INTO HAccountMembership (accountId,memberOf) VALUES (1,4);
