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
