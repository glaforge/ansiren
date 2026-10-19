# Ansiren

A Java library that offers a simple API to render text on stdout with ANSI escape codes, and also to output syntax highlighted markdown text using those ANSI primitives. It uses the [commonmark-java](https://github.com/commonmark/commonmark-java) library for Markdown parsing.

## Features

- **Fluent ANSI API**: Easily build styled strings with colors, backgrounds, and styles.
- **Extended Colors**: Supports 256-color palette and 24-bit True Color (RGB).
- **Markdown Rendering**: Render markdown text to terminal with ANSI colors.
- **GitHub Flavored Markdown**: Supports tables (with Unicode box-drawing borders, column alignment, smart column width distribution, and multi-line cell wrapping) and task lists.
- **Automatic Word Wrapping**: Wraps text content and tables at 120 columns by default (configurable via `new MarkdownRenderer(maxWidth)`), preserving ANSI styles across line breaks.
- **Style Nesting**: Correctly handles nested styles in markdown (e.g., bold inside colored text).

## Installation

Add the following dependency to your `pom.xml` (adjust version as needed):

```xml
<dependency>
    <groupId>io.github.glaforge</groupId>
    <artifactId>ansiren</artifactId>
    <version>0.2.0</version>
</dependency>
```

## Usage

### ANSI Primitives

```java
import io.github.glaforge.ansiren.Ansi;

// Using static helpers
System.out.println(Ansi.red("Red text"));
System.out.println(Ansi.green("Green text"));

// Using fluent API
String text = Ansi.ready()
    .green().append("Green ")
    .bold().append("Bold Green ")
    .boldOff().append("Green again")
    .reset()
    .toString();
System.out.println(text);

// Extended colors
System.out.println(Ansi.ready().color256(42).append("Color 42").reset().toString());
System.out.println(Ansi.ready().colorRGB(255, 100, 50).append("Custom RGB").reset().toString());
```

### Markdown Rendering

```java
import io.github.glaforge.ansiren.MarkdownRenderer;

// Default 120-column wrapping (or pass a custom width: new MarkdownRenderer(80))
MarkdownRenderer renderer = new MarkdownRenderer();
String markdown = "# Heading\\n\\nThis is **bold** text.";
String rendered = renderer.render(markdown);
System.out.println(rendered);
```

## License

This project is licensed under the [Apache License, Version 2.0](LICENSE).

## Disclaimer

This is not an official Google project.
