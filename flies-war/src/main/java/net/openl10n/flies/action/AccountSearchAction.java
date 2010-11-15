package net.openl10n.flies.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


import net.openl10n.flies.dao.AccountDAO;
import net.openl10n.flies.model.HAccount;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

@Name("accountSearch")
@Scope(ScopeType.EVENT)
@AutoCreate
public class AccountSearchAction implements Serializable
{
   private static final long serialVersionUID = 1L;
   private String projectMaintainer;
   private List<HAccount> searchResults = new ArrayList<HAccount>();
   @In
   private AccountDAO accountDAO;
   @Logger
   Log log;

   public void setProjectMaintainer(String sr)
   {
      this.projectMaintainer = sr;
   }

   public String getProjectMaintainer()
   {
      return this.projectMaintainer;
   }

   public List<HAccount> getSearchResults()
   {
      return this.searchResults;
   }
   // EdgeNGramTokenFilter
   public List<HAccount> search(Object input)
   {
      String userInput = (String) input;
      return accountDAO.searchQuery(userInput);
   }
}
