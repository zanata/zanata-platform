package org.zanata.rest;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.enterprise.context.RequestScoped;
import javax.persistence.EntityManager;
import javax.security.auth.Subject;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import javax.inject.Inject;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.slf4j.Logger;
import org.zanata.security.Identity;
import org.zanata.common.LocaleId;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.security.annotations.NoSecurityCheck;
import org.zanata.util.SampleDataProfile;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RequestScoped
@Path("/data/sample")
@NoSecurityCheck
public class SampleDataResourceImpl implements SampleDataResource {
    private static final Logger log = getLogger(SampleDataResourceImpl.class);

    @Inject
    private SampleDataProfile sampleDataProfile;

    @SuppressFBWarnings(value = "SE_BAD_FIELD")
    @Inject
    private EntityManager entityManager;

    @Inject
    private Identity identity;

    @Override
    public Response makeSampleLanguages() {
        new RunAsOperationForTest() {
            @Override
            public void execute() {
                sampleDataProfile.makeSampleLanguages();
            }
        }.run();

        return Response.ok().build();
    }

    @Override
    @Transactional
    public Response addLanguage(final String localeId,
            @Nullable String pluralForms) {
        new RunAsOperationForTest() {
            @Override
            public void execute() {
                sampleDataProfile
                        .makeLanguage(true, new LocaleId(localeId), pluralForms);
            }
        }.run();

        return Response.ok().build();
    }

    @Override
    public Response makeSampleUsers() {
        new RunAsOperationForTest() {
            public void execute() {
                sampleDataProfile.makeSampleUsers();
            }
        }.run();

        return Response.ok().build();
    }

    @Override
    @Transactional
    public Response userJoinsLanguageTeams(
        @PathParam("username") String username,
        @QueryParam("locales") String localesCSV) {

        List<String> localesIds = Lists.newArrayList(
                Splitter.on(",").omitEmptyStrings().trimResults().split(
                        localesCSV));
        List<LocaleId> locales =
                localesIds.stream().map(LocaleId::new)
                        .collect(Collectors.toList());
        final HPerson hPerson = entityManager
            .createQuery("from HPerson p where p.account.username = :username",
                HPerson.class).setParameter("username", username).getSingleResult();
        final List<HLocale> hLocales = entityManager
            .createQuery("from HLocale where localeId in (:locales)",
                HLocale.class).setParameter("locales", locales).getResultList();
        new RunAsOperationForTest() {
            public void execute() {
                sampleDataProfile.addUsersToLanguage(hPerson, hLocales);
            }
        }.run();

        return Response.ok().build();
    }

    @Override
    public Response makeSampleProject() {
        new RunAsOperationForTest() {
            public void execute() {
                sampleDataProfile.makeSampleProject();
            }
        }.run();

        return Response.ok().build();
    }

    @Override
    public Response deleteExceptEssentialData() {
        new RunAsOperationForTest() {
            public void execute() {
                sampleDataProfile.deleteExceptEssentialData();
            }
        }.run();

        return Response.ok().build();
    }

    @Override
    public Response allowAnonymousUser(boolean value) {
        new RunAsOperationForTest() {
            @Override
            public void execute() {
                sampleDataProfile.setAllowAnonymousUserConfig(value);
            }
        }.run();
        return Response.ok().build();
    }

    @Override
    @SuppressFBWarnings("GBU_GUAVA_BETA_CLASS_USAGE")
    public Response dummyService(long timeInMillis,
            String qualifiedExceptionClass) throws Throwable {
        if (timeInMillis > 0) {
            log.info("I am going to take a nap for {} ms", timeInMillis);
            Thread.sleep(timeInMillis);
        }
        if (Strings.isNullOrEmpty(qualifiedExceptionClass)) {
            return Response.ok().build();
        }

        try {
            Class<?> exceptionClass = Class.forName(qualifiedExceptionClass);
            boolean isThrowable =
                    Throwable.class.isAssignableFrom(exceptionClass);
            if (!isThrowable) {
                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity(qualifiedExceptionClass + " is not a Throwable")
                        .build();
            }
            log.info("about to throw exception: {}", exceptionClass);
            throw (Throwable) exceptionClass.newInstance();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage()).build();
        }
    }

    abstract class RunAsOperationForTest implements
            Identity.RunAsOperation {
        @Override
        public Principal getPrincipal() {
            return null;
        }

        @Override
        public Subject getSubject() {
            return null;
        }

        @Override
        public boolean isSystemOperation() {
            // A system operation allows any security checks to pass
            return true;
        }

        @Override
        public void run() {
            identity.runAs(this);
        }
    }
}
