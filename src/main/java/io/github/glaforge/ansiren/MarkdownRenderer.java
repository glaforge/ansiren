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
import org.commonmark.node.HardLineBreak;
import org.commonmark.node.Heading;
import org.commonmark.node.HtmlInline;
import org.commonmark.node.Link;
import org.commonmark.node.ListItem;
import org.commonmark.node.Node;
import org.commonmark.node.OrderedList;
import org.commonmark.node.Paragraph;
import org.commonmark.node.SoftLineBreak;
import org.commonmark.node.StrongEmphasis;
import org.commonmark.node.Text;
import org.commonmark.parser.Parser;

import java.util.regex.Matcher;

public class MarkdownRenderer {
    public static final int DEFAULT_WIDTH = 120;

    private final Parser parser;
    private final int maxWidth;

    public MarkdownRenderer() {
        this(DEFAULT_WIDTH);
    }

    public MarkdownRenderer(int maxWidth) {
        if (maxWidth <= 0) {
            throw new IllegalArgumentException("maxWidth must be positive");
        }
        this.maxWidth = maxWidth;
        this.parser = Parser.builder()
                .extensions(Arrays.asList(
                        TablesExtension.create(),
                        YamlFrontMatterExtension.create(),
                        TaskListItemsExtension.create()
                ))
                .build();
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public String render(String markdown) {
        Node document = parser.parse(markdown);
        AnsiVisitor visitor = new AnsiVisitor(maxWidth);
        document.accept(visitor);
        return visitor.getResult();
    }

    private static class AnsiVisitor extends AbstractVisitor {
        private static final Pattern ANSI_PATTERN = Pattern.compile("\\u001B\\[[0-9;]*m");
        private static final Pattern CELL_BULLET_PATTERN = Pattern.compile("^(?:[-*•])\\s+(.*)$");

        private final StringBuilder sb = new StringBuilder();
        private final int maxWidth;
        private int listLevel = 0;

        private record RenderedCell(
                String content,
                int visibleLength,
                boolean header,
                TableCell.Alignment alignment
        ) {}

        private record WrappedLine(
                String text,
                int visibleLength
        ) {}

        public AnsiVisitor(int maxWidth) {
            this.maxWidth = maxWidth;
        }

        public String getResult() {
            return sb.toString();
        }

        private String renderChildrenInline(Node parent) {
            AnsiVisitor inlineVisitor = new AnsiVisitor(maxWidth);
            inlineVisitor.visitChildren(parent);
            return inlineVisitor.getResult();
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
            String prefix = ansi.toString();
            String reset = Ansi.ready().reset().toString();
            String inline = renderChildrenInline(heading).strip();
            List<WrappedLine> lines = wrapAnsi(inline, maxWidth);
            for (int i = 0; i < lines.size(); i++) {
                if (i > 0) {
                    sb.append("\n");
                }
                sb.append(prefix).append(lines.get(i).text()).append(reset);
            }
            sb.append("\n\n");
        }

        @Override
        public void visit(Paragraph paragraph) {
            String inline = renderChildrenInline(paragraph);
            if (paragraph.getParent() instanceof ListItem) {
                int indentWidth = 2 * listLevel;
                int available = Math.max(1, maxWidth - indentWidth);
                List<WrappedLine> lines = wrapAnsi(inline.strip(), available);
                String continuationIndent = "  ".repeat(Math.max(0, listLevel));
                for (int i = 0; i < lines.size(); i++) {
                    if (i > 0) {
                        sb.append("\n").append(continuationIndent);
                    }
                    sb.append(lines.get(i).text());
                }
                sb.append("\n");
            } else if (paragraph.getParent() instanceof BlockQuote) {
                int available = Math.max(1, maxWidth - 2);
                List<WrappedLine> lines = wrapAnsi(inline.strip(), available);
                String quotePrefix = Ansi.ready().cyan().toString() + "> ";
                for (int i = 0; i < lines.size(); i++) {
                    if (i > 0) {
                        sb.append("\n").append(quotePrefix);
                    }
                    sb.append(lines.get(i).text());
                }
                sb.append("\n\n");
            } else {
                List<WrappedLine> lines = wrapAnsi(inline.strip(), maxWidth);
                for (int i = 0; i < lines.size(); i++) {
                    if (i > 0) {
                        sb.append("\n");
                    }
                    sb.append(lines.get(i).text());
                }
                sb.append("\n\n");
            }
        }

        @Override
        public void visit(SoftLineBreak softLineBreak) {
            sb.append(" ");
        }

        @Override
        public void visit(HardLineBreak hardLineBreak) {
            sb.append("\n");
        }

        @Override
        public void visit(HtmlInline htmlInline) {
            String tag = htmlInline.getLiteral();
            if (tag != null && tag.matches("(?i)<br\\s*/?>")) {
                sb.append("\n");
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

        private static String formatCellBullets(String rawContent) {
            String[] lines = rawContent.split("\n", -1);
            StringBuilder formatted = new StringBuilder();
            String bulletPrefix = Ansi.ready().brightBlue().append("* ").reset().toString();
            for (int i = 0; i < lines.length; i++) {
                if (i > 0) {
                    formatted.append("\n");
                }
                String line = lines[i].strip();
                Matcher m = CELL_BULLET_PATTERN.matcher(line);
                if (m.matches()) {
                    formatted.append(bulletPrefix).append(m.group(1));
                } else {
                    formatted.append(line);
                }
            }
            return formatted.toString();
        }

        private static int maxLineVisibleLength(String content) {
            int max = 0;
            for (String line : content.split("\n", -1)) {
                max = Math.max(max, visibleLength(line));
            }
            return max;
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
                                String content = formatCellBullets(renderChildrenInline(cell).strip());
                                int visibleLen = maxLineVisibleLength(content);
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

            int[] naturalWidths = new int[numCols];
            Arrays.fill(naturalWidths, 1);
            for (List<RenderedCell> row : headRows) {
                for (int i = 0; i < row.size(); i++) {
                    naturalWidths[i] = Math.max(naturalWidths[i], row.get(i).visibleLength());
                }
            }
            for (List<RenderedCell> row : bodyRows) {
                for (int i = 0; i < row.size(); i++) {
                    naturalWidths[i] = Math.max(naturalWidths[i], row.get(i).visibleLength());
                }
            }

            int[] colWidths = computeColumnWidths(naturalWidths, maxWidth);

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

        private static int[] computeColumnWidths(int[] naturalWidths, int maxWidth) {
            int numCols = naturalWidths.length;
            int borderOverhead = 3 * numCols + 1;
            int availableContentWidth = Math.max(numCols, maxWidth - borderOverhead);

            int totalNatural = 0;
            for (int w : naturalWidths) {
                totalNatural += w;
            }
            if (totalNatural <= availableContentWidth) {
                return naturalWidths.clone();
            }

            int[] colWidths = new int[numCols];
            boolean[] locked = new boolean[numCols];
            int remainingBudget = availableContentWidth;

            while (true) {
                double totalWeight = 0;
                int unlockedCount = 0;
                for (int i = 0; i < numCols; i++) {
                    if (!locked[i]) {
                        totalWeight += Math.sqrt(naturalWidths[i]);
                        unlockedCount++;
                    }
                }
                if (unlockedCount == 0 || totalWeight == 0) {
                    break;
                }

                boolean anyLocked = false;
                double equalShare = (double) remainingBudget / unlockedCount;
                for (int i = 0; i < numCols; i++) {
                    if (!locked[i]) {
                        double weightedShare = remainingBudget * (Math.sqrt(naturalWidths[i]) / totalWeight);
                        if (naturalWidths[i] <= equalShare || weightedShare >= naturalWidths[i]) {
                            colWidths[i] = naturalWidths[i];
                            locked[i] = true;
                            remainingBudget -= naturalWidths[i];
                            anyLocked = true;
                        }
                    }
                }
                if (!anyLocked) {
                    break;
                }
            }

            double totalWeight = 0;
            List<Integer> unlockedIndices = new ArrayList<>();
            for (int i = 0; i < numCols; i++) {
                if (!locked[i]) {
                    totalWeight += Math.sqrt(naturalWidths[i]);
                    unlockedIndices.add(i);
                }
            }

            if (!unlockedIndices.isEmpty()) {
                int allocated = 0;
                double[] exactShares = new double[numCols];
                for (int i : unlockedIndices) {
                    exactShares[i] = remainingBudget * (Math.sqrt(naturalWidths[i]) / totalWeight);
                    colWidths[i] = Math.max(1, (int) Math.floor(exactShares[i]));
                    allocated += colWidths[i];
                }
                int remainder = remainingBudget - allocated;
                unlockedIndices.sort((a, b) -> Double.compare(
                        (exactShares[b] - colWidths[b]),
                        (exactShares[a] - colWidths[a])
                ));
                for (int idx = 0; idx < unlockedIndices.size() && remainder > 0; idx++) {
                    int col = unlockedIndices.get(idx);
                    if (colWidths[col] < naturalWidths[col]) {
                        colWidths[col]++;
                        remainder--;
                    }
                }
            }

            return colWidths;
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
            List<List<WrappedLine>> wrappedCells = new ArrayList<>(colWidths.length);
            int rowHeight = 1;

            for (int i = 0; i < colWidths.length; i++) {
                RenderedCell cell = i < row.size()
                        ? row.get(i)
                        : new RenderedCell("", 0, isHeadRow, null);
                List<WrappedLine> lines = wrapAnsi(cell.content(), colWidths[i]);
                wrappedCells.add(lines);
                rowHeight = Math.max(rowHeight, lines.size());
            }

            for (int lineIdx = 0; lineIdx < rowHeight; lineIdx++) {
                sb.append(verticalBar);
                for (int i = 0; i < colWidths.length; i++) {
                    RenderedCell cell = i < row.size()
                            ? row.get(i)
                            : new RenderedCell("", 0, isHeadRow, null);
                    List<WrappedLine> cellLines = wrappedCells.get(i);
                    WrappedLine line = lineIdx < cellLines.size()
                            ? cellLines.get(lineIdx)
                            : new WrappedLine("", 0);

                    int padding = Math.max(0, colWidths[i] - line.visibleLength());
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
                    if ((cell.header() || isHeadRow) && !line.text().isEmpty()) {
                        sb.append(Ansi.ready().bold().brightCyan().toString())
                                .append(line.text())
                                .append(Ansi.ready().reset().toString());
                    } else {
                        sb.append(line.text());
                    }
                    sb.append(" ".repeat(rightPad)).append(" ").append(verticalBar);
                }
                sb.append("\n");
            }
        }

        private static int visibleLength(String text) {
            return ANSI_PATTERN.matcher(text).replaceAll("").length();
        }

        private static List<WrappedLine> wrapAnsi(String text, int maxWidth) {
            if (maxWidth <= 0 || text.isEmpty()) {
                return List.of(new WrappedLine(text, visibleLength(text)));
            }
            if (visibleLength(text) <= maxWidth && !text.contains("\n")) {
                return List.of(new WrappedLine(text, visibleLength(text)));
            }

            List<WrappedLine> result = new ArrayList<>();
            List<String> activeCodes = new ArrayList<>();
            String reset = Ansi.ready().reset().toString();

            String[] paragraphs = text.split("\n", -1);
            for (String paragraph : paragraphs) {
                if (paragraph.isEmpty()) {
                    result.add(new WrappedLine("", 0));
                    continue;
                }

                String strippedParagraph = ANSI_PATTERN.matcher(paragraph).replaceAll("");
                boolean isBulletParagraph = maxWidth > 2 && (strippedParagraph.startsWith("* ")
                        || strippedParagraph.startsWith("- ")
                        || strippedParagraph.startsWith("• "));
                String continuationIndent = isBulletParagraph ? "  " : "";
                int continuationIndentLen = continuationIndent.length();

                String[] words = paragraph.split(" +");
                StringBuilder lineSb = new StringBuilder();
                int lineVisLen = 0;
                boolean isFirstLineOfParagraph = true;

                for (String word : words) {
                    if (word.isEmpty()) {
                        continue;
                    }
                    int wordVisLen = visibleLength(word);
                    int indentForNewLine = isFirstLineOfParagraph ? 0 : continuationIndentLen;

                    if (wordVisLen + indentForNewLine <= maxWidth) {
                        if (lineVisLen == 0) {
                            if (!isFirstLineOfParagraph && continuationIndentLen > 0) {
                                lineSb.append(continuationIndent);
                                lineVisLen = continuationIndentLen;
                            }
                            if (!activeCodes.isEmpty()) {
                                lineSb.append(String.join("", activeCodes));
                            }
                            lineSb.append(word);
                            lineVisLen += wordVisLen;
                            updateActiveCodes(activeCodes, word);
                        } else if (lineVisLen + 1 + wordVisLen <= maxWidth) {
                            lineSb.append(" ").append(word);
                            lineVisLen += 1 + wordVisLen;
                            updateActiveCodes(activeCodes, word);
                        } else {
                            if (!activeCodes.isEmpty()) {
                                lineSb.append(reset);
                            }
                            result.add(new WrappedLine(lineSb.toString(), lineVisLen));
                            lineSb.setLength(0);
                            isFirstLineOfParagraph = false;
                            lineVisLen = 0;
                            if (continuationIndentLen > 0) {
                                lineSb.append(continuationIndent);
                                lineVisLen = continuationIndentLen;
                            }
                            if (!activeCodes.isEmpty()) {
                                lineSb.append(String.join("", activeCodes));
                            }
                            lineSb.append(word);
                            lineVisLen += wordVisLen;
                            updateActiveCodes(activeCodes, word);
                        }
                    } else {
                        if (lineVisLen > 0) {
                            if (!activeCodes.isEmpty()) {
                                lineSb.append(reset);
                            }
                            result.add(new WrappedLine(lineSb.toString(), lineVisLen));
                            lineSb.setLength(0);
                            lineVisLen = 0;
                            isFirstLineOfParagraph = false;
                        }

                        int idx = 0;
                        while (idx < word.length()) {
                            if (lineVisLen == 0 && lineSb.isEmpty()) {
                                if (!isFirstLineOfParagraph && continuationIndentLen > 0) {
                                    lineSb.append(continuationIndent);
                                    lineVisLen = continuationIndentLen;
                                }
                                if (!activeCodes.isEmpty()) {
                                    lineSb.append(String.join("", activeCodes));
                                }
                            }
                            Matcher m = ANSI_PATTERN.matcher(word);
                            if (m.find(idx) && m.start() == idx) {
                                String seq = m.group();
                                lineSb.append(seq);
                                applyAnsiCode(activeCodes, seq);
                                idx = m.end();
                            } else {
                                lineSb.append(word.charAt(idx));
                                lineVisLen++;
                                idx++;
                                while (idx < word.length()) {
                                    Matcher trailing = ANSI_PATTERN.matcher(word);
                                    if (trailing.find(idx) && trailing.start() == idx) {
                                        String seq = trailing.group();
                                        lineSb.append(seq);
                                        applyAnsiCode(activeCodes, seq);
                                        idx = trailing.end();
                                    } else {
                                        break;
                                    }
                                }
                                if (lineVisLen == maxWidth && idx < word.length()) {
                                    if (!activeCodes.isEmpty()) {
                                        lineSb.append(reset);
                                    }
                                    result.add(new WrappedLine(lineSb.toString(), lineVisLen));
                                    lineSb.setLength(0);
                                    lineVisLen = 0;
                                    isFirstLineOfParagraph = false;
                                }
                            }
                        }
                    }
                }

                if (lineVisLen > 0 || !lineSb.isEmpty()) {
                    if (!activeCodes.isEmpty()) {
                        lineSb.append(reset);
                    }
                    result.add(new WrappedLine(lineSb.toString(), lineVisLen));
                }
            }

            return result.isEmpty() ? List.of(new WrappedLine("", 0)) : result;
        }

        private static void updateActiveCodes(List<String> activeCodes, String segment) {
            Matcher matcher = ANSI_PATTERN.matcher(segment);
            while (matcher.find()) {
                applyAnsiCode(activeCodes, matcher.group());
            }
        }

        private static void applyAnsiCode(List<String> activeCodes, String code) {
            switch (code) {
                case "\u001B[0m" -> activeCodes.clear();
                case "\u001B[22m" -> {
                    activeCodes.remove("\u001B[1m");
                    activeCodes.remove("\u001B[2m");
                }
                case "\u001B[23m" -> activeCodes.remove("\u001B[3m");
                case "\u001B[24m" -> activeCodes.remove("\u001B[4m");
                default -> {
                    if (!activeCodes.contains(code)) {
                        activeCodes.add(code);
                    }
                }
            }
        }
    }
}
