package org.zanata.action;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.faces.context.FacesContext;
import javax.validation.ConstraintViolationException;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;
import org.zanata.annotation.CachedMethods;
import org.zanata.common.LocaleId;
import org.zanata.dao.GlossaryDAO;
import org.zanata.dao.LocaleDAO;
import org.zanata.exception.ZanataServiceException;
import org.zanata.model.HLocale;
import org.zanata.rest.dto.Glossary;
import org.zanata.service.GlossaryFileService;

@Name("glossaryAction")
@Scope(ScopeType.PAGE)
@CachedMethods
public class GlossaryAction implements Serializable {
    /**
    *
    */
    private static final long serialVersionUID = 1L;

    @Logger
    Log log;

    @In
    private GlossaryDAO glossaryDAO;

    @In
    private LocaleDAO localeDAO;

    @In
    private GlossaryFileService glossaryFileServiceImpl;

    private GlossaryFileUploadHelper glossaryFileUpload;

    private String localeToDelete;

    public void initialize() {
        glossaryFileUpload = new GlossaryFileUploadHelper();
    }

    public List<HLocale> getAvailableLocales() {
        return localeDAO.findAllActive();
    }

    public GlossaryFileUploadHelper getGlossaryFileUpload() {
        return glossaryFileUpload;
    }

    public String delete() {
        int rowCount = 0;
        if (StringUtils.isNotEmpty(localeToDelete)) {
            rowCount =
                    glossaryDAO.deleteAllEntries(new LocaleId(localeToDelete));
            log.info("Glossary deleted (" + localeToDelete + "): " + rowCount);
        }
        FacesMessages.instance().add(Severity.INFO, "Glossary deleted: {0}",
                rowCount);
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
            FacesMessages.instance().add(Severity.INFO,
                    "Glossary file {0} uploaded.",
                    this.glossaryFileUpload.getFileName());
        } catch (ZanataServiceException e) {
            FacesMessages.instance().add(Severity.ERROR, e.getMessage(),
                    this.glossaryFileUpload.getFileName());
        } catch (ConstraintViolationException e) {
            FacesMessages.instance().add(Severity.ERROR, "Invalid arguments");
        }

        // NB This needs to be done as for some reason seam is losing the
        // parameters when redirecting
        // This is efectively the same as returning void
        return FacesContext.getCurrentInstance().getViewRoot().getViewId();
    }

    public List<Status> getStats() {
        List<Status> result = new ArrayList<Status>();

        Map<HLocale, Integer> statsMap =
                glossaryDAO.getGlossaryTermCountByLocale();

        for (Entry<HLocale, Integer> entry : statsMap.entrySet()) {
            result.add(new Status(entry.getKey().getLocaleId().getId(), entry
                    .getKey().retrieveDisplayName(), entry.getValue()));
        }

        Collections.sort(result);
        return result;
    }

    public String getLocaleToDelete() {
        return localeToDelete;
    }

    public void setLocaleToDelete(String localeToDelete) {
        this.localeToDelete = localeToDelete;
    }

    /**
     * Helper class to upload glossary files.
     */
    public static class GlossaryFileUploadHelper implements Serializable {
        private static final long serialVersionUID = 1L;
        private InputStream fileContents;
        private String fileName;
        private String sourceLang = "en-US";
        private String transLang;
        private boolean treatSourceCommentsAsTarget = false;
        private String commentCols = "pos,description";

        public InputStream getFileContents() {
            return fileContents;
        }

        public LocaleId getTransLocaleId() {
            if (StringUtils.isNotEmpty(getTransLang())) {
                return new LocaleId(getTransLang());
            }
            return null;
        }

        public LocaleId getSourceLocaleId() {
            if (StringUtils.isNotEmpty(getSourceLang())) {
                return new LocaleId(getSourceLang());
            }
            return null;
        }

        public void setFileContents(InputStream fileContents) {
            this.fileContents = fileContents;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getSourceLang() {
            return sourceLang;
        }

        public void setSourceLang(String sourceLang) {
            this.sourceLang = sourceLang;
        }

        public String getTransLang() {
            return transLang;
        }

        public void setTransLang(String transLang) {
            this.transLang = transLang;
        }

        public boolean isTreatSourceCommentsAsTarget() {
            return treatSourceCommentsAsTarget;
        }

        public void setTreatSourceCommentsAsTarget(
                boolean treatSourceCommentsAsTarget) {
            this.treatSourceCommentsAsTarget = treatSourceCommentsAsTarget;
        }

        public String getCommentCols() {
            return commentCols;
        }

        public void setCommentCols(String commentCols) {
            this.commentCols = commentCols;
        }

        public List<String> getCommentColsList() {
            String[] commentHeadersList =
                    StringUtils.split(getCommentCols(), ",");
            List<String> list = new ArrayList<String>();
            if (commentHeadersList != null && commentHeadersList.length > 0) {
                Collections.addAll(list, commentHeadersList);
            }
            return list;
        }
    }

    /**
     * Glossary status class
     *
     * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
     *
     */
    public static class Status implements Comparable<Status>, Serializable {
        private static final long serialVersionUID = 1L;
        private String localeId;
        private int entryCount;
        private String name;

        public Status(String localeId, String name, int entryCount) {
            this.localeId = localeId;
            this.entryCount = entryCount;
            this.name = name;
        }

        public String getLocaleId() {
            return localeId;
        }

        public int getEntryCount() {
            return entryCount;
        }

        public String getName() {
            return name;
        }

        @Override
        public int compareTo(Status o) {
            if (o.getEntryCount() == this.getEntryCount()) {
                return 0;
            }
            return o.getEntryCount() > this.getEntryCount() ? 1 : -1;
        }
    }
}
