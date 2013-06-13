/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.service.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.dbunit.operation.DatabaseOperation;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.common.ContentState;
import org.zanata.common.ContentType;
import org.zanata.common.LocaleId;
import org.zanata.dao.AccountDAO;
import org.zanata.dao.LocaleDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.model.HCopyTransOptions;
import org.zanata.model.HDocument;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.process.CopyTransProcessHandle;
import org.zanata.seam.SeamAutowire;
import org.zanata.service.CopyTransService;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.zanata.common.ContentState.*;
import static org.zanata.model.HCopyTransOptions.ConditionRuleAction;
import static org.zanata.model.HCopyTransOptions.ConditionRuleAction.DOWNGRADE_TO_FUZZY;
import static org.zanata.model.HCopyTransOptions.ConditionRuleAction.IGNORE;
import static org.zanata.model.HCopyTransOptions.ConditionRuleAction.REJECT;

/**
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Test(groups = { "business-tests" })
public class CopyTransServiceImplTest extends ZanataDbunitJpaTest
{
   private SeamAutowire seam = SeamAutowire.instance();

   @Override
   protected void prepareDBUnitOperations()
   {
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/ClearAllTables.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/LocalesData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/AccountData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/CopyTransTestData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
   }

   @BeforeMethod
   public void initializeSeam()
   {
      seam.reset()
            .use("entityManager", getEm())
            .use("session", getSession())
            .use(JpaIdentityStore.AUTHENTICATED_USER, seam.autowire(AccountDAO.class).getByUsername("demo"))
            .useImpl(LocaleServiceImpl.class)
            .ignoreNonResolvable();
   }

   /**
    * Use this test to individually test copy trans scenarios.
    */
   @Test(enabled = false)
   public void individualTest()
   {
      this.testCopyTrans(new CopyTransExecution(REJECT, IGNORE, DOWNGRADE_TO_FUZZY, true, true, true, true).expectTransState(Approved));
   }

   @DataProvider(name = "CopyTrans")
   public Object[][] createCopyTransTestParams()
   {
      Set<CopyTransExecution> expandedExecutions = generateAllExecutions();

      Object[][] val =  new Object[expandedExecutions.size()][1];
      int i=0;
      for( CopyTransExecution exe : expandedExecutions )
      {
         val[i++][0] = exe;
      }

      return val;
   }

   @Test
   public void testExpectedState()
   {
      CopyTransServiceImpl copyTransService = seam.autowire(CopyTransServiceImpl.class);

      // When the current context (last argument) is null, it should always return null
      assertThat(copyTransService.getExpectedContentState(true, DOWNGRADE_TO_FUZZY, null), nullValue());

      assertThat(copyTransService.getExpectedContentState(true, REJECT, Approved), is(Approved));
      assertThat(copyTransService.getExpectedContentState(false, REJECT, Approved), nullValue());
      assertThat(copyTransService.getExpectedContentState(true, DOWNGRADE_TO_FUZZY, Approved), is(Approved));
      assertThat(copyTransService.getExpectedContentState(false, DOWNGRADE_TO_FUZZY, Approved), is(NeedReview));
      assertThat(copyTransService.getExpectedContentState(true, IGNORE, Approved), is(Approved));
      assertThat(copyTransService.getExpectedContentState(false, IGNORE, Approved), is(Approved));

      assertThat(copyTransService.getExpectedContentState(true, REJECT, NeedReview), is(NeedReview));
      assertThat(copyTransService.getExpectedContentState(false, REJECT, NeedReview), nullValue());
      assertThat(copyTransService.getExpectedContentState(true, DOWNGRADE_TO_FUZZY, NeedReview), is(NeedReview));
      assertThat(copyTransService.getExpectedContentState(false, DOWNGRADE_TO_FUZZY, NeedReview), is(NeedReview));
      assertThat(copyTransService.getExpectedContentState(true, IGNORE, NeedReview), is(NeedReview));
      assertThat(copyTransService.getExpectedContentState(false, IGNORE, NeedReview), is(NeedReview));

      assertThat(copyTransService.getExpectedContentState(true, REJECT, Translated), is(Translated));
      assertThat(copyTransService.getExpectedContentState(false, REJECT, Translated), nullValue());
      assertThat(copyTransService.getExpectedContentState(true, DOWNGRADE_TO_FUZZY, Translated), is(Translated));
      assertThat(copyTransService.getExpectedContentState(false, DOWNGRADE_TO_FUZZY, Translated), is(NeedReview));
      assertThat(copyTransService.getExpectedContentState(true, IGNORE, Translated), is(Translated));
      assertThat(copyTransService.getExpectedContentState(false, IGNORE, Translated), is(Translated));
   }

   @Test(dataProvider = "CopyTrans", dependsOnMethods = "testExpectedState")
   public void testCopyTrans(CopyTransExecution execution)
   {
      // Prepare Execution
      ProjectIterationDAO iterationDAO = seam.autowire(ProjectIterationDAO.class);
      LocaleDAO localeDAO = seam.autowire(LocaleDAO.class);

      // Get the project iteration
      HProjectIteration projectIteration;
      if( execution.projectMatches )
      {
         projectIteration = iterationDAO.getBySlug("same-project", "different-version");
      }
      else
      {
         projectIteration = iterationDAO.getBySlug("different-project", "different-version");
      }

      // Set require translation review
      projectIteration.setRequireTranslationReview(execution.requireTranslationReview);

      // Create the document
      HDocument doc = new HDocument();
      doc.setContentType(ContentType.TextPlain);
      doc.setLocale(localeDAO.findByLocaleId( new LocaleId("en-US")));
      doc.setProjectIteration(projectIteration);
      if( execution.documentMatches )
      {
         doc.setFullPath("/same/document");
      }
      else
      {
         doc.setFullPath("/different/document");
      }
      projectIteration.getDocuments().put(doc.getDocId(), doc);

      // Create the text Flow
      HTextFlow textFlow = new HTextFlow();
      textFlow.setContents("Source Content"); // Source content matches
      textFlow.setPlural(false);
      textFlow.setObsolete(false);
      textFlow.setDocument(doc);
      if( execution.contextMatches )
      {
         textFlow.setResId("same-context");
      }
      else
      {
         textFlow.setResId("different-context");
      }
      doc.getTextFlows().add(textFlow);

      projectIteration = iterationDAO.makePersistent(projectIteration);

      HCopyTransOptions options = new HCopyTransOptions( execution.getContextMismatchAction(),
            execution.getDocumentMismatchAction(), execution.getProjectMismatchAction() );
      CopyTransProcessHandle handle = new CopyTransProcessHandle(projectIteration, "admin", options);

      CopyTransService copyTransService = seam.use("asynchronousProcessHandle", handle)
                                              .autowire(CopyTransServiceImpl.class);
      copyTransService.copyTransForIteration(projectIteration);

      // Validate execution
      HTextFlow targetTextFlow = (HTextFlow)
         getEm().createQuery("from HTextFlow tf where tf.document.projectIteration = :projectIteration " +
            "and tf.document.docId = :docId and tf.resId = :resId")
            .setParameter("projectIteration", projectIteration)
            .setParameter("docId", doc.getDocId())
            .setParameter("resId", textFlow.getResId())
            .getSingleResult();
      HTextFlowTarget target = targetTextFlow.getTargets().get(3L); // Id: 3 for Locale de

      if( execution.isExpectUntranslated() )
      {
         if( target != null && target.getState() != ContentState.New )
         {
            throw new AssertionError("Expected untranslated text flow but got state " + target.getState());
         }
      }
      else if( execution.getExpectedTranslationState() != null )
      {
         if( target == null )
         {
            throw new AssertionError("Expected state " + execution.getExpectedTranslationState() + ", but got untranslated.");
         }
         else if( execution.getExpectedTranslationState() != target.getState() )
         {
            throw new AssertionError("Expected state " + execution.getExpectedTranslationState() + ", but got " + target.getState());
         }
      }

      // Contents
      if( execution.getExpectedContents() != null )
      {
         if( target == null )
         {
            throw new AssertionError("Expected contents " + Arrays.toString( execution.getExpectedContents() ) + ", but got untranslated.");
         }
         else if( !Arrays.equals( execution.getExpectedContents(), target.getContents().toArray() ) )
         {
            throw new AssertionError("Expected contents " + Arrays.toString( execution.getExpectedContents() ) +
                  ", but got " + Arrays.toString( target.getContents().toArray() ));
         }
      }
   }

   private ContentState getExpectedContentState( CopyTransExecution execution )
   {
      ContentState expectedContentState = Translated;
      // our test data has content state Approved
      if (execution.getRequireTranslationReview() && execution.getContextMatches() && execution.getDocumentMatches() && execution.getProjectMatches())
      {
         expectedContentState = Approved;
      }

      expectedContentState = getExpectedContentState(execution.getContextMatches(), execution.getContextMismatchAction(), expectedContentState);
      expectedContentState = getExpectedContentState(execution.getProjectMatches(), execution.getProjectMismatchAction(), expectedContentState);
      expectedContentState = getExpectedContentState(execution.getDocumentMatches(), execution.getDocumentMismatchAction(), expectedContentState);
      return expectedContentState;
   }

   private ContentState getExpectedContentState( boolean match, ConditionRuleAction action, ContentState currentState )
   {
      if( currentState == null )
      {
         return null;
      }
      else if( !match )
      {
         if( action == DOWNGRADE_TO_FUZZY )
         {
            return ContentState.NeedReview;
         }
         else if( action == REJECT )
         {
            return null;
         }
      }
      return currentState;
   }

   private Set<CopyTransExecution> generateAllExecutions()
   {
      Set<CopyTransExecution> allExecutions = new HashSet<CopyTransExecution>();
      Set<Object[]> paramsSet = cartesianProduct(Arrays.asList(ConditionRuleAction.values()),
                                                 Arrays.asList(ConditionRuleAction.values()),
                                                 Arrays.asList(ConditionRuleAction.values()),
                                                 Arrays.asList(true, false),
                                                 Arrays.asList(true, false),
                                                 Arrays.asList(true, false),
                                                 Arrays.asList(true, false));

      for( Object[] params : paramsSet )
      {
         CopyTransExecution exec = new CopyTransExecution(
               (ConditionRuleAction)params[0], (ConditionRuleAction)params[1], (ConditionRuleAction)params[2],
               (Boolean)params[3], (Boolean)params[4], (Boolean)params[5], (Boolean)params[6]);

         ContentState expectedContentState = this.getExpectedContentState(exec);
         if( expectedContentState == null )
         {
            exec.expectUntranslated();
         }
         else
         {
            exec.expectTransState(expectedContentState).withContents("target-content-de");
         }
         allExecutions.add(exec);
      }
      return allExecutions;
   }

   /**
    * Utility method to generate a cartesian product of all possible scenarios
    */
   private Set<Object[]> cartesianProduct( Iterable<?> ... colls )
   {
      // Base case
      if( colls.length == 1 )
      {
         Iterable<?> lastSet = colls[0];
         Set<Object[]> product = new HashSet<Object[]>();

         for( Object elem : lastSet )
         {
            product.add(new Object[]{elem});
         }
         return product;
      }
      // Recursive case
      else
      {
         Iterable<?> lastSet = colls[colls.length-1];
         Set<Object[]> subProduct = cartesianProduct( Arrays.copyOfRange(colls, 0, colls.length-1) );
         Set<Object[]> fullProduct = new HashSet<Object[]>();

         for( Object[] subProdElem : subProduct )
         {
            for( Object elem : lastSet )
            {
               Object[] newSubProd = Arrays.copyOf(subProdElem, subProdElem.length + 1);
               newSubProd[newSubProd.length-1] = elem;
               fullProduct.add( newSubProd );
            }
         }

         return fullProduct;
      }
   }

   @Getter
   @Setter
   @EqualsAndHashCode(of = {"contextMismatchAction", "projectMismatchAction", "documentMismatchAction",
                            "contextMatches", "projectMatches", "documentMatches"})
   @ToString
   private static class CopyTransExecution implements Cloneable
   {
      private ConditionRuleAction contextMismatchAction;
      private ConditionRuleAction projectMismatchAction;
      private ConditionRuleAction documentMismatchAction;
      private Boolean contextMatches;
      private Boolean projectMatches;
      private Boolean documentMatches;
      private Boolean requireTranslationReview;
      private ContentState expectedTranslationState;
      private boolean expectUntranslated;
      private String[] expectedContents;

      private CopyTransExecution(ConditionRuleAction contextMismatchAction, ConditionRuleAction projectMismatchAction,
                                 ConditionRuleAction documentMismatchAction, Boolean contextMatches, Boolean projectMatches,
                                 Boolean documentMatches, Boolean requireTranslationReview)
      {
         this.contextMismatchAction = contextMismatchAction;
         this.projectMismatchAction = projectMismatchAction;
         this.documentMismatchAction = documentMismatchAction;
         this.contextMatches = contextMatches;
         this.projectMatches = projectMatches;
         this.documentMatches = documentMatches;
         this.requireTranslationReview = requireTranslationReview;
      }

      @Override
      public Object clone() throws CloneNotSupportedException
      {
         return super.clone();
      }

      public CopyTransExecution expectTransState( ContentState state )
      {
         this.expectedTranslationState = state;
         this.expectUntranslated = false;
         return this;
      }

      public CopyTransExecution expectUntranslated()
      {
         this.expectedTranslationState = null;
         this.expectUntranslated = true;
         return this;
      }

      public CopyTransExecution withContents( String ... contents )
      {
         this.expectedContents = contents;
         return this;
      }

      public Collection<CopyTransExecution> expand()
      {
         Set<CopyTransExecution> expanded = new HashSet<CopyTransExecution>();
         Set<CopyTransExecution> toExpand = new HashSet<CopyTransExecution>();

         toExpand.add(this);

         while( !toExpand.isEmpty() )
         {
            CopyTransExecution nextExec = toExpand.iterator().next();

            if( nextExec.contextMismatchAction == null )
            {
               for( ConditionRuleAction val : ConditionRuleAction.values() )
               {
                  try
                  {
                     CopyTransExecution expandedEx = (CopyTransExecution)nextExec.clone();
                     expandedEx.contextMismatchAction = val;
                     toExpand.add(expandedEx);
                  }
                  catch (CloneNotSupportedException e)
                  {
                     throw new RuntimeException(e);
                  }
               }
            }
            else if( nextExec.projectMismatchAction == null )
            {
               for( ConditionRuleAction val : ConditionRuleAction.values() )
               {
                  try
                  {
                     CopyTransExecution expandedEx = (CopyTransExecution)nextExec.clone();
                     expandedEx.projectMismatchAction = val;
                     toExpand.add(expandedEx);
                  }
                  catch (CloneNotSupportedException e)
                  {
                     throw new RuntimeException(e);
                  }
               }
            }
            else if( nextExec.documentMismatchAction == null )
            {
               for( ConditionRuleAction val : ConditionRuleAction.values() )
               {
                  try
                  {
                     CopyTransExecution expandedEx = (CopyTransExecution)nextExec.clone();
                     expandedEx.documentMismatchAction = val;
                     toExpand.add(expandedEx);
                  }
                  catch (CloneNotSupportedException e)
                  {
                     throw new RuntimeException(e);
                  }
               }
            }
            else if( nextExec.contextMatches == null )
            {
               for( Boolean val : new Boolean[]{Boolean.TRUE, Boolean.FALSE} )
               {
                  try
                  {
                     CopyTransExecution expandedEx = (CopyTransExecution)nextExec.clone();
                     expandedEx.contextMatches = val;
                     toExpand.add(expandedEx);
                  }
                  catch (CloneNotSupportedException e)
                  {
                     throw new RuntimeException(e);
                  }
               }
            }
            else if( nextExec.projectMatches == null )
            {
               for( Boolean val : new Boolean[]{Boolean.TRUE, Boolean.FALSE} )
               {
                  try
                  {
                     CopyTransExecution expandedEx = (CopyTransExecution)nextExec.clone();
                     expandedEx.projectMatches = val;
                     toExpand.add(expandedEx);
                  }
                  catch (CloneNotSupportedException e)
                  {
                     throw new RuntimeException(e);
                  }
               }
            }
            else if( nextExec.documentMatches == null )
            {
               for( Boolean val : new Boolean[]{Boolean.TRUE, Boolean.FALSE} )
               {
                  try
                  {
                     CopyTransExecution expandedEx = (CopyTransExecution)nextExec.clone();
                     expandedEx.documentMatches = val;
                     toExpand.add(expandedEx);
                  }
                  catch (CloneNotSupportedException e)
                  {
                     throw new RuntimeException(e);
                  }
               }
            }
            else
            {
               expanded.add( nextExec );
            }

            toExpand.remove(nextExec);
         }


         return expanded;
      }
   }
}
