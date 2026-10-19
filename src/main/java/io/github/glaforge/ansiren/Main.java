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
        MarkdownRenderer renderer = new MarkdownRenderer(); // defaults to 120 columns

        String markdown = """
                ---
                title: Ansiren Demo
                author: Antigravity
                ---
                
                # Ansiren Library
                
                Welcome to the **Ansiren** demo! This library allows you to render markdown with ANSI colors, automatic word wrapping at 120 columns by default, and smart table column layout.
                
                ## 1. Compact Table (Fits Naturally)
                
                | Feature | Alignment | Status |
                | :--- | :---: | ---: |
                | **ANSI Colors** | Left | `Done` |
                | *Tables* | Center | `Done` |
                
                ## 2. Asymmetric Table (Short Columns + Long Wrapped Description)
                
                | ID | Component | Status | Detailed Description |
                | :---: | :--- | :---: | :--- |
                | 1 | `Ansi` | **Stable** | Fluent builder API supporting standard 16 colors, 256-color palette, and 24-bit True Color RGB sequences with style nesting. |
                | 2 | `MarkdownRenderer` | **Active** | Renders GitHub Flavored Markdown to the terminal with automatic 120-column word wrapping, preserving narrow columns at their natural size while wrapping long text columns cleanly across multiple lines. |
                
                ## 3. Balanced Wide Table (Equal Distribution Across 3 Long Columns)
                
                | Architecture & Parsing | Column Space Allocation | Multi-line ANSI Cell Wrapping |
                | :--- | :--- | :--- |
                | Built on top of `commonmark-java` with GFM extensions for tables, task lists, and YAML front matter blocks. | Uses water-filling with **square-root weighting** so narrow columns never wrap while wide columns share space fairly. | Tracks active ANSI escape codes across line breaks so **bold phrases** and *italic spans* never bleed into adjacent columns. |
                
                ## 4. Five-Column Mixed Table
                
                | # | Tier | Latency | Throughput | Notes & Recommendations |
                | ---: | :---: | ---: | ---: | :--- |
                | 1 | `Flash-Lite` | 45 ms | 1,200 req/s | Best for high-volume classification, routing, and lightweight extraction tasks where speed is critical. |
                | 2 | `Flash` | 110 ms | 650 req/s | Ideal balance of **low latency** and strong reasoning for interactive coding and summarization workflows. |
                | 3 | `Pro` | 320 ms | 180 req/s | Designed for *complex multi-step planning*, deep architectural refactoring, and long-horizon agentic tasks. |
                
                ## 5. Table with Bullet Points in Cells
                
                | Version | Key Highlights | Technical Details |
                | :---: | :--- | :--- |
                | `v0.1.2` | - Unicode box-drawing borders<br>- Column alignment (`left`, `center`, `right`)<br>- Bold cyan headers | - Uses `TablesExtension` from CommonMark<br>- Strips ANSI escape codes when computing visible cell width |
                | `v0.1.3` | * Default **120-column** content wrapping<br>* Smart column space distribution<br>* Multi-line bullet lists inside cells | * Preserves narrow columns at their natural width<br>* Applies a 2-space hanging indent when a long bullet item wraps onto the next line inside a cell |
                
                ## 6. Long URL in Backticks (Paragraph & Table Cell)
                
                Example in a paragraph: `https://data.geopf.fr/wfs/ows?SERVICE=WFS&VERSION=2.0.0&REQUEST=GetFeature&TYPENAMES=CADASTRALPARCELS.PARCELLAIRE_EXPRESS%3Aparcelle&SRSNAME=CRS:84&BBOX=2.423481,48.845007,2.425665,48.846445,CRS:84&COUNT=5&OUTPUTFORMAT=application/json`
                
                | Endpoint | Sample WFS Request URL |
                | :--- | :--- |
                | IGN Géoplateforme | `https://data.geopf.fr/wfs/ows?SERVICE=WFS&VERSION=2.0.0&REQUEST=GetFeature&TYPENAMES=CADASTRALPARCELS.PARCELLAIRE_EXPRESS%3Aparcelle&SRSNAME=CRS:84&BBOX=2.423481,48.845007,2.425665,48.846445,CRS:84&COUNT=5&OUTPUTFORMAT=application/json` |
                """;

        String rendered = renderer.render(markdown);
        System.out.println(rendered);
    }
}
