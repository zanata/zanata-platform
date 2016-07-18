-- Username: admin
-- Password: admin1234
INSERT INTO HAccount (id, creationDate, lastChanged, versionNum, apiKey, enabled, passwordHash, username)
VALUES (1, now(), now(), 1, 'b6d7044e9ee3b2447c28fb7c50d86d98', 1, 'UZMf4PIqtTBGAo9wWKuTpg==', 'admin');

INSERT INTO HPerson (id, creationDate, lastChanged, versionNum, email, `name`, accountId)
VALUES (1, now(), now(), 1, 'admin@noreply.org', 'Administrator', 1);

INSERT INTO HAccountMembership(accountId, memberOf) VALUES (1, 5);
