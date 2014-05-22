package org.zanata.rest;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import lombok.extern.slf4j.Slf4j;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.security.RunAsOperation;
import org.zanata.common.LocaleId;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.util.SampleProjectProfile;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Uninterruptibles;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Path("/test/data/sample")
@Name("sampleProjectResourceImpl")
@Slf4j
public class SampleProjectResourceImpl implements SampleProjectResource {

    @In(create = true)
    private SampleProjectProfile sampleProjectProfile;

    @In
    private EntityManager entityManager;

    @Override
    public Response makeSampleLanguages() {
        new RunAsOperation() {
            @Override
            public void execute() {
                sampleProjectProfile.makeSampleLanguages();
            }
        }.addRole("admin").run();
        return Response.ok().build();
    }

    @Override
    public Response makeSampleUsers() {
        new RunAsOperation() {
            public void execute() {
                sampleProjectProfile.makeSampleUsers();
            }
        }.addRole("admin").run();
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
                Lists.transform(localesIds, new Function<String, LocaleId>() {
                    @Override
                    public LocaleId apply(String input) {
                        return new LocaleId(input);
                    }
                });
        final HPerson hPerson = entityManager
            .createQuery("from HPerson p where p.account.username = :username",
                HPerson.class).setParameter("username", username).getSingleResult();
        final List<HLocale> hLocales = entityManager
            .createQuery("from HLocale where localeId in (:locales)",
                HLocale.class).setParameter("locales", locales).getResultList();
        new RunAsOperation() {
            public void execute() {
                sampleProjectProfile.addUsersToLanguage(hPerson, hLocales);
            }
        }.addRole("admin").run();
        return Response.ok().build();
    }

    @Override
    public Response makeSampleProject() {
        new RunAsOperation() {
            public void execute() {
                sampleProjectProfile.makeSampleProject();
            }
        }.addRole("admin").run();
        return Response.ok().build();
    }

    @Override
    public Response deleteExceptEssentialData() {
        new RunAsOperation() {
            public void execute() {
                sampleProjectProfile.deleteExceptEssentialData();
            }
        }.addRole("admin").run();
        return Response.ok().build();
    }

    @Override
    public Response dummyService(long timeInMillis,
            String qualifiedExceptionClass) throws Throwable {
        if (timeInMillis > 0) {
            log.info("I am going to take a nap for {} ms", timeInMillis);
            Uninterruptibles.sleepUninterruptibly(timeInMillis,
                    TimeUnit.MILLISECONDS);
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
}
