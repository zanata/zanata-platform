package org.zanata.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.datamodel.DataModelSelection;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.log.Log;
import org.zanata.common.LocaleId;
import org.zanata.dao.LocaleDAO;
import org.zanata.dao.PersonDAO;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.service.LanguageTeamService;
import org.zanata.service.LocaleService;

@Name("languageTeamMembersAction")
@Scope(ScopeType.PAGE)
public class LanguageTeamMembersAction implements Serializable
{
   private static final long serialVersionUID = 1L;
   
   @Logger
   Log log;
   
   @DataModel
   private List<HPerson> searchResults;

   @DataModelSelection
   private HPerson selectedPerson;
   
   @In
   private LocaleService localeServiceImpl;
   
   @In
   private LanguageTeamService languageTeamServiceImpl;
   
   @In
   private PersonDAO personDAO;
   
   @In
   private LocaleDAO localeDAO;
   
   @RequestParameter
   private String id;
   
   private String language;
   
   private HLocale locale;
   
   private String searchTerm;
   
   
   @Create
   public void initLocale()
   {
      if( id != null )
      {
         this.language = this.id;
         locale = localeServiceImpl.getByLocaleId(new LocaleId(language));
      }
      log.debug("init language: {0}", locale.getLocaleId().getId());
   }
   
   public void searchForPerson()
   {
      this.searchResults = this.personDAO.findAllContainingName( this.searchTerm );
   }
   
   public void addPersonToTeam( final Long personId )
   {
      final HPerson newTeamMember = this.personDAO.findById(personId, false); 
      this.locale.addMember(newTeamMember);
      this.localeDAO.makePersistent( this.locale );
      this.localeDAO.flush();
   }
   
   public boolean isPersonInTeam( final Long personId )
   {
      for( HPerson teamMember : this.locale.getMembers() )
      {
         if( teamMember.getId().equals( personId ) )
         {
            return true;
         }
      }
      return false;
   }
   
   public List<HPerson> getTeamMembers()
   {
      return new ArrayList<HPerson>(this.languageTeamServiceImpl.getLanguageTeamMembers( this.language ));
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

   public HLocale getLocale()
   {
      return locale;
   }

   public void setLocale(HLocale locale)
   {
      this.locale = locale;
   }
}
