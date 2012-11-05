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

package org.zanata.webtrans.client.service;

import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.rpc.NoOpAsyncCallback;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.auth.Identity;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.rpc.NoOpResult;
import org.zanata.webtrans.shared.rpc.TransUnitEditAction;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Singleton
public class TranslatorInteractionService
{
   private final Identity identity;
   private final CachingDispatchAsync dispatcher;

   @Inject
   public TranslatorInteractionService(Identity identity, CachingDispatchAsync dispatcher)
   {
      this.identity = identity;
      this.dispatcher = dispatcher;
   }

   public void transUnitSelected(TransUnit selectedTransUnit)
   {
      dispatcher.execute(new TransUnitEditAction(identity.getPerson(), selectedTransUnit.getId()), new NoOpAsyncCallback<NoOpResult>());
   }

   public EditorClientId getCurrentEditorClientId()
   {
      return identity.getEditorClientId();
   }

   public void personExit(Person person, TransUnitId selectedTransUnitId)
   {
      if (selectedTransUnitId != null)
      {
         dispatcher.execute(new TransUnitEditAction(person, selectedTransUnitId), new NoOpAsyncCallback<NoOpResult>());
      }
   }
}
