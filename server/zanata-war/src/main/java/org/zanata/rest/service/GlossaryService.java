package org.zanata.rest.service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.StreamingOutput;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.apache.commons.lang.StringUtils;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.zanata.adapter.glossary.GlossaryCSVWriter;
import org.zanata.adapter.glossary.GlossaryPoWriter;
import org.zanata.common.GlossarySortField;
import org.zanata.common.LocaleId;
import org.zanata.dao.GlossaryDAO;
import org.zanata.dao.ProjectDAO;
import org.zanata.exception.ZanataServiceException;
import org.zanata.model.HGlossaryEntry;
import org.zanata.model.HGlossaryTerm;
import org.zanata.model.HLocale;
import org.zanata.model.HProject;
import org.zanata.rest.GlossaryFileUploadForm;
import org.zanata.rest.dto.GlossaryEntry;
import org.zanata.rest.dto.GlossaryInfo;
import org.zanata.rest.dto.GlossaryLocaleInfo;
import org.zanata.rest.dto.GlossaryResults;
import org.zanata.rest.dto.GlossaryTerm;
import org.zanata.rest.dto.LocaleDetails;
import org.zanata.rest.dto.QualifiedName;
import org.zanata.rest.dto.ResultList;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.GlossaryFileService;
import org.zanata.service.LocaleService;
import org.zanata.service.impl.GlossaryFileServiceImpl;
import org.zanata.service.impl.LocaleServiceImpl;

@RequestScoped
@Named("glossaryService")
@Path(GlossaryResource.SERVICE_PATH)
@Transactional
public class GlossaryService implements GlossaryResource {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(GlossaryService.class);

    public static final String PROJECT_QUALIFIER_PREFIX = "project/";
    @Inject
    private GlossaryDAO glossaryDAO;
    @Inject
    private GlossaryFileService glossaryFileServiceImpl;
    @Inject
    private ZanataIdentity identity;
    @Inject
    private LocaleService localeServiceImpl;
    @Inject
    private ProjectDAO projectDAO;

    @Override
    public Response getQualifiedName() {
        return Response.ok(new QualifiedName(GLOBAL_QUALIFIED_NAME)).build();
    }

    @Override
    public Response getInfo(
            @DefaultValue(GLOBAL_QUALIFIED_NAME) @QueryParam("qualifiedName") String qualifiedName) {
        if (!identity.isLoggedIn()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        HLocale srcLocale = getSourceLocale();
        int entryCount = glossaryDAO.getEntryCountBySourceLocales(
                srcLocale.getLocaleId(), qualifiedName);
        GlossaryLocaleInfo srcGlossaryLocale = new GlossaryLocaleInfo(
                LocaleServiceImpl.convertToDTO(srcLocale), entryCount);
        Map<LocaleId, Integer> transMap = glossaryDAO
                .getTranslationLocales(srcLocale.getLocaleId(), qualifiedName);
        List<HLocale> supportedLocales =
                localeServiceImpl.getSupportedLocales();
        List<GlossaryLocaleInfo> transLocale = Lists.newArrayList();
        supportedLocales.stream().filter(
                locale -> !locale.getLocaleId().equals(srcLocale.getLocaleId()))
                .forEach(locale -> {
                    LocaleDetails localeDetails =
                            LocaleServiceImpl.convertToDTO(locale);
                    int count = transMap.containsKey(locale.getLocaleId())
                            ? transMap.get(locale.getLocaleId()) : 0;
                    transLocale
                            .add(new GlossaryLocaleInfo(localeDetails, count));
                });
        GlossaryInfo glossaryInfo =
                new GlossaryInfo(srcGlossaryLocale, transLocale);
        return Response.ok(glossaryInfo).build();
    }

    @Override
    public Response getEntries(
            @DefaultValue("en-US") @QueryParam("srcLocale") LocaleId srcLocale,
            @QueryParam("transLocale") LocaleId transLocale,
            @DefaultValue("1") @QueryParam("page") int page,
            @DefaultValue("1000") @QueryParam("sizePerPage") int sizePerPage,
            @QueryParam("filter") String filter,
            @QueryParam("sort") String fields,
            @DefaultValue(GLOBAL_QUALIFIED_NAME) @QueryParam("qualifiedName") String qualifiedName) {
        if (!identity.isLoggedIn()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        int offset = (validatePage(page) - 1) * validatePageSize(sizePerPage);
        List<HGlossaryEntry> hGlossaryEntries = glossaryDAO.getEntriesByLocale(
                srcLocale, offset, validatePageSize(sizePerPage), filter,
                convertToSortField(fields), qualifiedName);
        int totalCount =
                glossaryDAO.getEntriesCount(srcLocale, filter, qualifiedName);
        ResultList<GlossaryEntry> resultList = new ResultList<GlossaryEntry>();
        resultList.setTotalCount(totalCount);
        if (transLocale != null) {
            // filter out all terms other than source term and term in
            // transLocale
            HLocale locale = localeServiceImpl.getByLocaleId(transLocale);
            transferEntriesLocaleResource(hGlossaryEntries, resultList, locale);
        } else {
            // filter out all terms other than source term
            HLocale locale = localeServiceImpl.getByLocaleId(srcLocale);
            transferEntriesLocaleResource(hGlossaryEntries, resultList, locale);
        }
        return Response.ok(resultList).build();
    }

    @Override
    public Response downloadFile(@DefaultValue("csv") String fileType,
            String commaSeparatedLanguage,
            @DefaultValue(GLOBAL_QUALIFIED_NAME) @QueryParam("qualifiedName") String qualifiedName) {
        Response response =
                checkGlossaryPermission(qualifiedName, "glossary-download");
        if (response != null) {
            return response;
        }
        if (!fileType.equalsIgnoreCase("csv")
                && !fileType.equalsIgnoreCase("po")) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Not supported file type-" + fileType).build();
        }
        LocaleId srcLocaleId = getSourceLocale().getLocaleId();
        // use commaSeparatedLanguage is exist,
        // otherwise use all translations available for all glossary entry
        Set<LocaleId> transList;
        if (StringUtils.isEmpty(commaSeparatedLanguage)) {
            transList = glossaryDAO
                    .getTranslationLocales(srcLocaleId, qualifiedName).keySet();
        } else {
            transList = Sets.newHashSet();
            for (String locale : commaSeparatedLanguage.split(",")) {
                transList.add(new LocaleId(locale));
            }
        }
        List<HLocale> supportedLocales =
                localeServiceImpl.getSupportedLocales();
        // filter out not supported locale from transList
        List<LocaleId> transLocales = supportedLocales.stream()
                .filter(hLocale -> transList.contains(hLocale.getLocaleId()))
                .map(HLocale::getLocaleId).collect(Collectors.toList());
        List<GlossaryEntry> entries = Lists.newArrayList();
        transferEntriesResource(glossaryDAO.getEntries(qualifiedName), entries);
        try {
            GlossaryStreamingOutput output = fileType.equalsIgnoreCase("csv")
                    ? new CSVStreamingOutput(entries, srcLocaleId, transLocales)
                    : new PotStreamingOutput(entries, srcLocaleId,
                            transLocales);
            String filename = getFileName(qualifiedName, fileType);
            return Response.ok()
                    .header("Content-Disposition",
                            "attachment; filename=\"" + filename + "\"")
                    .entity(output).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error while generating glossary file. Please try again")
                    .build();
        }
    }

    /**
     * Generate file name by qualifiedName and type e.g. - project/zanata, -
     * csv, returns zanata_glossary.csv - po, returns zanata_glossary.zip -
     * global/default, - csv, returns glossary.csv - po, returns glossary.zip
     */
    private String getFileName(String qualifiedName, String type) {
        String filePrefix = isProjectGlossary(qualifiedName)
                ? getProjectSlug(qualifiedName) + "_" : "";
        return type.equalsIgnoreCase("csv") ? filePrefix + "glossary.csv"
                : filePrefix + "glossary.zip";
    }

    @Override
    public Response post(List<GlossaryEntry> glossaryEntries,
            @QueryParam("locale") String locale,
            @DefaultValue(GLOBAL_QUALIFIED_NAME) @QueryParam("qualifiedName") String qualifiedName) {
        if (StringUtils.isBlank(locale)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Locale \'" + locale + "\' is required.").build();
        }
        LocaleId localeId = new LocaleId(locale);
        if (!localeServiceImpl.localeExists(localeId)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Locale \'" + locale + "\' is not supported.")
                    .build();
        }
        if (isProjectGlossary(qualifiedName)) {
            HProject project = getProjectByQualifiedName(qualifiedName);
            if (project == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Invalid qualifiedName:" + qualifiedName)
                        .build();
            }
            identity.checkPermission("glossary-update", project,
                    localeServiceImpl.getByLocaleId(localeId));
        } else {
            identity.checkPermission("", "glossary-insert");
        }
        GlossaryResults results = saveOrUpdateGlossaryEntries(glossaryEntries,
                Optional.of(localeId));
        return Response.ok(results).build();
    }

    @Override
    public Response upload(@MultipartForm GlossaryFileUploadForm form) {
        String qualifiedName = form.getQualifiedName();
        Response response =
                checkGlossaryPermission(qualifiedName, "glossary-insert");
        if (response != null) {
            return response;
        }
        try {
            LocaleId srcLocaleId = new LocaleId(form.getSrcLocale());
            LocaleId transLocaleId = null;
            if (StringUtils.isNotEmpty(form.getTransLocale())) {
                transLocaleId = new LocaleId(form.getTransLocale());
            }
            Map<LocaleId, List<GlossaryEntry>> entries = glossaryFileServiceImpl
                    .parseGlossaryFile(form.getFileStream(), form.getFileName(),
                            srcLocaleId, transLocaleId, qualifiedName);
            GlossaryResults overallResult = new GlossaryResults();
            for (Map.Entry<LocaleId, List<GlossaryEntry>> entry : entries
                    .entrySet()) {
                GlossaryResults result = saveOrUpdateGlossaryEntries(
                        entry.getValue(), Optional.ofNullable(transLocaleId));
                overallResult.getGlossaryEntries()
                        .addAll(result.getGlossaryEntries());
                overallResult.getWarnings().addAll(result.getWarnings());
            }
            return Response.ok()
                    .header("Content-Disposition", "attachment; filename=\""
                            + form.getFileName() + "\"")
                    .entity(overallResult).build();
        } catch (ZanataServiceException e) {
            log.error(e.toString(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e).build();
        }
    }

    @Override
    public Response deleteEntry(Long id,
        @DefaultValue(GLOBAL_QUALIFIED_NAME) @QueryParam("qualifiedName") String qualifiedName) {
        Response response =
                checkGlossaryPermission(qualifiedName, "glossary-delete");
        if (response != null) {
            return response;
        }
        HGlossaryEntry entry = glossaryDAO.findById(id);
        if (entry != null) {
            GlossaryEntry deletedEntry = generateGlossaryEntry(entry);
            glossaryDAO.makeTransient(entry);
            glossaryDAO.flush();
            return Response.ok(deletedEntry).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Glossary entry not found: " + id).build();
        }
    }

    @Override
    public Response deleteAllEntries(
            @DefaultValue(GLOBAL_QUALIFIED_NAME) @QueryParam("qualifiedName") String qualifiedName) {
        Response response =
                checkGlossaryPermission(qualifiedName, "glossary-delete");
        if (response != null) {
            return response;
        }
        int rowCount = glossaryDAO.deleteAllEntries(qualifiedName);
        log.info("Delete all glossary entry: " + rowCount);
        return Response.ok(rowCount).build();
    }

    private Response checkGlossaryPermission(String qualifiedName,
            String action) {
        if (isProjectGlossary(qualifiedName)) {
            HProject project = getProjectByQualifiedName(qualifiedName);
            if (project == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Invalid qualifiedName:" + qualifiedName)
                        .build();
            }
            identity.checkPermission(project, action);
        } else {
            identity.checkPermission("", action);
        }
        return null;
    }

    public static boolean isProjectGlossary(String qualifiedName) {
        return StringUtils.isNotBlank(qualifiedName)
                && qualifiedName.startsWith(PROJECT_QUALIFIER_PREFIX);
    }

    public static String getProjectSlug(String qualifiedName) {
        return qualifiedName.replaceFirst(PROJECT_QUALIFIER_PREFIX, "");
    }

    /**
     * Extract project slug from glossary qualifiedName by replace first
     * occurrence of {@link #PROJECT_QUALIFIER_PREFIX} with empty string.
     *
     * e.g. project/zanata returns zanata
     */
    private HProject getProjectByQualifiedName(String qualifiedName) {
        String projectSlug = getProjectSlug(qualifiedName);
        if (StringUtils.isNotBlank(projectSlug)) {
            return projectDAO.getBySlug(projectSlug);
        }
        return null;
    }

    private int validatePage(int page) {
        return page < 1 ? 1 : page;
    }

    private int validatePageSize(int sizePerPage) {
        return (sizePerPage > MAX_PAGE_SIZE) ? MAX_PAGE_SIZE
                : ((sizePerPage < 1) ? 1 : sizePerPage);
    }

    /**
     * Set en-US as source, should get this from server settings.
     */
    private HLocale getSourceLocale() {
        LocaleId srcLocaleId = LocaleId.EN_US;
        return localeServiceImpl.getByLocaleId(srcLocaleId);
    }

    private List<GlossarySortField>
            convertToSortField(String commaSeparatedFields) {
        List<GlossarySortField> result = Lists.newArrayList();
        String[] fields = StringUtils.split(commaSeparatedFields, ",");
        if (fields == null || fields.length <= 0) {
            // default sorting
            result.add(GlossarySortField
                    .getByField(GlossarySortField.SRC_CONTENT));
            return result;
        }
        for (String field : fields) {
            GlossarySortField sortField = GlossarySortField.getByField(field);
            if (sortField != null) {
                result.add(sortField);
            }
        }
        return result;
    }

    private GlossaryResults saveOrUpdateGlossaryEntries(
            List<GlossaryEntry> entries, Optional<LocaleId> transLocaleId) {
        GlossaryFileServiceImpl.GlossaryProcessed results =
                glossaryFileServiceImpl.saveOrUpdateGlossary(entries,
                        transLocaleId);
        List<GlossaryEntry> glossaryEntriesDTO = Lists.newArrayList();
        transferEntriesResource(results.getGlossaryEntries(),
                glossaryEntriesDTO);
        return new GlossaryResults(glossaryEntriesDTO, results.getWarnings());
    }

    private void transferEntriesResource(List<HGlossaryEntry> hGlossaryEntries,
            List<GlossaryEntry> glossaryEntries) {
        for (HGlossaryEntry hGlossaryEntry : hGlossaryEntries) {
            GlossaryEntry glossaryEntry = generateGlossaryEntry(hGlossaryEntry);
            for (HGlossaryTerm term : hGlossaryEntry.getGlossaryTerms()
                    .values()) {
                GlossaryTerm glossaryTerm = generateGlossaryTerm(term);
                glossaryEntry.getGlossaryTerms().add(glossaryTerm);
            }
            glossaryEntries.add(glossaryEntry);
        }
    }

    public void transferEntriesLocaleResource(
            List<HGlossaryEntry> hGlossaryEntries,
            ResultList<GlossaryEntry> resultList, HLocale locale) {
        for (HGlossaryEntry hGlossaryEntry : hGlossaryEntries) {
            GlossaryEntry glossaryEntry = generateGlossaryEntry(hGlossaryEntry);
            HLocale srcLocale = hGlossaryEntry.getSrcLocale();
            Optional<GlossaryTerm> srcTerm =
                    getGlossaryTerm(hGlossaryEntry, srcLocale);
            if (srcTerm.isPresent()) {
                glossaryEntry.getGlossaryTerms().add(0, srcTerm.get());
            }
            if (locale != srcLocale) {
                Optional<GlossaryTerm> transTerm =
                        getGlossaryTerm(hGlossaryEntry, locale);
                if (transTerm.isPresent()) {
                    glossaryEntry.getGlossaryTerms().add(transTerm.get());
                }
            }
            resultList.getResults().add(glossaryEntry);
        }
    }

    public Optional<GlossaryTerm> getGlossaryTerm(HGlossaryEntry hGlossaryEntry,
            HLocale locale) {
        if (!hGlossaryEntry.getGlossaryTerms().containsKey(locale)) {
            return Optional.empty();
        }
        HGlossaryTerm hGlossaryTerm =
                hGlossaryEntry.getGlossaryTerms().get(locale);
        return Optional.of(generateGlossaryTerm(hGlossaryTerm));
    }

    public GlossaryEntry generateGlossaryEntry(HGlossaryEntry hGlossaryEntry) {
        GlossaryEntry glossaryEntry = new GlossaryEntry(hGlossaryEntry.getId());
        glossaryEntry.setSrcLang(hGlossaryEntry.getSrcLocale().getLocaleId());
        glossaryEntry.setSourceReference(hGlossaryEntry.getSourceRef());
        glossaryEntry.setPos(hGlossaryEntry.getPos());
        glossaryEntry.setDescription(hGlossaryEntry.getDescription());
        glossaryEntry.setQualifiedName(new QualifiedName(
                hGlossaryEntry.getGlossary().getQualifiedName()));
        glossaryEntry.setTermsCount(hGlossaryEntry.getGlossaryTerms().size());
        return glossaryEntry;
    }

    public GlossaryTerm generateGlossaryTerm(HGlossaryTerm hGlossaryTerm) {
        GlossaryTerm glossaryTerm = new GlossaryTerm();
        glossaryTerm.setContent(hGlossaryTerm.getContent());
        glossaryTerm.setLocale(hGlossaryTerm.getLocale().getLocaleId());
        String username = "";
        if (hGlossaryTerm.getLastModifiedBy() != null) {
            username = hGlossaryTerm.getLastModifiedBy().getName();
        }
        glossaryTerm.setLastModifiedBy(username);
        glossaryTerm.setLastModifiedDate(hGlossaryTerm.getLastChanged());
        glossaryTerm.setComment(hGlossaryTerm.getComment());
        return glossaryTerm;
    }

    private abstract class GlossaryStreamingOutput implements StreamingOutput {
        protected final List<GlossaryEntry> entries;
        protected final LocaleId srcLocaleId;
        protected final List<LocaleId> transLocales;

        public GlossaryStreamingOutput(List<GlossaryEntry> entries,
                LocaleId srcLocaleId, List<LocaleId> transLocales) {
            this.entries = entries;
            this.srcLocaleId = srcLocaleId;
            this.transLocales = transLocales;
        }

        public abstract void write(OutputStream output)
                throws IOException, WebApplicationException;
    }

    private class CSVStreamingOutput extends GlossaryStreamingOutput {

        public CSVStreamingOutput(List<GlossaryEntry> entries,
                LocaleId srcLocaleId, List<LocaleId> transLocales) {
            super(entries, srcLocaleId, transLocales);
        }

        @Override
        public void write(OutputStream output)
                throws IOException, WebApplicationException {
            try {
                GlossaryCSVWriter writer = new GlossaryCSVWriter();
                writer.write(output, entries, srcLocaleId, transLocales);
            } finally {
                output.close();
            }
        }
    }

    private class PotStreamingOutput extends GlossaryStreamingOutput {

        public PotStreamingOutput(List<GlossaryEntry> entries,
                LocaleId srcLocaleId, List<LocaleId> transLocales) {
            super(entries, srcLocaleId, transLocales);
        }

        @Override
        public void write(OutputStream output)
                throws IOException, WebApplicationException {
            final ZipOutputStream zipOutput = new ZipOutputStream(output);
            zipOutput.setMethod(ZipOutputStream.DEFLATED);
            try {
                GlossaryPoWriter writer = new GlossaryPoWriter(false);
                for (LocaleId transLocale : transLocales) {
                    String filename = transLocale + ".po";
                    zipOutput.putNextEntry(new ZipEntry(filename));
                    writer.write(zipOutput, entries, srcLocaleId, transLocale);
                    zipOutput.closeEntry();
                }
            } finally {
                zipOutput.close();
            }
        }
    }
}
