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
package org.zanata.webtrans.client.ui.table.column;

import java.util.HashMap;

import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.ui.DocumentNode;
import org.zanata.webtrans.client.ui.HasStatsFilter;
import org.zanata.webtrans.client.ui.TransUnitCountGraph;
import org.zanata.webtrans.client.ui.table.cell.TransUnitCountGraphCell;

import com.google.gwt.user.cellview.client.Column;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 */
public class StatisticColumn extends Column<DocumentNode, TransUnitCountGraph> implements HasStatsFilter
{
   private final HashMap<Long, TransUnitCountGraph> statsWidgets = new HashMap<Long, TransUnitCountGraph>();

   private final WebTransMessages messages;
   private String statsOption = STATS_OPTION_WORDS;

   public StatisticColumn(final WebTransMessages messages)
   {
      super(new TransUnitCountGraphCell());
      this.messages = messages;
   }

   @Override
   public TransUnitCountGraph getValue(DocumentNode docNode)
   {
      long id = docNode.getDocInfo().getId().getId();
      TransUnitCountGraph graph;
      if (!statsWidgets.containsKey(id))
      {
         graph = new TransUnitCountGraph(messages, false);
         graph.setStats(docNode.getDocInfo().getStats(), true);
         statsWidgets.put(id, graph);
      }
      else
      {
         if (statsOption.equals(STATS_OPTION_MESSAGE))
         {
            statsWidgets.get(id).setStats(docNode.getDocInfo().getStats(), false);
         }
         else
         {
            statsWidgets.get(id).setStats(docNode.getDocInfo().getStats(), true);
         }
      }
      return statsWidgets.get(id);
   }

   @Override
   public void setStatsFilter(String option)
   {
      statsOption = option;
   }

}
