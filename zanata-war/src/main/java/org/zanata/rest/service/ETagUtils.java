package org.zanata.rest.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import javax.ws.rs.core.EntityTag;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;
import org.zanata.dao.DocumentDAO;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.model.po.HPoHeader;
import org.zanata.rest.NoSuchEntityException;
import org.zanata.rest.dto.extensions.gettext.PoHeader;
import org.zanata.util.HashUtil;

import static org.zanata.common.EntityStatus.OBSOLETE;

@Name("eTagUtils")
@Scope(ScopeType.STATELESS)
@AutoCreate
public class ETagUtils
{

   @In
   private Session session;

   @In
   private DocumentDAO documentDAO;

   Log log = Logging.getLog(ETagUtils.class);


   /**
    * Retrieves the ETag for the Project
    * 
    * This algorithm takes into account changes in Project Iterations as well.
    * 
    * @param slug Project slug
    * @return calculated EntityTag
    * @throws NoSuchEntityException if project is obsolete or does not exist
    */
   public EntityTag generateTagForProject(String slug)
   {
      Integer projectVersion = (Integer) session.createQuery("select p.versionNum from HProject p where slug =:slug " +
      		"and status not in (:statusList)")
      		.setParameter("slug", slug)
      		.setParameterList("statusList", new Object[]{OBSOLETE})
            .setComment("ETagUtils.generateTagForProject-project")
      		.uniqueResult();

      if (projectVersion == null)
         throw new NoSuchEntityException("Project '" + slug + "' not found.");

      @SuppressWarnings("unchecked")
      List<Integer> iterationVersions = session.createQuery("select i.versionNum from HProjectIteration i " +
      		"where i.project.slug =:slug and status not in (:statusList)")
      		.setParameter("slug", slug)
      		.setParameterList("statusList", new Object[]{OBSOLETE})
            .setComment("ETagUtils.generateTagForProject-iteration")
      		.list();

      String hash = HashUtil.generateHash(projectVersion + ':' + StringUtils.join(iterationVersions, ':'));

      return EntityTag.valueOf(hash);
   }

   /**
    * Retrieves the ETag for the ProjectIteration
    * 
    * @param projectSlug project slug
    * @param iterationSlug iteration slug
    * @return calculated EntityTag
    * @throw NoSuchEntityException if iteration is obsolete or does not exist
    */
   public EntityTag generateETagForIteration(String projectSlug, String iterationSlug)
   {
      Integer iterationVersion = (Integer) session.createQuery("select i.versionNum from HProjectIteration i where i.slug =:islug and i.project.slug =:pslug " +
            "and status not in (:statusList) and i.project.status not in (:statusList)")
            .setParameter("islug", iterationSlug)
            .setParameter("pslug", projectSlug)
            .setParameterList("statusList", new Object[]{OBSOLETE})
            .setComment("ETagUtils.generateETagForIteration")
            .uniqueResult();

      if (iterationVersion == null)
         throw new NoSuchEntityException("Project Iteration '" + iterationSlug + "' not found.");

      String hash = HashUtil.generateHash(String.valueOf(iterationVersion));

      return EntityTag.valueOf(hash);
   }

   public EntityTag generateETagForDocument(HProjectIteration iteration, String id, Set<String> extensions)
   {
      HDocument doc = documentDAO.getByDocIdAndIteration(iteration, id);
      if (doc == null)
         throw new NoSuchEntityException("Document '" + id + "' not found.");

      Integer hashcode = 1;
      hashcode = hashcode * 31 + doc.getRevision();

      int extHash = 0;
      if (extensions.contains(PoHeader.ID))
      {
         HPoHeader header = doc.getPoHeader();
         if (header != null)
         {
            extHash = header.getVersionNum();
         }
      }
      hashcode = hashcode * 31 + extHash;

      return EntityTag.valueOf(String.valueOf(hashcode));
   }

   public EntityTag generateETagForTranslatedDocument(HProjectIteration iteration, String id, HLocale locale, Set<String> extensions)
   {
      HDocument doc = documentDAO.getByDocIdAndIteration(iteration, id);
      if (doc == null)
         throw new NoSuchEntityException("Document '" + id + "' not found.");

      ByteArrayOutputStream hashBuffer = new ByteArrayOutputStream();

      try
      {
         doc.writeHashState(hashBuffer);
         if( doc.getPoHeader() != null )
         {
            doc.getPoHeader().writeHashState(hashBuffer);
         }
         // TODO This might need to be a query to avoid N+1 problems
         for( HTextFlow tf : doc.getTextFlows() )
         {
            if( tf.getTargets().containsKey( locale.getId() ) )
            {
               HTextFlowTarget textFlowTarget = tf.getTargets().get(locale.getId());
               textFlowTarget.writeHashState(hashBuffer);
               if( textFlowTarget.getComment() != null )
               {
                  textFlowTarget.getComment().writeHashState(hashBuffer);
               }
            }
         }
      }
      catch (IOException e)
      {
         throw new RuntimeException("Problem writing to ETag buffer.", e);
      }

      return EntityTag.valueOf(HashUtil.md5Hex(hashBuffer.toString()));
   }
}
