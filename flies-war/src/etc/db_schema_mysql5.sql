
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

    create table HAccountPermission (
        permissionId integer not null auto_increment,
        action varchar(255) binary,
        discriminator varchar(255) binary,
        recipient varchar(255) binary,
        target varchar(255) binary,
        primary key (permissionId)
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
        communityId bigint not null,
        personId bigint not null,
        primary key (personId, communityId)
    ) ENGINE=InnoDB;

    create table HCommunity_Officer (
        communityId bigint not null,
        personId bigint not null,
        primary key (personId, communityId)
    ) ENGINE=InnoDB;

    create table HDocument (
        id bigint not null auto_increment,
        creationDate datetime not null,
        lastChanged datetime not null,
        versionNum integer not null,
        contentType varchar(255) binary not null,
        docId varchar(255) binary not null,
        locale varchar(255) binary not null,
        name varchar(255) binary not null,
        obsolete bit not null,
        path varchar(255) binary not null,
        revision integer not null,
        last_modified_by_id bigint,
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
        locale varchar(255) binary not null,
        name varchar(255) binary,
        obsolete bit not null,
        path varchar(255) binary,
        revision integer,
        document_id bigint,
        last_modified_by_id bigint,
        primary key (id),
        unique (document_id, revision)
    ) ENGINE=InnoDB;

    create table HFliesLocale (
        id varchar(80) binary not null,
        icuLocaleId varchar(255) binary not null,
        parentId varchar(80) binary,
        primary key (id)
    ) ENGINE=InnoDB;

    create table HFliesLocale_Friends (
        localeId varchar(80) binary not null,
        friendId varchar(80) binary not null
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
        targetLanguage varchar(255) binary not null,
        comment_id bigint,
        document_id bigint,
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
        locale varchar(255) binary not null,
        state integer not null,
        tf_revision integer not null,
        comment_id bigint,
        last_modified_by_id bigint,
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

    create table HTribe (
        id bigint not null auto_increment,
        creationDate datetime not null,
        lastChanged datetime not null,
        versionNum integer not null,
        chiefId bigint,
        localeId varchar(80) binary not null,
        primary key (id),
        unique (localeId)
    ) ENGINE=InnoDB;

    create table HTribe_Leader (
        personId bigint not null,
        tribeId bigint not null,
        primary key (tribeId, personId)
    ) ENGINE=InnoDB;

    create table HTribe_Member (
        personId bigint not null,
        tribeId bigint not null,
        primary key (tribeId, personId)
    ) ENGINE=InnoDB;

    alter table HAccountActivationKey 
        add index FK86E79CA44A0EDB13 (accountId), 
        add constraint FK86E79CA44A0EDB13 
        foreign key (accountId) 
        references HAccount (id);

    alter table HAccountMembership 
        add index FK9D5DB27B8AFBEC12 (memberOf), 
        add constraint FK9D5DB27B8AFBEC12 
        foreign key (memberOf) 
        references HAccountRole (id);

    alter table HAccountMembership 
        add index FK9D5DB27B4A0EDB13 (accountId), 
        add constraint FK9D5DB27B4A0EDB13 
        foreign key (accountId) 
        references HAccount (id);

    alter table HAccountResetPasswordKey 
        add index FK85C9EFDA4A0EDB13 (accountId), 
        add constraint FK85C9EFDA4A0EDB13 
        foreign key (accountId) 
        references HAccount (id);

    alter table HAccountRoleGroup 
        add index FK3321CC648AFBEC12 (memberOf), 
        add constraint FK3321CC648AFBEC12 
        foreign key (memberOf) 
        references HAccountRole (id);

    alter table HAccountRoleGroup 
        add index FK3321CC647A88DA32 (roleId), 
        add constraint FK3321CC647A88DA32 
        foreign key (roleId) 
        references HAccountRole (id);

    alter table HCommunity 
        add index FKD3DF20814C1F95C5 (ownerId), 
        add constraint FKD3DF20814C1F95C5 
        foreign key (ownerId) 
        references HPerson (id);

    alter table HCommunity_Member 
        add index FK8BEBF038A5679DE7 (personId), 
        add constraint FK8BEBF038A5679DE7 
        foreign key (personId) 
        references HPerson (id);

    alter table HCommunity_Member 
        add index FK8BEBF038ADFEE80B (communityId), 
        add constraint FK8BEBF038ADFEE80B 
        foreign key (communityId) 
        references HCommunity (id);

    alter table HCommunity_Officer 
        add index FK5CB3E758A5679DE7 (personId), 
        add constraint FK5CB3E758A5679DE7 
        foreign key (personId) 
        references HPerson (id);

    alter table HCommunity_Officer 
        add index FK5CB3E758ADFEE80B (communityId), 
        add constraint FK5CB3E758ADFEE80B 
        foreign key (communityId) 
        references HCommunity (id);

    alter table HDocument 
        add index FKEA766D83B13DF08D (last_modified_by_id), 
        add constraint FKEA766D83B13DF08D 
        foreign key (last_modified_by_id) 
        references HPerson (id);

    alter table HDocument 
        add index FKEA766D835063A1C9 (project_iteration_id), 
        add constraint FKEA766D835063A1C9 
        foreign key (project_iteration_id) 
        references HProjectIteration (id);

    alter table HDocument 
        add index FKEA766D8360005CD9 (poHeader_id), 
        add constraint FKEA766D8360005CD9 
        foreign key (poHeader_id) 
        references HPoHeader (id);

    alter table HDocumentHistory 
        add index FK27976591F8A0A2BC (document_id), 
        add constraint FK27976591F8A0A2BC 
        foreign key (document_id) 
        references HDocument (id);

    alter table HDocumentHistory 
        add index FK27976591B13DF08D (last_modified_by_id), 
        add constraint FK27976591B13DF08D 
        foreign key (last_modified_by_id) 
        references HPerson (id);

    alter table HFliesLocale 
        add index FK6CAF0A33D884B2E (parentId), 
        add constraint FK6CAF0A33D884B2E 
        foreign key (parentId) 
        references HFliesLocale (id);

    alter table HFliesLocale_Friends 
        add index FKF87125D9A2AAA022 (friendId), 
        add constraint FKF87125D9A2AAA022 
        foreign key (friendId) 
        references HFliesLocale (id);

    alter table HFliesLocale_Friends 
        add index FKF87125D968C8A4DE (localeId), 
        add constraint FKF87125D968C8A4DE 
        foreign key (localeId) 
        references HFliesLocale (id);

    alter table HPerson 
        add index FK6F0931BD4A0EDB13 (accountId), 
        add constraint FK6F0931BD4A0EDB13 
        foreign key (accountId) 
        references HAccount (id);

    alter table HPoHeader 
        add index FK9A0ABDD4F8A0A2BC (document_id), 
        add constraint FK9A0ABDD4F8A0A2BC 
        foreign key (document_id) 
        references HDocument (id);

    alter table HPoHeader 
        add index FK9A0ABDD42DC34DA6 (comment_id), 
        add constraint FK9A0ABDD42DC34DA6 
        foreign key (comment_id) 
        references HSimpleComment (id);

    alter table HPoTargetHeader 
        add index FK1BC71985F8A0A2BC (document_id), 
        add constraint FK1BC71985F8A0A2BC 
        foreign key (document_id) 
        references HDocument (id);

    alter table HPoTargetHeader 
        add index FK1BC719852DC34DA6 (comment_id), 
        add constraint FK1BC719852DC34DA6 
        foreign key (comment_id) 
        references HSimpleComment (id);

    alter table HPotEntryData 
        add index FK17A648CF2DC34DA6 (comment_id), 
        add constraint FK17A648CF2DC34DA6 
        foreign key (comment_id) 
        references HSimpleComment (id);

    alter table HPotEntryData 
        add index FK17A648CF71CA5CE5 (tf_id), 
        add constraint FK17A648CF71CA5CE5 
        foreign key (tf_id) 
        references HTextFlow (id);

    alter table HProjectIteration 
        add index FK31C1E42C4A451E5F (project_id), 
        add constraint FK31C1E42C4A451E5F 
        foreign key (project_id) 
        references HProject (id);

    alter table HProjectIteration 
        add index FK31C1E42C59934BEB (parentId), 
        add constraint FK31C1E42C59934BEB 
        foreign key (parentId) 
        references HProjectIteration (id);

    alter table HProject_Maintainer 
        add index FK1491F2E6A5679DE7 (personId), 
        add constraint FK1491F2E6A5679DE7 
        foreign key (personId) 
        references HPerson (id);

    alter table HProject_Maintainer 
        add index FK1491F2E6B55BD1EB (projectId), 
        add constraint FK1491F2E6B55BD1EB 
        foreign key (projectId) 
        references HProject (id);

    alter table HTextFlow 
        add index FK7B40F863F8A0A2BC (document_id), 
        add constraint FK7B40F863F8A0A2BC 
        foreign key (document_id) 
        references HDocument (id);

    alter table HTextFlow 
        add index FK7B40F863F8DC9359 (potEntryData_id), 
        add constraint FK7B40F863F8DC9359 
        foreign key (potEntryData_id) 
        references HPotEntryData (id);

    alter table HTextFlow 
        add index FK7B40F8632DC34DA6 (comment_id), 
        add constraint FK7B40F8632DC34DA6 
        foreign key (comment_id) 
        references HSimpleComment (id);

    alter table HTextFlowHistory 
        add index FK46C4DEB171CA5CE5 (tf_id), 
        add constraint FK46C4DEB171CA5CE5 
        foreign key (tf_id) 
        references HTextFlow (id);

    alter table HTextFlowTarget 
        add index FK1E933FD4B13DF08D (last_modified_by_id), 
        add constraint FK1E933FD4B13DF08D 
        foreign key (last_modified_by_id) 
        references HPerson (id);

    alter table HTextFlowTarget 
        add index FK1E933FD42DC34DA6 (comment_id), 
        add constraint FK1E933FD42DC34DA6 
        foreign key (comment_id) 
        references HSimpleComment (id);

    alter table HTextFlowTarget 
        add index FK1E933FD471CA5CE5 (tf_id), 
        add constraint FK1E933FD471CA5CE5 
        foreign key (tf_id) 
        references HTextFlow (id);

    alter table HTextFlowTargetHistory 
        add index FKF1098620B13DF08D (last_modified_by_id), 
        add constraint FKF1098620B13DF08D 
        foreign key (last_modified_by_id) 
        references HPerson (id);

    alter table HTextFlowTargetHistory 
        add index FKF1098620CE3B3557 (target_id), 
        add constraint FKF1098620CE3B3557 
        foreign key (target_id) 
        references HTextFlowTarget (id);

    alter table HTribe 
        add index FK7FB20BC6B7757AD7 (chiefId), 
        add constraint FK7FB20BC6B7757AD7 
        foreign key (chiefId) 
        references HPerson (id);

    alter table HTribe 
        add index FK7FB20BC668C8A4DE (localeId), 
        add constraint FK7FB20BC668C8A4DE 
        foreign key (localeId) 
        references HFliesLocale (id);

    alter table HTribe_Leader 
        add index FK20177C2A5679DE7 (personId), 
        add constraint FK20177C2A5679DE7 
        foreign key (personId) 
        references HPerson (id);

    alter table HTribe_Leader 
        add index FK20177C2EED54855 (tribeId), 
        add constraint FK20177C2EED54855 
        foreign key (tribeId) 
        references HTribe (id);

    alter table HTribe_Member 
        add index FK3BBBD53A5679DE7 (personId), 
        add constraint FK3BBBD53A5679DE7 
        foreign key (personId) 
        references HPerson (id);

    alter table HTribe_Member 
        add index FK3BBBD53EED54855 (tribeId), 
        add constraint FK3BBBD53EED54855 
        foreign key (tribeId) 
        references HTribe (id);
