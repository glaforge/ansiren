package io.github.glaforge.ansiren;

import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.front.matter.YamlFrontMatterExtension;
import org.commonmark.ext.front.matter.YamlFrontMatterNode;

import java.util.Arrays;

public class MarkdownRenderer {
    private final Parser parser;

    public MarkdownRenderer() {
        this.parser = Parser.builder()
                .extensions(Arrays.asList(
                        TablesExtension.create(),
                        YamlFrontMatterExtension.create()
                ))
                .build();
    }

    public String render(String markdown) {
        Node document = parser.parse(markdown);
        AnsiVisitor visitor = new AnsiVisitor();
        document.accept(visitor);
        return visitor.getResult();
    }

    private static class AnsiVisitor extends AbstractVisitor {
        private final StringBuilder sb = new StringBuilder();
        private int listLevel = 0;

        public String getResult() {
            return sb.toString();
        }

        @Override
        public void visit(Heading heading) {
            int level = heading.getLevel();
            Ansi ansi = Ansi.ready().bold();
            switch (level) {
                case 1 -> ansi.brightRed();
                case 2 -> ansi.brightGreen();
                case 3 -> ansi.brightYellow();
                default -> ansi.brightBlue();
            }
            sb.append(ansi.toString());
            visitChildren(heading);
            sb.append(Ansi.ready().reset().toString()).append("\n\n");
        }

        @Override
        public void visit(Paragraph paragraph) {
            visitChildren(paragraph);
            sb.append("\n\n");
        }

        @Override
        public void visit(Text text) {
            sb.append(text.getLiteral());
        }

        @Override
        public void visit(StrongEmphasis strongEmphasis) {
            sb.append(Ansi.ready().bold().toString());
            visitChildren(strongEmphasis);
            sb.append(Ansi.ready().reset().toString());
        }

        @Override
        public void visit(Emphasis emphasis) {
            sb.append(Ansi.ready().italic().toString());
            visitChildren(emphasis);
            sb.append(Ansi.ready().reset().toString());
        }

        @Override
        public void visit(Code code) {
            sb.append(Ansi.ready().yellow().toString())
                    .append("`")
                    .append(code.getLiteral())
                    .append("`")
                    .append(Ansi.ready().reset().toString());
        }

        @Override
        public void visit(FencedCodeBlock fencedCodeBlock) {
            sb.append(Ansi.ready().brightBlack().toString())
                    .append("```")
                    .append(fencedCodeBlock.getInfo() != null ? fencedCodeBlock.getInfo() : "")
                    .append("\n")
                    .append(fencedCodeBlock.getLiteral())
                    .append("```")
                    .append(Ansi.ready().reset().toString())
                    .append("\n\n");
        }

        @Override
        public void visit(BlockQuote blockQuote) {
            sb.append(Ansi.ready().cyan().toString()).append("> ");
            visitChildren(blockQuote);
            sb.append(Ansi.ready().reset().toString());
        }

        @Override
        public void visit(BulletList bulletList) {
            listLevel++;
            visitChildren(bulletList);
            listLevel--;
            if (listLevel == 0) {
                sb.append("\n");
            }
        }

        @Override
        public void visit(OrderedList orderedList) {
            listLevel++;
            visitChildren(orderedList);
            listLevel--;
            if (listLevel == 0) {
                sb.append("\n");
            }
        }

        @Override
        public void visit(ListItem listItem) {
            sb.append("  ".repeat(Math.max(0, listLevel - 1)))
                    .append(Ansi.ready().brightBlue().toString())
                    .append("* ")
                    .append(Ansi.ready().reset().toString());
            visitChildren(listItem);
            sb.append("\n");
        }

        @Override
        public void visit(Link link) {
            sb.append(Ansi.ready().underline().blue().toString());
            visitChildren(link);
            sb.append(Ansi.ready().reset().toString())
                    .append(" (")
                    .append(link.getDestination())
                    .append(")");
        }

        @Override
        public void visit(CustomNode customNode) {
            if (customNode instanceof YamlFrontMatterNode node) {
                sb.append(Ansi.ready().brightMagenta().toString())
                        .append("---")
                        .append("\n");
                for (String line : node.getValues()) {
                    sb.append(line).append("\n");
                }
                sb.append("---")
                        .append(Ansi.ready().reset().toString())
                        .append("\n\n");
            } else {
                visitChildren(customNode);
            }
        }
    }
}
