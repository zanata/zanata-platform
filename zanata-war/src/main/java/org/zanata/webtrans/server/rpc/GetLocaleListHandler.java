package org.zanata.webtrans.server.rpc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.dao.LocaleDAO;
import org.zanata.dao.ProjectDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.model.HLocale;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.security.ZanataIdentity;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.shared.model.IdForLocale;
import org.zanata.webtrans.shared.model.Locale;
import org.zanata.webtrans.shared.model.ProjectIterationId;
import org.zanata.webtrans.shared.rpc.GetLocaleList;
import org.zanata.webtrans.shared.rpc.GetLocaleListResult;

@Name("webtrans.gwt.GetLocaleListHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(GetLocaleList.class)
public class GetLocaleListHandler extends AbstractActionHandler<GetLocaleList, GetLocaleListResult>
{
   @In
   private ZanataIdentity identity;
   @In
   private LocaleDAO localeDAO;
   @In
   private ProjectIterationDAO projectIterationDAO;
   @In
   private ProjectDAO projectDAO;
   private ProjectIterationId iterationId;
   private String projectSlug;

   @Override
   public GetLocaleListResult execute(GetLocaleList action, ExecutionContext context) throws ActionException
   {
      identity.checkLoggedIn();

      iterationId = action.getWorkspaceId().getProjectIterationId();
      projectSlug = iterationId.getProjectSlug();

      List<Locale> locales = new ArrayList<Locale>();

      List<HLocale> hLocales = getLocaleList();

      for (HLocale hLocale : hLocales)
      {
         Locale locale = new Locale(new IdForLocale(hLocale.getId(), hLocale.getLocaleId()), hLocale.retrieveDisplayName());
         locales.add(locale);
      }
      return new GetLocaleListResult(locales);
   }

   @Override
   public void rollback(GetLocaleList action, GetLocaleListResult result, ExecutionContext context) throws ActionException
   {
   }

   /**
    *
    * @return the current locales for this iteration
    */
   private List<HLocale> getLocaleList()
   {
      HProjectIteration iteration = projectIterationDAO.getBySlug(projectSlug, iterationId.getIterationSlug());
      if (iteration.getOverrideLocales())
      {
         return new ArrayList<HLocale>(iteration.getCustomizedLocales());
      }
      return getSupportedLanguageByProject(projectSlug);
   }

   private List<HLocale> getSupportedLanguageByProject(@Nonnull String project)
   {
      HProject proj = projectDAO.getBySlug(project);
      if (proj.getOverrideLocales())
      {
         return new ArrayList<HLocale>(proj.getCustomizedLocales());
      }
      return localeDAO.findAllActiveAndEnabledByDefault();
   }
}
