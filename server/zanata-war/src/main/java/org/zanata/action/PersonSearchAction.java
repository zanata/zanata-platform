package org.zanata.action;

import java.io.Serializable;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.datamodel.DataModelSelection;
import org.zanata.dao.PersonDAO;
import org.zanata.model.HPerson;

@Name("personSearchAction")
@Scope(ScopeType.PAGE)
public class PersonSearchAction implements Serializable
{
   private static final long serialVersionUID = 1L;
   
   @DataModel
   private List<HPerson> searchResults;

   @DataModelSelection
   @Out(required=false)
   private HPerson selectedPerson;
   
   @In
   private PersonDAO personDAO;
   
   private String searchTerm;
   
   
   
   public void search()
   {
      this.searchResults = this.personDAO.findAllContainingName( this.searchTerm );
   }
   
   public String getSearchTerm()
   {
      return searchTerm;
   }

   public void setSearchTerm(String searchTerm)
   {
      this.searchTerm = searchTerm;
   }

   public List<HPerson> getSearchResults()
   {
      return searchResults;
   }

   public void setSearchResults(List<HPerson> searchResults)
   {
      this.searchResults = searchResults;
   }

   public HPerson getSelectedPerson()
   {
      return selectedPerson;
   }

   public void setSelectedPerson(HPerson selectedPerson)
   {
      this.selectedPerson = selectedPerson;
   }
}
