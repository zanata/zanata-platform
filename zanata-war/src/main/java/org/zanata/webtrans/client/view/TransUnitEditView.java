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

package org.zanata.webtrans.client.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class TransUnitEditView extends Composite implements TransUnitEditDisplay
{
   interface TransUnitTableViewUiBinder extends UiBinder<VerticalPanel, TransUnitEditView>
   {
   }

   private static TransUnitTableViewUiBinder uiBinder = GWT.create(TransUnitTableViewUiBinder.class);

   private VerticalPanel rootPanel;

   public TransUnitEditView()
   {
      rootPanel = uiBinder.createAndBindUi(this);
   }

   @Override
   public void setDisplayTable(TransUnitListDisplay displayTable)
   {
       rootPanel.add(displayTable);
   }

   @Override
   public Widget asWidget()
   {
      return rootPanel;
   }
}