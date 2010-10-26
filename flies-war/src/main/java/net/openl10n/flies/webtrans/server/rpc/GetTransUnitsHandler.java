package net.openl10n.flies.webtrans.server.rpc;

import java.util.ArrayList;
import java.util.List;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;
import net.openl10n.flies.common.ContentState;
import net.openl10n.flies.model.HLocale;
import net.openl10n.flies.model.HTextFlow;
import net.openl10n.flies.model.HTextFlowTarget;
import net.openl10n.flies.security.FliesIdentity;
import net.openl10n.flies.service.LocaleService;
import net.openl10n.flies.webtrans.server.ActionHandlerFor;
import net.openl10n.flies.webtrans.shared.model.TransUnit;
import net.openl10n.flies.webtrans.shared.model.TransUnitId;
import net.openl10n.flies.webtrans.shared.rpc.GetTransUnits;
import net.openl10n.flies.webtrans.shared.rpc.GetTransUnitsResult;

import org.hibernate.Query;
import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

@Name("webtrans.gwt.GetTransUnitHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(GetTransUnits.class)
public class GetTransUnitsHandler extends AbstractActionHandler<GetTransUnits, GetTransUnitsResult>
{

   @Logger
   Log log;
   @In
   private Session session;

   @In
   private LocaleService localeServiceImpl;

   @SuppressWarnings("unchecked")
   @Override
   public GetTransUnitsResult execute(GetTransUnits action, ExecutionContext context) throws ActionException
   {

      FliesIdentity.instance().checkLoggedIn();

      log.info("Fetching Transunits for {0}", action.getDocumentId());

      Query query = session.createQuery("from HTextFlow tf where tf.obsolete=0 and tf.document.id = :id order by tf.pos").setParameter("id", action.getDocumentId().getValue());

      int size = query.list().size();

      List<HTextFlow> textFlows = query.setFirstResult(action.getOffset()).setMaxResults(action.getCount()).list();

      ArrayList<TransUnit> units = new ArrayList<TransUnit>();
      for (HTextFlow textFlow : textFlows)
      {

         TransUnitId tuId = new TransUnitId(textFlow.getId());

         // EditState editstate = workspace.getTransUnitStatus(tuId);
         TransUnit tu = new TransUnit(tuId, action.getWorkspaceId().getLocaleId(), textFlow.getContent(), CommentsUtil.toString(textFlow.getComment()), "", ContentState.New);
         HLocale hLocale = localeServiceImpl.getSupportedLanguageByLocale(action.getWorkspaceId().getLocaleId());
         HTextFlowTarget target = textFlow.getTargets().get(hLocale);
         if (target != null)
         {
            tu.setTarget(target.getContent());
            tu.setStatus(target.getState());
         }
         units.add(tu);
      }

      return new GetTransUnitsResult(action.getDocumentId(), units, size);
   }

   @Override
   public void rollback(GetTransUnits action, GetTransUnitsResult result, ExecutionContext context) throws ActionException
   {
   }

}