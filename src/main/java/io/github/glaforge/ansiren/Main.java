package io.github.glaforge.ansiren;

public class Main {
    public static void main(String[] args) {
        MarkdownRenderer renderer = new MarkdownRenderer();

        String markdown = """
                ---
                title: Ansiren Demo
                author: Antigravity
                ---
                
                # Ansiren Library
                
                Welcome to the **Ansiren** demo! This library allows you to render markdown with ANSI colors.
                
                ## Features
                
                * **Simple API** for ANSI codes
                * **Markdown** rendering
                * *GitHub Flavored* extensions (like tables)
                
                ## Example Code
                
                `Ansi.red("Hello")`
                
                ```java
                System.out.println(Ansi.green("Success!"));
                ```
                
                > This is a blockquote to test styling.
                
                Enjoy using Ansiren!
                """;

        String rendered = renderer.render(markdown);
        System.out.println(rendered);
    }
}
