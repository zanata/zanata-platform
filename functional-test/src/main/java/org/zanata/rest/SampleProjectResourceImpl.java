package org.zanata.rest;

import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.security.RunAsOperation;
import org.zanata.common.LocaleId;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.util.SampleProjectProfile;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Path("/test/data/sample")
@Name("sampleProjectResourceImpl")
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
        @QueryParam("locales") Set<LocaleId> locales) {


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
}
