package org.zanata.webtrans.client.ui;

public interface HasStatsFilter
{
   final String STATS_OPTION_WORDS = "Words";
   final String STATS_OPTION_MESSAGE = "Message";
   
   void setStatsFilter(String option, DocumentNode documentNode);
}
