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
        assertTrue(bulletList.contains("\u001B[94m* \u001B[0mItem 1"));
        
        String orderedList = renderer.render("1. Item 1\n2. Item 2");
        assertTrue(orderedList.contains("\u001B[94m* \u001B[0mItem 1"));
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
}
