package org.zanata.adapter.asciidoc;

import net.sf.okapi.common.BOMNewlineEncodingDetector;
import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.exceptions.OkapiBadFilterInputException;
import net.sf.okapi.common.exceptions.OkapiUnsupportedEncodingException;
import net.sf.okapi.common.filters.AbstractFilter;
import net.sf.okapi.common.filters.FilterConfiguration;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.filters.json.Parameters;
import net.sf.okapi.filters.json.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class AsciidocFilter extends AbstractFilter {
    private static final String MIME_TYPE = "application/asciidoc";
    private static final Logger log = LoggerFactory.getLogger(AsciidocFilter.class);

    private BOMNewlineEncodingDetector detector;
    private RawDocument currentRawDocument;
    private AsciidocEventBuilder eventBuilder;
    private Parameters params;
    private AsciidocParser parser;

    public AsciidocFilter() {
        super();
        setMimeType(MIME_TYPE);
        setMultilingual(false);
        setName("okf_asciidoc");
        setDisplayName("Asciidoc Filter");

        addConfiguration(new FilterConfiguration(
                getName(),
                MIME_TYPE,
                getClass().getName(),
                "Asciidoc",
                "Asciidoc files",
                null,
                ".adoc"));
        setParameters(new Parameters());
    }

    @Override
    protected boolean isUtf8Encoding() {
        return detector != null && detector.hasUtf8Encoding();
    }

    @Override
    protected boolean isUtf8Bom() {
        return detector != null && detector.hasUtf8Bom();
    }

    @Override
    public void open(RawDocument input) {
        open(input, true);
    }

    @Override
    public void open(RawDocument input, boolean generateSkeleton) {
        currentRawDocument = input;

        detector = new BOMNewlineEncodingDetector(input.getStream(), input.getEncoding());
        detector.detectAndRemoveBom();
        setNewlineType(detector.getNewlineType().toString());

        String encoding = getDetectedEncoding();
        input.setEncoding(encoding);
        setEncoding(encoding);
        setOptions(input.getSourceLocale(), input.getTargetLocale(), encoding, generateSkeleton);

        BufferedReader reader;
        try {
            reader = new BufferedReader(new InputStreamReader(detector.getInputStream(), encoding));
        }
        catch (UnsupportedEncodingException e) {
            throw new OkapiUnsupportedEncodingException(
                    String.format("The encoding '%s' is not supported.", encoding), e);
        }

        if (input.getInputURI() != null) {
            setDocumentName(input.getInputURI().getPath());
        }

        // Create EventBuilder with document name as rootId
        if (eventBuilder == null) {
            eventBuilder = new AsciidocEventBuilder(getParentId(), this);
        } else {
            eventBuilder.reset(getParentId(), this);
        }
        eventBuilder.setPreserveWhitespace(true);

        // Compile code finder rules
        if (params.getUseCodeFinder()) {
            params.getCodeFinder().compile();
            eventBuilder.setCodeFinder(params.getCodeFinder());
        }

        eventBuilder.addFilterEvent(createStartFilterEvent());
        this.parser = new AsciidocParser(reader, eventBuilder);
        try {
            parser.parse();
        } catch (ParseException e) {
            String error = String.format("Error parsing ASCIIDOC file: %s", e.getMessage());
            log.error(error, e);
            throw new OkapiBadFilterInputException(error, e);
        }

        eventBuilder.flushRemainingTempEvents();
        eventBuilder.addFilterEvent(createEndFilterEvent());
    }

    @Override
    public boolean hasNext() {
        return eventBuilder.hasNext();
    }

    @Override
    public Event next() {
        return eventBuilder.next();
    }

    @Override
    public IParameters getParameters() {
        return params;
    }

    @Override
    public void setParameters(IParameters iParameters) {
        this.params = (Parameters)iParameters;
    }

    @Override
    public void close() {
        if (currentRawDocument != null) {
            currentRawDocument.close();
            detector = null;
            eventBuilder = null;
        }
    }

    private String getDetectedEncoding() {
        String detectedEncoding = getEncoding();
        if (detector.isDefinitive()) {
            detectedEncoding = detector.getEncoding();
            log.debug("Overriding user set encoding (if any). Setting auto-detected encoding {}.",
                    detectedEncoding);

        } else if (!detector.isDefinitive() && getEncoding().equals(RawDocument.UNKOWN_ENCODING)) {
            detectedEncoding = detector.getEncoding();
            log.debug("Default encoding and detected encoding not found. Using best guess encoding {}",
                    detectedEncoding);
        }
        return detectedEncoding;
    }
}
