-- this will create user translator/translator
INSERT INTO HAccount (id,creationDate,lastChanged,versionNum,apiKey,enabled,passwordHash,username,mergedInto) VALUES (2,{ts '2012-04-03 15:06:28'},{ts '2012-04-03 15:06:28'},1,'d83882201764f7d339e97c4b087f0806',true,'Fr5JHlcaEqKLSHjnBm4gXg==','translator',null);
INSERT INTO HPerson (id,creationDate,lastChanged,versionNum,email,name,accountId) VALUES (2,{ts '2012-04-03 15:06:28'},{ts '2012-04-03 15:06:28'},0,'translator@example.com','translator',2);

INSERT INTO HAccountMembership (accountId,memberOf) VALUES (2,1);
INSERT INTO HAccountMembership (accountId,memberOf) VALUES (2,5);
