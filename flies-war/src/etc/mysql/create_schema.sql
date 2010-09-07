    create table HAccount (
        id bigint not null auto_increment,
        creationDate datetime not null,
        lastChanged datetime not null,
        versionNum integer not null,
        apiKey varchar(32) binary,
        enabled bit not null,
        passwordHash varchar(255) binary,
        username varchar(255) binary,
        primary key (id),
        unique (username)
    ) ENGINE=InnoDB;

    create table HAccountActivationKey (
        keyHash varchar(32) binary not null,
        accountId bigint not null,
        primary key (keyHash),
        unique (accountId)
    ) ENGINE=InnoDB;

    create table HAccountMembership (
        accountId bigint not null,
        memberOf integer not null,
        primary key (accountId, memberOf)
    ) ENGINE=InnoDB;

    create table HAccountResetPasswordKey (
        keyHash varchar(32) binary not null,
        accountId bigint not null,
        primary key (keyHash),
        unique (accountId)
    ) ENGINE=InnoDB;

    create table HAccountRole (
        id integer not null auto_increment,
        conditional bit not null,
        name varchar(255) binary,
        primary key (id)
    ) ENGINE=InnoDB;

    create table HAccountRoleGroup (
        roleId integer not null,
        memberOf integer not null,
        primary key (roleId, memberOf)
    ) ENGINE=InnoDB;

    create table HApplicationConfiguration (
        id bigint not null auto_increment,
        creationDate datetime not null,
        lastChanged datetime not null,
        versionNum integer not null,
        config_key varchar(255) binary not null,
        config_value longtext not null,
        primary key (id),
        unique (config_key)
    ) ENGINE=InnoDB;

    create table HCommunity (
        id bigint not null auto_increment,
        creationDate datetime not null,
        lastChanged datetime not null,
        versionNum integer not null,
        slug varchar(40) binary not null,
        description varchar(100) binary,
        homeContent longtext,
        name varchar(255) binary not null,
        ownerId bigint not null,
        primary key (id),
        unique (slug)
    ) ENGINE=InnoDB;

    create table HCommunity_Member (
        personId bigint not null,
        communityId bigint not null,
        primary key (communityId, personId)
    ) ENGINE=InnoDB;

    create table HCommunity_Officer (
        personId bigint not null,
        communityId bigint not null,
        primary key (communityId, personId)
    ) ENGINE=InnoDB;

    create table HDocument (
        id bigint not null auto_increment,
        creationDate datetime not null,
        lastChanged datetime not null,
        versionNum integer not null,
        contentType varchar(255) binary not null,
        docId varchar(255) binary not null,
        name varchar(255) binary not null,
        obsolete bit not null,
        path varchar(255) binary not null,
        revision integer not null,
        last_modified_by_id bigint,
        locale bigint not null,
        poHeader_id bigint,
        project_iteration_id bigint not null,
        primary key (id),
        unique (docId, project_iteration_id)
    ) ENGINE=InnoDB;

    create table HDocumentHistory (
        id bigint not null auto_increment,
        contentType varchar(255) binary not null,
        docId varchar(255) binary not null,
        lastChanged datetime,
        name varchar(255) binary,
        obsolete bit not null,
        path varchar(255) binary,
        revision integer,
        document_id bigint,
        last_modified_by_id bigint,
        locale bigint not null,
        primary key (id),
        unique (document_id, revision)
    ) ENGINE=InnoDB;

    create table HLocale (
        id bigint not null auto_increment,
        creationDate datetime not null,
        lastChanged datetime not null,
        versionNum integer not null,
        active bit not null,
        localeId varchar(255) binary not null,
        primary key (id),
        unique (localeId)
    ) ENGINE=InnoDB;

    create table HLocale_Member (
        personId bigint not null,
        supportedLanguageId bigint not null,
        primary key (supportedLanguageId, personId)
    ) ENGINE=InnoDB;

    create table HPerson (
        id bigint not null auto_increment,
        creationDate datetime not null,
        lastChanged datetime not null,
        versionNum integer not null,
        email varchar(255) binary not null,
        name varchar(80) binary not null,
        accountId bigint,
        primary key (id),
        unique (email)
    ) ENGINE=InnoDB;

    create table HPoHeader (
        id bigint not null auto_increment,
        creationDate datetime not null,
        lastChanged datetime not null,
        versionNum integer not null,
        entries longtext,
        comment_id bigint,
        document_id bigint unique,
        primary key (id),
        unique (document_id)
    ) ENGINE=InnoDB;

    create table HPoTargetHeader (
        id bigint not null auto_increment,
        creationDate datetime not null,
        lastChanged datetime not null,
        versionNum integer not null,
        entries longtext,
        comment_id bigint,
        document_id bigint,
        targetLanguage bigint not null,
        primary key (id),
        unique (document_id, targetLanguage)
    ) ENGINE=InnoDB;

    create table HPotEntryData (
        id bigint not null auto_increment,
        context varchar(255) binary,
        flags varchar(255) binary,
        refs varchar(255) binary,
        comment_id bigint,
        tf_id bigint unique,
        primary key (id),
        unique (tf_id)
    ) ENGINE=InnoDB;

    create table HProject (
        projecttype varchar(31) binary not null,
        id bigint not null auto_increment,
        creationDate datetime not null,
        lastChanged datetime not null,
        versionNum integer not null,
        slug varchar(40) binary not null,
        description varchar(100) binary,
        homeContent longtext,
        name varchar(80) binary not null,
        primary key (id),
        unique (slug)
    ) ENGINE=InnoDB;

    create table HProjectIteration (
        id bigint not null auto_increment,
        creationDate datetime not null,
        lastChanged datetime not null,
        versionNum integer not null,
        slug varchar(40) binary not null,
        active bit not null,
        description varchar(255) binary,
        name varchar(20) binary,
        parentId bigint,
        project_id bigint not null,
        primary key (id),
        unique (slug, project_id)
    ) ENGINE=InnoDB;

    create table HProject_Maintainer (
        personId bigint not null,
        projectId bigint not null,
        primary key (projectId, personId)
    ) ENGINE=InnoDB;

    create table HSimpleComment (
        id bigint not null auto_increment,
        comment longtext not null,
        primary key (id)
    ) ENGINE=InnoDB;

    create table HTextFlow (
        id bigint not null auto_increment,
        content longtext not null,
        obsolete bit not null,
        pos integer not null,
        resId varchar(255) binary not null,
        revision integer not null,
        comment_id bigint,
        document_id bigint not null,
        potEntryData_id bigint,
        primary key (id),
        unique (document_id, resId)
    ) ENGINE=InnoDB;

    create table HTextFlowHistory (
        id bigint not null auto_increment,
        content longtext,
        obsolete bit not null,
        pos integer,
        revision integer,
        tf_id bigint,
        primary key (id),
        unique (revision, tf_id)
    ) ENGINE=InnoDB;

    create table HTextFlowTarget (
        id bigint not null auto_increment,
        creationDate datetime not null,
        lastChanged datetime not null,
        versionNum integer not null,
        content longtext not null,
        state integer not null,
        tf_revision integer not null,
        comment_id bigint,
        last_modified_by_id bigint,
        locale bigint not null,
        tf_id bigint,
        primary key (id),
        unique (locale, tf_id)
    ) ENGINE=InnoDB;

    create table HTextFlowTargetHistory (
        id bigint not null auto_increment,
        content longtext,
        lastChanged datetime,
        state integer,
        tf_revision integer,
        versionNum integer,
        last_modified_by_id bigint,
        target_id bigint,
        primary key (id),
        unique (target_id, versionNum)
    ) ENGINE=InnoDB;

    alter table HAccountActivationKey 
        add index FK86E79CA4FA68C45F (accountId), 
        add constraint FK86E79CA4FA68C45F 
        foreign key (accountId) 
        references HAccount (id);

    alter table HAccountMembership 
        add index FK9D5DB27B3E684F5E (memberOf), 
        add constraint FK9D5DB27B3E684F5E 
        foreign key (memberOf) 
        references HAccountRole (id);

    alter table HAccountMembership 
        add index FK9D5DB27BFA68C45F (accountId), 
        add constraint FK9D5DB27BFA68C45F 
        foreign key (accountId) 
        references HAccount (id);

    alter table HAccountResetPasswordKey 
        add index FK85C9EFDAFA68C45F (accountId), 
        add constraint FK85C9EFDAFA68C45F 
        foreign key (accountId) 
        references HAccount (id);

    alter table HAccountRoleGroup 
        add index FK3321CC643E684F5E (memberOf), 
        add constraint FK3321CC643E684F5E 
        foreign key (memberOf) 
        references HAccountRole (id);

    alter table HAccountRoleGroup 
        add index FK3321CC642DF53D7E (roleId), 
        add constraint FK3321CC642DF53D7E 
        foreign key (roleId) 
        references HAccountRole (id);

    alter table HCommunity 
        add index FKD3DF208177D52F9 (ownerId), 
        add constraint FKD3DF208177D52F9 
        foreign key (ownerId) 
        references HPerson (id);

    alter table HCommunity_Member 
        add index FK8BEBF03860C55B1B (personId), 
        add constraint FK8BEBF03860C55B1B 
        foreign key (personId) 
        references HPerson (id);

    alter table HCommunity_Member 
        add index FK8BEBF038AF83AE57 (communityId), 
        add constraint FK8BEBF038AF83AE57 
        foreign key (communityId) 
        references HCommunity (id);

    alter table HCommunity_Officer 
        add index FK5CB3E75860C55B1B (personId), 
        add constraint FK5CB3E75860C55B1B 
        foreign key (personId) 
        references HPerson (id);

    alter table HCommunity_Officer 
        add index FK5CB3E758AF83AE57 (communityId), 
        add constraint FK5CB3E758AF83AE57 
        foreign key (communityId) 
        references HCommunity (id);

    alter table HDocument 
        add index FKEA766D836C9BADC1 (last_modified_by_id), 
        add constraint FKEA766D836C9BADC1 
        foreign key (last_modified_by_id) 
        references HPerson (id);

    alter table HDocument 
        add index FKEA766D83FEA3B54A (locale), 
        add constraint FKEA766D83FEA3B54A 
        foreign key (locale) 
        references HLocale (id);

    alter table HDocument 
        add index FKEA766D8351ED6DFD (project_iteration_id), 
        add constraint FKEA766D8351ED6DFD 
        foreign key (project_iteration_id) 
        references HProjectIteration (id);

    alter table HDocument 
        add index FKEA766D83136CC025 (poHeader_id), 
        add constraint FKEA766D83136CC025 
        foreign key (poHeader_id) 
        references HPoHeader (id);

    alter table HDocumentHistory 
        add index FK279765915383E2F0 (document_id), 
        add constraint FK279765915383E2F0 
        foreign key (document_id) 
        references HDocument (id);

    alter table HDocumentHistory 
        add index FK279765916C9BADC1 (last_modified_by_id), 
        add constraint FK279765916C9BADC1 
        foreign key (last_modified_by_id) 
        references HPerson (id);

    alter table HDocumentHistory 
        add index FK27976591FEA3B54A (locale), 
        add constraint FK27976591FEA3B54A 
        foreign key (locale) 
        references HLocale (id);

    alter table HLocale_Member 
        add index FK82DF50D760C55B1B (personId), 
        add constraint FK82DF50D760C55B1B 
        foreign key (personId) 
        references HPerson (id);

    alter table HLocale_Member 
        add index FK82DF50D73A932491 (supportedLanguageId), 
        add constraint FK82DF50D73A932491 
        foreign key (supportedLanguageId) 
        references HLocale (id);

    alter table HPerson 
        add index FK6F0931BDFA68C45F (accountId), 
        add constraint FK6F0931BDFA68C45F 
        foreign key (accountId) 
        references HAccount (id);

    alter table HPoHeader 
        add index FK9A0ABDD45383E2F0 (document_id), 
        add constraint FK9A0ABDD45383E2F0 
        foreign key (document_id) 
        references HDocument (id);

    alter table HPoHeader 
        add index FK9A0ABDD4B7A40DF2 (comment_id), 
        add constraint FK9A0ABDD4B7A40DF2 
        foreign key (comment_id) 
        references HSimpleComment (id);

    alter table HPoTargetHeader 
        add index FK1BC719855383E2F0 (document_id), 
        add constraint FK1BC719855383E2F0 
        foreign key (document_id) 
        references HDocument (id);

    alter table HPoTargetHeader 
        add index FK1BC719857D208AD9 (targetLanguage), 
        add constraint FK1BC719857D208AD9 
        foreign key (targetLanguage) 
        references HLocale (id);

    alter table HPoTargetHeader 
        add index FK1BC71985B7A40DF2 (comment_id), 
        add constraint FK1BC71985B7A40DF2 
        foreign key (comment_id) 
        references HSimpleComment (id);

    alter table HPotEntryData 
        add index FK17A648CFB7A40DF2 (comment_id), 
        add constraint FK17A648CFB7A40DF2 
        foreign key (comment_id) 
        references HSimpleComment (id);

    alter table HPotEntryData 
        add index FK17A648CFCCAD9D19 (tf_id), 
        add constraint FK17A648CFCCAD9D19 
        foreign key (tf_id) 
        references HTextFlow (id);

    alter table HProjectIteration 
        add index FK31C1E42C4BCEEA93 (project_id), 
        add constraint FK31C1E42C4BCEEA93 
        foreign key (project_id) 
        references HProject (id);

    alter table HProjectIteration 
        add index FK31C1E42C5B1D181F (parentId), 
        add constraint FK31C1E42C5B1D181F 
        foreign key (parentId) 
        references HProjectIteration (id);

    alter table HProject_Maintainer 
        add index FK1491F2E660C55B1B (personId), 
        add constraint FK1491F2E660C55B1B 
        foreign key (personId) 
        references HPerson (id);

    alter table HProject_Maintainer 
        add index FK1491F2E665B5BB37 (projectId), 
        add constraint FK1491F2E665B5BB37 
        foreign key (projectId) 
        references HProject (id);

    alter table HTextFlow 
        add index FK7B40F8635383E2F0 (document_id), 
        add constraint FK7B40F8635383E2F0 
        foreign key (document_id) 
        references HDocument (id);

    alter table HTextFlow 
        add index FK7B40F8638D8E70A5 (potEntryData_id), 
        add constraint FK7B40F8638D8E70A5 
        foreign key (potEntryData_id) 
        references HPotEntryData (id);

    alter table HTextFlow 
        add index FK7B40F863B7A40DF2 (comment_id), 
        add constraint FK7B40F863B7A40DF2 
        foreign key (comment_id) 
        references HSimpleComment (id);

    alter table HTextFlowHistory 
        add index FK46C4DEB1CCAD9D19 (tf_id), 
        add constraint FK46C4DEB1CCAD9D19 
        foreign key (tf_id) 
        references HTextFlow (id);

    alter table HTextFlowTarget 
        add index FK1E933FD46C9BADC1 (last_modified_by_id), 
        add constraint FK1E933FD46C9BADC1 
        foreign key (last_modified_by_id) 
        references HPerson (id);

    alter table HTextFlowTarget 
        add index FK1E933FD4FEA3B54A (locale), 
        add constraint FK1E933FD4FEA3B54A 
        foreign key (locale) 
        references HLocale (id);

    alter table HTextFlowTarget 
        add index FK1E933FD4B7A40DF2 (comment_id), 
        add constraint FK1E933FD4B7A40DF2 
        foreign key (comment_id) 
        references HSimpleComment (id);

    alter table HTextFlowTarget 
        add index FK1E933FD4CCAD9D19 (tf_id), 
        add constraint FK1E933FD4CCAD9D19 
        foreign key (tf_id) 
        references HTextFlow (id);

    alter table HTextFlowTargetHistory 
        add index FKF10986206C9BADC1 (last_modified_by_id), 
        add constraint FKF10986206C9BADC1 
        foreign key (last_modified_by_id) 
        references HPerson (id);

    alter table HTextFlowTargetHistory 
        add index FKF109862080727E8B (target_id), 
        add constraint FKF109862080727E8B 
        foreign key (target_id) 
        references HTextFlowTarget (id);
