package org.zanata.action;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.faces.context.FacesContext;
import javax.validation.ConstraintViolationException;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.common.LocaleId;
import org.zanata.dao.GlossaryDAO;
import org.zanata.dao.LocaleDAO;
import org.zanata.exception.ZanataServiceException;
import org.zanata.i18n.Messages;
import org.zanata.model.HLocale;
import org.zanata.rest.dto.Glossary;
import org.zanata.service.GlossaryFileService;
import org.zanata.ui.AbstractListFilter;
import org.zanata.ui.InMemoryListFilter;
import org.zanata.ui.faces.FacesMessages;
import org.zanata.util.ServiceLocator;
import com.google.common.collect.Lists;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import static javax.faces.application.FacesMessage.SEVERITY_ERROR;

@Name("glossaryAction")
@Scope(ScopeType.PAGE)
@Slf4j
public class GlossaryAction implements Serializable {
    private static final long serialVersionUID = 1L;

    @In
    private GlossaryDAO glossaryDAO;

    @In
    private LocaleDAO localeDAO;

    @In
    private GlossaryFileService glossaryFileServiceImpl;

    @In
    private Messages msgs;

    @In("jsfMessages")
    private FacesMessages facesMessages;

    @Getter
    private GlossaryFileUploadHelper glossaryFileUpload =
            new GlossaryFileUploadHelper();

    private List<GlossaryEntry> glossaryEntries;

    @Getter
    private SortingType glossarySortingList = new SortingType(
            Lists.newArrayList(SortingType.SortOption.ALPHABETICAL,
                    SortingType.SortOption.Entry));

    private final GlossaryEntryComparator glossaryEntryComparator =
            new GlossaryEntryComparator(
                    getGlossarySortingList());

    public String getDeleteConfirmationMessage(String localeId) {
        return msgs.format("jsf.Glossary.delete.confirm", localeId);
    }

    public List<HLocale> getAvailableLocales() {
        return localeDAO.findAllActive();
    }

    public String deleteGlossary(String localeId) {
        int rowCount = 0;
        if (StringUtils.isNotEmpty(localeId)) {
            rowCount =
                    glossaryDAO.deleteAllEntries(new LocaleId(localeId));
            log.info("Glossary deleted (" + localeId + "): " + rowCount);
        }
        facesMessages.addGlobal(msgs.format("jsf.Glossary.deleted", rowCount,
                localeId));
        return FacesContext.getCurrentInstance().getViewRoot().getViewId();
    }

    public String uploadFile() {
        log.info("Uploading Glossary...");
        try {
            List<Glossary> glossaries =
                    glossaryFileServiceImpl.parseGlossaryFile(
                            glossaryFileUpload.getFileContents(),
                            glossaryFileUpload.getFileName(),
                            glossaryFileUpload.getSourceLocaleId(),
                            glossaryFileUpload.getTransLocaleId(),
                            glossaryFileUpload.treatSourceCommentsAsTarget,
                            glossaryFileUpload.getCommentColsList());

            for (Glossary glossary : glossaries) {
                glossaryFileServiceImpl.saveGlossary(glossary);
            }
            facesMessages.addGlobal("Glossary file {0} uploaded.",
                    this.glossaryFileUpload.getFileName());
        } catch (ZanataServiceException e) {
            facesMessages.addGlobal(SEVERITY_ERROR, e.getMessage(),
                    this.glossaryFileUpload.getFileName());
        } catch (ConstraintViolationException e) {
            facesMessages.addGlobal(SEVERITY_ERROR, "Invalid arguments");
        }

        // NB This needs to be done as for some reason seam is losing the
        // parameters when redirecting
        // This is efectively the same as returning void
        return FacesContext.getCurrentInstance().getViewRoot().getViewId();
    }

    /**
     * Sort glossary entry list
     */
    public void sortGlossaryEntries() {
        Collections.sort(getEntries(), glossaryEntryComparator);
        glossaryFilter.reset();
    }

    public List<GlossaryEntry> getEntries() {
        if (glossaryEntries == null) {
            glossaryEntries = Lists.newArrayList();
            Map<HLocale, Integer> statsMap =
                    glossaryDAO.getGlossaryTermCountByLocale();
            for (Entry<HLocale, Integer> entry : statsMap.entrySet()) {
                glossaryEntries.add(
                        new GlossaryEntry(entry.getKey().getLocaleId().getId(),
                                entry
                                        .getKey().retrieveDisplayName(),
                                entry.getValue()));
            }
        }
        return glossaryEntries;
    }

    private List<GlossaryEntry> getEntries(GlossaryDAO glossaryDAO) {
        if (this.glossaryDAO == null) {
            this.glossaryDAO = glossaryDAO;
        }
        return getEntries();
    }

    /**
     * Helper class to upload glossary files.
     */
    public static class GlossaryFileUploadHelper implements Serializable {
        private static final long serialVersionUID = 1L;

        @Getter
        @Setter
        private InputStream fileContents;

        @Getter
        @Setter
        private String fileName;

        @Getter
        @Setter
        private String sourceLang = "en-US";

        @Getter
        @Setter
        private String transLang;

        @Getter
        @Setter
        private boolean treatSourceCommentsAsTarget = false;

        @Getter
        @Setter
        private String commentCols = "pos,description";

        public LocaleId getTransLocaleId() {
            return getLocaleId(getTransLang());
        }

        public LocaleId getSourceLocaleId() {
            return getLocaleId(getSourceLang());
        }

        private LocaleId getLocaleId(String lang) {
            if (StringUtils.isNotEmpty(lang)) {
                return new LocaleId(lang);
            }
            return null;
        }

        public List<String> getCommentColsList() {
            String[] commentHeadersList =
                    StringUtils.split(getCommentCols(), ",");
            return Lists.newArrayList(commentHeadersList);
        }
    }

    private class GlossaryEntryComparator implements Comparator<GlossaryEntry> {
        private SortingType sortingType;

        public GlossaryEntryComparator(SortingType sortingType) {
            this.sortingType = sortingType;
        }

        @Override
        public int compare(GlossaryEntry entry1, GlossaryEntry entry2) {
            SortingType.SortOption selectedSortOption =
                    sortingType.getSelectedSortOption();

            if (!selectedSortOption.isAscending()) {
                GlossaryEntry temp = entry1;
                entry1 = entry2;
                entry2 = temp;
            }

            if (selectedSortOption.equals(SortingType.SortOption.ALPHABETICAL)) {
                return entry1.getDisplayName().compareTo(
                        entry2.getDisplayName());
            } else if (selectedSortOption.equals(SortingType.SortOption.Entry)) {
                return entry1.getEntryCount() > entry2.getEntryCount() ? 1 : -1;
            }

            return 0;
        }
    }

    /**
     * Glossary entry class
     *
     * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
     *
     */
    @AllArgsConstructor
    public class GlossaryEntry implements Serializable {
        @Getter
        private String localeId;
        @Getter
        private String displayName;
        @Getter
        private int entryCount;
    }

    @Getter
    private final AbstractListFilter<GlossaryEntry> glossaryFilter =
            new InMemoryListFilter<GlossaryEntry>() {
                private GlossaryDAO glossaryDAO = ServiceLocator.instance()
                        .getInstance(GlossaryDAO.class);

                @Override
                protected List<GlossaryEntry> fetchAll() {
                    return getEntries(glossaryDAO);
                }

                @Override
                protected boolean include(GlossaryEntry elem,
                        String filter) {
                    return StringUtils.containsIgnoreCase(elem.getLocaleId(),
                            filter)
                            || StringUtils.containsIgnoreCase(
                                    elem.getDisplayName(), filter);
                }
            };
}
