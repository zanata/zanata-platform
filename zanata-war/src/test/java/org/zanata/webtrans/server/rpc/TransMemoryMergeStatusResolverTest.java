/*
 * Copyright 2012, Red Hat, Inc. and individual contributors as indicated by the
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

package org.zanata.webtrans.server.rpc;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.ContentState;
import org.zanata.common.ContentType;
import org.zanata.common.LocaleId;
import org.zanata.model.HDocument;
import org.zanata.model.HProject;
import org.zanata.model.HLocale;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.webtrans.shared.model.TransMemoryDetails;
import org.zanata.webtrans.shared.model.TransMemoryResultItem;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.model.TransUnitUpdateRequest;
import org.zanata.webtrans.shared.rpc.MergeOption;
import org.zanata.webtrans.shared.rpc.TransMemoryMerge;
import com.google.common.collect.Lists;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
public class TransMemoryMergeStatusResolverTest
{
   TransMemoryMergeStatusResolver resolver;
   private TransMemoryMerge action;
   private HTextFlow textFlow;
   private TransMemoryDetails tmDetail;
   private String docId = "/po/message.po";
   private String projectName = "project name";
   private String resId = "resId";
   private String msgContext;

   @BeforeMethod
   public void beforeMethod()
   {
      resolver = TransMemoryMergeStatusResolver.newInstance();

      HDocument document = new HDocument(docId, "message.po", "/po", ContentType.PO, new HLocale(new LocaleId("en")));
      HProjectIteration projectIteration = new HProjectIteration();
      HProject project = new HProject();
      project.setName(projectName);
      projectIteration.setProject(project);
      document.setProjectIteration(projectIteration);
      textFlow = new HTextFlow(document, resId, "this is a string.");

      tmDetail = tmDetail(projectName, docId, resId, msgContext);

   }

   private static TransMemoryResultItem tmResultWithSimilarity(double percent)
   {
      return new TransMemoryResultItem(null, null, null, 0, percent);
   }

   private static TransMemoryDetails tmDetail(String projectName, String docId, String resId, String msgContext)
   {
      return new TransMemoryDetails(null, null, projectName, null, docId, resId, msgContext, null, null);
   }

   private static TransMemoryMerge mergeTMAction(MergeOption differentProjectOption, MergeOption differentDocumentOption, MergeOption differentResIdOption)
   {
      TransUnitUpdateRequest updateRequest = new TransUnitUpdateRequest(new TransUnitId(1), null, null, 0);
      return new TransMemoryMerge(80, Lists.newArrayList(updateRequest), differentProjectOption, differentDocumentOption, differentResIdOption);
   }

   private static TransMemoryMerge mergeTMActionWhenResIdIsDifferent(MergeOption resIdOption)
   {
      return mergeTMAction(MergeOption.IGNORE_CHECK, MergeOption.IGNORE_CHECK, resIdOption);
   }

   private static TransMemoryMerge mergeTMActionWhenDocIdIsDifferent(MergeOption documentOption)
   {
      return mergeTMAction(MergeOption.IGNORE_CHECK, documentOption, MergeOption.IGNORE_CHECK);
   }

   private TransMemoryMerge mergeTMActionWhenProjectNameIsDifferent(MergeOption projectOption)
   {
      return mergeTMAction(projectOption, MergeOption.IGNORE_CHECK, MergeOption.IGNORE_CHECK);
   }

   @Test
   public void notOneHundredMatchWillBeSetAsFuzzy() {
      action = mergeTMAction(MergeOption.IGNORE_CHECK, MergeOption.IGNORE_CHECK, MergeOption.IGNORE_CHECK);
      ContentState result = resolver.workOutStatus(action, textFlow, tmDetail, tmResultWithSimilarity(90), null);

      assertThat(result, equalTo(ContentState.NeedReview));
   }

   @Test
   public void differentResIdAndOptionIsFuzzy() {
      TransMemoryDetails tmDetail = tmDetail(projectName, docId, "different res id", msgContext);
      action = mergeTMActionWhenResIdIsDifferent(MergeOption.FUZZY);
      assertThat(resolver.workOutStatus(action, textFlow, tmDetail, tmResultWithSimilarity(100), null), equalTo(ContentState.NeedReview));
   }

   @Test
   public void differentResIdAndOptionIsIgnore() {
      TransMemoryDetails tmDetail = tmDetail(projectName, docId, "different res id", msgContext);

      action = mergeTMActionWhenResIdIsDifferent(MergeOption.IGNORE_CHECK);
      assertThat(resolver.workOutStatus(action, textFlow, tmDetail, tmResultWithSimilarity(100), null), equalTo(ContentState.Approved));
   }

   @Test
   public void differentResIdAndOptionIsReject() {
      TransMemoryDetails tmDetail = tmDetail(projectName, docId, "different res id", msgContext);
      action = mergeTMActionWhenResIdIsDifferent(MergeOption.REJECT);
      assertThat(resolver.workOutStatus(action, textFlow, tmDetail, tmResultWithSimilarity(80), null), is(nullValue()));
   }

   @Test
   public void differentMsgContextAndOptionIsFuzzy() {
      TransMemoryDetails tmDetail = tmDetail(projectName, docId, resId, "different msg context");
      action = mergeTMActionWhenResIdIsDifferent(MergeOption.FUZZY);
      assertThat(resolver.workOutStatus(action, textFlow, tmDetail, tmResultWithSimilarity(100), null), equalTo(ContentState.NeedReview));
   }

   @Test
   public void differentMsgContextAndOptionIsIgnore() {
      TransMemoryDetails tmDetail = tmDetail(projectName, docId, resId, "different msg context");

      action = mergeTMActionWhenResIdIsDifferent(MergeOption.IGNORE_CHECK);
      assertThat(resolver.workOutStatus(action, textFlow, tmDetail, tmResultWithSimilarity(100), null), equalTo(ContentState.Approved));
   }

   @Test
   public void differentMsgContextAndOptionIsReject() {
      TransMemoryDetails tmDetail = tmDetail(projectName, docId, resId, "different msg context");
      action = mergeTMActionWhenResIdIsDifferent(MergeOption.REJECT);
      assertThat(resolver.workOutStatus(action, textFlow, tmDetail, tmResultWithSimilarity(80), null), is(nullValue()));
   }

   @Test
   public void differentDocIdAndOptionIsFuzzy() {
      TransMemoryDetails tmDetail = tmDetail(projectName, "different doc id", resId, msgContext);
      action = mergeTMActionWhenDocIdIsDifferent(MergeOption.FUZZY);
      assertThat(resolver.workOutStatus(action, textFlow, tmDetail, tmResultWithSimilarity(100), null), equalTo(ContentState.NeedReview));
   }

   @Test
   public void differentDocIdAndOptionIsIgnore() {
      TransMemoryDetails tmDetail = tmDetail(projectName, "different doc id", resId, msgContext);
      action = mergeTMActionWhenDocIdIsDifferent(MergeOption.IGNORE_CHECK);
      assertThat(resolver.workOutStatus(action, textFlow, tmDetail, tmResultWithSimilarity(100), null), equalTo(ContentState.Approved));
   }

   @Test
   public void differentDocIdAndOptionIsReject() {
      TransMemoryDetails tmDetail = tmDetail(projectName, "different doc id", resId, msgContext);
      action = mergeTMActionWhenDocIdIsDifferent(MergeOption.REJECT);
      assertThat(resolver.workOutStatus(action, textFlow, tmDetail, tmResultWithSimilarity(100), null), is(nullValue()));
   }

   @Test
   public void differentProjectNameAndOptionIsFuzzy() {
      TransMemoryDetails tmDetail = tmDetail("different project name", docId, resId, msgContext);
      action = mergeTMActionWhenProjectNameIsDifferent(MergeOption.FUZZY);
      assertThat(resolver.workOutStatus(action, textFlow, tmDetail, tmResultWithSimilarity(100), null), equalTo(ContentState.NeedReview));
   }

   @Test
   public void differentProjectNameAndOptionIsIgnore() {
      TransMemoryDetails tmDetail = tmDetail("different project name", docId, resId, msgContext);
      action = mergeTMActionWhenProjectNameIsDifferent(MergeOption.IGNORE_CHECK);
      assertThat(resolver.workOutStatus(action, textFlow, tmDetail, tmResultWithSimilarity(100), null), equalTo(ContentState.Approved));
   }

   @Test
   public void differentProjectNameAndOptionIsReject() {
      TransMemoryDetails tmDetail = tmDetail("different project name", docId, resId, msgContext);
      action = mergeTMActionWhenProjectNameIsDifferent(MergeOption.REJECT);
      assertThat(resolver.workOutStatus(action, textFlow, tmDetail, tmResultWithSimilarity(100), null), is(nullValue()));
   }

   @Test
   public void willRejectIfThereIsOldTranslationButCanNotFindTranslationToSetAsApproved()
   {
      TransMemoryDetails tmDetail = tmDetail("different project name", docId, resId, msgContext);
      action = mergeTMActionWhenProjectNameIsDifferent(MergeOption.FUZZY);

      HTextFlowTarget oldTarget = new HTextFlowTarget(textFlow, new HLocale());
      oldTarget.setState(ContentState.NeedReview);

      assertThat(resolver.workOutStatus(action, textFlow, tmDetail, tmResultWithSimilarity(100), oldTarget), is(nullValue()));
   }

}
