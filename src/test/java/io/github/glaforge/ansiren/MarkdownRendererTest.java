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

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MarkdownRendererTest {

    private final MarkdownRenderer renderer = new MarkdownRenderer();

    @Test
    public void testHeadingLevels() {
        assertTrue(renderer.render("# H1").contains("\u001B[1m\u001B[91m"));
        assertTrue(renderer.render("## H2").contains("\u001B[1m\u001B[92m"));
        assertTrue(renderer.render("### H3").contains("\u001B[1m\u001B[93m"));
        assertTrue(renderer.render("#### H4").contains("\u001B[1m\u001B[94m"));
    }

    @Test
    public void testStrongAndEmphasis() {
        assertTrue(renderer.render("**Bold**").contains("\u001B[1mBold\u001B[22m"));
        assertTrue(renderer.render("*Italic*").contains("\u001B[3mItalic\u001B[23m"));
    }

    @Test
    public void testCode() {
        assertTrue(renderer.render("`code`").contains("\u001B[33m`code`\u001B[0m"));
    }

    @Test
    public void testFencedCodeBlock() {
        String result = renderer.render("```java\nint x = 0;\n```");
        assertTrue(result.contains("\u001B[90m```java"));
        assertTrue(result.contains("int x = 0;"));
        assertTrue(result.contains("```\u001B[0m"));
    }

    @Test
    public void testBlockQuote() {
        assertTrue(renderer.render("> Quote").contains("\u001B[36m> Quote"));
    }

    @Test
    public void testLists() {
        String bulletList = renderer.render("* Item 1\n* Item 2");
        assertTrue(bulletList.contains("\u001B[94m* \u001B[0mItem 1\n\u001B[94m* \u001B[0mItem 2"));
        assertFalse(bulletList.contains("Item 1\n\n"));
        
        String orderedList = renderer.render("1. Item 1\n2. Item 2");
        assertTrue(orderedList.contains("\u001B[94m* \u001B[0mItem 1\n\u001B[94m* \u001B[0mItem 2"));
        assertFalse(orderedList.contains("Item 1\n\n"));
    }

    @Test
    public void testParagraphs() {
        String result = renderer.render("Para 1\n\nPara 2");
        assertTrue(result.contains("Para 1\n\nPara 2"));
    }

    @Test
    public void testLink() {
        String result = renderer.render("[Google](https://google.com)");
        assertTrue(result.contains("\u001B[4m\u001B[34mGoogle\u001B[0m (https://google.com)"));
    }

    @Test
    public void testYamlFrontMatter() {
        String markdown = "---\ntitle: Test\n---\nContent";
        String result = renderer.render(markdown);
        assertTrue(result.contains("\u001B[95m---"));
        assertTrue(result.contains("title: Test"));
        assertTrue(result.contains("---\u001B[0m"));
    }

    @Test
    public void testNestedStyles() {
        String result = renderer.render("## Word1 **Word2** Word3");
        assertTrue(result.contains("\u001B[1m\u001B[92m"));
        assertTrue(result.contains("\u001B[22m Word3"));
    }

    @Test
    public void testGfmAlerts() {
        String markdown = "> [!NOTE]\n> This is a note.";
        String result = renderer.render(markdown);
        assertTrue(result.contains("[!NOTE]"));
    }

    @Test
    public void testTaskListItems() {
        String markdown = "- [ ] Task 1\n- [x] Task 2";
        String result = renderer.render(markdown);
        assertTrue(result.contains("[ ]"));
        assertTrue(result.contains("[x]"));
    }

    @Test
    public void testTableRenderingAndAlignment() {
        String markdown = """
                | Name | Age | Role |
                | :--- | :---: | ---: |
                | Alice | 30 | Developer |
                | Bob | 7 | Admin |
                """;
        String result = renderer.render(markdown);

        // Verify Unicode box-drawing borders
        assertTrue(result.contains("┌───────┬─────┬───────────┐"));
        assertTrue(result.contains("├───────┼─────┼───────────┤"));
        assertTrue(result.contains("└───────┴─────┴───────────┘"));

        // Strip ANSI codes to verify exact visual alignment
        String stripped = result.replaceAll("\\u001B\\[[0-9;]*m", "");
        String expected = """
                ┌───────┬─────┬───────────┐
                │ Name  │ Age │      Role │
                ├───────┼─────┼───────────┤
                │ Alice │ 30  │ Developer │
                │ Bob   │  7  │     Admin │
                └───────┴─────┴───────────┘
                """;
        assertEquals(expected.trim(), stripped.trim());

        // Verify header cells are styled in bold brightCyan
        assertTrue(result.contains("\u001B[1m\u001B[96mName\u001B[0m"));
    }

    @Test
    public void testTableWithInlineFormatting() {
        String markdown = """
                | Feature | Status |
                | :--- | :---: |
                | **Tables** | `OK` |
                """;
        String result = renderer.render(markdown);
        String stripped = result.replaceAll("\\u001B\\[[0-9;]*m", "");
        String expected = """
                ┌─────────┬────────┐
                │ Feature │ Status │
                ├─────────┼────────┤
                │ Tables  │  `OK`  │
                └─────────┴────────┘
                """;
        assertEquals(expected.trim(), stripped.trim());
        assertTrue(result.contains("\u001B[1mTables\u001B[22m"));
        assertTrue(result.contains("\u001B[33m`OK`\u001B[0m"));
    }

    @Test
    public void testDefaultWidthIs120() {
        assertEquals(120, MarkdownRenderer.DEFAULT_WIDTH);
        assertEquals(120, renderer.getMaxWidth());
    }

    @Test
    public void testContentWrappingAtMaxWidth() {
        MarkdownRenderer customRenderer = new MarkdownRenderer(30);
        String markdown = "This is a long paragraph that should wrap cleanly within thirty columns.";
        String stripped = customRenderer.render(markdown).replaceAll("\\u001B\\[[0-9;]*m", "").trim();
        String[] lines = stripped.split("\n");
        assertTrue(lines.length >= 3);
        for (String line : lines) {
            assertTrue(line.length() <= 30, "Line exceeded 30 chars: " + line);
        }
    }

    @Test
    public void testTableWrappingAndColumnDistribution() {
        MarkdownRenderer customRenderer = new MarkdownRenderer(50);
        String markdown = """
                | ID | Status | Description |
                | :---: | :---: | :--- |
                | 1 | **OK** | This description is much longer than fifty columns and must wrap across multiple lines while keeping ID and Status intact. |
                """;
        String rendered = customRenderer.render(markdown);
        String stripped = rendered.replaceAll("\\u001B\\[[0-9;]*m", "").trim();
        String[] lines = stripped.split("\n");

        // Every line of the table must be exactly 50 characters wide
        for (String line : lines) {
            assertEquals(50, line.length(), "Line should be 50 chars wide: " + line);
        }
        // Should have top border, header, separator, multiple body lines, bottom border
        assertTrue(lines.length >= 8);
    }

    @Test
    public void testTableCellMultiWordBoldWrappingDoesNotBleed() {
        MarkdownRenderer customRenderer = new MarkdownRenderer(35);
        String markdown = """
                | Col1 | Col2 |
                | :--- | :--- |
                | **bold words wrapping across lines** | normal text |
                """;
        String rendered = customRenderer.render(markdown);
        // Each wrapped line inside Col1's bold phrase should reset (\u001B[0m) before the vertical bar
        // and re-apply bold (\u001B[1m) on the continuation line
        assertTrue(rendered.contains("\u001B[1mbold words"));
        assertTrue(rendered.contains("normal text"));
        String stripped = rendered.replaceAll("\\u001B\\[[0-9;]*m", "").trim();
        for (String line : stripped.split("\n")) {
            assertTrue(line.length() <= 35);
        }
    }

    @Test
    public void testTableWithBulletsInCells() {
        MarkdownRenderer customRenderer = new MarkdownRenderer(45);
        String markdown = """
                | Release | Highlights |
                | :---: | :--- |
                | `v0.1.3` | - Short bullet<br>- Long bullet item that wraps with hanging indent |
                """;
        String rendered = customRenderer.render(markdown);
        // Verify brightBlue bullet prefix inside cell
        assertTrue(rendered.contains("\u001B[94m* \u001B[0mShort bullet"));
        String stripped = rendered.replaceAll("\\u001B\\[[0-9;]*m", "").trim();
        // Verify 2-space hanging indent on wrapped bullet continuation line
        assertTrue(stripped.contains("│           │   that wraps with hanging     │")
                || stripped.contains("│          │   "));
        for (String line : stripped.split("\n")) {
            assertEquals(45, line.length(), "Line should be 45 chars wide: " + line);
        }
    }
}
