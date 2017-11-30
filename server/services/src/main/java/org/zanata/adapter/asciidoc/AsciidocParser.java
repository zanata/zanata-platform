package org.zanata.adapter.asciidoc;

import net.sf.okapi.common.skeleton.GenericSkeleton;
import net.sf.okapi.filters.json.parser.ParseException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Cell;
import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.ast.DescriptionList;
import org.asciidoctor.ast.DescriptionListEntry;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.ListItem;
import org.asciidoctor.ast.Row;
import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.ast.Table;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.zanata.adapter.asciidoc.AsciidocUtils.ATTR_WHITELIST;
import static org.zanata.adapter.asciidoc.AsciidocUtils.NEWLINE;
import static org.zanata.adapter.asciidoc.AsciidocUtils.getAdmonitionSkeleton;
import static org.zanata.adapter.asciidoc.AsciidocUtils.getBlockSkeleton;
import static org.zanata.adapter.asciidoc.AsciidocUtils.getDocAttribute;
import static org.zanata.adapter.asciidoc.AsciidocUtils.getId;
import static org.zanata.adapter.asciidoc.AsciidocUtils.getRole;
import static org.zanata.adapter.asciidoc.AsciidocUtils.getSectionTitleSkeleton;
import static org.zanata.adapter.asciidoc.AsciidocUtils.getBlockTitle;
import static org.zanata.adapter.asciidoc.AsciidocUtils.getBlockTitleSkeleton;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class AsciidocParser {

    private final BufferedReader reader;
    private final AsciidocEventBuilder eventBuilder;
    private final Asciidoctor asciidoctor;
    private final Map<String, Object> options;

    public AsciidocParser(BufferedReader reader, AsciidocEventBuilder eventBuilder) {
        this.reader = reader;
        this.eventBuilder = eventBuilder;

        asciidoctor = Asciidoctor.Factory.create();
        options = new HashMap<>();
    }

    public void parse() throws ParseException {
        try {
            String contents = IOUtils.toString(reader);
            if (StringUtils.isBlank(contents)) {
                return;
            }

            Document doc = asciidoctor.load(contents, options);
            if (doc == null) {
                return;
            }
            parseSectionTitle(doc.getDoctitle(), 0);
            parseDocAttributes(doc.getAttributes());

            for (StructuralNode node : doc.getBlocks()) {
//                analyze(node, eventBuilder);
                parse(node);
            }
        } catch (IOException e) {
            throw new ParseException("Cannot parse asciidoc file");
        }
    }

    private void parse(@Nonnull ContentNode node) {
        Optional<String> id = getId(node.getAttributes());
        if (id.isPresent()) {
            appendNewLine();
            appendNonTranslatable(id.get(), AsciidocUtils.getResId(id.get()),
                    true);
        }
        Optional<String> role = getRole(node.getAttributes());
        if (role.isPresent()) {
            appendNewLine();
            appendNonTranslatable(role.get(),
                    AsciidocUtils.getResId(role.get()),
                    true);
        }

        if (node instanceof Section) {
            Section section = (Section) node;
            parseSectionTitle(section.getTitle(), section.getLevel());
            processInnerBlock(section);
        } else if (node instanceof Block) {
            Block block = (Block) node;
            Optional<String> title = getBlockTitle(node.getAttributes());
            if (title.isPresent()) {
                parseBlockTitle(title.get());
            }
            if (block.getLines() != null && !block.getLines().isEmpty()) {
                if (AsciidocUtils.isAdmonition(block.getAttributes())) {
                    GenericSkeleton skeleton =
                            new GenericSkeleton(getAdmonitionSkeleton(block.getAttributes()));

                    eventBuilder.startTextUnit(skeleton);
                    for (String content : block.getLines()) {
                        eventBuilder.addToTextUnit(content + NEWLINE);
                    }
                    eventBuilder.setTextUnitName(AsciidocUtils
                            .getResId(String.join(NEWLINE, block.getLines())));
                    eventBuilder.setTextUnitTranslatable(true);
                    eventBuilder.endTextUnit();
                    appendNewLine();
                } else {
                    appendBlock();
                    eventBuilder.startTextUnit();
                    for (String content : block.getLines()) {
                        eventBuilder.addToTextUnit(content + NEWLINE);
                    }
                    eventBuilder.setTextUnitName(AsciidocUtils
                            .getResId(String.join(NEWLINE, block.getLines())));
                    eventBuilder.setTextUnitTranslatable(true);
                    eventBuilder.endTextUnit();
                    processInnerBlock(block);
                    appendBlock();
                }
            }
        } else if(node instanceof Table) {
            Table table = (Table)node;
            processTableAttributes(table.getAttributes());
            parseBlockTitle(table.getTitle());
            appendNonTranslatable(AsciidocUtils.getTableSkeleton(), "", true);
            for (Row row: table.getHeader()) {
                for (Cell cell: row.getCells()) {
                    parse(cell);
                }
                appendNewLine();
            }
            for (Row row: table.getBody()) {
                for (Cell cell: row.getCells()) {
                    parse(cell);
                }
                appendNewLine();
            }
            for (Row row: table.getFooter()) {
                for (Cell cell: row.getCells()) {
                    parse(cell);
                }
                appendNewLine();
            }
            appendNonTranslatable(AsciidocUtils.getTableSkeleton(), "", true);
        } else if(node instanceof Cell) {
            Cell cell = (Cell)node;
            eventBuilder.startTextUnit(
                    new GenericSkeleton(AsciidocUtils.getCellStartSkeleton()));
            eventBuilder.addToTextUnit(cell.getText());
            eventBuilder.endTextUnit(
                    new GenericSkeleton(AsciidocUtils.getCellEndSkeleton()));
        } else if(node instanceof org.asciidoctor.ast.List) {

        } else if(node instanceof ListItem) {

        } else if(node instanceof DescriptionList) {

        }
    }

    private void processInnerBlock(StructuralNode node) {
        for (StructuralNode innerNode : node.getBlocks()) {
            appendBlock();
            parse(innerNode);
            appendBlock();
        }
    }

    private void processTableAttributes(
            @Nullable Map<String, Object> attributes) {
        if (attributes != null && !attributes.isEmpty()) {
            List<String> tblAttrs = new ArrayList<>();
            for (String attr: AsciidocUtils.TABLE_ATTR) {
                if (attributes.containsKey(attr)) {
                    tblAttrs.add(attr + "=\"" + attributes.get(attr) + "\"");
                }
            }
            if (!tblAttrs.isEmpty()) {
                appendNonTranslatable("[" + String.join(", ", tblAttrs) + "]",
                        "", true);
            }
        }
    }

    private void appendBlock() {
        appendNonTranslatable(getBlockSkeleton(), "", true);
    }

    private void parseBlockTitle(String title) {
        GenericSkeleton skeleton =
                new GenericSkeleton(getBlockTitleSkeleton());
        eventBuilder.startTextUnit(skeleton);
        eventBuilder.startTextUnit(title);
        eventBuilder.setTextUnitName(AsciidocUtils.getResId(title));
        eventBuilder.setTextUnitTranslatable(true);
        eventBuilder.endTextUnit();

        appendNewLine();
    }

    private void parseSectionTitle(@Nullable String value, int level) {
        if (StringUtils.isNotBlank(value)) {
            GenericSkeleton skeleton =
                    new GenericSkeleton(getSectionTitleSkeleton(level));
            eventBuilder.startTextUnit(skeleton);
            eventBuilder.addToTextUnit(value);
            eventBuilder.setTextUnitName(AsciidocUtils.getResId(value));
            eventBuilder.setTextUnitTranslatable(true);
            eventBuilder.endTextUnit();

            appendNewLine();
        }
    }

    private void appendNonTranslatable(String value, String name,
            boolean endWithNewline) {
        eventBuilder.startTextUnit();
        eventBuilder.addToTextUnit(value + (endWithNewline ? NEWLINE : ""));
        eventBuilder.setTextUnitName(name);
        eventBuilder.setTextUnitTranslatable(false);
        eventBuilder.endTextUnit();
    }

    private void parseDocAttributes(@Nullable Map<String, Object> attrs) {
        if (attrs != null && !attrs.isEmpty()) {
            int authorCount = ((Long)attrs.get("authorcount")).intValue();
            if (authorCount > 0) {
                List<String> authors = new ArrayList<>();
                for (int i = 1; i <= authorCount; i++) {
                    String name = (String)attrs.get("author_" + i);
                    String email = (String)attrs.get("email_" + i);
                    authors.add(name + " <" + email + ">");
                }
                String value = String.join(" ;", authors);
                String name =
                        AsciidocUtils.getResId(String.join(" ;", authors));
                appendNonTranslatable(value, name, true);
            }

            for (String attr: ATTR_WHITELIST) {
                if (attrs.containsKey(attr)) {
                    String value =
                            getDocAttribute(attr, attrs.get(attr));
                    appendNonTranslatable(value, AsciidocUtils.getResId(value),
                            true);
                }
            }
        }
    }

    protected void appendNewLine() {
        appendNonTranslatable("", "", true);
    }

    private void appendTextUnit(String msg, int level,
            AsciidocEventBuilder eventBuilder) {
        if (StringUtils.isBlank(msg)) {
            appendNonTranslatable(msg, "", false);
        } else {
            eventBuilder.startTextUnit(msg);
            eventBuilder.endTextUnit();
        }
    }

    private void analyze(@Nonnull ContentNode node,
            AsciidocEventBuilder eventBuilder) {
        if (node instanceof Section) {
            Section section = (Section) node;
            parseSectionTitle(section.getTitle(), section.getLevel());
        }

        if (node instanceof Block) {
            Block block = (Block) node;
            appendTextUnit(String.join("\n", block.getLines()),
                    block.getLevel(), eventBuilder);
        }

        if(node instanceof Table) {
            Table table = (Table)node;
            for (Row row: table.getBody()) {
                for (Cell cell: row.getCells()) {
                    analyze(cell, eventBuilder);
                }
            }
        }

        if(node instanceof Cell) {
            Cell cell = (Cell)node;
            appendTextUnit(cell.getText(), 1, eventBuilder);
        }

        if(node instanceof org.asciidoctor.ast.List) {
            org.asciidoctor.ast.List list = (org.asciidoctor.ast.List)node;
            for (StructuralNode structuralNode: list.getItems()) {
                analyze(structuralNode, eventBuilder);
            }
        }

        if(node instanceof ListItem) {
            ListItem item = (ListItem)node;
            appendTextUnit(item.getText(), item.getLevel(), eventBuilder);
        }

        if(node instanceof DescriptionList) {
            DescriptionList descriptionList = (DescriptionList)node;
            for (DescriptionListEntry entry: descriptionList.getItems()) {
                for(ListItem item: entry.getTerms()) {
                    analyze(item, eventBuilder);
                }
            }
        }

        if (node instanceof StructuralNode) {
            StructuralNode structuralNode = (StructuralNode) node;
            if (structuralNode.getBlocks() != null) {
                for (StructuralNode childNode : structuralNode.getBlocks()) {
                    analyze(childNode, eventBuilder);
                }
            }
        }
    }
}
