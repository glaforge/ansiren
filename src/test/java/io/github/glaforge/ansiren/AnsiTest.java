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

public class AnsiTest {

    @Test
    public void testStyles() {
        assertEquals("\u001B[1m", Ansi.ready().bold().toString());
        assertEquals("\u001B[2m", Ansi.ready().faint().toString());
        assertEquals("\u001B[3m", Ansi.ready().italic().toString());
        assertEquals("\u001B[4m", Ansi.ready().underline().toString());
        assertEquals("\u001B[5m", Ansi.ready().blink().toString());
        assertEquals("\u001B[7m", Ansi.ready().reverse().toString());
        assertEquals("\u001B[8m", Ansi.ready().conceal().toString());
    }

    @Test
    public void testStyleOffs() {
        assertEquals("\u001B[22m", Ansi.ready().boldOff().toString());
        assertEquals("\u001B[23m", Ansi.ready().italicOff().toString());
        assertEquals("\u001B[24m", Ansi.ready().underlineOff().toString());
    }

    @Test
    public void testForegroundColors() {
        assertEquals("\u001B[30m", Ansi.ready().black().toString());
        assertEquals("\u001B[31m", Ansi.ready().red().toString());
        assertEquals("\u001B[32m", Ansi.ready().green().toString());
        assertEquals("\u001B[33m", Ansi.ready().yellow().toString());
        assertEquals("\u001B[34m", Ansi.ready().blue().toString());
        assertEquals("\u001B[35m", Ansi.ready().magenta().toString());
        assertEquals("\u001B[36m", Ansi.ready().cyan().toString());
        assertEquals("\u001B[37m", Ansi.ready().white().toString());
    }

    @Test
    public void testBrightForegroundColors() {
        assertEquals("\u001B[90m", Ansi.ready().brightBlack().toString());
        assertEquals("\u001B[91m", Ansi.ready().brightRed().toString());
        assertEquals("\u001B[92m", Ansi.ready().brightGreen().toString());
        assertEquals("\u001B[93m", Ansi.ready().brightYellow().toString());
        assertEquals("\u001B[94m", Ansi.ready().brightBlue().toString());
        assertEquals("\u001B[95m", Ansi.ready().brightMagenta().toString());
        assertEquals("\u001B[96m", Ansi.ready().brightCyan().toString());
        assertEquals("\u001B[97m", Ansi.ready().brightWhite().toString());
    }

    @Test
    public void testBackgroundColors() {
        assertEquals("\u001B[40m", Ansi.ready().bgBlack().toString());
        assertEquals("\u001B[41m", Ansi.ready().bgRed().toString());
        assertEquals("\u001B[42m", Ansi.ready().bgGreen().toString());
        assertEquals("\u001B[43m", Ansi.ready().bgYellow().toString());
        assertEquals("\u001B[44m", Ansi.ready().bgBlue().toString());
        assertEquals("\u001B[45m", Ansi.ready().bgMagenta().toString());
        assertEquals("\u001B[46m", Ansi.ready().bgCyan().toString());
        assertEquals("\u001B[47m", Ansi.ready().bgWhite().toString());
    }

    @Test
    public void testMisc() {
        assertEquals("\u001B[0m", Ansi.ready().reset().toString());
        assertEquals("Hello", Ansi.ready().append("Hello").toString());
    }

    @Test
    public void testStaticHelpers() {
        assertEquals("\u001B[1mHello\u001B[0m", Ansi.bold("Hello"));
        assertEquals("\u001B[31mHello\u001B[0m", Ansi.red("Hello"));
        assertEquals("\u001B[32mHello\u001B[0m", Ansi.green("Hello"));
        assertEquals("\u001B[33mHello\u001B[0m", Ansi.yellow("Hello"));
        assertEquals("\u001B[34mHello\u001B[0m", Ansi.blue("Hello"));
        assertEquals("\u001B[35mHello\u001B[0m", Ansi.magenta("Hello"));
        assertEquals("\u001B[36mHello\u001B[0m", Ansi.cyan("Hello"));
        assertEquals("\u001B[37mHello\u001B[0m", Ansi.white("Hello"));
    }

    @Test
    public void testExtendedColors() {
        assertEquals("\u001B[38;5;42m", Ansi.ready().color256(42).toString());
        assertEquals("\u001B[48;5;42m", Ansi.ready().bgColor256(42).toString());
        assertEquals("\u001B[38;2;255;100;50m", Ansi.ready().colorRGB(255, 100, 50).toString());
        assertEquals("\u001B[48;2;255;100;50m", Ansi.ready().bgColorRGB(255, 100, 50).toString());
    }
}
