/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `HAccount` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creationDate` datetime NOT NULL,
  `lastChanged` datetime NOT NULL,
  `versionNum` int(11) NOT NULL,
  `apiKey` varchar(32) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `enabled` bit(1) NOT NULL,
  `passwordHash` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `username` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `HAccountActivationKey` (
  `keyHash` varchar(32) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `accountId` bigint(20) NOT NULL,
  PRIMARY KEY (`keyHash`),
  UNIQUE KEY `accountId` (`accountId`),
  KEY `FK86E79CA44A0EDB13` (`accountId`),
  CONSTRAINT `FK86E79CA44A0EDB13` FOREIGN KEY (`accountId`) REFERENCES `HAccount` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `HAccountMembership` (
  `accountId` bigint(20) NOT NULL,
  `memberOf` int(11) NOT NULL,
  PRIMARY KEY (`accountId`,`memberOf`),
  KEY `FK9D5DB27B8AFBEC12` (`memberOf`),
  KEY `FK9D5DB27B4A0EDB13` (`accountId`),
  CONSTRAINT `FK9D5DB27B4A0EDB13` FOREIGN KEY (`accountId`) REFERENCES `HAccount` (`id`),
  CONSTRAINT `FK9D5DB27B8AFBEC12` FOREIGN KEY (`memberOf`) REFERENCES `HAccountRole` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `HAccountPermission` (
  `permissionId` int(11) NOT NULL AUTO_INCREMENT,
  `action` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `discriminator` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `recipient` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `target` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`permissionId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `HAccountResetPasswordKey` (
  `keyHash` varchar(32) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `accountId` bigint(20) NOT NULL,
  PRIMARY KEY (`keyHash`),
  UNIQUE KEY `accountId` (`accountId`),
  KEY `FK85C9EFDA4A0EDB13` (`accountId`),
  CONSTRAINT `FK85C9EFDA4A0EDB13` FOREIGN KEY (`accountId`) REFERENCES `HAccount` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `HAccountRole` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `conditional` bit(1) NOT NULL,
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `HAccountRoleGroup` (
  `roleId` int(11) NOT NULL,
  `memberOf` int(11) NOT NULL,
  PRIMARY KEY (`roleId`,`memberOf`),
  KEY `FK3321CC648AFBEC12` (`memberOf`),
  KEY `FK3321CC647A88DA32` (`roleId`),
  CONSTRAINT `FK3321CC647A88DA32` FOREIGN KEY (`roleId`) REFERENCES `HAccountRole` (`id`),
  CONSTRAINT `FK3321CC648AFBEC12` FOREIGN KEY (`memberOf`) REFERENCES `HAccountRole` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `HCommunity` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creationDate` datetime NOT NULL,
  `lastChanged` datetime NOT NULL,
  `versionNum` int(11) NOT NULL,
  `slug` varchar(40) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `description` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `homeContent` longtext,
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `ownerId` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `slug` (`slug`),
  KEY `FKD3DF20814C1F95C5` (`ownerId`),
  CONSTRAINT `FKD3DF20814C1F95C5` FOREIGN KEY (`ownerId`) REFERENCES `HPerson` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `HCommunity_Member` (
  `communityId` bigint(20) NOT NULL,
  `personId` bigint(20) NOT NULL,
  PRIMARY KEY (`personId`,`communityId`),
  KEY `FK8BEBF038A5679DE7` (`personId`),
  KEY `FK8BEBF038ADFEE80B` (`communityId`),
  CONSTRAINT `FK8BEBF038ADFEE80B` FOREIGN KEY (`communityId`) REFERENCES `HCommunity` (`id`),
  CONSTRAINT `FK8BEBF038A5679DE7` FOREIGN KEY (`personId`) REFERENCES `HPerson` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `HCommunity_Officer` (
  `communityId` bigint(20) NOT NULL,
  `personId` bigint(20) NOT NULL,
  PRIMARY KEY (`personId`,`communityId`),
  KEY `FK5CB3E758A5679DE7` (`personId`),
  KEY `FK5CB3E758ADFEE80B` (`communityId`),
  CONSTRAINT `FK5CB3E758ADFEE80B` FOREIGN KEY (`communityId`) REFERENCES `HCommunity` (`id`),
  CONSTRAINT `FK5CB3E758A5679DE7` FOREIGN KEY (`personId`) REFERENCES `HPerson` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `HDocument` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creationDate` datetime NOT NULL,
  `lastChanged` datetime NOT NULL,
  `versionNum` int(11) NOT NULL,
  `contentType` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `docId` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `locale` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `obsolete` bit(1) NOT NULL,
  `path` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `revision` int(11) NOT NULL,
  `last_modified_by_id` bigint(20) DEFAULT NULL,
  `poHeader_id` bigint(20) DEFAULT NULL,
  `project_iteration_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `docId` (`docId`,`project_iteration_id`),
  KEY `FKEA766D83B13DF08D` (`last_modified_by_id`),
  KEY `FKEA766D835063A1C9` (`project_iteration_id`),
  KEY `FKEA766D8360005CD9` (`poHeader_id`),
  CONSTRAINT `FKEA766D8360005CD9` FOREIGN KEY (`poHeader_id`) REFERENCES `HPoHeader` (`id`),
  CONSTRAINT `FKEA766D835063A1C9` FOREIGN KEY (`project_iteration_id`) REFERENCES `HProjectIteration` (`id`),
  CONSTRAINT `FKEA766D83B13DF08D` FOREIGN KEY (`last_modified_by_id`) REFERENCES `HPerson` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = latin1 */ ;
/*!50003 SET character_set_results = latin1 */ ;
/*!50003 SET collation_connection  = latin1_swedish_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `HDocument_Update` BEFORE UPDATE on `HDocument` FOR EACH ROW BEGIN IF NEW.revision != OLD.revision THEN INSERT INTO HDocumentHistory(document_id,revision,contentType,docId,locale,name,path,lastChanged,last_modified_by_id,obsolete) VALUES (OLD.id,OLD.revision,OLD.contentType,OLD.docId,OLD.locale,OLD.name,OLD.path,OLD.lastChanged,OLD.last_modified_by_id,OLD.obsolete); END IF; END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `HDocumentHistory` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `contentType` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `docId` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `lastChanged` datetime DEFAULT NULL,
  `locale` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `obsolete` bit(1) NOT NULL,
  `path` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `revision` int(11) DEFAULT NULL,
  `document_id` bigint(20) DEFAULT NULL,
  `last_modified_by_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `document_id` (`document_id`,`revision`),
  KEY `FK27976591F8A0A2BC` (`document_id`),
  KEY `FK27976591B13DF08D` (`last_modified_by_id`),
  CONSTRAINT `FK27976591B13DF08D` FOREIGN KEY (`last_modified_by_id`) REFERENCES `HPerson` (`id`),
  CONSTRAINT `FK27976591F8A0A2BC` FOREIGN KEY (`document_id`) REFERENCES `HDocument` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `HFliesLocale` (
  `id` varchar(80) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `icuLocaleId` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `parentId` varchar(80) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK6CAF0A33D884B2E` (`parentId`),
  CONSTRAINT `FK6CAF0A33D884B2E` FOREIGN KEY (`parentId`) REFERENCES `HFliesLocale` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `HFliesLocale_Friends` (
  `localeId` varchar(80) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `friendId` varchar(80) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  KEY `FKF87125D9A2AAA022` (`friendId`),
  KEY `FKF87125D968C8A4DE` (`localeId`),
  CONSTRAINT `FKF87125D968C8A4DE` FOREIGN KEY (`localeId`) REFERENCES `HFliesLocale` (`id`),
  CONSTRAINT `FKF87125D9A2AAA022` FOREIGN KEY (`friendId`) REFERENCES `HFliesLocale` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `HPerson` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creationDate` datetime NOT NULL,
  `lastChanged` datetime NOT NULL,
  `versionNum` int(11) NOT NULL,
  `email` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `name` varchar(80) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `accountId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK6F0931BD4A0EDB13` (`accountId`),
  CONSTRAINT `FK6F0931BD4A0EDB13` FOREIGN KEY (`accountId`) REFERENCES `HAccount` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `HPoHeader` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creationDate` datetime NOT NULL,
  `lastChanged` datetime NOT NULL,
  `versionNum` int(11) NOT NULL,
  `entries` longtext,
  `comment_id` bigint(20) DEFAULT NULL,
  `document_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `document_id` (`document_id`),
  UNIQUE KEY `document_id_2` (`document_id`),
  KEY `FK9A0ABDD4F8A0A2BC` (`document_id`),
  KEY `FK9A0ABDD42DC34DA6` (`comment_id`),
  CONSTRAINT `FK9A0ABDD42DC34DA6` FOREIGN KEY (`comment_id`) REFERENCES `HSimpleComment` (`id`),
  CONSTRAINT `FK9A0ABDD4F8A0A2BC` FOREIGN KEY (`document_id`) REFERENCES `HDocument` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `HPoTargetHeader` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creationDate` datetime NOT NULL,
  `lastChanged` datetime NOT NULL,
  `versionNum` int(11) NOT NULL,
  `entries` longtext,
  `targetLanguage` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `comment_id` bigint(20) DEFAULT NULL,
  `document_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `document_id` (`document_id`,`targetLanguage`),
  KEY `FK1BC71985F8A0A2BC` (`document_id`),
  KEY `FK1BC719852DC34DA6` (`comment_id`),
  CONSTRAINT `FK1BC719852DC34DA6` FOREIGN KEY (`comment_id`) REFERENCES `HSimpleComment` (`id`),
  CONSTRAINT `FK1BC71985F8A0A2BC` FOREIGN KEY (`document_id`) REFERENCES `HDocument` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `HPotEntryData` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `context` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `flags` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `refs` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `comment_id` bigint(20) DEFAULT NULL,
  `tf_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `tf_id` (`tf_id`),
  UNIQUE KEY `tf_id_2` (`tf_id`),
  KEY `FK17A648CF2DC34DA6` (`comment_id`),
  KEY `FK17A648CF71CA5CE5` (`tf_id`),
  CONSTRAINT `FK17A648CF71CA5CE5` FOREIGN KEY (`tf_id`) REFERENCES `HTextFlow` (`id`),
  CONSTRAINT `FK17A648CF2DC34DA6` FOREIGN KEY (`comment_id`) REFERENCES `HSimpleComment` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `HProject` (
  `projecttype` varchar(31) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creationDate` datetime NOT NULL,
  `lastChanged` datetime NOT NULL,
  `versionNum` int(11) NOT NULL,
  `slug` varchar(40) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `description` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `homeContent` longtext,
  `name` varchar(80) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `slug` (`slug`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `HProjectIteration` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creationDate` datetime NOT NULL,
  `lastChanged` datetime NOT NULL,
  `versionNum` int(11) NOT NULL,
  `slug` varchar(40) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `active` bit(1) NOT NULL,
  `description` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `name` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `parentId` bigint(20) DEFAULT NULL,
  `project_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `slug` (`slug`,`project_id`),
  KEY `FK31C1E42C4A451E5F` (`project_id`),
  KEY `FK31C1E42C59934BEB` (`parentId`),
  CONSTRAINT `FK31C1E42C59934BEB` FOREIGN KEY (`parentId`) REFERENCES `HProjectIteration` (`id`),
  CONSTRAINT `FK31C1E42C4A451E5F` FOREIGN KEY (`project_id`) REFERENCES `HProject` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `HProject_Maintainer` (
  `personId` bigint(20) NOT NULL,
  `projectId` bigint(20) NOT NULL,
  PRIMARY KEY (`projectId`,`personId`),
  KEY `FK1491F2E6A5679DE7` (`personId`),
  KEY `FK1491F2E6B55BD1EB` (`projectId`),
  CONSTRAINT `FK1491F2E6B55BD1EB` FOREIGN KEY (`projectId`) REFERENCES `HProject` (`id`),
  CONSTRAINT `FK1491F2E6A5679DE7` FOREIGN KEY (`personId`) REFERENCES `HPerson` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `HSimpleComment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `comment` longtext NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `HTextFlow` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `content` longtext NOT NULL,
  `obsolete` bit(1) NOT NULL,
  `pos` int(11) NOT NULL,
  `resId` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `revision` int(11) NOT NULL,
  `comment_id` bigint(20) DEFAULT NULL,
  `document_id` bigint(20) NOT NULL,
  `potEntryData_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `document_id` (`document_id`,`resId`),
  KEY `FK7B40F863F8A0A2BC` (`document_id`),
  KEY `FK7B40F863F8DC9359` (`potEntryData_id`),
  KEY `FK7B40F8632DC34DA6` (`comment_id`),
  CONSTRAINT `FK7B40F8632DC34DA6` FOREIGN KEY (`comment_id`) REFERENCES `HSimpleComment` (`id`),
  CONSTRAINT `FK7B40F863F8A0A2BC` FOREIGN KEY (`document_id`) REFERENCES `HDocument` (`id`),
  CONSTRAINT `FK7B40F863F8DC9359` FOREIGN KEY (`potEntryData_id`) REFERENCES `HPotEntryData` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = latin1 */ ;
/*!50003 SET character_set_results = latin1 */ ;
/*!50003 SET collation_connection  = latin1_swedish_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `HTextFlow_Update` BEFORE UPDATE on `HTextFlow` FOR EACH ROW BEGIN IF NEW.revision != OLD.revision THEN INSERT INTO HTextFlowHistory(tf_id,revision,content, obsolete, pos) VALUES (OLD.id,OLD.revision,OLD.content,OLD.obsolete,OLD.pos); END IF; END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `HTextFlowHistory` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `content` longtext,
  `obsolete` bit(1) NOT NULL,
  `pos` int(11) DEFAULT NULL,
  `revision` int(11) DEFAULT NULL,
  `tf_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `revision` (`revision`,`tf_id`),
  KEY `FK46C4DEB171CA5CE5` (`tf_id`),
  CONSTRAINT `FK46C4DEB171CA5CE5` FOREIGN KEY (`tf_id`) REFERENCES `HTextFlow` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `HTextFlowTarget` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creationDate` datetime NOT NULL,
  `lastChanged` datetime NOT NULL,
  `versionNum` int(11) NOT NULL,
  `content` longtext NOT NULL,
  `locale` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `state` int(11) NOT NULL,
  `tf_revision` int(11) NOT NULL,
  `comment_id` bigint(20) DEFAULT NULL,
  `last_modified_by_id` bigint(20) DEFAULT NULL,
  `tf_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `locale` (`locale`,`tf_id`),
  KEY `FK1E933FD4B13DF08D` (`last_modified_by_id`),
  KEY `FK1E933FD42DC34DA6` (`comment_id`),
  KEY `FK1E933FD471CA5CE5` (`tf_id`),
  CONSTRAINT `FK1E933FD471CA5CE5` FOREIGN KEY (`tf_id`) REFERENCES `HTextFlow` (`id`),
  CONSTRAINT `FK1E933FD42DC34DA6` FOREIGN KEY (`comment_id`) REFERENCES `HSimpleComment` (`id`),
  CONSTRAINT `FK1E933FD4B13DF08D` FOREIGN KEY (`last_modified_by_id`) REFERENCES `HPerson` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = latin1 */ ;
/*!50003 SET character_set_results = latin1 */ ;
/*!50003 SET collation_connection  = latin1_swedish_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `HTextFlowTarget_Update` BEFORE UPDATE on `HTextFlowTarget` FOR EACH ROW BEGIN IF NEW.versionNum != OLD.versionNum THEN INSERT INTO HTextFlowTarget(target_id,versionNum,content, lastChanged, last_modified_by_id, state, tf_revision) VALUES (OLD.id,OLD.versionNum,OLD.content,OLD.lastChanged,OLD.last_modified_by_id,OLD.state,OLD.tf_revision); END IF; END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `HTextFlowTargetHistory` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `content` longtext,
  `lastChanged` datetime DEFAULT NULL,
  `state` int(11) DEFAULT NULL,
  `tf_revision` int(11) DEFAULT NULL,
  `versionNum` int(11) DEFAULT NULL,
  `last_modified_by_id` bigint(20) DEFAULT NULL,
  `target_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `target_id` (`target_id`,`versionNum`),
  KEY `FKF1098620B13DF08D` (`last_modified_by_id`),
  KEY `FKF1098620CE3B3557` (`target_id`),
  CONSTRAINT `FKF1098620CE3B3557` FOREIGN KEY (`target_id`) REFERENCES `HTextFlowTarget` (`id`),
  CONSTRAINT `FKF1098620B13DF08D` FOREIGN KEY (`last_modified_by_id`) REFERENCES `HPerson` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `HTribe` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creationDate` datetime NOT NULL,
  `lastChanged` datetime NOT NULL,
  `versionNum` int(11) NOT NULL,
  `chiefId` bigint(20) DEFAULT NULL,
  `localeId` varchar(80) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `localeId` (`localeId`),
  KEY `FK7FB20BC6B7757AD7` (`chiefId`),
  KEY `FK7FB20BC668C8A4DE` (`localeId`),
  CONSTRAINT `FK7FB20BC668C8A4DE` FOREIGN KEY (`localeId`) REFERENCES `HFliesLocale` (`id`),
  CONSTRAINT `FK7FB20BC6B7757AD7` FOREIGN KEY (`chiefId`) REFERENCES `HPerson` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `HTribe_Leader` (
  `personId` bigint(20) NOT NULL,
  `tribeId` bigint(20) NOT NULL,
  PRIMARY KEY (`tribeId`,`personId`),
  KEY `FK20177C2A5679DE7` (`personId`),
  KEY `FK20177C2EED54855` (`tribeId`),
  CONSTRAINT `FK20177C2EED54855` FOREIGN KEY (`tribeId`) REFERENCES `HTribe` (`id`),
  CONSTRAINT `FK20177C2A5679DE7` FOREIGN KEY (`personId`) REFERENCES `HPerson` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `HTribe_Member` (
  `personId` bigint(20) NOT NULL,
  `tribeId` bigint(20) NOT NULL,
  PRIMARY KEY (`tribeId`,`personId`),
  KEY `FK3BBBD53A5679DE7` (`personId`),
  KEY `FK3BBBD53EED54855` (`tribeId`),
  CONSTRAINT `FK3BBBD53EED54855` FOREIGN KEY (`tribeId`) REFERENCES `HTribe` (`id`),
  CONSTRAINT `FK3BBBD53A5679DE7` FOREIGN KEY (`personId`) REFERENCES `HPerson` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
