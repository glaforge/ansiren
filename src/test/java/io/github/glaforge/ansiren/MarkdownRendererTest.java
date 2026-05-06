package io.github.glaforge.ansiren;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MarkdownRendererTest {

    @Test
    public void testHeading() {
        MarkdownRenderer renderer = new MarkdownRenderer();
        String result = renderer.render("# Heading");
        // Heading 1 is bright red and bold
        assertTrue(result.contains("\u001B[1m\u001B[91mHeading\u001B[0m"));
    }

    @Test
    public void testStrong() {
        MarkdownRenderer renderer = new MarkdownRenderer();
        String result = renderer.render("**Bold**");
        assertTrue(result.contains("\u001B[1mBold\u001B[0m"));
    }

    @Test
    public void testCode() {
        MarkdownRenderer renderer = new MarkdownRenderer();
        String result = renderer.render("`code`");
        assertTrue(result.contains("\u001B[33m`code`\u001B[0m"));
    }
}
