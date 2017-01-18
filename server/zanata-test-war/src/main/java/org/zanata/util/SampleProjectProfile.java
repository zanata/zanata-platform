package org.zanata.util;

import com.github.huangp.entityunit.entity.Callbacks;
import com.github.huangp.entityunit.entity.EntityCleaner;
import com.github.huangp.entityunit.entity.EntityMaker;
import com.github.huangp.entityunit.entity.EntityMakerBuilder;
import com.github.huangp.entityunit.entity.FixIdCallback;
import com.github.huangp.entityunit.maker.FixedValueMaker;
import com.ibm.icu.util.ULocale;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.slf4j.Logger;
import org.zanata.common.ContentType;
import org.zanata.common.LocaleId;
import org.zanata.model.HAccount;
import org.zanata.model.HAccountRole;
import org.zanata.model.HApplicationConfiguration;
import org.zanata.model.HDocument;
import org.zanata.model.HGlossaryEntry;
import org.zanata.model.HGlossaryTerm;
import org.zanata.model.HLocale;
import org.zanata.model.HLocaleMember;
import org.zanata.model.HPerson;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlowTarget;
import org.zanata.model.tm.TransMemoryUnit;

import javax.annotation.Nullable;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@ApplicationScoped
@Transactional
public class SampleProjectProfile {
    private static final Logger log = getLogger(SampleProjectProfile.class);

    @Inject
    private EntityManager entityManager;

    @Inject @Zanata
    private EntityManagerFactory entityManagerFactory;

    private HLocale enUSLocale;

    private HPerson admin;

    public void deleteExceptEssentialData() {
        EntityCleaner.deleteAll(entityManager, ZanataEntities
                .entitiesForRemoval());

        enUSLocale = makeLanguage(false, LocaleId.EN_US, "nplurals=2; plural=(n != 1);");

        List<HApplicationConfiguration> configurations = entityManager
                .createQuery("from HApplicationConfiguration",
                        HApplicationConfiguration.class).getResultList();

        for (HApplicationConfiguration configuration : configurations) {
            entityManager.remove(configuration);
        }
        entityManager.flush();

        purgeLuceneIndexes();
        // TODO probably should delete cache as well
    }

    private void purgeLuceneIndexes() {
        FullTextEntityManager em =
                Search.getFullTextEntityManager(
                        entityManagerFactory.createEntityManager());
        try {
            em.purgeAll(HAccount.class);
            em.purgeAll(HGlossaryEntry.class);
            em.purgeAll(HGlossaryTerm.class);
            em.purgeAll(HProject.class);
            em.purgeAll(HProjectIteration.class);
            em.purgeAll(TransMemoryUnit.class);
            em.purgeAll(HTextFlowTarget.class);
        } finally {
            em.close();
        }
    }

    public void makeSampleLanguages() {
        makeLanguage(true, LocaleId.FR, "nplurals=2; plural=(n > 1);");

        makeLanguage(true, new LocaleId("hi"), "nplurals=2; plural=(n != 1);");

        makeLanguage(true, new LocaleId("pl"), "nplurals=3; plural=(n==1 ? 0 : n%10>=2 && n%10<=4 && (n%100<10 || n%100>=20) ? 1 : 2);");
    }

    public HLocale makeLanguage(boolean enabledByDefault, LocaleId localeId,
            String displayName, String nativeName,
            @Nullable String pluralForms) {
        return forLocale(enabledByDefault, localeId, displayName, nativeName, pluralForms)
                .makeAndPersist(entityManager,
                    HLocale.class);
    }

    public HLocale makeLanguage(boolean enabledByDefault, LocaleId localeId,
            @Nullable String pluralForms) {
        ULocale uLocale = new ULocale(localeId.getId());
        return forLocale(enabledByDefault, localeId, uLocale.getDisplayName(),
            uLocale.getDisplayName(uLocale), pluralForms).makeAndPersist(entityManager,
                HLocale.class);
    }

    private static EntityMaker forLocale(boolean enabledByDefault,
            LocaleId localeId, String displayName, String nativeName, String pluralForms) {
        return EntityMakerBuilder
                .builder()
                .addFieldOrPropertyMaker(HLocale.class, "active",
                    FixedValueMaker.ALWAYS_TRUE_MAKER)
                .addFieldOrPropertyMaker(HLocale.class, "displayName",
                        FixedValueMaker.fix(displayName))
                .addFieldOrPropertyMaker(HLocale.class, "nativeName",
                    FixedValueMaker.fix(nativeName))
                .addFieldOrPropertyMaker(HLocale.class, "pluralForms",
                    FixedValueMaker.fix(pluralForms))
                .addFieldOrPropertyMaker(HLocale.class, "enabledByDefault",
                        FixedValueMaker.fix(enabledByDefault))
                .addConstructorParameterMaker(HLocale.class, 0,
                    FixedValueMaker.fix(localeId)).build();
    }

    public void makeSampleUsers() {
        HAccount adminAccount =
                makeAccount("admin", "Eyox7xbNQ09MkIfRyH+rjg==",
                        "b6d7044e9ee3b2447c28fb7c50d86d98", 1L);
        assignAccountRole(1L, "admin");

        admin = makePerson(1L, "Administrator", "admin");
        assignAccountToPerson(adminAccount, admin);

        HAccount translatorAccount =
                makeAccount("translator", "Fr5JHlcaEqKLSHjnBm4gXg==",
                        "d83882201764f7d339e97c4b087f0806", 2L);
        assignAccountRole(2L, "user");
        HPerson translator = makePerson(2L, "translator", "translator");
        assignAccountToPerson(translatorAccount, translator);

        HAccount glossaristAccount =
                makeAccount("glossarist", "fRIeiPDPlSMtHbBNoqDjNQ==",
                        "b1e3daa18e41c0ce79829e87ce66b201", 3L);
        assignAccountRole(3L, "glossarist");
        HPerson glossarist = makePerson(3L, "glossarist", "glossarist");
        assignAccountToPerson(glossaristAccount, glossarist);

        HAccount gAdminAccount =
                makeAccount("glossaryadmin", "/W0YpteXk+WtymQ7H84kPQ==",
                        "5a6a34d28d39ff90ea47402311f339d4", 4L);
        assignAccountRole(4L, "glossary-admin");
        HPerson glossaryAdmin =
                makePerson(4L, "glossary-admin", "glossary-admin");
        assignAccountToPerson(gAdminAccount, glossaryAdmin);
    }

    private HPerson makePerson(Long wantedIdValue, String nameValue,
            String username) {
        return EntityMakerBuilder
                .builder()
                .addFieldOrPropertyMaker(HPerson.class, "name",
                        FixedValueMaker.fix(nameValue))
                .addFieldOrPropertyMaker(HPerson.class, "email",
                        FixedValueMaker.fix(username + "@example.com"))
                .build()
                .makeAndPersist(entityManager, HPerson.class,
                        new FixIdCallback(HPerson.class, wantedIdValue));
    }

    private void assignAccountToPerson(HAccount account, HPerson hPerson) {
        hPerson.setAccount(account);
    }

    private void assignAccountRole(Long accountId, String roleName) {
        entityManager.find(HAccount.class, accountId).getRoles()
                .add(getAccountRole(roleName));
    }

    private HAccountRole getAccountRole(String roleName) {
        SampleProjectProfile.log.debug("get account role with name {}", roleName);
        return entityManager.createQuery(
                String.format("from HAccountRole where name = '%s' ", roleName),
                HAccountRole.class)
                .getSingleResult();
    }

    public void addUsersToLanguage(HPerson person, Iterable<HLocale> locales) {
        for (HLocale locale : locales) {
            entityManager.persist(asLocaleMember(person, locale));
        }
    }

    private static HLocaleMember asLocaleMember(HPerson person, HLocale locale) {
        return new HLocaleMember(person, locale, true, false, false);
    }

    private HAccount makeAccount(String username, String passwordHash,
            String apiKey, Long accountId) {
        EntityMaker maker = EntityMakerBuilder
                        .builder()
                        // .includeOptionalOneToOne()
                        .addFieldOrPropertyMaker(HAccount.class, "enabled",
                                FixedValueMaker.ALWAYS_TRUE_MAKER)
                        .addFieldOrPropertyMaker(HAccount.class, "username",
                                FixedValueMaker.fix(username))
                        .addFieldOrPropertyMaker(HAccount.class,
                                "passwordHash",
                                FixedValueMaker.fix(passwordHash))
                        .addFieldOrPropertyMaker(HAccount.class, "apiKey",
                                FixedValueMaker.fix(apiKey)).build();
        return maker.makeAndPersist(entityManager, HAccount.class,
                new FixIdCallback(HAccount.class, accountId));
    }

    public void makeSampleProject() {
        EntityMaker maker =
                EntityMakerBuilder
                        .builder()
                        // project
                        .addFieldOrPropertyMaker(HProject.class, "slug",
                                FixedValueMaker.fix("about-fedora"))
                        .addFieldOrPropertyMaker(HProject.class, "name",
                                FixedValueMaker.fix("about fedora"))
                        // iteration
                        .addFieldOrPropertyMaker(HProjectIteration.class,
                                "slug", FixedValueMaker.fix("master"))
                        .addFieldOrPropertyMaker(HProject.class,
                                "sourceViewURL",
                          FixedValueMaker.EMPTY_STRING_MAKER)
                        // document
                        // public HDocument(String docId, String name, String
                        // path,
                        // ContentType contentType, HLocale locale)
                        .addConstructorParameterMaker(HDocument.class, 0,
                                FixedValueMaker.fix("About_Fedora"))
                        .addConstructorParameterMaker(HDocument.class, 1,
                                FixedValueMaker.fix("About_Fedora"))
                        .addConstructorParameterMaker(HDocument.class, 2,
                                FixedValueMaker.EMPTY_STRING_MAKER)
                        .addConstructorParameterMaker(HDocument.class, 3,
                                FixedValueMaker.fix(ContentType.PO))
                        .reuseEntity(enUSLocale).build();

        maker.makeAndPersist(entityManager, HTextFlowTarget.class,
                Callbacks.wireManyToMany(HProject.class, admin));

    }

    public void setAllowAnonymousUserConfig(boolean value) {
        List<HApplicationConfiguration> config = entityManager
                .createQuery("from HApplicationConfiguration where key = :key",
                        HApplicationConfiguration.class)
                .setParameter("key",
                        HApplicationConfiguration.KEY_ALLOW_ANONYMOUS_USER)
                .getResultList();
        if (config.size() == 1) {
            HApplicationConfiguration configEntity = config.get(0);
            configEntity.setValue(Boolean.toString(value));
            entityManager.persist(configEntity);
        }
        // since we assume null value means allow anonymous access, we don't need to do anything here.
    }
}
