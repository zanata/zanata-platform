package org.zanata.rest.service;

import java.util.List;
import java.util.Map;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.apache.commons.lang.StringUtils;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.zanata.common.GlossarySortField;
import org.zanata.common.LocaleId;
import org.zanata.dao.GlossaryDAO;
import org.zanata.exception.ZanataServiceException;
import org.zanata.model.HGlossaryEntry;
import org.zanata.model.HGlossaryTerm;
import org.zanata.model.HLocale;
import org.zanata.rest.GlossaryFileUploadForm;
import org.zanata.rest.dto.GlossaryEntry;
import org.zanata.rest.dto.GlossaryInfo;
import org.zanata.rest.dto.GlossaryLocaleInfo;
import org.zanata.rest.dto.GlossaryResults;
import org.zanata.rest.dto.GlossaryTerm;
import org.zanata.rest.dto.LocaleDetails;
import org.zanata.rest.dto.ResultList;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.GlossaryFileService;
import org.zanata.service.LocaleService;
import org.zanata.service.impl.GlossaryFileServiceImpl;

@RequestScoped
@Named("glossaryService")
@Path(GlossaryResource.SERVICE_PATH)
@Slf4j
@Transactional
public class GlossaryService implements GlossaryResource {
    @Context
    private Request request;

    @Inject
    private GlossaryDAO glossaryDAO;

    @Inject
    private GlossaryFileService glossaryFileServiceImpl;

    @Inject
    private ZanataIdentity identity;

    @Inject
    private LocaleService localeServiceImpl;

    @Override
    public Response getInfo() {
        ResponseBuilder response = request.evaluatePreconditions();
        if (response != null) {
            return response.build();
        }

        //set en-US as source, should get this from server settings.
        LocaleId srcLocaleId = LocaleId.EN_US;
        HLocale srcLocale = localeServiceImpl.getByLocaleId(srcLocaleId);

        int entryCount =
                glossaryDAO.getEntryCountBySourceLocales(LocaleId.EN_US);

        GlossaryLocaleInfo srcGlossaryLocale =
                new GlossaryLocaleInfo(generateLocaleDetails(srcLocale), entryCount);

        Map<LocaleId, Integer> transMap =
                glossaryDAO.getTranslationLocales(srcLocaleId);

        List<HLocale> supportedLocales =
            localeServiceImpl.getSupportedLocales();

        List<GlossaryLocaleInfo> transLocale = Lists.newArrayList();

        supportedLocales.stream()
            .filter(locale -> !locale.getLocaleId().equals(srcLocaleId))
            .forEach(locale -> {
                LocaleDetails localeDetails = generateLocaleDetails(locale);
                int count = transMap.containsKey(locale.getLocaleId()) ?
                    transMap.get(locale.getLocaleId()) : 0;

                transLocale.add(new GlossaryLocaleInfo(localeDetails, count));
            });
        GlossaryInfo glossaryInfo =
            new GlossaryInfo(srcGlossaryLocale, transLocale);

        return Response.ok(glossaryInfo).build();
    }

    private LocaleDetails generateLocaleDetails(HLocale locale) {
        return new LocaleDetails(locale.getLocaleId(),
            locale.retrieveDisplayName(), "");
    }

    private List<GlossarySortField> convertToSortField(
        String commaSeparatedFields) {
        List<GlossarySortField> result = Lists.newArrayList();

        String[] fields = StringUtils.split(commaSeparatedFields, ",");
        if(fields == null || fields.length <= 0) {
            //default sorting
            result.add(GlossarySortField
                    .getByField(GlossarySortField.SRC_CONTENT));
            return result;
        }

        for (String field : fields) {
            GlossarySortField sortField = GlossarySortField.getByField(field);
            if(sortField != null) {
                result.add(sortField);
            }
        }
        return result;
    }

    @Override
    public Response getEntries(
            @DefaultValue("en-US") @QueryParam("srcLocale") LocaleId srcLocale,
            @QueryParam("transLocale") LocaleId transLocale,
            @DefaultValue("1") @QueryParam("page") int page,
            @DefaultValue("1000") @QueryParam("sizePerPage") int sizePerPage,
            @QueryParam("filter") String filter,
            @QueryParam("sort") String fields) {

        ResponseBuilder response = request.evaluatePreconditions();
        if (response != null) {
            return response.build();
        }

        if(sizePerPage > MAX_PAGE_SIZE || sizePerPage < 1 || page < 1) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        int offset = (page - 1) * sizePerPage;

        List<HGlossaryEntry> hGlossaryEntries =
            glossaryDAO.getEntriesByLocale(srcLocale, offset, sizePerPage,
                filter, convertToSortField(fields));
        int totalCount =
            glossaryDAO.getEntriesCount(srcLocale, filter);

        ResultList<GlossaryEntry> resultList = new ResultList<GlossaryEntry>();
        resultList.setTotalCount(totalCount);

        if(transLocale != null) {
            //filter out all terms other than source term and term in transLocale
            HLocale locale = localeServiceImpl.getByLocaleId(transLocale);
            transferEntriesLocaleResource(hGlossaryEntries, resultList, locale);
        } else {
            //filter out all terms other than source term
            HLocale locale = localeServiceImpl.getByLocaleId(srcLocale);
            transferEntriesLocaleResource(hGlossaryEntries, resultList, locale);
        }
        return Response.ok(resultList).build();
    }

    @Override
    public Response post(List<GlossaryEntry> glossaryEntries) {
        identity.checkPermission("", "glossary-insert");
        ResponseBuilder response = request.evaluatePreconditions();
        if (response != null) {
            return response.build();
        }

        GlossaryResults results = saveGlossaryEntries(glossaryEntries);

        return Response.ok(results).build();
    }

    @Override
    public Response upload(@MultipartForm GlossaryFileUploadForm form) {
        identity.checkPermission("", "glossary-insert");

        final Response response;
        try {
            LocaleId srcLocaleId = new LocaleId(form.getSrcLocale());
            LocaleId transLocaleId = new LocaleId(form.getTransLocale());

            List<List<GlossaryEntry>> glossaryEntries =
                    glossaryFileServiceImpl
                            .parseGlossaryFile(form.getFileStream(),
                                    form.getFileName(), srcLocaleId,
                                    transLocaleId);

            GlossaryResults overallResult = new GlossaryResults();
            for (List<GlossaryEntry> entries : glossaryEntries) {
                GlossaryResults result = saveGlossaryEntries(entries);
                overallResult.getGlossaryEntries().addAll(
                        result.getGlossaryEntries());
                overallResult.getWarnings().addAll(result.getWarnings());
            }

            response =
                    Response.ok()
                            .header("Content-Disposition",
                                    "attachment; filename=\""
                                            + form.getFileName() + "\"")
                            .entity(overallResult)
                            .build();
            return response;
        } catch (ZanataServiceException e) {
            log.error(e.toString(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e).build();
        }
    }

    private GlossaryResults saveGlossaryEntries(
        List<GlossaryEntry> glossaryEntries) {

        GlossaryFileServiceImpl.GlossaryProcessed results =
                glossaryFileServiceImpl.saveOrUpdateGlossary(glossaryEntries);

        List<GlossaryEntry> glossaryEntriesDTO = Lists.newArrayList();
        transferEntriesResource(results.getGlossaryEntries(), glossaryEntriesDTO);

        return new GlossaryResults(
                glossaryEntriesDTO, results.getWarnings());
    }

    @Override
    public Response deleteEntry(Long id) {
        identity.checkPermission("", "glossary-delete");

        ResponseBuilder response = request.evaluatePreconditions();
        if (response != null) {
            return response.build();
        }

        HGlossaryEntry entry = glossaryDAO.findById(id);
        GlossaryEntry deletedEntry = generateGlossaryEntry(entry);

        if(entry != null) {
            glossaryDAO.makeTransient(entry);
            glossaryDAO.flush();
            return Response.ok(deletedEntry).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                .entity("Glossary " + id + "entry not found").build();
        }
    }

    @Override
    public Response deleteAllEntries() {
        identity.checkPermission("", "glossary-delete");
        ResponseBuilder response = request.evaluatePreconditions();
        if (response != null) {
            return response.build();
        }
        int rowCount = glossaryDAO.deleteAllEntries();
        log.info("Glossary delete all: " + rowCount);

        return Response.ok().build();
    }

    public void transferEntriesResource(List<HGlossaryEntry> hGlossaryEntries,
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
            List<HGlossaryEntry> hGlossaryEntries, ResultList<GlossaryEntry> resultList,
            HLocale locale) {
        for (HGlossaryEntry hGlossaryEntry : hGlossaryEntries) {
            GlossaryEntry glossaryEntry = generateGlossaryEntry(hGlossaryEntry);

            GlossaryTerm srcTerm =
                getGlossaryTerm(hGlossaryEntry, hGlossaryEntry.getSrcLocale());
            if (srcTerm != null) {
                glossaryEntry.getGlossaryTerms().add(srcTerm);
            }

            GlossaryTerm transTerm = getGlossaryTerm(hGlossaryEntry, locale);
            if (transTerm != null) {
                glossaryEntry.getGlossaryTerms().add(transTerm);
            }
            resultList.getResults().add(glossaryEntry);
        }
    }

    public GlossaryTerm getGlossaryTerm(HGlossaryEntry hGlossaryEntry,
        HLocale locale) {
        if (!hGlossaryEntry.getGlossaryTerms().containsKey(locale)) {
            return null;
        }
        HGlossaryTerm hGlossaryTerm =
            hGlossaryEntry.getGlossaryTerms().get(locale);
        GlossaryTerm glossaryTerm = generateGlossaryTerm(hGlossaryTerm);
        return glossaryTerm;
    }

    public GlossaryEntry generateGlossaryEntry(HGlossaryEntry hGlossaryEntry) {
        GlossaryEntry glossaryEntry = new GlossaryEntry(hGlossaryEntry.getId());
        glossaryEntry.setSrcLang(hGlossaryEntry.getSrcLocale().getLocaleId());
        glossaryEntry.setSourceReference(hGlossaryEntry.getSourceRef());
        glossaryEntry.setPos(hGlossaryEntry.getPos());
        glossaryEntry.setDescription(hGlossaryEntry.getDescription());
        glossaryEntry.setTermsCount(hGlossaryEntry.getGlossaryTerms().size());
        return glossaryEntry;
    }

    public GlossaryTerm generateGlossaryTerm(HGlossaryTerm hGlossaryTerm) {
        GlossaryTerm glossaryTerm = new GlossaryTerm();
        glossaryTerm.setContent(hGlossaryTerm.getContent());
        glossaryTerm.setLocale(hGlossaryTerm.getLocale().getLocaleId());
        String username = "";
        if(hGlossaryTerm.getLastModifiedBy() != null) {
            username = hGlossaryTerm.getLastModifiedBy().getName();
        }
        glossaryTerm.setLastModifiedBy(username);
        glossaryTerm.setLastModifiedDate(hGlossaryTerm.getLastChanged());
        glossaryTerm.setComment(hGlossaryTerm.getComment());

        return glossaryTerm;
    }
}
