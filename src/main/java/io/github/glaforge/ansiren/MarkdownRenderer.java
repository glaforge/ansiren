/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.glaforge.ansiren;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.commonmark.ext.front.matter.YamlFrontMatterBlock;
import org.commonmark.ext.front.matter.YamlFrontMatterExtension;
import org.commonmark.ext.front.matter.YamlFrontMatterNode;
import org.commonmark.ext.gfm.tables.TableBlock;
import org.commonmark.ext.gfm.tables.TableBody;
import org.commonmark.ext.gfm.tables.TableCell;
import org.commonmark.ext.gfm.tables.TableHead;
import org.commonmark.ext.gfm.tables.TableRow;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.task.list.items.TaskListItemMarker;
import org.commonmark.ext.task.list.items.TaskListItemsExtension;
import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.BlockQuote;
import org.commonmark.node.BulletList;
import org.commonmark.node.Code;
import org.commonmark.node.CustomBlock;
import org.commonmark.node.CustomNode;
import org.commonmark.node.Emphasis;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.Heading;
import org.commonmark.node.Link;
import org.commonmark.node.ListItem;
import org.commonmark.node.Node;
import org.commonmark.node.OrderedList;
import org.commonmark.node.Paragraph;
import org.commonmark.node.StrongEmphasis;
import org.commonmark.node.Text;
import org.commonmark.parser.Parser;

public class MarkdownRenderer {
    private final Parser parser;

    public MarkdownRenderer() {
        this.parser = Parser.builder()
                .extensions(Arrays.asList(
                        TablesExtension.create(),
                        YamlFrontMatterExtension.create(),
                        TaskListItemsExtension.create()
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
        private static final Pattern ANSI_PATTERN = Pattern.compile("\\u001B\\[[0-9;]*m");

        private final StringBuilder sb = new StringBuilder();
        private int listLevel = 0;

        private record RenderedCell(
                String content,
                int visibleLength,
                boolean header,
                TableCell.Alignment alignment
        ) {}

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
            if (paragraph.getParent() instanceof ListItem) {
                sb.append("\n");
            } else {
                sb.append("\n\n");
            }
        }

        @Override
        public void visit(Text text) {
            sb.append(text.getLiteral());
        }

        @Override
        public void visit(StrongEmphasis strongEmphasis) {
            sb.append(Ansi.ready().bold().toString());
            visitChildren(strongEmphasis);
            sb.append(Ansi.ready().boldOff().toString());
        }

        @Override
        public void visit(Emphasis emphasis) {
            sb.append(Ansi.ready().italic().toString());
            visitChildren(emphasis);
            sb.append(Ansi.ready().italicOff().toString());
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
            if (listItem.getFirstChild() == null) {
                sb.append("\n");
            }
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
        public void visit(CustomBlock customBlock) {
            if (customBlock instanceof YamlFrontMatterBlock) {
                sb.append(Ansi.ready().brightMagenta().toString())
                        .append("---")
                        .append("\n");
                visitChildren(customBlock);
                sb.append("---")
                        .append(Ansi.ready().reset().toString())
                        .append("\n\n");
            } else if (customBlock instanceof TableBlock tableBlock) {
                renderTable(tableBlock);
            } else {
                visitChildren(customBlock);
            }
        }

        @Override
        public void visit(CustomNode customNode) {
            if (customNode instanceof TaskListItemMarker marker) {
                if (marker.isChecked()) {
                    sb.append("[x] ");
                } else {
                    sb.append("[ ] ");
                }
            } else if (customNode instanceof YamlFrontMatterNode node) {
                sb.append(node.getKey()).append(": ");
                for (String value : node.getValues()) {
                    sb.append(value).append(" ");
                }
                sb.append("\n");
            } else {
                visitChildren(customNode);
            }
        }

        private void renderTable(TableBlock tableBlock) {
            List<List<RenderedCell>> headRows = new ArrayList<>();
            List<List<RenderedCell>> bodyRows = new ArrayList<>();

            for (Node section = tableBlock.getFirstChild(); section != null; section = section.getNext()) {
                boolean isHead = section instanceof TableHead;
                if (!isHead && !(section instanceof TableBody)) {
                    continue;
                }
                for (Node rowNode = section.getFirstChild(); rowNode != null; rowNode = rowNode.getNext()) {
                    if (rowNode instanceof TableRow) {
                        List<RenderedCell> row = new ArrayList<>();
                        for (Node cellNode = rowNode.getFirstChild(); cellNode != null; cellNode = cellNode.getNext()) {
                            if (cellNode instanceof TableCell cell) {
                                AnsiVisitor cellVisitor = new AnsiVisitor();
                                cellVisitor.visitChildren(cell);
                                String content = cellVisitor.getResult().strip();
                                int visibleLen = ANSI_PATTERN.matcher(content).replaceAll("").length();
                                row.add(new RenderedCell(content, visibleLen, cell.isHeader(), cell.getAlignment()));
                            }
                        }
                        if (isHead) {
                            headRows.add(row);
                        } else {
                            bodyRows.add(row);
                        }
                    }
                }
            }

            int numCols = 0;
            for (List<RenderedCell> row : headRows) {
                numCols = Math.max(numCols, row.size());
            }
            for (List<RenderedCell> row : bodyRows) {
                numCols = Math.max(numCols, row.size());
            }
            if (numCols == 0) {
                return;
            }

            int[] colWidths = new int[numCols];
            Arrays.fill(colWidths, 1);
            for (List<RenderedCell> row : headRows) {
                for (int i = 0; i < row.size(); i++) {
                    colWidths[i] = Math.max(colWidths[i], row.get(i).visibleLength());
                }
            }
            for (List<RenderedCell> row : bodyRows) {
                for (int i = 0; i < row.size(); i++) {
                    colWidths[i] = Math.max(colWidths[i], row.get(i).visibleLength());
                }
            }

            appendHorizontalBorder(colWidths, "┌", "┬", "┐");
            for (List<RenderedCell> row : headRows) {
                appendTableRow(row, colWidths, true);
            }
            if (!headRows.isEmpty() && !bodyRows.isEmpty()) {
                appendHorizontalBorder(colWidths, "├", "┼", "┤");
            }
            for (List<RenderedCell> row : bodyRows) {
                appendTableRow(row, colWidths, false);
            }
            appendHorizontalBorder(colWidths, "└", "┴", "┘");
            sb.append("\n");
        }

        private void appendHorizontalBorder(int[] colWidths, String left, String middle, String right) {
            sb.append(Ansi.ready().brightBlack().toString()).append(left);
            for (int i = 0; i < colWidths.length; i++) {
                if (i > 0) {
                    sb.append(middle);
                }
                sb.append("─".repeat(colWidths[i] + 2));
            }
            sb.append(right).append(Ansi.ready().reset().toString()).append("\n");
        }

        private void appendTableRow(List<RenderedCell> row, int[] colWidths, boolean isHeadRow) {
            String verticalBar = Ansi.ready().brightBlack().append("│").reset().toString();
            sb.append(verticalBar);
            for (int i = 0; i < colWidths.length; i++) {
                RenderedCell cell = i < row.size()
                        ? row.get(i)
                        : new RenderedCell("", 0, isHeadRow, null);
                int padding = Math.max(0, colWidths[i] - cell.visibleLength());
                int leftPad = 0;
                int rightPad = padding;
                if (cell.alignment() == TableCell.Alignment.RIGHT) {
                    leftPad = padding;
                    rightPad = 0;
                } else if (cell.alignment() == TableCell.Alignment.CENTER) {
                    leftPad = padding / 2;
                    rightPad = padding - leftPad;
                }

                sb.append(" ").append(" ".repeat(leftPad));
                if (cell.header() || isHeadRow) {
                    sb.append(Ansi.ready().bold().brightCyan().toString())
                            .append(cell.content())
                            .append(Ansi.ready().reset().toString());
                } else {
                    sb.append(cell.content());
                }
                sb.append(" ".repeat(rightPad)).append(" ").append(verticalBar);
            }
            sb.append("\n");
        }
    }
}
