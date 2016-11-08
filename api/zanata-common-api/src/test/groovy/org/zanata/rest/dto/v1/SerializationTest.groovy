package org.zanata.rest.dto.v1

import spock.lang.Specification;

import org.codehaus.jackson.map.ObjectMapper;
import org.zanata.common.ContentState;
import org.zanata.common.ContentType;
import org.zanata.common.LocaleId;
import org.zanata.common.ResourceType;
import org.zanata.rest.dto.Glossary;
import org.zanata.rest.dto.GlossaryEntry;
import org.zanata.rest.dto.GlossaryTerm;
import org.zanata.rest.dto.Link;
import org.zanata.rest.dto.Links;
import org.zanata.rest.dto.Person;
import org.zanata.rest.dto.Project;
import org.zanata.rest.dto.extensions.comment.SimpleComment;
import org.zanata.rest.dto.extensions.gettext.HeaderEntry;
import org.zanata.rest.dto.extensions.gettext.PoHeader;
import org.zanata.rest.dto.extensions.gettext.PoTargetHeader;
import org.zanata.rest.dto.extensions.gettext.PotEntryHeader;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.ResourceMeta;
import org.zanata.rest.dto.resource.TextFlow;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.rest.dto.resource.TranslationsResource;

import static org.zanata.rest.dto.v1.JaxbTestUtil.roundTripXml

public class SerializationTest extends Specification {

    private ObjectMapper mapper;

    def setup() {
        mapper = new ObjectMapper();
    }

    private def roundTripJson(def obj) {
        String json = mapper.writeValueAsString(obj);
        return mapper.readValue(json, obj.class);
    }

    private Person createPerson() {
        return new Person("user@localhost", "User");
    }

    private Project createProject() {
        Project p = new Project().createSample();

        Links links = new Links();
        links.add(new Link(new URI("http://www.zanata.org"), "", "linkType"));
        links.add(new Link(new URI("http://www2.zanata.org"), "", "linkType"));
        p.setLinks(links);
        return p
    }

    def "round trip for Project"() {
        given:
        def p = createProject()

        when:
        Project p2 = roundTripJson(p)
        Project p3 = roundTripXml(p);

        then:
        p2 == p
        p3 == p
    }

    def "round trip for Person"() {
        given:
        Person p = createPerson();

        when:
        Person p2 = roundTripJson(p)
        Person p3 = roundTripXml(p);

        then:
        p2 == p
        p3 == p
    }

    def "round trip for ResourceMeta"() {
        given:
        ResourceMeta res = new ResourceMeta("id");
        PoHeader poHeader = new PoHeader("comment", new HeaderEntry("h1", "v1"),
            new HeaderEntry("h2", "v2"));
        res.getExtensions(true).add(poHeader);

        when:
        ResourceMeta res2 = roundTripJson(res)
        ResourceMeta res3 = roundTripXml(res, PoHeader.class);

        then:
        res2 == res
        res3 == res
    }

    def "round trip for SourceResource"() {
        given:
        Resource sourceResource = new Resource("Acls.pot");
        sourceResource.setType(ResourceType.FILE);
        sourceResource.setContentType(ContentType.PO);
        sourceResource.setLang(LocaleId.EN);
        TextFlow tf = new TextFlow();
        tf.setContents("ttff");
        SimpleComment comment = new SimpleComment("test");
        PotEntryHeader pot = new PotEntryHeader();
        pot.setContext("context");
        pot.getReferences().add("fff");
        tf.getExtensions(true).add(comment);
        tf.getExtensions(true).add(pot);

        TextFlow tf2 = new TextFlow();
        tf2.setContents("ttff2");
        sourceResource.getTextFlows().add(tf);
        sourceResource.getTextFlows().add(tf2);
        PoHeader poHeader = new PoHeader("comment", new HeaderEntry("h1", "v1"),
            new HeaderEntry("h2", "v2"));
        sourceResource.getExtensions(true).add(
            poHeader);

        when:
        Resource res2 = roundTripJson(sourceResource)
        Resource res3 = roundTripXml(sourceResource, PoHeader.class);

        then:
        res2 == sourceResource
        res3 == sourceResource
    }

    def "round trip for TranslationsResource"() {
        given:
        TranslationsResource entity = new TranslationsResource();
        TextFlowTarget target = new TextFlowTarget("rest1");
        target.setContents("hello world");
        target.setState(ContentState.Translated);
        target.setTranslator(createPerson());
        SimpleComment comment = new SimpleComment("testcomment");
        target.getExtensions(true).add(comment);
        // for the convenience of test
        entity.getTextFlowTargets().add(target);
        entity.getExtensions(true);
        PoTargetHeader poTargetHeader =
                new PoTargetHeader("target header comment", new HeaderEntry(
                        "ht", "vt1"), new HeaderEntry("th2", "tv2"));

        entity.getExtensions(true).add(poTargetHeader);

        when:
        TranslationsResource res2 = roundTripJson(entity)
        TranslationsResource res3 = roundTripXml(entity);

        then:
        res2 == entity
        res3 == entity
    }

    def "round trip for Glossary"() {
        given:
        Glossary glossary = new Glossary();

        GlossaryEntry entry = new GlossaryEntry();
        entry.setSrcLang(LocaleId.EN_US);
        entry.setSourceReference("source ref");

        GlossaryTerm term = new GlossaryTerm();
        term.setContent("testData1");
        term.setLocale(LocaleId.EN_US);
        term.setComment("comment1");
        term.setComment("comment2");
        term.setComment("comment3");

        GlossaryTerm term2 = new GlossaryTerm();
        term2.setContent("testData2");
        term2.setLocale(LocaleId.DE);
        term2.setComment("comment4");
        term2.setComment("comment5");
        term2.setComment("comment6");

        entry.getGlossaryTerms().add(term);
        entry.getGlossaryTerms().add(term2);
        glossary.getGlossaryEntries().add(entry);

        when:
        Glossary glossary2 = roundTripJson(glossary)
        Glossary glossary3 = roundTripXml(glossary);

        then:
        glossary2 == glossary
        glossary3 == glossary
    }
}
